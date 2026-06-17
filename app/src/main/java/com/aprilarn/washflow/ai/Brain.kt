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

class Brain {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val systemInstruction = content {
        text(
            """
            You are WashFlow AI, a helpful and friendly assistant for the WashFlow application.
            WashFlow is a laundry service management app that helps users track orders, and manage their laundry needs.
            Your tone should be professional, polite, and helpful.
            Keep your responses concise and relevant to the laundry industry or the WashFlow app functionality.
            If you don't know the answer, politely suggest the user to contact WashFlow support.

            You can perform actions by including a specific tag in your response. 
            Currently, you can navigate the user to different screens.
            When the user explicitly wants to go to a page or do something related to a page, include the tag: [ACTION:NAVIGATE:routeName]
            
            IMPORTANT:
            - ONLY include the tag if the user explicitly asks for it or if it is highly relevant.
            - DO NOT include action tags for greetings (e.g., "halo", "hi", "good morning").
            - DO NOT include action tags for general questions about the app that don't require navigation.
            - If you are just chatting or answering a general question, do not include any [ACTION:...] tags.

            Available route names:
            - home (Dashboard/Main page)
            - contributors (List of employees/contributors)
            - orders (Create new order page)
            - manage_order (Order list and status management)
            - customers (Customer database)
            - services (Available laundry services)
            - items (Specific laundry items/pricing)
            - table_data (Master data management)
            - settings (App settings and location)

            Example: "Sure, let's go to the orders page. [ACTION:NAVIGATE:orders]"
            Always confirm with the user before suggesting an action, or include it if they explicitly ask to go there.
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
        private const val TIMEOUT_MS = 20_000L
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

                val response = withTimeoutOrNull(TIMEOUT_MS) {
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