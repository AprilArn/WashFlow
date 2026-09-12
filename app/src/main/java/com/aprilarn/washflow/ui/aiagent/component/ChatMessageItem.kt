package com.aprilarn.washflow.ui.aiagent.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.aiagent.ChatMessage
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.utils.MarkdownUtils
import kotlinx.coroutines.delay

@Composable
fun AiMessageHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = GrayBlue,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Aira",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = GrayBlue
        )
    }
}

@Composable
fun TypewriterText(
    text: String,
    progress: Int,
    modifier: Modifier = Modifier,
    isNewMessage: Boolean = true,
    onTextUpdate: () -> Unit = {}
) {
    val displayedText = remember(text, progress) {
        if (progress < 0 || progress >= text.length) text else text.take(progress)
    }

    val view = LocalView.current

    LaunchedEffect(progress) {
        if (progress > 0 && progress < text.length) {
            val currentChar = text[progress - 1]
            if (currentChar.isWhitespace()) {
                val prevChar = if (progress > 1) text[progress - 2] else null
                if (prevChar == null || !prevChar.isWhitespace()) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            onTextUpdate()
        }

        if (progress >= text.length && isNewMessage) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    Text(
        text = MarkdownUtils.parseMarkdown(displayedText),
        style = MaterialTheme.typography.bodyMedium,
        color = MainFontBlack,
        lineHeight = 20.sp,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PromptItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clickable { }
            .padding(12.dp)
    ) {
        Text(text, color = MainFontBlack, fontSize = 13.sp)
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    profilePictureUrl: String?,
    isAlreadyAnimated: Boolean = false,
    progress: Int = -1,
    customers: List<Customers> = emptyList(),
    items: List<Items> = emptyList(),
    services: List<Services> = emptyList(),
    onConfirmAction: (AiAgentAction?) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onTextUpdate: () -> Unit = {}
) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFFF0F2F5))
                    .padding(12.dp)
            ) {
                Text(
                    text = MarkdownUtils.parseMarkdown(message.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainFontBlack
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = profilePictureUrl,
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GrayBlue.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            AiMessageHeader()
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = message.isThinking,
                transitionSpec = {
                    if (targetState) {
                        fadeIn(animationSpec = tween(300))
                            .togetherWith(ExitTransition.None)
                    } else {
                        fadeIn(animationSpec = tween(300))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = "AiContentTransition"
            ) { thinking ->
                if (thinking) {
                    Text(
                        text = "Thinking...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Gray,
                        lineHeight = 20.sp
                    )
                } else {
                    val isTypewriterActive by remember(progress, message.text) {
                        derivedStateOf { progress >= 0 && progress < message.text.length }
                    }
                    var delayedShowActionCard by remember(message.id, isAlreadyAnimated) { 
                        mutableStateOf(isAlreadyAnimated) 
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        TypewriterText(
                            text = message.text,
                            progress = progress,
                            isNewMessage = !isAlreadyAnimated,
                            onTextUpdate = onTextUpdate
                        )

                        val hasAction = message.action != null &&
                                message.action !is AiAgentAction.None &&
                                !message.actionExecuted &&
                                !message.actionCancelled &&
                                !message.isThinking

                        LaunchedEffect(isTypewriterActive, hasAction, isAlreadyAnimated) {
                            if (isAlreadyAnimated) {
                                delayedShowActionCard = hasAction
                            } else if (!isTypewriterActive && hasAction) {
                                delay(250L)
                                delayedShowActionCard = true
                            } else {
                                delayedShowActionCard = false
                            }
                        }

                        LaunchedEffect(delayedShowActionCard) {
                            if (delayedShowActionCard) {
                                onTextUpdate()
                            }
                        }

                        AnimatedVisibility(
                            visible = delayedShowActionCard,
                            enter = if (isAlreadyAnimated) EnterTransition.None else fadeIn(animationSpec = tween(500)),
                            exit = if (isAlreadyAnimated) ExitTransition.None else fadeOut(animationSpec = tween(500))
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                ActionConfirmationCard(
                                    action = message.action!!,
                                    customers = customers,
                                    items = items,
                                    services = services,
                                    onConfirm = onConfirmAction,
                                    onCancel = onCancelAction
                                )
                            }
                        }

                        if (message.actionExecuted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Action executed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        if (message.actionCancelled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Action cancelled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}