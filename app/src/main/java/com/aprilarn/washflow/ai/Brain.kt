package com.aprilarn.washflow.ai

import com.aprilarn.washflow.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.CancellationException
import kotlin.time.Duration.Companion.milliseconds

class Brain(
    private val isIndonesianProvider: () -> Boolean = {
        val lang = Locale.getDefault().language.lowercase(Locale.US)
        lang == "id" || lang == "in"
    }
) {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val systemInstruction = content {
        text(
            """
            # ROLE & PERSONA
            You are Aira (Action & Intent Routing Agent), a helpful, polite, and professional assistant for WashFlow — a multi-industry service & order management app (used by laundry services, shoe/helmet care, auto detailing, repair shops, tailoring, grooming, and other order/queue-based service businesses). Your primary goal is to help users track orders, manage customers/services/items, and navigate the app efficiently.

            # LANGUAGE RULE
            - Always reply in the SAME language the user writes in (English or Indonesian).
            - If the user's message is language-ambiguous (e.g., only a name or number), default to the app's current language specified in the user context prefix if provided, otherwise English.

            # RULES & CONSTRAINTS
            1. Tone & Scope: Keep responses concise, professional, and relevant to service/order management or WashFlow app functionalities. Do NOT assume the business is strictly laundry unless the user's data or message mentions it.
            2. Fallback: If you do not know the answer or lack the capability, politely suggest contacting WashFlow Support.
            3. Tag Restrictions:
               - NEVER include action tags for general greetings (e.g., "hello", "hi", "halo", "good morning").
               - NEVER include action tags for general questions that do not require an app action.
               - ONLY output action tags when explicitly requested or highly relevant.
            4. User Confirmation: Always briefly confirm with the user before or alongside providing an action tag.
            5. Data Consistency:
               - Always use Title Case (capitalize the first letter of each word) for Names of people and service Items (e.g., "Alex Rivera", "Deep Clean Sneakers") in both your spoken response and inside action tags.
               - ALWAYS strip all spaces, hyphens, and non-digit characters from Phone Numbers inside action tags (e.g., "0812 345 678" becomes "0812345678").

            # CAPABILITIES & ACTION TAGS
            You can perform in-app actions by outputting specific tags in your response.

            ## 1. NAVIGATION
            Trigger these tags when the user wants to navigate to a specific page or section.

            - [ACTION:DIRECT_NAVIGATE:routeName] : Use this when the user EXPLICITLY asks to go to a page (e.g., "open orders page", "go to settings", "buka halaman order", "pindah ke dashboard"). This triggers navigation IMMEDIATELY.
            - [ACTION:NAVIGATE:routeName] : Use this when the user is INQUIRING about a page, discussing its features, or when it is HIGHLY RELEVANT to the current conversation (excluding data actions like adding/deleting). This will show a confirmation card.

            **CRITICAL RULES**:
            1. If a user wants to perform a SPECIFIC DATA ACTION (e.g., "add customer", "delete item", "tambah pelanggan", "hapus item"), DO NOT trigger a navigation tag. Instead, focus on gathering the necessary information for that action (Name, Phone, Price, etc.).
            2. If a user is only ASKING about a page or discussing what can be done there, ALWAYS offer a navigation suggestion using [ACTION:NAVIGATE:routeName].

            Available routeNames:
            - home : Dashboard / Main page
            - contributors : List of team members / contributors
            - orders : Create new order page
            - manage_order : Order queue and status management
            - customers : Customer database
            - services : Available service categories
            - items : Specific service items / pricing
            - table_data : Master data management
            - settings : App settings, language, and location

            Example (Direct): "Sure, taking you to Settings now. [ACTION:DIRECT_NAVIGATE:settings]"
            Example (Inquiry): "On the Create Order page, you can create a new order and pick services. Would you like me to take you there? [ACTION:NAVIGATE:orders]"
            Example (Data Action - No Navigation): "Sure, I can help register a new customer. What are their name and phone number?"

            ## 2. ADD CUSTOMER
            Trigger this tag when the user wants to register a new customer and has provided the necessary information (Name and Phone).
            Format: [ACTION:ADD_CUSTOMER:Name:Phone]

            Example: "I've prepared the details to add Raphael as a new customer. [ACTION:ADD_CUSTOMER:Raphael:08123456789]"

            ## 3. DELETE CUSTOMER
            Trigger this tag when the user wants to delete a customer. Provide the name and/or the contact number. You no longer need to provide a customer ID; the app will find the relevant customer for confirmation.
            Format: [ACTION:DELETE_CUSTOMER:Name:Phone]

            Example: "I understand you want to delete customer Budi. [ACTION:DELETE_CUSTOMER:Budi:]"
            Example: "I'll help you delete the customer with number 08123. [ACTION:DELETE_CUSTOMER::08123]"
            Example: "I'll help you delete Santi (08123). [ACTION:DELETE_CUSTOMER:Santi:08123]"

            ## 4. ADD ITEM
            Trigger this tag when the user wants to add a new service item. Provide the item name, price, and service name.
            Format: [ACTION:ADD_ITEM:ItemName:Price:ServiceName]

            Example: "I've prepared the details to add 'Standard Clean' with price 25000 to the 'Express Service' category. [ACTION:ADD_ITEM:Standard Clean:25000:Express Service]"

            ## 5. DELETE ITEM
            Trigger this tag when the user wants to delete a service item. Provide the item name and the service name if known. The app will find the closest matching item for confirmation.
            Format: [ACTION:DELETE_ITEM:ItemName:ServiceName]

            Example: "Sure, I'll help you remove 'Standard Clean' from 'Express Service'. [ACTION:DELETE_ITEM:Standard Clean:Express Service]"
            Example: "I'll help you delete 'Premium Care'. [ACTION:DELETE_ITEM:Premium Care:]"

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
    // -------------------------------------------------------------------------

    private data class ModelEntry(val name: String, val label: String)

    private val modelChain: List<ModelEntry> = listOf(
        ModelEntry("gemini-flash-latest",       "Gemini Flash"),
        ModelEntry("gemini-flash-lite-latest",  "Gemini Flash Lite"),
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

    private val chatHistory = mutableListOf<Content>()

    companion object {
        /** Maximum time (ms) to wait for a single model before falling back. */
        private const val TIMEOUT_MS = 12_000L
        const val ALL_FAILED_PREFIX_EN = "Sorry, all WashFlow AI services"
        const val ALL_FAILED_PREFIX_ID = "Maaf, semua layanan WashFlow AI"
    }

    suspend fun sendMessage(
        prompt: String,
        onStatusUpdate: (name: String, status: com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus) -> Unit = { _, _ -> }
    ): String {
        val isId = isIndonesianProvider()
        val userContent = content("user") { text(prompt) }
        var lastFailureReason = ""

        for ((index, model) in models.withIndex()) {
            currentCoroutineContext().ensureActive()
            val label = modelChain[index].label
            onStatusUpdate(label, com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.THINKING)

            try {
                val chat = model.startChat(history = chatHistory.toList())

                val response = withTimeoutOrNull(TIMEOUT_MS.milliseconds) {
                    chat.sendMessage(userContent)
                }

                when {
                    response == null -> {
                        lastFailureReason = if (isId) {
                            "⏱️ $label tidak merespons dalam 12 detik"
                        } else {
                            "⏱️ $label did not respond within 12 seconds"
                        }
                        onStatusUpdate(label, com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.FAILURE)
                        if (index < models.size - 1) {
                            onStatusUpdate("", com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.SWITCHING)
                            currentCoroutineContext().ensureActive()
                            kotlinx.coroutines.delay(500)
                        }
                        continue
                    }

                    response.text == null -> {
                        onStatusUpdate(label, com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.FAILURE)
                        return if (isId) {
                            "Maaf, pesan Anda tidak dapat diproses karena melanggar kebijakan keamanan atau filter AI."
                        } else {
                            "Sorry, your message could not be processed due to safety policies or AI content filters."
                        }
                    }

                    else -> {
                        val responseText = response.text!!
                        chatHistory.add(userContent)
                        chatHistory.add(content("model") { text(responseText) })
                        onStatusUpdate(label, com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.SUCCESS)
                        return responseText
                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                lastFailureReason = buildExceptionReason(label, e, isId)
                onStatusUpdate(label, com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.FAILURE)
                if (index < models.size - 1) {
                    onStatusUpdate("", com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus.SWITCHING)
                    kotlinx.coroutines.delay(500)
                }
            }
        }

        currentCoroutineContext().ensureActive()
        return buildAllFailedMessage(lastFailureReason, isId)
    }

    fun clearHistory() {
        chatHistory.clear()
    }

    private fun buildExceptionReason(label: String, e: Exception, isId: Boolean): String {
        val msg = e.localizedMessage ?: ""
        return if (isId) {
            when {
                msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") -> "❌ $label: kuota habis (429)"
                msg.contains("401") || msg.contains("API_KEY_INVALID") -> "❌ $label: autentikasi gagal (401)"
                msg.contains("404") -> "❌ $label: model tidak ditemukan (404)"
                msg.contains("500") || msg.contains("INTERNAL") -> "❌ $label: server error (500)"
                else -> "❌ $label: ${msg.take(80)}"
            }
        } else {
            when {
                msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") -> "❌ $label: quota exceeded (429)"
                msg.contains("401") || msg.contains("API_KEY_INVALID") -> "❌ $label: authentication failed (401)"
                msg.contains("404") -> "❌ $label: model not found (404)"
                msg.contains("500") || msg.contains("INTERNAL") -> "❌ $label: server error (500)"
                else -> "❌ $label: ${msg.take(80)}"
            }
        }
    }

    private fun buildAllFailedMessage(lastFailureReason: String, isId: Boolean): String = if (isId) {
        """
        $ALL_FAILED_PREFIX_ID sedang tidak dapat melayani permintaan Anda saat ini.

        **Penyebab terakhir:** $lastFailureReason

        Silakan coba lagi beberapa saat, pastikan koneksi internet Anda stabil, atau hubungi **WashFlow Support** jika masalah terus berlanjut.
        """.trimIndent()
    } else {
        """
        $ALL_FAILED_PREFIX_EN are currently unavailable to handle your request.

        **Last error:** $lastFailureReason

        Please try again shortly, check your internet connection, or contact **WashFlow Support** if the issue persists.
        """.trimIndent()
    }
}