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

    // ── Collapsible Header/Footer State ────────────────────────────────────────
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var footerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var footerOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // Footer (TextField): Scroll Up/To Top (delta > 0) hides
                // Footer (TextField): Scroll Down/To Bottom (delta < 0) shows
                val newFooterOffset = footerOffsetPx + delta
                footerOffsetPx = newFooterOffset.coerceIn(0f, footerHeightPx)

                // Header: Sync with Footer
                if (footerHeightPx > 0) {
                    val footerProgress = footerOffsetPx / footerHeightPx // 0f (visible) to 1f (hidden)
                    headerOffsetPx = footerProgress * -headerHeightPx
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Snap Footer
                if (footerOffsetPx > 0f && footerOffsetPx < footerHeightPx) {
                    val targetFooterOffset = if (footerOffsetPx > footerHeightPx / 2) footerHeightPx else 0f
                    
                    androidx.compose.animation.core.Animatable(footerOffsetPx).animateTo(
                        targetValue = targetFooterOffset,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) {
                        footerOffsetPx = value
                        
                        // Sync Header during animation
                        if (footerHeightPx > 0) {
                            val footerProgress = footerOffsetPx / footerHeightPx
                            headerOffsetPx = footerProgress * -headerHeightPx
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
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
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
                                }
                            }

                            item {
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
                            }

                            item {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    "Prompts to try",
                                    fontWeight = FontWeight.Bold,
                                    color = MainFontBlack,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            item { PromptItem("Extract all hardcoded strings from this class and move them into strings.xml") }
                            item { PromptItem("Add documentation to my current file") }
                            item { PromptItem("Update kotlin in @libs.version.toml to the latest version") }
                            item { PromptItem("Make my Theme's color scheme warmer") }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
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

                    androidx.compose.animation.AnimatedVisibility(
                        visible = listState.canScrollForward,
                        enter = scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(animationSpec = tween(300)),
                        exit = scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(200)
                        ) + fadeOut(animationSpec = tween(200)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .graphicsLayer {
                                // Move with footer
                                translationY = footerOffsetPx
                            }
                    ) {
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    val totalItems = listState.layoutInfo.totalItemsCount
                                    if (totalItems > 0) {
                                        // Ensure bottom alignment when manually scrolling to bottom
                                        listState.animateScrollToItem(totalItems - 1, 100000)
                                        // FIX: Reset interruption flag so auto-scroll resumes
                                        userHasInterrupted = false
                                    }
                                }
                            },
                            shape = CircleShape,
                            color = Color(0xFF60B0FF).copy(alpha = 0.9f),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Color(0xFFC1DFFF).copy(alpha = 0.9f)),
                            modifier = Modifier.height(36.dp)
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

                    // ── Header (Overlay) ───────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { headerHeightPx = it.size.height.toFloat() }
                            .graphicsLayer { translationY = headerOffsetPx }
                            .background(Color.White)
                            .align(Alignment.TopCenter)
                    ) {
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
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Ask WashFlow AI...", color = Color.Gray) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Default
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
                                            enabled = inputMessage.text.isNotBlank() && !processingWithGracePeriod,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (inputMessage.text.isNotBlank() && !processingWithGracePeriod)
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
                }
            }
        }
    }
}


@Composable
fun ChatMessageItem(
    message: ChatMessage,
    profilePictureUrl: String?,
    isAlreadyAnimated: Boolean = false,
    customers: List<Customers> = emptyList(),
    onConfirmAction: (AiAgentAction?) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onTextUpdate: () -> Unit = {},
    onAnimationStateChange: (Boolean) -> Unit = {}
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
                    // Start as active if it's a new message to prevent the card from flickering/appearing
                    // for a split second before the typewriter effect actually kicks in.
                    var isTypewriterActive by remember { mutableStateOf(!isAlreadyAnimated) }
                    var delayedShowActionCard by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        TypewriterText(
                            text = message.text,
                            isNewMessage = !isAlreadyAnimated,
                            onTextUpdate = onTextUpdate,
                            onAnimationStateChange = { animating ->
                                isTypewriterActive = animating
                                onAnimationStateChange(animating)
                            }
                        )

                        val hasAction = message.action != null &&
                                message.action !is AiAgentAction.None &&
                                !message.actionExecuted &&
                                !message.actionCancelled &&
                                !message.isThinking

                        LaunchedEffect(isTypewriterActive, hasAction) {
                            if (!isTypewriterActive && hasAction) {
                                delay(250L)
                                delayedShowActionCard = true
                            } else {
                                delayedShowActionCard = false
                            }
                        }

                        // Sync scroll when the confirmation card appears
                        LaunchedEffect(delayedShowActionCard) {
                            if (delayedShowActionCard) {
                                // Just one scroll update since there's no layout height animation (fade only)
                                onTextUpdate()
                            }
                        }

                        AnimatedVisibility(
                            visible = delayedShowActionCard,
                            enter = fadeIn(animationSpec = tween(500)),
                            exit = fadeOut(animationSpec = tween(500))
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                ActionConfirmationCard(
                                    action = message.action!!,
                                    customers = customers,
                                    onConfirm = onConfirmAction,
                                    onCancel = onCancelAction
                                )
                            }
                        }

                        if (message.actionExecuted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
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
                                    Icons.Default.Close,
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

@Preview(showBackground = true)
@Composable
fun AiAgentPanelChatPreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = TextFieldValue(""),
        messages = listOf(
            ChatMessage(text = "Hello, can you help me?", isUser = true),
            ChatMessage(text = "Sure! What can I do for you?", isUser = false),
            ChatMessage(text = "I want to track my order.", isUser = true),
            ChatMessage(text = "I can help you with that. Which order would you like to track?", isUser = false)
        ),
        isAiThinking = false,
        currentModelName = "Gemini Flash",
        modelStatus = AiModelStatus.IDLE,
        wasMessageAnimated = { true },
        onMessageAnimated = {},
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AiAgentPanelBulletPointPreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = TextFieldValue(""),
        messages = listOf(
            ChatMessage(
                text = "I can assist you with:\n\n* **Tracking your orders**: Get real-time updates.\n* **Managing your account**: Help with navigation.\n* **Answering general questions**: Provide information.",
                isUser = false
            )
        ),
        isAiThinking = false,
        currentModelName = "Gemini Flash",
        modelStatus = AiModelStatus.IDLE,
        wasMessageAnimated = { true },  // Preview: pretend all already animated
        onMessageAnimated = {},
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AiAgentPanelIdlePreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = TextFieldValue(""),
        messages = emptyList(),
        isAiThinking = false,
        currentModelName = null,
        modelStatus = AiModelStatus.IDLE,
        wasMessageAnimated = { true },
        onMessageAnimated = {},
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onConfirmAction = { _, _ -> },
        onCancelAction = {},
        onDismiss = {}
    )
}
