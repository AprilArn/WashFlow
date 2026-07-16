package com.aprilarn.washflow.ui.aiagent

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.TextFieldValue
import com.aprilarn.washflow.ui.aiagent.component.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AiAgentPanel(
    expanded: Boolean,
    userName: String,
    profilePictureUrl: String?,
    inputMessage: TextFieldValue,
    messages: List<ChatMessage>,
    isAiThinking: Boolean,
    isTypewriterActive: Boolean = false,
    currentModelName: String? = null,
    modelStatus: AiModelStatus = AiModelStatus.IDLE,
    customers: List<Customers> = emptyList(),
    items: List<Items> = emptyList(),
    services: List<Services> = emptyList(),
    animatedMessageIds: Set<String> = emptySet(),
    wasMessageAnimated: (String) -> Boolean,
    onMessageAnimated: (String) -> Unit,
    getAnimationProgress: (String) -> Int, // New callback
    onInputChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    onClearHistory: () -> Unit,
    onConfirmAction: (String, AiAgentAction?) -> Unit,
    onCancelAction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // --- Auto-scroll Logic ---
    var userHasInterrupted by remember { mutableStateOf(false) }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf true
            val lastVisibleItem = visibleItems.last()
            lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                    (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset + 5
        }
    }

    // --- Dynamic Header/Footer Offsets ---
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var footerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var footerOffsetPx by remember { mutableFloatStateOf(0f) }

    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    // Auto-reveal Header at top or Footer at bottom
    LaunchedEffect(isAtTop) {
        if (isAtTop && headerOffsetPx < 0f) {
            androidx.compose.animation.core.Animatable(headerOffsetPx).animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) { headerOffsetPx = value }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && footerOffsetPx > 0f) {
            androidx.compose.animation.core.Animatable(footerOffsetPx).animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) {
                footerOffsetPx = value
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
                
                if (source == NestedScrollSource.UserInput && kotlin.math.abs(delta) > 0.5f) {
                    if (!isAtBottom || delta > 0) userHasInterrupted = true
                }

                val isScrollable = listState.canScrollForward || listState.canScrollBackward
                if (!isScrollable) {
                    footerOffsetPx = 0f
                    headerOffsetPx = 0f
                    return Offset.Zero
                }

                if (!listState.canScrollForward && delta < 0) {
                    // Force show footer at very bottom
                    val newFooterOffset = footerOffsetPx + delta
                    footerOffsetPx = newFooterOffset.coerceIn(0f, footerHeightPx)
                    if (footerHeightPx > 0) {
                        val footerProgress = footerOffsetPx / footerHeightPx
                        headerOffsetPx = footerProgress * -headerHeightPx
                    }
                } else if (!listState.canScrollBackward && delta > 0) {
                    // Force show header at very top
                    val newHeaderOffset = headerOffsetPx + delta
                    headerOffsetPx = newHeaderOffset.coerceIn(-headerHeightPx, 0f)
                } else {
                    val syncedHeaderOffset = if (footerHeightPx > 0) (footerOffsetPx / footerHeightPx) * -headerHeightPx else 0f
                    val isOutOfSync = headerOffsetPx > syncedHeaderOffset + 1f

                    if ((isOutOfSync && delta < 0) || (isAtTop && delta > 0)) {
                        // Keep header visible if pinned or at top
                        val newFooterOffset = footerOffsetPx + delta
                        footerOffsetPx = newFooterOffset.coerceIn(0f, footerHeightPx)
                        headerOffsetPx = 0f
                    } else {
                        // Standard synced behavior
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

                if (footerOffsetPx > 0f && footerOffsetPx < footerHeightPx) {
                    val targetFooterOffset = if (footerOffsetPx > footerHeightPx / 2) footerHeightPx else 0f
                    androidx.compose.animation.core.Animatable(footerOffsetPx).animateTo(
                        targetValue = targetFooterOffset,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) {
                        footerOffsetPx = value
                        val syncedHeaderOffset = if (footerHeightPx > 0) (footerOffsetPx / footerHeightPx) * -headerHeightPx else 0f
                        val wasOutOfSync = headerOffsetPx > syncedHeaderOffset + 1f

                        if (footerHeightPx > 0) {
                            if (isAtTop) {
                                headerOffsetPx = 0f
                            } else if (wasOutOfSync && targetFooterOffset == 0f) {
                                headerOffsetPx = 0f
                            } else {
                                headerOffsetPx = syncedHeaderOffset
                            }
                        }
                    }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

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

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) showMenu = false
    }

    var processingWithGracePeriod by remember { mutableStateOf(false) }
    LaunchedEffect(isAiThinking, isTypewriterActive) {
        if (isAiThinking || isTypewriterActive) {
            processingWithGracePeriod = true
        } else {
            delay(250L)
            processingWithGracePeriod = false
        }
    }

    val isDragging by listState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isAtBottom, isDragging) {
        if (isAtBottom && !isDragging) userHasInterrupted = false
    }

    LaunchedEffect(isDragging) {
        if (isDragging && !isAtBottom) userHasInterrupted = true
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1, 100000)
            userHasInterrupted = false
        }
    }

    // --- UI Layout ---
    Box(modifier = Modifier.fillMaxSize()) {
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
                                    val alreadyAnimated =
                                        remember(message.id) { wasMessageAnimated(message.id) }
                                    var animProgress by remember(message.id) {
                                        mutableFloatStateOf(
                                            if (alreadyAnimated) 1f else 0f
                                        )
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
                                                }
                                            }
                                    ) {
                                        ChatMessageItem(
                                            message = message,
                                            profilePictureUrl = profilePictureUrl,
                                            isAlreadyAnimated = alreadyAnimated,
                                            progress = getAnimationProgress(message.id),
                                            customers = customers,
                                            items = items,
                                            services = services,
                                            onConfirmAction = { updatedAction ->
                                                onConfirmAction(message.id, updatedAction)
                                                if (message.action is AiAgentAction.Navigate) onDismiss()
                                            },
                                            onCancelAction = { onCancelAction(message.id) },
                                            onTextUpdate = {
                                                if (!userHasInterrupted) {
                                                    coroutineScope.launch {
                                                        listState.scrollToItem(
                                                            messages.size - 1,
                                                            100000
                                                        )
                                                    }
                                                    headerOffsetPx = 0f
                                                    footerOffsetPx = 0f
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(if (message.isUser) 12.dp else 32.dp))
                                    }
                                }
                            }
                        }

                        // Header Overlay
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

                        // Input Overlay
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

                        // Scroll-to-bottom FAB
                        AnimatedVisibility(
                            visible = !isAtBottom && messages.isNotEmpty(),
                            enter = fadeIn(animationSpec = tween(400)) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            exit = fadeOut(animationSpec = tween(300)) + scaleOut(
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
                                            launch {
                                                androidx.compose.animation.core.Animatable(
                                                    footerOffsetPx
                                                ).animateTo(0f) {
                                                    footerOffsetPx = value
                                                    if (footerHeightPx > 0) headerOffsetPx =
                                                        (footerOffsetPx / footerHeightPx) * -headerHeightPx
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
}
