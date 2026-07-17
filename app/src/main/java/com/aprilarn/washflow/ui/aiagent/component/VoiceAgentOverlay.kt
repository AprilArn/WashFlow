package com.aprilarn.washflow.ui.aiagent.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.aiagent.VoiceAgentStatus
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.utils.MarkdownUtils

@Composable
fun VoiceAgentOverlay(
    status: VoiceAgentStatus,
    text: String = "",
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var userHasInterrupted by remember { mutableStateOf(false) }
    val isDragging by scrollState.interactionSource.collectIsDraggedAsState()

    // Detect if we are at the bottom to resume auto-scroll
    val isAtBottom by remember {
        derivedStateOf {
            scrollState.value >= scrollState.maxValue
        }
    }

    LaunchedEffect(isDragging) {
        if (isDragging) userHasInterrupted = true
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !isDragging) userHasInterrupted = false
    }

    // Auto-scroll logic during typewriter
    LaunchedEffect(text) {
        if (!userHasInterrupted && status == VoiceAgentStatus.ANSWERING) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Reset scroll and interruption when status changes (e.g. back to listening)
    LaunchedEffect(status) {
        if (status == VoiceAgentStatus.LISTENING || status == VoiceAgentStatus.IDLE) {
            scrollState.scrollTo(0)
            userHasInterrupted = false
        }
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
                expandHorizontally(
                    expandFrom = Alignment.CenterHorizontally,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) +
                slideInVertically(
                    initialOffsetY = { fullHeight -> -fullHeight },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        exit = fadeOut(animationSpec = tween(400)) +
                shrinkHorizontally(
                    shrinkTowards = Alignment.CenterHorizontally,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) +
                slideOutVertically(
                    targetOffsetY = { fullHeight -> -fullHeight },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 58.dp, bottom = 48.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 200.dp) // Limit height
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    alignment = Alignment.Center
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            // Kita definisikan warna background kiri di sini
            val leftBgColor = GrayBlue.copy(alpha = 0.08f)

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    // Menggambar background kiri secara presisi tanpa mengacaukan tinggi
                    .drawBehind {
                        drawRect(
                            color = leftBgColor,
                            size = androidx.compose.ui.geometry.Size(64.dp.toPx(), size.height)
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Area
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .padding(vertical = 16.dp), // Memberikan padding agar selalu memiliki tinggi minimum
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = icon to (status == VoiceAgentStatus.THINKING),
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f)) togetherWith
                                    (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)) using SizeTransform(clip = false)
                        },
                        label = "VoiceAgentIcon"
                    ) { (targetIcon, isProcessing) ->
                        Icon(
                            imageVector = targetIcon,
                            contentDescription = null,
                            tint = GrayBlue,
                            modifier = Modifier
                                .size(24.dp)
                                .then(
                                    if (isProcessing) Modifier.graphicsLayer { rotationZ = rotation }
                                    else Modifier
                                )
                        )
                    }
                }

                // Right Content Area
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false) // Allow taking space but not forcing it
                        .verticalScroll(scrollState) // Enable vertical scroll
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = status,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith
                                    fadeOut(animationSpec = tween(400)) using SizeTransform(clip = false)
                        },
                        label = "VoiceAgentContent",
                        contentAlignment = Alignment.CenterStart
                    ) { targetStatus ->
                        val targetTitle = when (targetStatus) {
                            VoiceAgentStatus.LISTENING -> "Listening..."
                            VoiceAgentStatus.THINKING -> "Thinking..."
                            VoiceAgentStatus.ANSWERING -> "Aira"
                            else -> ""
                        }

                        Column {
                            Text(
                                text = targetTitle,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MainFontBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )

                            if (text.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = MarkdownUtils.parseMarkdown(text),
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
}