package com.aprilarn.washflow.ui.feature.aiagent.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.feature.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.feature.aiagent.ChatMessage
import com.aprilarn.washflow.utils.MarkdownUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiMessageHeader(isThinking: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "aiSparkleTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aiSparkleScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aiSparkleAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, Color.White.copy(alpha = 0.90f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = stringResource(R.string.ai_name),
                tint = GrayBlue,
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        if (isThinking) {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                    }
            )
        }

        if (isThinking) {
            Text(
                text = stringResource(R.string.ai_thinking),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF475569).copy(alpha = pulseAlpha)
            )
        }
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
fun PromptItem(text: String, onClick: () -> Unit = {}) {
    val chipShape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(chipShape)
            .background(Color.White.copy(alpha = 0.58f))
            .border(1.dp, Color.White.copy(alpha = 0.82f), chipShape)
            .clickable { onClick() }
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
            val bubbleShape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(bubbleShape)
                    .background(Color.White.copy(alpha = 0.74f))
                    .border(1.dp, Color.White.copy(alpha = 0.88f), bubbleShape)
                    .padding(12.dp)
            ) {
                Text(
                    text = MarkdownUtils.parseMarkdown(message.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainFontBlack
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (!profilePictureUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = stringResource(R.string.ai_user_profile_cd),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GrayBlue.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, Color.White.copy(alpha = 0.90f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = stringResource(R.string.ai_user_profile_cd),
                        tint = GrayBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        val context = LocalContext.current
        val view = LocalView.current
        var copied by remember(message.id) { mutableStateOf(false) }

        LaunchedEffect(copied) {
            if (copied) {
                delay(2000L)
                copied = false
            }
        }

        val timeText = remember(message.timestamp) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            AiMessageHeader(isThinking = message.isThinking)

            if (!message.isThinking) {
                Spacer(modifier = Modifier.height(8.dp))

                val isTypewriterActive by remember(progress, message.text) {
                    derivedStateOf { progress >= 0 && progress < message.text.length }
                }
                val hasAction = message.action != null &&
                        message.action !is AiAgentAction.None &&
                        !message.actionExecuted &&
                        !message.actionCancelled
                var delayedShowActionCard by remember(message.id, isAlreadyAnimated) {
                    mutableStateOf(isAlreadyAnimated && !isTypewriterActive && hasAction)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    TypewriterText(
                        text = message.text,
                        progress = progress,
                        isNewMessage = !isAlreadyAnimated,
                        onTextUpdate = onTextUpdate
                    )

                    LaunchedEffect(isTypewriterActive, hasAction, isAlreadyAnimated) {
                        when {
                            isTypewriterActive || !hasAction -> {
                                delayedShowActionCard = false
                            }
                            isAlreadyAnimated -> {
                                delayedShowActionCard = true
                            }
                            else -> {
                                delay(250L)
                                delayedShowActionCard = true
                            }
                        }
                    }

                    LaunchedEffect(delayedShowActionCard) {
                        if (delayedShowActionCard) {
                            onTextUpdate()
                        }
                    }

                    AnimatedVisibility(
                        visible = delayedShowActionCard,
                        enter = if (isAlreadyAnimated) {
                            EnterTransition.None
                        } else {
                            fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(350))
                        },
                        exit = if (isAlreadyAnimated) {
                            ExitTransition.None
                        } else {
                            fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(250))
                        }
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
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.ai_action_executed),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    if (message.actionCancelled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.ai_action_cancelled),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !isTypewriterActive && message.text.isNotBlank(),
                        enter = fadeIn(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(150))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val copyChipShape = RoundedCornerShape(8.dp)
                            Row(
                                modifier = Modifier
                                    .clip(copyChipShape)
                                    .background(Color.White.copy(alpha = 0.50f))
                                    .border(1.dp, Color.White.copy(alpha = 0.78f), copyChipShape)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Aira", message.text))
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        copied = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                    contentDescription = stringResource(
                                        if (copied) R.string.ai_copied_message else R.string.ai_copy_message
                                    ),
                                    tint = if (copied) Color(0xFF2E7D32) else Color(0xFF475569),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = stringResource(
                                        if (copied) R.string.ai_copied_message else R.string.ai_copy_message
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (copied) Color(0xFF2E7D32) else Color(0xFF475569)
                                )
                            }

                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}