// com/aprilarn/washflow/ui/components/NotificationPreviewItem.kt
package com.aprilarn.washflow.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Notifications
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
    notification: Notifications,
    onRemove: (wasSwiped: Boolean) -> Unit
) {
    var rawOffsetX by remember { mutableFloatStateOf(0f) }
    var isVisible by remember { mutableStateOf(false) }
    var isFalling by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    val timeoutMillis = 15000L
    val progress = remember { Animatable(1f) }
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
            val remainingTime = (progress.value * timeoutMillis).toInt()
            if (remainingTime > 0) {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = remainingTime, easing = LinearEasing)
                )
            }
            
            // Jika progress mencapai 0 dan tidak sedang drag/jatuh, tutup notif
            if (progress.value <= 0f && !isDragging && !isFalling) {
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
            isFalling -> -45f // Lebih miring saat jatuh
            offsetX < -deleteThreshold -> (offsetX / 20f).coerceIn(-15f, 0f)
            else -> 0f
        },
        animationSpec = if (isFalling) tween(800) else spring()
    )

    if (fallingY > 1500f) {
        SideEffect { onRemove(true) }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
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
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Icon Area
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getNotificationPreviewIcon(notification.title),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = notification.title,
                                color = MainFontBlack,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notification.message,
                                color = Color.Gray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.LightGray.copy(alpha = 0.5f),
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        trackColor = Color.Transparent,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
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
        Text("Preview Notifikasi (Normal)", fontWeight = FontWeight.Bold)
        NotificationPreviewItem(
            notification = sampleNotif1,
            onRemove = {}
        )
        
        Text("Preview Notifikasi (Pesan Panjang)", fontWeight = FontWeight.Bold)
        NotificationPreviewItem(
            notification = sampleNotif2,
            onRemove = {}
        )
    }
}
