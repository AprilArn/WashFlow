package com.aprilarn.washflow.ui.feature.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
private fun getNotificationPreviewIcon(title: String): ImageVector {
    return when (title) {
        "Order Baru", "New Order" -> Icons.Rounded.ShoppingBasket
        "Contributor Baru", "New Contributor" -> Icons.Rounded.PersonAdd
        "Update Sistem", "Sistem Update", "System Update", "Order Diperbarui", "Order Updated" -> Icons.Rounded.Update
        else -> Icons.Rounded.Notifications
    }
}

private fun resolveNotificationAccent(title: String): Pair<Color, Color> {
    return when (title) {
        "Order Baru", "New Order" -> PastelBlueSoftBg to PastelBlue
        "Contributor Baru", "New Contributor" -> PastelGreenBg to PastelGreen
        "Update Sistem", "Sistem Update", "System Update", "Order Diperbarui", "Order Updated" -> PastelAmberBg to PastelAmber
        else -> PastelBlueSoftBg to PastelBlue
    }
}

@Composable
private fun localizePreviewTitle(title: String): String {
    return when (title) {
        "Order Baru", "New Order" -> stringResource(R.string.notif_title_new_order)
        "Order Diperbarui", "Order Updated" -> stringResource(R.string.notif_title_order_updated)
        "Contributor Baru", "New Contributor" -> stringResource(R.string.notif_title_new_contributor)
        "Update Sistem", "Sistem Update", "System Update" -> stringResource(R.string.notif_title_system_update)
        else -> title
    }
}

@Composable
fun NotificationPreviewItem(
    modifier: Modifier = Modifier,
    notification: Notifications,
    isVisibleInitial: Boolean = false,
    isCompact: Boolean = false,
    onClick: () -> Unit = {},
    onRemove: (wasSwiped: Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    var rawOffsetX by remember { mutableFloatStateOf(0f) }
    var isVisible by remember { mutableStateOf(isVisibleInitial) }
    var isFalling by remember { mutableStateOf(false) }
    var compactDismissDir by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isDismissing = isFalling || compactDismissDir != 0

    val timeoutMillis = 15000L
    val progress = remember { Animatable(0f) }

    // Tablet drag constants
    val deleteThreshold = 300f
    val fallThreshold = 300f
    val maxDragLimit = -500f // Batas maksimal geser ke kiri
    val resistanceThreshold = -350f // Titik mulai terasa berat (resistance)

    // Compact (Phone) drag constants
    val screenWidthPx = windowSize.width.toFloat().coerceAtLeast(1f)
    val compactDismissThresholdPx = with(density) { 105.dp.toPx() }

    // Animasi snap back saat dilepas
    val offsetX by animateFloatAsState(
        targetValue = if (isCompact && compactDismissDir != 0) {
            compactDismissDir * (screenWidthPx * 1.15f)
        } else {
            rawOffsetX
        },
        animationSpec = when {
            isCompact && compactDismissDir != 0 -> tween(durationMillis = 240, easing = FastOutLinearInEasing)
            isDragging || isFalling -> snap()
            else -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "offsetX"
    )

    val compactAlpha by animateFloatAsState(
        targetValue = if (isCompact && compactDismissDir != 0) 0f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "compactAlpha"
    )

    LaunchedEffect(compactDismissDir) {
        if (isCompact && compactDismissDir != 0) {
            delay(240)
            onRemove(true)
        }
    }

    LaunchedEffect(isDragging, isDismissing) {
        if (!isDragging && !isDismissing) {
            isVisible = true
            val remainingTime = ((1f - progress.value) * timeoutMillis).toInt()
            if (remainingTime > 0) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = remainingTime,
                        easing = LinearOutSlowInEasing // Animasi dari cepat, lalu perlahan melambat
                    )
                )
            }

            // Jika progress mencapai 100% dan tidak sedang drag/dismiss, tutup notif
            if (progress.value >= 1f) {
                isVisible = false
                delay(400)
                onRemove(false)
            }
        } else {
            // Berhenti animasi jika sedang drag atau dismiss
            progress.stop()
        }
    }

    // Dynamic screen height for tablet falling dismissal
    val screenHeightPx = windowSize.height.toFloat().coerceAtLeast(1f)
    val safeFallingY = screenHeightPx + 500f

    // Constant velocity: 2000px / 800ms = 2.5px/ms
    val fallingDuration = remember(safeFallingY) { (safeFallingY / 2.5f).toInt() }
    val rotationDuration = remember(fallingDuration) { (fallingDuration * 1.25f).toInt() }

    val fallingX by animateFloatAsState(
        targetValue = if (!isCompact && isFalling) -1000f else 0f,
        animationSpec = tween(durationMillis = fallingDuration, easing = FastOutLinearInEasing),
        label = "fallingX"
    )

    val fallingY by animateFloatAsState(
        targetValue = if (!isCompact && isFalling) safeFallingY else 0f,
        animationSpec = tween(durationMillis = fallingDuration, easing = FastOutLinearInEasing),
        label = "fallingY"
    )

    // Efek miring saat digeser
    val rotationZ by animateFloatAsState(
        targetValue = if (isCompact) {
            ((offsetX / screenWidthPx.coerceAtLeast(1f)) * 5f).coerceIn(-6f, 6f)
        } else {
            when {
                isFalling -> -55f // Lebih miring saat jatuh
                offsetX < -deleteThreshold -> (offsetX / 20f).coerceIn(-15f, 0f)
                else -> 0f
            }
        },
        animationSpec = if (!isCompact && isFalling) tween(rotationDuration) else spring(),
        label = "rotationZ"
    )

    if (!isCompact && fallingY > screenHeightPx) {
        SideEffect { onRemove(true) }
    }

    val enterTransition = if (isCompact) {
        slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(220))
    } else {
        slideInHorizontally(initialOffsetX = { it }) + fadeIn()
    }

    val exitTransition = if (isCompact) {
        slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(240, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(200))
    } else {
        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = enterTransition,
        exit = exitTransition,
        modifier = modifier
    ) {
        val widthModifier = if (isCompact) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.width(400.dp)
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX + fallingX
                    translationY = fallingY
                    this.rotationZ = rotationZ
                    alpha = compactAlpha
                    transformOrigin = if (isCompact) TransformOrigin.Center else TransformOrigin(1f, 0.5f)
                }
                .then(widthModifier)
                .padding(vertical = 4.dp)
                .pointerInput(isCompact) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragCancel = {
                            isDragging = false
                            rawOffsetX = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            if (isCompact) {
                                when {
                                    rawOffsetX <= -compactDismissThresholdPx -> compactDismissDir = -1
                                    rawOffsetX >= compactDismissThresholdPx -> compactDismissDir = 1
                                    else -> rawOffsetX = 0f
                                }
                            } else {
                                if (rawOffsetX < -fallThreshold) {
                                    isFalling = true
                                } else {
                                    rawOffsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val previousOffset = rawOffsetX
                            if (isCompact) {
                                val maxCompactOffset = screenWidthPx * 0.65f
                                val newOffset = (rawOffsetX + dragAmount).coerceIn(-maxCompactOffset, maxCompactOffset)
                                if (newOffset != rawOffsetX) {
                                    rawOffsetX = newOffset
                                    val crossed = (abs(previousOffset) < compactDismissThresholdPx && abs(rawOffsetX) >= compactDismissThresholdPx) ||
                                        (abs(previousOffset) >= compactDismissThresholdPx && abs(rawOffsetX) < compactDismissThresholdPx)
                                    if (crossed) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            } else {
                                // Hitung dragAmount dengan resistensi jika sudah melewati ambang batas
                                val effectiveDrag = if (rawOffsetX < resistanceThreshold && dragAmount < 0) {
                                    dragAmount * 0.4f // Beri beban 60% lebih berat
                                } else {
                                    dragAmount
                                }

                                // Batasi agar hanya bisa geser ke kiri dan tidak melewati maxDragLimit
                                val newOffset = (rawOffsetX + effectiveDrag).coerceIn(maxDragLimit, 0f)

                                if (newOffset != rawOffsetX) {
                                    rawOffsetX = newOffset

                                    // Trigger haptic feedback when crossing the fallThreshold (-300f)
                                    val threshold = -fallThreshold
                                    val crossedGoingLeft = previousOffset > threshold && rawOffsetX <= threshold
                                    val crossedGoingRight = previousOffset < threshold && rawOffsetX >= threshold

                                    if (crossedGoingLeft || crossedGoingRight) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            if (isCompact) {
                val (iconBg, iconTint) = remember(notification.title) {
                    resolveNotificationAccent(notification.title)
                }

                Surface(
                    onClick = {
                        scope.launch {
                            isVisible = false
                            delay(260)
                            onClick()
                        }
                    },
                    enabled = isVisible && !isDismissing,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, PastelBlueBg),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getNotificationPreviewIcon(notification.title),
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = localizePreviewTitle(notification.title),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = GrayBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.notif_just_now),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PastelNeutralText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Text(
                                    text = notification.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress.value },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .align(Alignment.BottomCenter),
                            color = iconTint.copy(alpha = 0.6f),
                            trackColor = iconBg.copy(alpha = 0.65f),
                        )
                    }
                }
            } else {
                Surface(
                    onClick = {
                        scope.launch {
                            isVisible = false
                            delay(400) // Wait for AnimatedVisibility exit
                            onClick()
                        }
                    },
                    enabled = isVisible && !isFalling,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Icon Area - Full Height Strip
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(72.dp)
                                    .background(GrayBlue.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getNotificationPreviewIcon(notification.title),
                                    contentDescription = null,
                                    tint = GrayBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = localizePreviewTitle(notification.title),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MainFontBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notification.message,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFF64748B),
                                    lineHeight = 18.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = GrayBlue.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(20.dp)
                            )
                        }

                        // Progress bar at the bottom
                        LinearProgressIndicator(
                            progress = { progress.value },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.BottomCenter),
                            color = GrayBlue.copy(alpha = 0.6f),
                            trackColor = Color.Transparent,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPreviewItemPreview() {
    val sampleNotif1 = Notifications(
        notificationId = "1",
        title = "New Order",
        message = "April Arn created a new order for Verstappen.",
        senderUid = "system",
        timestamp = Timestamp.now()
    )
    val sampleNotif2 = Notifications(
        notificationId = "2",
        title = "System Update",
        message = "Version 2.0.4 is now available with payment workflow improvements.",
        senderUid = "system",
        timestamp = Timestamp.now()
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(360.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NotificationPreviewItem(
            notification = sampleNotif1,
            isVisibleInitial = true,
            isCompact = true,
            onClick = {},
            onRemove = {}
        )

        NotificationPreviewItem(
            notification = sampleNotif2,
            isVisibleInitial = true,
            isCompact = true,
            onClick = {},
            onRemove = {}
        )
    }
}
