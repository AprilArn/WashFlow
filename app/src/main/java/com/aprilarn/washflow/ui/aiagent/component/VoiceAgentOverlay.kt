package com.aprilarn.washflow.ui.aiagent.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.aiagent.ChatMessage
import com.aprilarn.washflow.ui.aiagent.VoiceAgentStatus
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.utils.MarkdownUtils
import kotlin.math.abs

@Composable
fun VoiceAgentOverlay(
    status: VoiceAgentStatus,
    modifier: Modifier = Modifier,
    text: String = "",
    lastMessage: ChatMessage? = null,
    customers: List<Customers> = emptyList(),
    items: List<Items> = emptyList(),
    services: List<Services> = emptyList(),
    onConfirmAction: (AiAgentAction?) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onDismissListening: () -> Unit = {},
    onDismissThinking: () -> Unit = {},
    onDismissAnswering: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var userHasInterrupted by remember { mutableStateOf(false) }
    val isDraggingScroll by scrollState.interactionSource.collectIsDraggedAsState()

    // Swipe state
    val haptic = LocalHapticFeedback.current
    var rawOffsetX by remember { mutableFloatStateOf(0f) }
    var isDraggingSwipe by remember { mutableStateOf(false) }
    var dismissedBySwipe by remember { mutableStateOf(false) }
    
    val dismissThreshold = 300f
    val resistanceThreshold = 350f
    val maxDragLimit = 500f

    // Falling animation states
    var isFalling by remember { mutableStateOf(false) }
    var fallDirection by remember { mutableFloatStateOf(0f) } // -1f for left, 1f for right

    // Dynamic screen height for better dismissal
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val safeFallingY = screenHeightPx + 500f
    
    // Constant velocity: 2000px / 800ms = 2.5px/ms
    val fallingDuration = remember(safeFallingY) { (safeFallingY / 2.5f).toInt() }
    val rotationDuration = remember(fallingDuration) { (fallingDuration * 1.25f).toInt() }

    val fallingX by animateFloatAsState(
        targetValue = if (isFalling || (dismissedBySwipe && status == VoiceAgentStatus.IDLE)) 1000f * fallDirection else 0f,
        animationSpec = if (isFalling) tween(durationMillis = fallingDuration, easing = FastOutLinearInEasing) else snap(),
        label = "fallingX"
    )

    val fallingY by animateFloatAsState(
        targetValue = if (isFalling || (dismissedBySwipe && status == VoiceAgentStatus.IDLE)) safeFallingY else 0f,
        animationSpec = if (isFalling) tween(durationMillis = fallingDuration, easing = FastOutLinearInEasing) else snap(),
        label = "fallingY"
    )

    // Efek miring saat digeser dan jatuh (hanya mulai miring saat mencapai dismissThreshold)
    val overlayRotationZ by animateFloatAsState(
        targetValue = when {
            isFalling -> 55f * fallDirection
            abs(rawOffsetX) > dismissThreshold -> (rawOffsetX / 20f).coerceIn(-15f, 15f)
            else -> 0f
        },
        animationSpec = if (isFalling) tween(rotationDuration) else spring(),
        label = "overlayRotationZ"
    )

    // Pivot logic: if swiping left, pivot on right; if swiping right, pivot on left
    val pivotX by remember {
        derivedStateOf {
            val offset = if (isFalling) fallDirection else rawOffsetX
            when {
                offset < 0 -> 1f
                offset > 0 -> 0f
                else -> 0.5f
            }
        }
    }

    // Trigger dismissal when fallingY reaches threshold
    if (fallingY > screenHeightPx) {
        SideEffect {
            isFalling = false
            // We don't reset fallDirection or dismissedBySwipe here to keep targets stable during fade
        }
    }
    
    // Reset offset and falling states when starting a new active state
    LaunchedEffect(status) {
        if (status != VoiceAgentStatus.IDLE) {
            rawOffsetX = 0f
            isFalling = false
            fallDirection = 0f
            dismissedBySwipe = false
            
            // Trigger vibration on mode change
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val offsetX by animateFloatAsState(
        targetValue = rawOffsetX,
        animationSpec = if (isDraggingSwipe) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "offsetX"
    )

    // Detect if we are at the bottom to resume auto-scroll (with a small threshold)
    val isAtBottom by remember {
        derivedStateOf {
            scrollState.value >= (scrollState.maxValue - 10).coerceAtLeast(0)
        }
    }

    LaunchedEffect(isDraggingScroll) {
        // Mark as interrupted only if dragging away from the bottom
        if (isDraggingScroll && !isAtBottom) userHasInterrupted = true
    }

    // Reset interruption when user stops dragging at the bottom or flings back to bottom
    LaunchedEffect(isAtBottom, isDraggingScroll) {
        if (isAtBottom && !isDraggingScroll) {
            userHasInterrupted = false
        }
    }

    // Auto-scroll logic triggered by content growth (maxValue) or text changes
    LaunchedEffect(scrollState.maxValue, text) {
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
        VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> Icons.Default.AutoAwesome
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

    val isEffectivelyVisible = status != VoiceAgentStatus.IDLE || isFalling

    AnimatedVisibility(
        visible = isEffectivelyVisible,
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
                (if (!dismissedBySwipe) {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                } else ExitTransition.None),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 58.dp, bottom = 48.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 400.dp) // Increased height to accommodate confirmation card
                .wrapContentHeight()
                .graphicsLayer {
                    translationX = offsetX + fallingX
                    translationY = fallingY
                    this.rotationZ = overlayRotationZ
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(pivotX, 0.5f)
                }
                .pointerInput(status) {
                    if (status == VoiceAgentStatus.IDLE || isFalling) return@pointerInput
                    
                    detectHorizontalDragGestures(
                        onDragStart = { isDraggingSwipe = true },
                        onDragCancel = {
                            isDraggingSwipe = false
                            rawOffsetX = 0f
                        },
                        onDragEnd = {
                            isDraggingSwipe = false
                            if (abs(rawOffsetX) > dismissThreshold) {
                                // Trigger logic immediately
                                when (status) {
                                    VoiceAgentStatus.LISTENING -> onDismissListening()
                                    VoiceAgentStatus.THINKING -> onDismissThinking()
                                    VoiceAgentStatus.ANSWERING, VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> onDismissAnswering()
                                    else -> {}
                                }
                                
                                // Start visual falling animation
                                dismissedBySwipe = true
                                isFalling = true
                                fallDirection = if (rawOffsetX > 0) 1f else -1f
                            } else {
                                rawOffsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val previousOffset = rawOffsetX
                            
                            // Hitung dragAmount dengan resistensi jika sudah melewati ambang batas
                            val effectiveDrag = if (abs(rawOffsetX) > resistanceThreshold && 
                                ((rawOffsetX > 0 && dragAmount > 0) || (rawOffsetX < 0 && dragAmount < 0))) {
                                dragAmount * 0.4f // Beri beban lebih berat
                            } else {
                                dragAmount
                            }

                            // Batasi agar tidak terlalu jauh
                            rawOffsetX = (rawOffsetX + effectiveDrag).coerceIn(-maxDragLimit, maxDragLimit)
                            
                            // Haptic feedback when crossing threshold
                            val crossedThreshold = abs(previousOffset) < dismissThreshold && abs(rawOffsetX) >= dismissThreshold
                            val backThreshold = abs(previousOffset) >= dismissThreshold && abs(rawOffsetX) < dismissThreshold

                            if (crossedThreshold || backThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                }
                .clip(RoundedCornerShape(16.dp))
                .animateContentSize(
                    animationSpec = if (isFalling) snap() else spring(
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
                        .verticalScroll(scrollState, enabled = !isDraggingSwipe) // Enable vertical scroll
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
                            VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> "Aira"
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

                            // Show ActionConfirmationCard if needed
                            if (status == VoiceAgentStatus.WAITING_FOR_CONFIRMATION && 
                                lastMessage?.action != null && 
                                !lastMessage.actionExecuted && 
                                !lastMessage.actionCancelled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ActionConfirmationCard(
                                    action = lastMessage.action,
                                    customers = customers,
                                    items = items,
                                    services = services,
                                    onConfirm = onConfirmAction,
                                    onCancel = onCancelAction
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}