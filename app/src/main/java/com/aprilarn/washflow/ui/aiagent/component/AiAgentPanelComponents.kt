package com.aprilarn.washflow.ui.aiagent.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.ui.aiagent.AiModelStatus
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack

@Composable
fun AiAgentPanelHeader(
    onClearHistory: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Aira",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            ),
            color = MainFontBlack
        )
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MainFontBlack
                )
            }

            if (showMenu) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(x = 0, y = 120),
                    onDismissRequest = { showMenu = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(end = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                            Text(
                                text = "Delete History",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onClearHistory()
                                        showMenu = false
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAgentPanelInputArea(
    inputMessage: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    modelStatus: AiModelStatus,
    currentModelName: String?,
    isProcessing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent {
                        if (it.key == Key.Enter && !it.isShiftPressed) {
                            if (it.type == KeyEventType.KeyDown) {
                                if (inputMessage.text.isNotBlank() && !isProcessing) {
                                    onSendMessage()
                                }
                            }
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text("Ask WashFlow AI...", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputMessage.text.isNotBlank() && !isProcessing) {
                            onSendMessage()
                        }
                    }
                ),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = GrayBlue
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MainFontBlack)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AnimatedContent(
                            targetState = modelStatus to currentModelName,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "ModelIndicatorTransition"
                        ) { (status, name) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (status == AiModelStatus.IDLE) "Idle" else (name ?: ""),
                                    color = Gray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                when (status) {
                                    AiModelStatus.IDLE -> {
                                        Icon(
                                            imageVector = Icons.Default.MoreHoriz,
                                            contentDescription = "Idle",
                                            modifier = Modifier.size(14.dp),
                                            tint = Gray
                                        )
                                    }
                                    AiModelStatus.THINKING -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = GrayBlue
                                        )
                                    }
                                    AiModelStatus.SUCCESS -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Success",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                    }
                                    AiModelStatus.FAILURE -> {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Failed",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.Red
                                        )
                                    }
                                    AiModelStatus.SWITCHING -> {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Switching",
                                            modifier = Modifier.size(14.dp),
                                            tint = GrayBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onSendMessage,
                        enabled = inputMessage.text.isNotBlank() && !isProcessing,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (inputMessage.text.isNotBlank() && !isProcessing)
                                    GrayBlue
                                else
                                    Color(0xFFE0E0E0)
                            )
                            .height(38.dp)
                            .width(52.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiAgentEmptyState(userName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Hi, $userName",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GrayBlue,
                fontSize = 32.sp
            )
        )
        Text(
            text = "What can I help you today?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Gray,
                fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(64.dp))

        // Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2124))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "More ways to access AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Upgrade to a qualified Google AI plan for subscription access to Gemini, or provide API keys to use Anthropic, OpenAI, and Gemini via AI Studio. For offline development, run local models via local LLM hosts.",
                    color = Color(0xFFB0B0B0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Prompts to try",
            fontWeight = FontWeight.Bold,
            color = MainFontBlack,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        PromptItem("Extract all hardcoded strings from this class and move them into strings.xml")
        PromptItem("Add documentation to my current file")
        PromptItem("Update kotlin in @libs.version.toml to the latest version")
        PromptItem("Make my Theme's color scheme warmer")

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AiAgentScrollToBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF60B0FF).copy(alpha = 0.9f),
        border = BorderStroke(1.dp, Color(0xFFC1DFFF).copy(alpha = 0.9f)),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Scroll to bottom",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }
    }
}