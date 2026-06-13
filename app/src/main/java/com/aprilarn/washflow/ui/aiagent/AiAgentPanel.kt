package com.aprilarn.washflow.ui.aiagent

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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.utils.MarkdownUtils

@Composable
fun AiAgentPanel(
    expanded: Boolean,
    userName: String,
    profilePictureUrl: String?,
    inputMessage: String,
    messages: List<ChatMessage>,
    isAiThinking: Boolean,
    currentModelName: String? = null,
    modelStatus: AiModelStatus = AiModelStatus.IDLE,
    wasMessageAnimated: (String) -> Boolean,
    onMessageAnimated: (String) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // ── Auto-scroll State ──────────────────────────────────────────────────────
    var userHasInterrupted by remember { mutableStateOf(false) }

    // Detect if user is at the bottom to reset interruption
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf true
            val lastVisibleItem = visibleItems.last()
            // If last item is the last index and its bottom is close to the viewport bottom
            lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                    (layoutInfo.viewportEndOffset - lastVisibleItem.offset) <= 100 // threshold in pixels
        }
    }

    // If user scrolls back to bottom, we resume auto-scroll
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) userHasInterrupted = false
    }

    // Detect manual scroll to set interruption
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && !isAtBottom) {
            userHasInterrupted = true
        }
    }

    // ── Auto-scroll ────────────────────────────────────────────────────────────
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
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
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Header ─────────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WashFlow AI",
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

                    // ── Message list ───────────────────────────────────────────
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
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
                                            onTextUpdate = {
                                                if (!userHasInterrupted) {
                                                    coroutineScope.launch {
                                                        listState.scrollToItem(messages.size - 1)
                                                    }
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
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
                        ) {
                            Surface(
                                onClick = {
                                    coroutineScope.launch {
                                        val totalItems = listState.layoutInfo.totalItemsCount
                                        if (totalItems > 0) {
                                            listState.animateScrollToItem(totalItems - 1)
                                            // FIX: Reset interruption flag so auto-scroll resumes
                                            userHasInterrupted = false
                                        }
                                    }
                                },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.8f),
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 1f)),
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
                                        color = GrayBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = GrayBlue
                                    )
                                }
                            }
                        }
                    }

                    // ── Input area ─────────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                                    ),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onSend = {
                                            if (inputMessage.isNotBlank() && !isAiThinking) {
                                                onSendMessage()
                                            }
                                        }
                                    ),
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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = if (modelStatus == AiModelStatus.IDLE) "Idle" else (currentModelName ?: ""),
                                                    color = Gray,
                                                    fontSize = 12.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                AnimatedContent(
                                                    targetState = modelStatus,
                                                    label = "ModelStatusIcon"
                                                ) { status ->
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
                                            enabled = inputMessage.isNotBlank() && !isAiThinking,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (inputMessage.isNotBlank() && !isAiThinking)
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
fun AiMessageHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = GrayBlue,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "WashFlow AI",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = GrayBlue
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    profilePictureUrl: String?,
    isAlreadyAnimated: Boolean = false,
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
                    TypewriterText(
                        text = message.text,
                        isNewMessage = !isAlreadyAnimated,
                        onTextUpdate = onTextUpdate
                    )
                }
            }
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Long = 10L,
    isNewMessage: Boolean = true,
    onTextUpdate: () -> Unit = {}
) {
    var displayedText by rememberSaveable {
        mutableStateOf(if (isNewMessage && text.isNotEmpty()) text.take(1) else text)
    }
    var animationFinished by rememberSaveable { mutableStateOf(!isNewMessage) }
    
    var lastProcessedText by rememberSaveable { mutableStateOf(text) }

    LaunchedEffect(text) {
        if (text != lastProcessedText) {
            lastProcessedText = text
            if (isNewMessage) {
                displayedText = text.take(1)
                animationFinished = false
            } else {
                displayedText = text
                animationFinished = true
            }
        }

        if (!animationFinished) {
            if (text.isEmpty()) {
                displayedText = ""
            } else {
                val startIndex = displayedText.length
                for (index in startIndex until text.length) {
                    displayedText = text.substring(0, index + 1)
                    onTextUpdate()
                    delay(delayMillis)
                }
            }
            animationFinished = true
        } else {
            displayedText = text
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


@Preview(showBackground = true)
@Composable
fun AiAgentPanelBulletPointPreview() {
    AiAgentPanel(
        expanded = true,
        userName = "April",
        profilePictureUrl = null,
        inputMessage = "",
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
        inputMessage = "",
        messages = emptyList(),
        isAiThinking = false,
        currentModelName = null,
        modelStatus = AiModelStatus.IDLE,
        wasMessageAnimated = { true },
        onMessageAnimated = {},
        onInputChange = {},
        onSendMessage = {},
        onClearHistory = {},
        onDismiss = {}
    )
}