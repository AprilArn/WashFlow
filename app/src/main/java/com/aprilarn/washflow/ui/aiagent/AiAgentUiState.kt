package com.aprilarn.washflow.ui.aiagent

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

data class AiAgentUiState(
    val expanded: Boolean = false,
    val userName: String = "",
    val profilePictureUrl: String? = null,
    val inputMessage: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isAiThinking: Boolean = false
)
