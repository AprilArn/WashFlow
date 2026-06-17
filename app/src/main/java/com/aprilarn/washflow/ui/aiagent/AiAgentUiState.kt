package com.aprilarn.washflow.ui.aiagent

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
    val inputMessage: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val currentModelName: String? = null,
    val modelStatus: AiModelStatus = AiModelStatus.IDLE
)
