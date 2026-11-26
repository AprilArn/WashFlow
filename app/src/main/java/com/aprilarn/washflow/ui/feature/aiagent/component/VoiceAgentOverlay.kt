package com.aprilarn.washflow.ui.feature.aiagent.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.feature.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.feature.aiagent.ChatMessage
import com.aprilarn.washflow.ui.feature.aiagent.VoiceAgentStatus
import com.aprilarn.washflow.utils.MarkdownUtils
import kotlinx.coroutines.delay
import kotlin.math.abs

private fun resolveVoiceStatusAccent(status: VoiceAgentStatus): Pair<Color, Color> {
    return when (status) {
        VoiceAgentStatus.LISTENING -> PastelRoseBg to PastelRose
        VoiceAgentStatus.THINKING -> PastelAmberBg to PastelAmber
        VoiceAgentStatus.ANSWERING,
        VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> PastelBlueSoftBg to PastelBlue
        VoiceAgentStatus.IDLE -> PastelBlueSoftBg to PastelBlue
    }
}

@Composable
fun VoiceAgentOverlay(
    status: VoiceAgentStatus,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    isVisibleInitial: Boolean = false,
    text: String = "",
    lastMessage: ChatMessage? = null,
    isAlreadyAnimated: Boolean = false,
    customers: List<Customers> = emptyList(),
    items: List<Items> = emptyList(),
    services: List<Services> = emptyList(),
    onConfirmAction: (AiAgentAction?) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onInteractionChange: (Boolean) -> Unit = {},
    onDismissListening: () -> Unit = {},
    onDismissThinking: () -> Unit = {},
    onDismissAnswering: () -> Unit = {}
) {
    // Session management (OUTSIDE KEY)
    var sessionKey by remember { mutableIntStateOf(0) }
    val isStatusActive = status != VoiceAgentStatus.IDLE
    var wasActive by remember { mutableStateOf(false) }

    // Increment sessionKey immediately during composition when transitioning from IDLE to ACTIVE
    if (isStatusActive && !wasActive) {
        sessionKey++
    }
    SideEffect {
        wasActive = isStatusActive
    }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize

    // Trigger vibration on mode change (Session-independent)
    LaunchedEffect(status) {
        if (status != VoiceAgentStatus.IDLE) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun dispatchDismissForCurrentStatus() {
        when (status) {
            VoiceAgentStatus.LISTENING -> onDismissListening()
            VoiceAgentStatus.THINKING -> onDismissThinking()
            VoiceAgentStatus.ANSWERING, VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> onDismissAnswering()
            VoiceAgentStatus.IDLE -> Unit
        }
    }

    key(sessionKey) {
        // Session-specific states (INSIDE KEY) - everything here resets on new session
        val scrollState = rememberScrollState()
        var userHasInterrupted by remember { mutableStateOf(false) }
        val isDraggingScroll by scrollState.interactionSource.collectIsDraggedAsState()

        var rawOffsetX by remember { mutableFloatStateOf(0f) }
        var isDraggingSwipe by remember { mutableStateOf(false) }
        var dismissedBySwipe by remember { mutableStateOf(false) }
        var isFalling by remember { mutableStateOf(false) }
        var fallDirection by remember { mutableFloatStateOf(-1f) }
        var lastDragDirection by remember { mutableFloatStateOf(-1f) }

        if (status != VoiceAgentStatus.IDLE && !isFalling) {
            BackHandler {
                dispatchDismissForCurrentStatus()
            }
        }

        // Freeze states for falling animation consistency
        var lastActiveStatus by remember { mutableStateOf(status) }
        var lastActiveText by remember { mutableStateOf(text) }

        LaunchedEffect(status, text) {
            if (status != VoiceAgentStatus.IDLE) {
                lastActiveStatus = status
                lastActiveText = text
            }
        }

        val displayStatus = if (isFalling) lastActiveStatus else status
        val displayText = if (isFalling) lastActiveText else text

        val screenWidthPx = windowSize.width.toFloat().coerceAtLeast(1f)
        val screenHeightPx = windowSize.height.toFloat().coerceAtLeast(1f)

        val dismissThreshold = if (isCompact) with(density) { 105.dp.toPx() } else 300f
        val resistanceThreshold = if (isCompact) with(density) { 125.dp.toPx() } else 350f
        val maxDragLimit = if (isCompact) {
            with(density) { 180.dp.toPx() }
                .coerceAtMost(screenWidthPx * 0.55f)
                .coerceAtLeast(resistanceThreshold + with(density) { 20.dp.toPx() })
        } else {
            500f
        }

        // Dynamic screen height for better dismissal
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

        val offsetX by animateFloatAsState(
            targetValue = rawOffsetX,
            animationSpec = if (isDraggingSwipe || isFalling) snap() else spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "offsetX"
        )

        val isReadyToDismiss = isFalling || abs(offsetX) >= dismissThreshold

        val overlayRotationZ by animateFloatAsState(
            targetValue = when {
                isFalling -> 55f * fallDirection
                abs(offsetX) >= dismissThreshold ->
                    (offsetX / (dismissThreshold / 15f)).coerceIn(-15f, 15f)
                else -> 0f
            },
            animationSpec = if (isFalling) tween(rotationDuration) else spring(),
            label = "overlayRotationZ"
        )

        val activeDirection = if (isFalling) fallDirection else lastDragDirection
        val pivotX = if (activeDirection > 0f) 0f else 1f

        val hasFallenOut = isFalling && fallingY > screenHeightPx
        LaunchedEffect(hasFallenOut) {
            if (hasFallenOut) {
                isFalling = false
            }
        }

        // Scroll and interruption logic (INSIDE KEY)
        val isAtBottom by remember {
            derivedStateOf {
                scrollState.value >= (scrollState.maxValue - 10).coerceAtLeast(0)
            }
        }

        LaunchedEffect(isDraggingScroll) {
            if (isDraggingScroll && !isAtBottom) userHasInterrupted = true
        }

        LaunchedEffect(isAtBottom, isDraggingScroll) {
            if (isAtBottom && !isDraggingScroll) {
                userHasInterrupted = false
            }
        }

        LaunchedEffect(scrollState.maxValue, displayText) {
            if (!userHasInterrupted && status == VoiceAgentStatus.ANSWERING) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        LaunchedEffect(status) {
            if (status == VoiceAgentStatus.LISTENING || status == VoiceAgentStatus.IDLE) {
                scrollState.scrollTo(0)
                userHasInterrupted = false
            }
        }

        val icon = when (displayStatus) {
            VoiceAgentStatus.LISTENING -> Icons.Rounded.Mic
            VoiceAgentStatus.THINKING -> Icons.Rounded.Sync
            VoiceAgentStatus.ANSWERING,
            VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> Icons.Rounded.AutoAwesome
            VoiceAgentStatus.IDLE -> Icons.Rounded.AutoAwesome
        }

        val infiniteTransition = rememberInfiniteTransition(label = "VoiceOverlayAnimations")
        val syncRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "syncRotation"
        )
        val micPulseScale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.24f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "micPulseScale"
        )
        val micPulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.42f,
            targetValue = 0.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "micPulseAlpha"
        )
        val activitySweep by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1350, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "activitySweep"
        )

        val (accentBg, accentTint) = remember(displayStatus) {
            resolveVoiceStatusAccent(displayStatus)
        }
        val animatedAccentBg by animateColorAsState(
            targetValue = accentBg,
            animationSpec = tween(durationMillis = 220),
            label = "voiceAccentBg"
        )
        val animatedAccentTint by animateColorAsState(
            targetValue = accentTint,
            animationSpec = tween(durationMillis = 220),
            label = "voiceAccentTint"
        )
        val borderColor by animateColorAsState(
            targetValue = if (isReadyToDismiss) {
                PastelRose.copy(alpha = 0.65f)
            } else {
                accentTint.copy(alpha = 0.24f)
            },
            animationSpec = tween(durationMillis = 180),
            label = "voiceBorderColor"
        )

        val isEffectivelyVisible = status != VoiceAgentStatus.IDLE || isFalling
        val visibleState = remember { MutableTransitionState(isVisibleInitial) }
        visibleState.targetState = isEffectivelyVisible

        val cardShape = RoundedCornerShape(20.dp)
        val hasConfirmationCard = status == VoiceAgentStatus.WAITING_FOR_CONFIRMATION &&
            lastMessage?.action != null &&
            !lastMessage.actionExecuted &&
            !lastMessage.actionCancelled
        val showActivityBar = displayStatus == VoiceAgentStatus.LISTENING ||
            displayStatus == VoiceAgentStatus.THINKING

        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(320)) +
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
            exit = fadeOut(animationSpec = tween(300)) +
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
                } else {
                    ExitTransition.None
                }),
            modifier = modifier.graphicsLayer {
                translationX = offsetX + fallingX
                translationY = fallingY
                this.rotationZ = overlayRotationZ
                transformOrigin = TransformOrigin(pivotX, 0.5f)
            }
        ) {
            val containerModifier = if (isCompact) {
                Modifier
                    .wrapContentWidth()
                    .heightIn(max = 460.dp)
                    .wrapContentHeight()
            } else {
                Modifier
                    .padding(top = 58.dp, bottom = 48.dp)
                    .widthIn(max = 520.dp)
                    .heightIn(max = 460.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
            }

            Surface(
                modifier = containerModifier
                    // Detect raw press/release to pause/reset timer
                    .pointerInput(status, isFalling) {
                        if (status == VoiceAgentStatus.IDLE || isFalling) return@pointerInput
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Press) {
                                    onInteractionChange(true)
                                } else if (event.type == PointerEventType.Release) {
                                    onInteractionChange(false)
                                }
                            }
                        }
                    }
                    .pointerInput(status, isCompact, isFalling) {
                        if (status == VoiceAgentStatus.IDLE || isFalling) return@pointerInput

                        detectHorizontalDragGestures(
                            onDragStart = { isDraggingSwipe = true },
                            onDragCancel = {
                                isDraggingSwipe = false
                                rawOffsetX = 0f
                            },
                            onDragEnd = {
                                isDraggingSwipe = false
                                if (abs(rawOffsetX) >= dismissThreshold) {
                                    dispatchDismissForCurrentStatus()
                                    dismissedBySwipe = true
                                    fallDirection = if (rawOffsetX > 0f) 1f else -1f
                                    isFalling = true
                                } else {
                                    rawOffsetX = 0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val previousOffset = rawOffsetX
                                val isPullingOutward = (rawOffsetX > 0f && dragAmount > 0f) ||
                                    (rawOffsetX < 0f && dragAmount < 0f)
                                val effectiveDrag = if (abs(rawOffsetX) > resistanceThreshold && isPullingOutward) {
                                    dragAmount * 0.4f
                                } else {
                                    dragAmount
                                }
                                val newOffset = (rawOffsetX + effectiveDrag).coerceIn(-maxDragLimit, maxDragLimit)

                                if (newOffset != rawOffsetX) {
                                    rawOffsetX = newOffset
                                    if (newOffset != 0f) {
                                        lastDragDirection = if (newOffset > 0f) 1f else -1f
                                    }

                                    val crossedThreshold = abs(previousOffset) < dismissThreshold && abs(rawOffsetX) >= dismissThreshold
                                    val backThreshold = abs(previousOffset) >= dismissThreshold && abs(rawOffsetX) < dismissThreshold
                                    if (crossedThreshold || backThreshold) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                        )
                    }
                    .shadow(
                        elevation = 12.dp,
                        shape = cardShape,
                        ambientColor = GrayBlue.copy(alpha = 0.28f),
                        spotColor = GrayBlue.copy(alpha = 0.34f)
                    )
                    .clip(cardShape)
                    .animateContentSize(
                        animationSpec = if (isFalling) snap() else spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        alignment = Alignment.Center
                    ),
                shape = cardShape,
                color = Color.White,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = 0.dp
            ) {
                val iconBoxSize = if (isCompact) 38.dp else 40.dp
                val iconBoxShape = RoundedCornerShape(12.dp)

                // Draw live activity bar purely in draw phase so it never stretches wrapContentWidth()
                Box(
                    modifier = Modifier.drawWithContent {
                        drawContent()
                        if (showActivityBar) {
                            val horizontalInset = 14.dp.toPx()
                            val trackWidth = (size.width - horizontalInset * 2).coerceAtLeast(0f)
                            val barHeight = 2.5.dp.toPx()
                            val barY = size.height - barHeight - 4.dp.toPx()
                            val radius = CornerRadius(barHeight / 2f, barHeight / 2f)

                            if (trackWidth > 0f) {
                                drawRoundRect(
                                    color = animatedAccentBg,
                                    topLeft = Offset(horizontalInset, barY),
                                    size = Size(trackWidth, barHeight),
                                    cornerRadius = radius
                                )
                                val segmentWidth = (trackWidth * 0.36f).coerceAtLeast(24.dp.toPx().coerceAtMost(trackWidth))
                                val travel = (trackWidth - segmentWidth).coerceAtLeast(0f)
                                val segmentX = horizontalInset + travel * activitySweep
                                drawRoundRect(
                                    color = animatedAccentTint.copy(alpha = 0.75f),
                                    topLeft = Offset(segmentX, barY),
                                    size = Size(segmentWidth, barHeight),
                                    cornerRadius = radius
                                )
                            }
                        }
                    }
                ) {
                    if (hasConfirmationCard) {
                        // Full-width layout specifically when confirmation form is displayed so fields aren't squeezed
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = if (isCompact) 12.dp else 14.dp,
                                        end = if (isCompact) 12.dp else 14.dp,
                                        top = 11.dp,
                                        bottom = 10.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                VoiceStatusIconBadge(
                                    icon = icon,
                                    displayStatus = displayStatus,
                                    accentBg = animatedAccentBg,
                                    accentTint = animatedAccentTint,
                                    iconBoxSize = iconBoxSize,
                                    iconBoxShape = iconBoxShape,
                                    isCompact = isCompact,
                                    syncRotation = syncRotation,
                                    micPulseScale = micPulseScale,
                                    micPulseAlpha = micPulseAlpha
                                )

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(animatedAccentTint)
                                    )
                                    Text(
                                        text = stringResource(R.string.ai_name),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MainFontBlack,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = animatedAccentBg,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = if (isCompact) 12.dp else 14.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(scrollState, enabled = !isDraggingSwipe)
                                    .padding(
                                        horizontal = if (isCompact) 14.dp else 16.dp,
                                        vertical = 12.dp
                                    )
                            ) {
                                if (displayText.isNotBlank()) {
                                    Text(
                                        text = MarkdownUtils.parseMarkdown(displayText),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp,
                                            lineHeight = 19.sp
                                        ),
                                        color = Color(0xFF475569)
                                    )
                                }

                                var showCard by remember(lastMessage.id, isAlreadyAnimated) {
                                    mutableStateOf(isAlreadyAnimated)
                                }

                                LaunchedEffect(isAlreadyAnimated) {
                                    if (!isAlreadyAnimated) {
                                        delay(300L)
                                    }
                                    showCard = true
                                }

                                AnimatedVisibility(
                                    visible = showCard,
                                    enter = if (isAlreadyAnimated) EnterTransition.None else fadeIn() + expandVertically(),
                                    exit = fadeOut()
                                ) {
                                    Column {
                                        if (displayText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
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
                    } else {
                        // Dynamic wrap-width layout: starts compact pill, expands smoothly horizontally as text grows
                        val hasText = displayText.isNotBlank()
                        Row(
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(
                                    start = if (isCompact) 12.dp else 14.dp,
                                    end = if (isCompact) 16.dp else 18.dp,
                                    top = if (isCompact) 11.dp else 12.dp,
                                    bottom = when {
                                        showActivityBar -> 14.dp
                                        isCompact -> 11.dp
                                        else -> 12.dp
                                    }
                                ),
                            verticalAlignment = if (hasText) Alignment.Top else Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            VoiceStatusIconBadge(
                                icon = icon,
                                displayStatus = displayStatus,
                                accentBg = animatedAccentBg,
                                accentTint = animatedAccentTint,
                                iconBoxSize = iconBoxSize,
                                iconBoxShape = iconBoxShape,
                                isCompact = isCompact,
                                syncRotation = syncRotation,
                                micPulseScale = micPulseScale,
                                micPulseAlpha = micPulseAlpha
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .verticalScroll(scrollState, enabled = !isDraggingSwipe),
                                verticalArrangement = Arrangement.Center
                            ) {
                                AnimatedContent(
                                    targetState = displayStatus,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(280)) togetherWith
                                            fadeOut(animationSpec = tween(220)) using SizeTransform(clip = false)
                                    },
                                    label = "VoiceAgentContent",
                                    contentAlignment = Alignment.CenterStart
                                ) { targetStatus ->
                                    val targetTitle = when (targetStatus) {
                                        VoiceAgentStatus.LISTENING -> stringResource(R.string.ai_listening)
                                        VoiceAgentStatus.THINKING -> stringResource(R.string.ai_thinking)
                                        VoiceAgentStatus.ANSWERING,
                                        VoiceAgentStatus.WAITING_FOR_CONFIRMATION -> stringResource(R.string.ai_name)
                                        VoiceAgentStatus.IDLE -> ""
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(animatedAccentTint)
                                            )
                                            Text(
                                                text = targetTitle,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    color = MainFontBlack,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (displayText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = MarkdownUtils.parseMarkdown(displayText),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.5.sp
                                                ),
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceStatusIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    displayStatus: VoiceAgentStatus,
    accentBg: Color,
    accentTint: Color,
    iconBoxSize: androidx.compose.ui.unit.Dp,
    iconBoxShape: RoundedCornerShape,
    isCompact: Boolean,
    syncRotation: Float,
    micPulseScale: Float,
    micPulseAlpha: Float
) {
    Box(
        modifier = Modifier.size(iconBoxSize),
        contentAlignment = Alignment.Center
    ) {
        if (displayStatus == VoiceAgentStatus.LISTENING) {
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .graphicsLayer {
                        scaleX = micPulseScale
                        scaleY = micPulseScale
                        alpha = micPulseAlpha
                    }
                    .clip(iconBoxShape)
                    .background(PastelRose)
            )
        }

        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .clip(iconBoxShape)
                .background(accentBg)
                .border(1.dp, accentTint.copy(alpha = 0.16f), iconBoxShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = icon to (displayStatus == VoiceAgentStatus.THINKING),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(240)) + scaleIn(initialScale = 0.8f)) togetherWith
                        (fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f)) using SizeTransform(clip = false)
                },
                label = "VoiceAgentIcon"
            ) { (targetIcon, isProcessing) ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = null,
                    tint = accentTint,
                    modifier = Modifier
                        .size(if (isCompact) 20.dp else 21.dp)
                        .then(
                            if (isProcessing) {
                                Modifier.graphicsLayer { rotationZ = syncRotation }
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}
