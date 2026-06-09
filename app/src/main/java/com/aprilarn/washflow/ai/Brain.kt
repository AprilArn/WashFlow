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
            response.text ?: "Maaf, AI tidak memberikan respon."
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: ""
            // Menangani error agar lebih user-friendly sesuai instruksi
            when {
                errorMsg.contains("429") || errorMsg.contains("RESOURCE_EXHAUSTED") ->
                    "Mohon maaf, layanan sedang sibuk atau kuota harian habis. Silakan tunggu beberapa saat lagi atau hubungi WashFlow Support."
                errorMsg.contains("404") ->
                    "Terjadi kesalahan konfigurasi pada sistem AI. Silakan hubungi WashFlow Support."
                else -> "Terjadi gangguan pada sistem AI. Silakan coba beberapa saat lagi atau hubungi WashFlow Support."
            }
        }
    }

    fun clearHistory() {
        chatHistory = model.startChat()
    }
}
