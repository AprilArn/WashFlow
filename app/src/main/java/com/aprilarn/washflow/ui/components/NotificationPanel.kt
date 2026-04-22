// com/aprilarn/washflow/ui/components/NotificationPanel.kt
package com.aprilarn.washflow.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.MainFontBlack
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun getNotificationIcon(title: String): ImageVector {
    return when (title) {
        "Order Baru" -> Icons.Outlined.ShoppingBasket
        "Contributor Baru" -> Icons.Outlined.PersonAdd
        "Update Sistem" -> Icons.Outlined.Update
        else -> Icons.Outlined.Notifications
    }
}

@Composable
fun NotificationPanel(
    expanded: Boolean,
    notifications: List<Notifications>,
    currentUid: String,
    onDismiss: () -> Unit,
    onNotificationClick: (Notifications) -> Unit
) {
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = MainFontBlack
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { /* TODO: Mark all as read */ }) {
                                Text(
                                    text = "Mark all as read",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("All", color = MainFontBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Unread", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                    if (notifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada notifikasi", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(notifications) { notif ->
                                val isUnread = currentUid !in notif.readBy

                                NotificationPanelItem(
                                    notification = notif,
                                    isUnread = isUnread,
                                    onClick = { onNotificationClick(notif) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPanelItem(
    notification: Notifications,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = notification.timestamp.toDate().let { formatter.format(it) }
    val itemIcon = getNotificationIcon(notification.title)
    val backgroundColor = if (isUnread) Color(0xFFF8FBFF) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .height(IntrinsicSize.Max)
            .background(backgroundColor)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- BAGIAN KIRI: Strip Latar Belakang Ikon ---
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(72.dp)
                .background(
                    if (isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else Color(0xFFF8FAFC)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = itemIcon,
                contentDescription = null,
                tint = if (isUnread) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                modifier = Modifier.size(26.dp)
            )
        }

        // --- BAGIAN TENGAH: Konten Teks ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isUnread) MainFontBlack else Gray
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

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateString,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFA0AABF)
            )
        }

        // --- BAGIAN KANAN: Indikator & Menu ---
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 16.dp, top = 18.dp, bottom = 18.dp)
        ) {
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.size(8.dp))
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPanelItemPreview() {
    NotificationPanelItem(
        notification = Notifications(
            notificationId = "1",
            title = "Order Baru",
            message = "Ada pesanan baru masuk dari pelanggan.",
            senderUid = "sender123"
        ),
        isUnread = true,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationPanelPreview() {
    NotificationPanel(
        expanded = true,
        notifications = listOf(
            Notifications(
                notificationId = "1",
                title = "Order Baru",
                message = "Pesanan cuci kering 5kg telah diterima.",
                readBy = emptyList()
            ),
            Notifications(
                notificationId = "1",
                title = "Order Baru",
                message = "Pesanan cuci kering 5kg telah diterima.",
                readBy = emptyList()
            ),
            Notifications(
                notificationId = "2",
                title = "Sistem Update",
                message = "Sistem akan melakukan maintenance malam ini pada pukul 23:00.",
                readBy = listOf("user123")
            )
        ),
        currentUid = "user123",
        onDismiss = {},
        onNotificationClick = {}
    )
}
