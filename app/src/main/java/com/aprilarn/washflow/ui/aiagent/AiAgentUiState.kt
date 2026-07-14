package com.aprilarn.washflow.ui.aiagent

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import androidx.compose.ui.text.input.TextFieldValue

enum class AiModelStatus {
    IDLE,
    THINKING,
    SUCCESS,
    FAILURE,
    SWITCHING
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isThinking: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val action: AiAgentAction? = null,
    val actionExecuted: Boolean = false,
    val actionCancelled: Boolean = false
)

data class AiAgentUiState(
    val expanded: Boolean = false,
    val userName: String = "",
    val profilePictureUrl: String? = null,
    val inputMessage: TextFieldValue = TextFieldValue(""),
    val messages: List<ChatMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val currentModelName: String? = null,
    val modelStatus: AiModelStatus = AiModelStatus.IDLE,
    val customers: List<Customers> = emptyList(),
    val items: List<Items> = emptyList(),
    val services: List<Services> = emptyList(),
    val animatedMessageIds: Set<String> = emptySet(),
    val isTypewriterActive: Boolean = false,
    val voiceAgentStatus: VoiceAgentStatus = VoiceAgentStatus.IDLE,
    val voiceAgentText: String = ""
)

enum class VoiceAgentStatus {
    IDLE, LISTENING, THINKING, ANSWERING
}
