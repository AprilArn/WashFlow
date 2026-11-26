package com.aprilarn.washflow.ui.feature.aiagent.component

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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus
import com.aprilarn.washflow.ui.core.theme.Gray
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg

@Composable
fun AiAgentPanelHeader(
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.ai_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            ),
            color = MainFontBlack
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.ai_more_options_cd),
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
                                    text = stringResource(R.string.ai_delete_history),
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

            Surface(
                onClick = {
                    showMenu = false
                    onDismiss()
                },
                shape = CircleShape,
                color = PastelBlueSoftBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.ai_close_panel_cd),
                        tint = GrayBlue,
                        modifier = Modifier.size(20.dp)
                    )
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
    onStopProcessing: () -> Unit,
    modelStatus: AiModelStatus,
    currentModelName: String?,
    cooldownSeconds: Int = 0,
    isAiThinking: Boolean,
    isTypewriterActive: Boolean
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
                                if (inputMessage.text.isNotBlank() && !isAiThinking && !isTypewriterActive && modelStatus != AiModelStatus.COOLDOWN) {
                                    onSendMessage()
                                }
                            }
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text(stringResource(R.string.ai_input_placeholder), color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputMessage.text.isNotBlank() && !isAiThinking && !isTypewriterActive && modelStatus != AiModelStatus.COOLDOWN) {
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
                            targetState = Triple(modelStatus, currentModelName, cooldownSeconds),
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "ModelIndicatorTransition"
                        ) { (status, name, cooldown) ->
                            val statusText = when (status) {
                                AiModelStatus.IDLE -> stringResource(R.string.ai_status_idle)
                                AiModelStatus.SWITCHING -> stringResource(R.string.ai_status_switching)
                                AiModelStatus.COOLDOWN -> stringResource(R.string.ai_status_cooldown, cooldown)
                                else -> name ?: ""
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    color = Gray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                when (status) {
                                    AiModelStatus.IDLE -> {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreHoriz,
                                            contentDescription = stringResource(R.string.ai_status_idle),
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
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = stringResource(R.string.ai_status_success_cd),
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                    }
                                    AiModelStatus.FAILURE -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.ai_status_failed_cd),
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.Red
                                        )
                                    }
                                    AiModelStatus.SWITCHING -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = stringResource(R.string.ai_status_switching_cd),
                                            modifier = Modifier.size(14.dp),
                                            tint = GrayBlue
                                        )
                                    }
                                    AiModelStatus.COOLDOWN -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Timer,
                                            contentDescription = stringResource(R.string.ai_status_cooldown_cd),
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val showStopButton = isAiThinking
                    val showDisabledSend = isTypewriterActive || modelStatus == AiModelStatus.COOLDOWN

                    IconButton(
                        onClick = {
                            if (showStopButton) onStopProcessing() else onSendMessage()
                        },
                        enabled = (inputMessage.text.isNotBlank() || showStopButton) && !showDisabledSend,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    showStopButton -> Color.Red
                                    showDisabledSend -> Color(0xFFE0E0E0)
                                    inputMessage.text.isNotBlank() -> GrayBlue
                                    else -> Color(0xFFE0E0E0)
                                }
                            )
                            .height(38.dp)
                            .width(52.dp)
                    ) {
                        Icon(
                            imageVector = if (showStopButton) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(
                                if (showStopButton) R.string.ai_action_stop_cd else R.string.ai_action_send_cd
                            ),
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
fun AiAgentEmptyState(
    userName: String,
    onPromptClick: (String) -> Unit
) {
    val promptAddCustomer = stringResource(R.string.ai_prompt_add_customer)
    val promptDeleteCustomer = stringResource(R.string.ai_prompt_delete_customer)
    val promptAddItem = stringResource(R.string.ai_prompt_add_item)
    val promptDeleteItem = stringResource(R.string.ai_prompt_delete_item)
    val promptNavigate = stringResource(R.string.ai_prompt_navigate)

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = stringResource(R.string.ai_greeting, userName),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GrayBlue,
                fontSize = 32.sp
            )
        )
        Text(
            text = stringResource(R.string.ai_greeting_subtitle),
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
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.ai_empty_tip_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.ai_empty_tip_body),
                    color = Color(0xFFB0B0B0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            stringResource(R.string.ai_prompts_header),
            fontWeight = FontWeight.Bold,
            color = MainFontBlack,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        PromptItem(text = promptAddCustomer, onClick = { onPromptClick(promptAddCustomer) })
        PromptItem(text = promptDeleteCustomer, onClick = { onPromptClick(promptDeleteCustomer) })
        PromptItem(text = promptAddItem, onClick = { onPromptClick(promptAddItem) })
        PromptItem(text = promptDeleteItem, onClick = { onPromptClick(promptDeleteItem) })
        PromptItem(text = promptNavigate, onClick = { onPromptClick(promptNavigate) })

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
                text = stringResource(R.string.ai_scroll_to_bottom),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }
    }
}