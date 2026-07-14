package com.aprilarn.washflow.ui.aiagent.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.aiagent.VoiceAgentStatus
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack

@Composable
fun VoiceAgentOverlay(
    status: VoiceAgentStatus,
    text: String = "",
    modifier: Modifier = Modifier
) {
    val displayTitle = when (status) {
        VoiceAgentStatus.LISTENING -> "Listening..."
        VoiceAgentStatus.THINKING -> "Thinking..."
        VoiceAgentStatus.ANSWERING -> "Aira"
        else -> ""
    }

    val icon = when (status) {
        VoiceAgentStatus.LISTENING -> Icons.Default.Mic
        VoiceAgentStatus.THINKING -> Icons.Default.Sync
        VoiceAgentStatus.ANSWERING -> Icons.Default.AutoAwesome
        else -> Icons.Default.AutoAwesome
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    AnimatedVisibility(
        visible = status != VoiceAgentStatus.IDLE,
        enter = fadeIn(animationSpec = tween(400)) + 
                scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut(animationSpec = tween(500)) +
               scaleOut(targetScale = 0.8f) + 
               slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.wrapContentWidth().height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Area
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(64.dp)
                        .background(
                            color = GrayBlue.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = icon to (status == VoiceAgentStatus.THINKING),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f) togetherWith
                                    fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
                        },
                        label = "VoiceAgentIcon"
                    ) { (targetIcon, isProcessing) ->
                        Icon(
                            imageVector = targetIcon,
                            contentDescription = null,
                            tint = GrayBlue,
                            modifier = Modifier.size(24.dp)
                                .then(
                                    if (isProcessing) Modifier.graphicsLayer { rotationZ = rotation }
                                    else Modifier
                                )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = displayTitle,
                        transitionSpec = {
                            (slideInVertically { it / 2 } + fadeIn(animationSpec = tween(300))) togetherWith
                                    (slideOutVertically { -it / 2 } + fadeOut(animationSpec = tween(300)))
                        },
                        label = "VoiceAgentTitle"
                    ) { targetTitle ->
                        Text(
                            text = targetTitle,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MainFontBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    AnimatedVisibility(
                        visible = status == VoiceAgentStatus.ANSWERING && text.isNotBlank(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}