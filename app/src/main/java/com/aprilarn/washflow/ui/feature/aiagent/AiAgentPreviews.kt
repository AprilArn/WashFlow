package com.aprilarn.washflow.ui.feature.aiagent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.feature.aiagent.component.*

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
        onStopProcessing = {},
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
        onStopProcessing = {},
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
        onStopProcessing = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Phone Voice Overlay - States")
@Composable
fun VoiceAgentOverlayPhoneStatesPreview() {
    Column(
        modifier = Modifier
            .width(360.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VoiceAgentOverlay(
            status = VoiceAgentStatus.THINKING,
            isCompact = true,
            isVisibleInitial = true
        )
        VoiceAgentOverlay(
            status = VoiceAgentStatus.LISTENING,
            isCompact = true,
            isVisibleInitial = true,
            text = "Tambahkan pelanggan baru bernama Budi..."
        )
        VoiceAgentOverlay(
            status = VoiceAgentStatus.ANSWERING,
            isCompact = true,
            isVisibleInitial = true,
            text = "Berikut ringkasannya:\n* **Order aktif**: 5 pesanan\n* **Siap diambil**: 2 pesanan"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Phone Voice Overlay - Confirmation")
@Composable
fun VoiceAgentOverlayPhoneConfirmationPreview() {
    val action = AiAgentAction.AddCustomer(name = "Budi Santoso", phoneNumber = "081234567890")
    Box(
        modifier = Modifier
            .width(360.dp)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        VoiceAgentOverlay(
            status = VoiceAgentStatus.WAITING_FOR_CONFIRMATION,
            isCompact = true,
            isVisibleInitial = true,
            isAlreadyAnimated = true,
            text = "Saya siap menambahkan pelanggan baru ini. Mohon konfirmasi ya.",
            lastMessage = ChatMessage(
                text = "Saya siap menambahkan pelanggan baru ini.",
                isUser = false,
                action = action
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Tablet Voice Overlay - Answering")
@Composable
fun VoiceAgentOverlayTabletPreview() {
    Column(
        modifier = Modifier
            .width(560.dp)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VoiceAgentOverlay(
            status = VoiceAgentStatus.ANSWERING,
            isCompact = false,
            isVisibleInitial = true,
            text = "Total pendapatan minggu ini adalah **Rp 1.450.000** dari 11 pelanggan."
        )
    }
}
