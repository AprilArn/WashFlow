package com.aprilarn.washflow.ai

import com.aprilarn.washflow.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class Brain {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val systemInstruction = content {
        text(
            """
            # ROLE & PERSONA
            You are Aira (an acronym for Action & Intent Routing Agent), a helpful, polite, and professional assistant for the WashFlow laundry service management app. Your primary goal is to help users track orders and manage their laundry needs.
            
            # RULES & CONSTRAINTS
            1. Tone & Scope: Keep responses concise, professional, and strictly relevant to the laundry industry or WashFlow app functionalities.
            2. Fallback: If you do not know the answer or lack the capability, politely suggest the user contact WashFlow Support.
            3. Tag Restrictions: 
               - NEVER include action tags for general greetings (e.g., "halo", "hi", "good morning").
               - NEVER include action tags for general questions that do not require an app action.
               - ONLY output action tags when explicitly requested or highly relevant.
            4. User Confirmation: Always briefly confirm with the user before or alongside providing an action tag.
            
            # CAPABILITIES & ACTION TAGS
            You can perform in-app actions by outputting specific tags in your response. 
            
            ## 1. NAVIGATION
            Trigger this tag when the user explicitly wants to navigate to a specific page or section.
            Format: [ACTION:NAVIGATE:routeName]
            
            Available routeNames:
            - home : Dashboard/Main page
            - contributors : List of employees/contributors
            - orders : Create new order page
            - manage_order : Order list and status management
            - customers : Customer database
            - services : Available laundry services
            - items : Specific laundry items/pricing
            - table_data : Master data management
            - settings : App settings and location
            
            Example: "Sure, let's go to the orders page. [ACTION:NAVIGATE:orders]"
            
            ## 2. ADD CUSTOMER
            Trigger this tag when the user wants to register a new customer and has provided the necessary information (Name and Phone).
            Format: [ACTION:ADD_CUSTOMER:Name:Phone]
            
            Example: "I've prepared the details to add raphael as a new customer. [ACTION:ADD_CUSTOMER:Raphael:08123456789]"
            
            ## 3. DELETE CUSTOMER
            Trigger this tag when the user wants to delete a customer. Provide the name and/or the contact number. You no longer need to provide a customer ID; the app will find the relevant customer for confirmation.
            Format: [ACTION:DELETE_CUSTOMER:Name:Phone]
            
            Example: "I understand you want to delete the customer 'Budi'. [ACTION:DELETE_CUSTOMER:Budi:]"
            Example: "I'll help you delete the customer with number '08123'. [ACTION:DELETE_CUSTOMER::08123]"
            Example: "I'll help you delete 'Santi' (08123). [ACTION:DELETE_CUSTOMER:Santi:08123]"
            
            """.trimIndent()
        )
    }

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
    )

    // -------------------------------------------------------------------------
    // Model chain — primary first, fallback(s) after.
    // Add more entries here whenever a new fallback is needed.
    // -------------------------------------------------------------------------

    private data class ModelEntry(val name: String, val label: String)

    private val modelChain: List<ModelEntry> = listOf(
        // Tier 1 – Model Cloud Komersial Utama (Paling Pintar, Agen Terbaik)
        ModelEntry("gemini-flash-latest",       "Gemini Flash"),          // Agen multi-step & tool-use terbaik
        ModelEntry("gemini-flash-lite-latest",  "Gemini Flash Lite"),     // Sangat cepat, hemat token, instruksi ketat

        // Tier 2 – Open Model Kategori Besar (Penalaran & Logika Tingkat Tinggi)
        ModelEntry("gemma-4-31b-it",            "Gemma 4 31B Dense"),     // Akurasi & nalar tertinggi di seri open model Google
        ModelEntry("gemma-4-26b-a4b-it",        "Gemma 4 26B A4B MoE"),   // Cepat (Active 4B), nalar kuat, hemat VRAM

        // Tier 3 – Open Model Kategori Medium (Laptop-Ready)
        ModelEntry("gemma-4-12b-it",            "Gemma 4 12B"),           // Encoder-free multimodal, pas untuk agen lokal
    )

    private val models: List<GenerativeModel> = modelChain.map { entry ->
        GenerativeModel(
            modelName = entry.name,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            },
            safetySettings = safetySettings,
            systemInstruction = systemInstruction
        )
    }

    // -------------------------------------------------------------------------
    // Shared conversation history.
    // Managed manually so ANY model in the chain can rebuild its Chat session
    // with the full context — the fallback model won't lose prior messages.
    // -------------------------------------------------------------------------

    private val chatHistory = mutableListOf<Content>()

    companion object {
        /** Maximum time (ms) to wait for a single model before falling back. */
        private const val TIMEOUT_MS = 16_000L
    }

    // -------------------------------------------------------------------------
    // Core API
    // -------------------------------------------------------------------------

    suspend fun sendMessage(
        prompt: String,
        onStatusUpdate: (name: String, status: com.aprilarn.washflow.ui.aiagent.AiModelStatus) -> Unit = { _, _ -> }
    ): String {
        val userContent = content("user") { text(prompt) }
        var lastFailureReason = ""

        for ((index, model) in models.withIndex()) {
            val label = modelChain[index].label
            onStatusUpdate(label, com.aprilarn.washflow.ui.aiagent.AiModelStatus.THINKING)

            try {
                // Rebuild chat session from the shared history on every attempt.
                // This ensures the fallback model starts with full conversation context.
                val chat = model.startChat(history = chatHistory.toList())

                val response = withTimeoutOrNull(TIMEOUT_MS.milliseconds) {
                    chat.sendMessage(userContent)
                }

                when {
                    // ---- Timeout: try the next model ----
                    response == null -> {
                        lastFailureReason = "⏱️ $label tidak merespons dalam 20 detik"
                        onStatusUpdate(label, com.aprilarn.washflow.ui.aiagent.AiModelStatus.FAILURE)
                        if (index < models.size - 1) {
                            onStatusUpdate("Switching...", com.aprilarn.washflow.ui.aiagent.AiModelStatus.SWITCHING)
                            kotlinx.coroutines.delay(500)
                        }
                        continue
                    }

                    // ---- Safety / content filter: no point trying another model ----
                    response.text == null -> {
                        onStatusUpdate(label, com.aprilarn.washflow.ui.aiagent.AiModelStatus.FAILURE)
                        return "Maaf, pesan Anda tidak dapat diproses karena melanggar " +
                                "kebijakan keamanan atau filter AI."
                    }

                    // ---- Success: commit to shared history and return ----
                    else -> {
                        val responseText = response.text!!
                        chatHistory.add(userContent)
                        chatHistory.add(content("model") { text(responseText) })
                        onStatusUpdate(label, com.aprilarn.washflow.ui.aiagent.AiModelStatus.SUCCESS)
                        return responseText
                    }
                }

            } catch (e: Exception) {
                lastFailureReason = buildExceptionReason(label, e)
                onStatusUpdate(label, com.aprilarn.washflow.ui.aiagent.AiModelStatus.FAILURE)
                if (index < models.size - 1) {
                    onStatusUpdate("Switching...", com.aprilarn.washflow.ui.aiagent.AiModelStatus.SWITCHING)
                    kotlinx.coroutines.delay(500)
                }
                // Continue to the next model in the chain
            }
        }

        // Every model in the chain has failed or timed out
        return buildAllFailedMessage(lastFailureReason)
    }

    fun clearHistory() {
        chatHistory.clear()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun buildExceptionReason(label: String, e: Exception): String {
        val msg = e.localizedMessage ?: ""
        return when {
            msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") ->
                "❌ $label: kuota habis (429)"
            msg.contains("401") || msg.contains("API_KEY_INVALID") ->
                "❌ $label: autentikasi gagal (401)"
            msg.contains("404") ->
                "❌ $label: model tidak ditemukan (404)"
            msg.contains("500") || msg.contains("INTERNAL") ->
                "❌ $label: server error (500)"
            else ->
                "❌ $label: ${msg.take(80)}"
        }
    }

    private fun buildAllFailedMessage(lastFailureReason: String): String =
        """
        Maaf, semua layanan WashFlow AI sedang tidak dapat melayani permintaan Anda saat ini.

        **Penyebab terakhir:** $lastFailureReason

        Silakan coba lagi beberapa saat, pastikan koneksi internet Anda stabil, atau hubungi **WashFlow Support** jika masalah terus berlanjut.
        """.trimIndent()
}