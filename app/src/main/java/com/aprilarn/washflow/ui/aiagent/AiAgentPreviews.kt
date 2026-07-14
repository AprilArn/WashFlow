package com.aprilarn.washflow.ui.aiagent

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.aprilarn.washflow.ui.aiagent.component.*

@Preview(showBackground = true)
@Composable
fun AiAgentPanelChatPreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = TextFieldValue(""),
        messages = listOf(
            ChatMessage(text = "Hello, can you help me?", isUser = true),
            ChatMessage(text = "Sure! What can I do for you?", isUser = false),
            ChatMessage(text = "I want to track my order.", isUser = true),
            ChatMessage(text = "I can help you with that. Which order would you like to track?", isUser = false)
        ),
        isAiThinking = false,
        currentModelName = "Gemini Flash",
        modelStatus = AiModelStatus.IDLE,
        customers = emptyList(),
        items = emptyList(),
        services = emptyList(),
        animatedMessageIds = emptySet(),
        wasMessageAnimated = { true },
        onMessageAnimated = {},
        getAnimationProgress = { -1 },
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AiAgentPanelBulletPointPreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = TextFieldValue(""),
        messages = listOf(
            ChatMessage(
                text = "I can assist you with:\n\n* **Tracking your orders**: Get real-time updates.\n* **Managing your account**: Help with navigation.\n* **Answering general questions**: Provide information.",
                isUser = false
            )
        ),
        isAiThinking = false,
        currentModelName = "Gemini Flash",
        modelStatus = AiModelStatus.IDLE,
        customers = emptyList(),
        items = emptyList(),
        services = emptyList(),
        animatedMessageIds = emptySet(),
        wasMessageAnimated = { true },  // Preview: pretend all already animated
        onMessageAnimated = {},
        getAnimationProgress = { -1 },
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AiAgentPanelIdlePreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = TextFieldValue(""),
        messages = emptyList(),
        isAiThinking = false,
        currentModelName = null,
        modelStatus = AiModelStatus.IDLE,
        customers = emptyList(),
        items = emptyList(),
        services = emptyList(),
        animatedMessageIds = emptySet(),
        wasMessageAnimated = { true },
        onMessageAnimated = {},
        getAnimationProgress = { -1 },
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}
