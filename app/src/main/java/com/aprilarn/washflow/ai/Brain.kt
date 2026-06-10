package com.aprilarn.washflow.ai

import com.aprilarn.washflow.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Brain {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val systemInstruction = content {
        text("""
            You are WashFlow AI, a helpful and friendly assistant for the WashFlow application.
            WashFlow is a laundry service management app that helps users track orders, and manage their laundry needs.
            Your tone should be professional, polite, and helpful.
            Keep your responses concise and relevant to the laundry industry or the WashFlow app functionality.
            If you don't know the answer, politely suggest the user to contact WashFlow support.
        """.trimIndent())
    }

    private val model = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
        ),
        systemInstruction = systemInstruction
    )

    private var chatHistory = model.startChat()

    suspend fun sendMessage(prompt: String): String {
        return try {
            val response = chatHistory.sendMessage(prompt)
            // Jika response.text null, kemungkinan besar terkena filter keamanan (Safety)
            response.text ?: "Maaf, pesan Anda tidak dapat diproses karena melanggar kebijakan keamanan atau filter AI."
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: ""
            when {
                errorMsg.contains("429") || errorMsg.contains("RESOURCE_EXHAUSTED") ->
                    "Mohon maaf, kuota harian AI sudah habis atau layanan sedang sibuk. Silakan coba lagi besok atau hubungi Support. (Error: 429)"

                errorMsg.contains("401") || errorMsg.contains("API_KEY_INVALID") ->
                    "Terjadi masalah otentikasi pada sistem AI. Silakan hubungi WashFlow Support. (Error: 401)"

                errorMsg.contains("404") ->
                    "Model AI tidak ditemukan atau terjadi kesalahan konfigurasi sistem. (Error: 404)"

                errorMsg.contains("500") || errorMsg.contains("INTERNAL") ->
                    "Server AI sedang mengalami gangguan teknis. Silakan coba beberapa saat lagi. (Error: 500)"

                else -> "Koneksi ke WashFlow AI terputus. Pastikan internet Anda stabil atau coba lagi nanti."
            }
        }
    }

    fun clearHistory() {
        chatHistory = model.startChat()
    }
}
