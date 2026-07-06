package com.aprilarn.washflow.ui.aiagent

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.ui.theme.SkyBlue
import com.aprilarn.washflow.ui.theme.SoftBlack
import com.aprilarn.washflow.utils.MarkdownUtils
import com.aprilarn.washflow.data.model.Customers
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun AiAgentPanel(
    expanded: Boolean,
    userName: String,
    profilePictureUrl: String?,
    inputMessage: TextFieldValue,
    messages: List<ChatMessage>,
    isAiThinking: Boolean,
    currentModelName: String? = null,
    modelStatus: AiModelStatus = AiModelStatus.IDLE,
    customers: List<Customers> = emptyList(),
    wasMessageAnimated: (String) -> Boolean,
    onMessageAnimated: (String) -> Unit,
    onInputChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    onClearHistory: () -> Unit,
    onConfirmAction: (String, AiAgentAction?) -> Unit,
    onCancelAction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var animatingMessageIds by remember { mutableStateOf(setOf<String>()) }
    val isAnyMessageAnimating by remember {
        derivedStateOf { animatingMessageIds.isNotEmpty() }
    }

    // ── Auto-scroll State ──────────────────────────────────────────────────────
    var userHasInterrupted by remember { mutableStateOf(false) }

    // Detect if user is at the bottom to reset interruption
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf true
            val lastVisibleItem = visibleItems.last()
            // Check if last item is at the end AND its bottom is visible/beyond viewport end
            lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                    (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset + 5
        }
    }

    // ── Collapsible Header/Footer State ────────────────────────────────────────
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var footerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var footerOffsetPx by remember { mutableFloatStateOf(0f) }

    // Detect if user is at the top
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    // Auto-reveal Header when reaching the top or Footer when reaching the bottom
    LaunchedEffect(isAtTop) {
        if (isAtTop && headerOffsetPx < 0f) {
            androidx.compose.animation.core.Animatable(headerOffsetPx).animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) {
                headerOffsetPx = value
            }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && footerOffsetPx > 0f) {
            androidx.compose.animation.core.Animatable(footerOffsetPx).animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) {
                footerOffsetPx = value
                
                // Keep header synced if not at top
                if (!isAtTop && footerHeightPx > 0) {
                    headerOffsetPx = (footerOffsetPx / footerHeightPx) * -headerHeightPx
                }
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                
                // Detection of user manual scroll to interrupt auto-scroll
                if (source == NestedScrollSource.UserInput && kotlin.math.abs(delta) > 0.5f) {
                    if (!isAtBottom || delta > 0) { // delta > 0 means scrolling UP
                        userHasInterrupted = true
                    }
                }

                // Only allow hiding if content is scrollable
                val isScrollable = listState.canScrollForward || listState.canScrollBackward
                if (!isScrollable) {
                    // Force visible if not scrollable
                    footerOffsetPx = 0f
                    headerOffsetPx = 0f
                    return Offset.Zero
                }

                // If user is at the bottom and tries to scroll DOWN (content moves UP, delta < 0), force SHOW footer
                if (!listState.canScrollForward && delta < 0) {
                    val newFooterOffset = footerOffsetPx + delta
                    footerOffsetPx = newFooterOffset.coerceIn(0f, footerHeightPx)
                    
                    // Sync Header with Footer (Standard Sync)
                    if (footerHeightPx > 0) {
                        val footerProgress = footerOffsetPx / footerHeightPx
                        headerOffsetPx = footerProgress * -headerHeightPx
                    }
                } 
                // If user is at the top and tries to scroll up (delta > 0), force show ONLY HEADER
                else if (!listState.canScrollBackward && delta > 0) {
                    val newHeaderOffset = headerOffsetPx + delta
                    headerOffsetPx = newHeaderOffset.coerceIn(-headerHeightPx, 0f)

                    // Removed: footerOffsetPx = footerHeightPx
                    // This allows the footer to stay at whatever position it was
                    // (likely 0f if the user was scrolling up bit by bit)
                }
                else {
                    // Standard behavior:
                    // delta > 0 (scroll up/to top) -> hide
                    // delta < 0 (scroll down/to bottom) -> show
                    
                    // Check if we are "out of sync" (Header is more visible than what Footer sync would suggest)
                    // Expected header offset if synced: (footerOffsetPx / footerHeightPx) * -headerHeightPx
                    val syncedHeaderOffset = if (footerHeightPx > 0) (footerOffsetPx / footerHeightPx) * -headerHeightPx else 0f
                    val isOutOfSync = headerOffsetPx > syncedHeaderOffset + 1f // Add small epsilon

                    if (isOutOfSync) {
                        // In Out of Sync mode (e.g. Header pinned at Top),
                        // scrolling affects Footer while Header stays locked at 0f.
                        val newFooterOffset = footerOffsetPx + delta
                        footerOffsetPx = newFooterOffset.coerceIn(0f, footerHeightPx)
                        headerOffsetPx = 0f
                    } else {
                        // Standard Sync
                        val newFooterOffset = footerOffsetPx + delta
                        footerOffsetPx = newFooterOffset.coerceIn(0f, footerHeightPx)

                        if (footerHeightPx > 0) {
                            val footerProgress = footerOffsetPx / footerHeightPx
                            headerOffsetPx = footerProgress * -headerHeightPx
                        }
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val isScrollable = listState.canScrollForward || listState.canScrollBackward
                if (!isScrollable) return super.onPostFling(consumed, available)

                // Snap Footer
                if (footerOffsetPx > 0f && footerOffsetPx < footerHeightPx) {
                    val targetFooterOffset = if (footerOffsetPx > footerHeightPx / 2) footerHeightPx else 0f
                    
                    androidx.compose.animation.core.Animatable(footerOffsetPx).animateTo(
                        targetValue = targetFooterOffset,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) {
                        footerOffsetPx = value
                        
                        // Check if we were in "Out of Sync" mode (Header visible at Top)
                        val syncedHeaderOffset = if (footerHeightPx > 0) (footerOffsetPx / footerHeightPx) * -headerHeightPx else 0f
                        val wasOutOfSync = headerOffsetPx > syncedHeaderOffset + 1f

                        if (footerHeightPx > 0) {
                            if (wasOutOfSync) {
                                // If we are at the top (Out of Sync), always keep Header at 0f
                                headerOffsetPx = 0f
                            } else {
                                // Standard Sync
                                headerOffsetPx = syncedHeaderOffset
                            }
                        }
                    }
                }

                return super.onPostFling(consumed, available)
            }
        }
    }

    // Reset offsets when expanded or messages change
    LaunchedEffect(expanded) {
        if (expanded) {
            headerOffsetPx = 0f
            footerOffsetPx = 0f
        }
    }

    LaunchedEffect(messages.size) {
        headerOffsetPx = 0f
        footerOffsetPx = 0f
    }

    // Close menu when scrolling
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) showMenu = false
    }

    val isProcessing = remember(isAiThinking, isAnyMessageAnimating, messages, animatingMessageIds) {
        derivedStateOf {
            if (isAiThinking || isAnyMessageAnimating) return@derivedStateOf true

            // Check if the last AI message is still "new" and waiting to animate
            val lastAiMessage = messages.lastOrNull { !it.isUser }
            if (lastAiMessage != null && !lastAiMessage.isThinking && !wasMessageAnimated(lastAiMessage.id)) {
                // If it's not in the animating set yet, it's about to be
                return@derivedStateOf true
            }

            false
        }
    }

    // Add a grace period to the processing state to prevent flicker
    var processingWithGracePeriod by remember { mutableStateOf(false) }
    LaunchedEffect(isProcessing.value) {
        if (isProcessing.value) {
            processingWithGracePeriod = true
        } else {
            // Wait for a short duration before re-enabling the send button
            delay(250L)
            processingWithGracePeriod = false
        }
    }

    // Detect actual manual dragging to set interruption
    val isDragging by listState.interactionSource.collectIsDraggedAsState()

    // If user scrolls back to bottom and releases drag, we resume auto-scroll
    LaunchedEffect(isAtBottom, isDragging) {
        if (isAtBottom && !isDragging) userHasInterrupted = false
    }

    LaunchedEffect(isDragging) {
        if (isDragging && !isAtBottom) {
            userHasInterrupted = true
        }
    }

    // ── Auto-scroll ────────────────────────────────────────────────────────────
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // Scroll with a large offset to ensure bottom-alignment even for long messages
            listState.scrollToItem(messages.size - 1, 100000)
            userHasInterrupted = false
        }
    }

    // ── Dim overlay ────────────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() }
        )
    }

    // ── Panel ──────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                ) {
                    // ── Message list ───────────────────────────────────────────
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = with(density) { headerHeightPx.toDp() },
                            bottom = with(density) { footerHeightPx.toDp() },
                            start = 24.dp,
                            end = 24.dp
                        )
                    ) {
                        if (messages.isEmpty()) {
                            item { AiAgentEmptyState(userName = userName) }
                        } else {
                            items(
                                items = messages,
                                key = { it.id }
                            ) { message ->

                                val alreadyAnimated = remember(message.id) {
                                    wasMessageAnimated(message.id)
                                }
                                var animProgress by remember(message.id) {
                                    mutableFloatStateOf(if (alreadyAnimated) 1f else 0f)
                                }

                                val animatedAlpha by animateFloatAsState(
                                    targetValue = animProgress,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "msgAlpha"
                                )

                                val animatedOffset by animateFloatAsState(
                                    targetValue = animProgress,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "msgOffset"
                                )

                                LaunchedEffect(message.id) {
                                    if (!alreadyAnimated) {
                                        delay(50L)
                                        animProgress = 1f
                                        onMessageAnimated(message.id)
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = animatedAlpha.coerceIn(0f, 1f)

                                            if (message.isUser) {
                                                val scale = 0.8f + (animatedOffset * 0.2f)
                                                scaleX = scale
                                                scaleY = scale
                                                translationY = 0f
                                            } else {
                                                scaleX = 1f
                                                scaleY = 1f
                                                translationY = 0f
                                            }
                                        }
                                ) {
                                    ChatMessageItem(
                                        message = message,
                                        profilePictureUrl = profilePictureUrl,
                                        isAlreadyAnimated = alreadyAnimated,
                                        customers = customers,
                                        onConfirmAction = { updatedAction ->
                                            onConfirmAction(message.id, updatedAction)
                                            if (message.action is AiAgentAction.Navigate) {
                                                onDismiss()
                                            }
                                        },
                                        onCancelAction = { onCancelAction(message.id) },
                                        onTextUpdate = {
                                            if (!userHasInterrupted) {
                                                coroutineScope.launch {
                                                    // Scroll with large offset for bottom alignment
                                                    listState.scrollToItem(messages.size - 1, 100000)
                                                }
                                                // Ensure UI is visible during typewriter
                                                headerOffsetPx = 0f
                                                footerOffsetPx = 0f
                                            }
                                        },
                                        onAnimationStateChange = { animating ->
                                            animatingMessageIds = if (animating) {
                                                animatingMessageIds + message.id
                                            } else {
                                                animatingMessageIds - message.id
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(if (message.isUser) 12.dp else 32.dp))
                                }
                            }
                        }
                    }

                    // ── Header (Overlay) ───────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { headerHeightPx = it.size.height.toFloat() }
                            .graphicsLayer { translationY = headerOffsetPx }
                            .background(Color.White)
                            .align(Alignment.TopCenter)
                    ) {
                        AiAgentPanelHeader(onClearHistory = onClearHistory)
                    }

                    // ── Input area (Overlay) ───────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { footerHeightPx = it.size.height.toFloat() }
                            .graphicsLayer { translationY = footerOffsetPx }
                            .background(Color.White)
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        AiAgentPanelInputArea(
                            inputMessage = inputMessage,
                            onInputChange = onInputChange,
                            onSendMessage = onSendMessage,
                            modelStatus = modelStatus,
                            currentModelName = currentModelName,
                            isProcessing = processingWithGracePeriod
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "AI can make mistakes, so double-check it",
                                color = Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // ── Floating Action Buttons (Top Layer) ────────────────────
                    AnimatedVisibility(
                        visible = !isAtBottom && messages.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(400)) + 
                                scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                        exit = fadeOut(animationSpec = tween(300)) + 
                               scaleOut(
                                   targetScale = 0.8f,
                                   animationSpec = tween(400)
                               ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = with(density) { footerHeightPx.toDp() + 16.dp })
                            .graphicsLayer { translationY = footerOffsetPx }
                    ) {
                        AiAgentScrollToBottomButton(
                            onClick = {
                                coroutineScope.launch {
                                    val totalItems = listState.layoutInfo.totalItemsCount
                                    if (totalItems > 0) {
                                        userHasInterrupted = false
                                        
                                        // Animate offsets to 0 alongside the scroll
                                        launch {
                                            androidx.compose.animation.core.Animatable(footerOffsetPx).animateTo(0f) {
                                                footerOffsetPx = value
                                                if (footerHeightPx > 0) {
                                                    headerOffsetPx = (footerOffsetPx / footerHeightPx) * -headerHeightPx
                                                }
                                            }
                                        }
                                        
                                        listState.animateScrollToItem(totalItems - 1, 100000)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}



