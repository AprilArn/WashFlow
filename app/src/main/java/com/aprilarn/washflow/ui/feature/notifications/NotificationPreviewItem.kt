package com.aprilarn.washflow.ui.feature.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
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

private fun hasOrderNavigationAction(title: String): Boolean {
    return title == "Order Baru" ||
        title == "Order Diperbarui" ||
        title == "New Order" ||
        title == "Order Updated"
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
    var fallDirection by remember { mutableFloatStateOf(-1f) }
    var lastDragDirection by remember { mutableFloatStateOf(-1f) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val timeoutMillis = 15000L
    val progress = remember { Animatable(0f) }

    // Tablet drag constants (swipe left only)
    val deleteThreshold = 300f
    val fallThreshold = 300f
    val maxDragLimit = -500f // Batas maksimal geser ke kiri
    val resistanceThreshold = -350f // Titik mulai terasa berat (resistance)

    // Compact (Phone) drag constants (bidirectional left & right, proportional to tablet)
    val screenWidthPx = windowSize.width.toFloat().coerceAtLeast(1f)
    val compactDismissThresholdPx = with(density) { 105.dp.toPx() }
    val compactResistanceThresholdPx = with(density) { 125.dp.toPx() }
    val compactMaxDragLimitPx = with(density) { 180.dp.toPx() }
        .coerceAtMost(screenWidthPx * 0.55f)
        .coerceAtLeast(compactResistanceThresholdPx + with(density) { 20.dp.toPx() })

    // Animasi snap back saat dilepas
    val offsetX by animateFloatAsState(
        targetValue = rawOffsetX,
        animationSpec = if (isDragging || isFalling) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "offsetX"
    )

    LaunchedEffect(isDragging, isFalling) {
        if (!isDragging && !isFalling) {
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
                delay(if (isCompact) 260L else 400L)
                onRemove(false)
            }
        } else {
            // Berhenti animasi jika sedang drag atau dismiss
            progress.stop()
        }
    }

    // Dynamic screen height for falling dismissal
    val screenHeightPx = windowSize.height.toFloat().coerceAtLeast(1f)
    val safeFallingY = screenHeightPx + 500f

    // Constant velocity: 2000px / 800ms = 2.5px/ms
    val fallingDuration = remember(safeFallingY) { (safeFallingY / 2.5f).toInt() }
    val rotationDuration = remember(fallingDuration) { (fallingDuration * 1.25f).toInt() }

    val fallingX by animateFloatAsState(
        targetValue = if (isFalling) 1000f * fallDirection else 0f,
        animationSpec = tween(durationMillis = fallingDuration, easing = FastOutLinearInEasing),
        label = "fallingX"
    )

    val fallingY by animateFloatAsState(
        targetValue = if (isFalling) safeFallingY else 0f,
        animationSpec = tween(durationMillis = fallingDuration, easing = FastOutLinearInEasing),
        label = "fallingY"
    )

    // Efek miring saat digeser melewati ambang batas & saat jatuh
    val rotationZ by animateFloatAsState(
        targetValue = when {
            isFalling -> 55f * fallDirection // Lebih miring saat jatuh ke kiri (-55f) atau kanan (+55f)
            isCompact && abs(offsetX) >= compactDismissThresholdPx ->
                (offsetX / (compactDismissThresholdPx / 15f)).coerceIn(-15f, 15f)
            !isCompact && offsetX < -deleteThreshold ->
                (offsetX / 20f).coerceIn(-15f, 0f)
            else -> 0f
        },
        animationSpec = if (isFalling) tween(rotationDuration) else spring(),
        label = "rotationZ"
    )

    val hasFallenOut = isFalling && fallingY > screenHeightPx
    LaunchedEffect(hasFallenOut) {
        if (hasFallenOut) {
            onRemove(true)
        }
    }

    val pivotX = if (isCompact) {
        val activeDirection = if (isFalling) fallDirection else lastDragDirection
        if (activeDirection > 0f) 0f else 1f
    } else {
        1f
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
                    transformOrigin = TransformOrigin(pivotX, 0.5f)
                }
                .then(widthModifier)
                .padding(vertical = 4.dp)
                .pointerInput(isCompact, isFalling) {
                    if (isFalling) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragCancel = {
                            isDragging = false
                            rawOffsetX = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            if (isCompact) {
                                if (abs(rawOffsetX) >= compactDismissThresholdPx) {
                                    fallDirection = if (rawOffsetX > 0f) 1f else -1f
                                    isFalling = true
                                } else {
                                    rawOffsetX = 0f
                                }
                            } else {
                                if (rawOffsetX < -fallThreshold) {
                                    fallDirection = -1f
                                    isFalling = true
                                } else {
                                    rawOffsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val previousOffset = rawOffsetX
                            if (isCompact) {
                                val isPullingOutward = (rawOffsetX > 0f && dragAmount > 0f) ||
                                    (rawOffsetX < 0f && dragAmount < 0f)
                                val effectiveDrag = if (abs(rawOffsetX) > compactResistanceThresholdPx && isPullingOutward) {
                                    dragAmount * 0.4f // Beri beban 60% lebih berat seperti tablet
                                } else {
                                    dragAmount
                                }

                                val newOffset = (rawOffsetX + effectiveDrag)
                                    .coerceIn(-compactMaxDragLimitPx, compactMaxDragLimitPx)

                                if (newOffset != rawOffsetX) {
                                    rawOffsetX = newOffset
                                    if (newOffset != 0f) {
                                        lastDragDirection = if (newOffset > 0f) 1f else -1f
                                    }

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
            val (iconBg, iconTint) = remember(notification.title) {
                resolveNotificationAccent(notification.title)
            }
            val showActionCue = remember(notification.title) {
                hasOrderNavigationAction(notification.title)
            }
            val isReadyToDismiss = isFalling || if (isCompact) {
                abs(offsetX) >= compactDismissThresholdPx
            } else {
                offsetX < -deleteThreshold
            }

            val cardShape = RoundedCornerShape(20.dp)
            val borderColor by animateColorAsState(
                targetValue = if (isReadyToDismiss) {
                    PastelRose.copy(alpha = 0.65f)
                } else {
                    iconTint.copy(alpha = 0.22f)
                },
                animationSpec = tween(durationMillis = 180),
                label = "previewBorderColor"
            )
            val progressColor by animateColorAsState(
                targetValue = if (isReadyToDismiss) {
                    PastelRose.copy(alpha = 0.75f)
                } else {
                    iconTint.copy(alpha = 0.65f)
                },
                animationSpec = tween(durationMillis = 180),
                label = "previewProgressColor"
            )
            val progressTrackColor by animateColorAsState(
                targetValue = if (isReadyToDismiss) {
                    PastelRoseBg
                } else {
                    iconBg.copy(alpha = 0.8f)
                },
                animationSpec = tween(durationMillis = 180),
                label = "previewProgressTrackColor"
            )

            Surface(
                onClick = {
                    scope.launch {
                        isVisible = false
                        delay(if (isCompact) 260L else 400L)
                        onClick()
                    }
                },
                enabled = isVisible && !isFalling,
                shape = cardShape,
                color = Color.White,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 10.dp,
                        shape = cardShape,
                        ambientColor = GrayBlue.copy(alpha = 0.28f),
                        spotColor = GrayBlue.copy(alpha = 0.34f)
                    )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (isCompact) 12.dp else 14.dp,
                                end = if (isCompact) 12.dp else 14.dp,
                                top = if (isCompact) 12.dp else 14.dp,
                                bottom = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val iconBoxShape = RoundedCornerShape(13.dp)
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 42.dp else 44.dp)
                                .clip(iconBoxShape)
                                .background(iconBg)
                                .border(1.dp, iconTint.copy(alpha = 0.14f), iconBoxShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getNotificationPreviewIcon(notification.title),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(if (isCompact) 21.dp else 22.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(iconTint)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = localizePreviewTitle(notification.title),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = MainFontBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(PastelNeutralBg)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.notif_just_now),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PastelNeutralText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Text(
                                text = notification.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = if (isCompact) 12.5.sp else 13.sp,
                                    lineHeight = if (isCompact) 17.5.sp else 18.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (showActionCue) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = PastelNeutralText.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50)),
                        color = progressColor,
                        trackColor = progressTrackColor,
                        strokeCap = StrokeCap.Round,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Phone Notification Preview")
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
        title = "New Contributor",
        message = "Budi Santoso joined your workspace.",
        senderUid = "system",
        timestamp = Timestamp.now()
    )
    val sampleNotif3 = Notifications(
        notificationId = "3",
        title = "Order Updated",
        message = "Order #WF-1042 status moved to Ready for Pickup.",
        senderUid = "system",
        timestamp = Timestamp.now()
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(360.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
        NotificationPreviewItem(
            notification = sampleNotif3,
            isVisibleInitial = true,
            isCompact = true,
            onClick = {},
            onRemove = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Tablet Notification Preview")
@Composable
fun NotificationPreviewItemTabletPreview() {
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
            .width(420.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NotificationPreviewItem(
            notification = sampleNotif1,
            isVisibleInitial = true,
            isCompact = false,
            onClick = {},
            onRemove = {}
        )
        NotificationPreviewItem(
            notification = sampleNotif2,
            isVisibleInitial = true,
            isCompact = false,
            onClick = {},
            onRemove = {}
        )
    }
}
