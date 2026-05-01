// com/aprilarn/washflow/ui/components/NotificationPreviewItem.kt
package com.aprilarn.washflow.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
private fun getNotificationPreviewIcon(title: String): ImageVector {
    return when (title) {
        "Order Baru" -> Icons.Outlined.ShoppingBasket
        "Contributor Baru" -> Icons.Outlined.PersonAdd
        "Update Sistem" -> Icons.Outlined.Update
        else -> Icons.Outlined.Notifications
    }
}

@Composable
fun NotificationPreviewItem(
    modifier: Modifier = Modifier,
    notification: Notifications,
    isVisibleInitial: Boolean = false,
    onClick: () -> Unit = {},
    onRemove: (wasSwiped: Boolean) -> Unit
) {
    var rawOffsetX by remember { mutableFloatStateOf(0f) }
    var isVisible by remember { mutableStateOf(isVisibleInitial) }
    var isFalling by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    val timeoutMillis = 15000L
    val progress = remember { Animatable(0f) }
    val deleteThreshold = 240f
    val fallThreshold = 300f

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
                    animationSpec = tween(durationMillis = remainingTime, easing = LinearEasing)
                )
            }
            
            // Jika progress mencapai 100% dan tidak sedang drag/jatuh, tutup notif
            if (progress.value >= 1f && !isDragging && !isFalling) {
                isVisible = false
                delay(400)
                onRemove(false)
            }
        } else {
            // Berhenti animasi jika sedang drag atau jatuh
            progress.stop()
        }
    }

    val fallingX by animateFloatAsState(
        targetValue = if (isFalling) -1000f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutLinearInEasing)
    )

    val fallingY by animateFloatAsState(
        targetValue = if (isFalling) 2000f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutLinearInEasing)
    )

    // Efek miring saat digeser
    val rotationZ by animateFloatAsState(
        targetValue = when {
            isFalling -> -55f // Lebih miring saat jatuh
            offsetX < -deleteThreshold -> (offsetX / 20f).coerceIn(-15f, 0f)
            else -> 0f
        },
        animationSpec = if (isFalling) tween(1000) else spring()
    )

    if (fallingY > 1500f) {
        SideEffect { onRemove(true) }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX + fallingX
                    translationY = fallingY
                    this.rotationZ = rotationZ
                    transformOrigin = TransformOrigin(1f, 0.5f) // Pivot di tengah kanan
                }
                .width(400.dp)
                .padding(vertical = 4.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragCancel = { 
                            isDragging = false
                            rawOffsetX = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            if (rawOffsetX < -fallThreshold) {
                                isFalling = true
                            } else {
                                rawOffsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Hanya izinkan geser ke kiri
                            if (rawOffsetX + dragAmount <= 0) {
                                rawOffsetX += dragAmount
                            }
                        }
                    )
                }
        ) {
            Surface(
                // shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick() }
                    // .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
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
                                text = notification.title,
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
                            imageVector = Icons.Default.Notifications,
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

@Preview
@Composable
fun NotificationPreviewItemPreview() {
    val sampleNotif1 = Notifications(
        notificationId = "1",
        title = "Order Baru",
        message = "Pesanan cuci kering 5kg telah diterima dari pelanggan Aprilia. Mohon segera diproses untuk menjaga kepuasan pelanggan.",
        senderUid = "system",
        timestamp = Timestamp.now()
    )
    val sampleNotif2 = Notifications(
        notificationId = "2",
        title = "Update Sistem",
        message = "Versi terbaru 2.0.4 kini tersedia dengan perbaikan bug pada sistem pembayaran.",
        senderUid = "system",
        timestamp = Timestamp.now()
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(450.dp), // Beri ruang lebih dari 400dp agar bayangan/shadow terlihat
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NotificationPreviewItem(
            notification = sampleNotif1,
            isVisibleInitial = true,
            onClick = {},
            onRemove = {}
        )

        NotificationPreviewItem(
            notification = sampleNotif2,
            isVisibleInitial = true,
            onClick = {},
            onRemove = {}
        )
    }
}
