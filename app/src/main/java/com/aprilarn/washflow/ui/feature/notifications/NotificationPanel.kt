package com.aprilarn.washflow.ui.feature.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.core.theme.Gray
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun getNotificationIcon(title: String): ImageVector {
    return when (title) {
        "Order Baru", "New Order" -> Icons.Rounded.ShoppingBasket
        "Contributor Baru", "New Contributor" -> Icons.Rounded.PersonAdd
        "Update Sistem", "Sistem Update", "System Update", "Order Diperbarui", "Order Updated" -> Icons.Rounded.Update
        else -> Icons.Rounded.Notifications
    }
}

@Composable
private fun localizeNotificationTitle(title: String): String {
    return when (title) {
        "Order Baru", "New Order" -> stringResource(R.string.notif_title_new_order)
        "Order Diperbarui", "Order Updated" -> stringResource(R.string.notif_title_order_updated)
        "Contributor Baru", "New Contributor" -> stringResource(R.string.notif_title_new_contributor)
        "Update Sistem", "Sistem Update", "System Update" -> stringResource(R.string.notif_title_system_update)
        else -> title
    }
}

@Composable
fun NotificationPanel(
    expanded: Boolean,
    notifications: List<Notifications>,
    currentUid: String,
    filter: NotificationFilter,
    onFilterChange: (NotificationFilter) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDismiss: () -> Unit,
    onNotificationClick: (Notifications) -> Unit
) {
    if (expanded) {
        BackHandler {
            onDismiss()
        }
    }

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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.notif_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = MainFontBlack
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(onClick = onMarkAllAsRead) {
                                    Text(
                                        text = stringResource(R.string.notif_mark_all_read),
                                        color = GrayBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Surface(
                                    onClick = onDismiss,
                                    shape = CircleShape,
                                    color = PastelBlueSoftBg,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.notif_close_cd),
                                            tint = GrayBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Kurangi jarak antar tombol
                        ) {
                            TextButton(
                                onClick = { onFilterChange(NotificationFilter.UNREAD) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.notif_filter_unread),
                                    color = if (filter == NotificationFilter.UNREAD) MainFontBlack else Color.Gray,
                                    fontWeight = if (filter == NotificationFilter.UNREAD) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            TextButton(
                                onClick = { onFilterChange(NotificationFilter.ALL) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.notif_filter_all),
                                    color = if (filter == NotificationFilter.ALL) MainFontBlack else Color.Gray,
                                    fontWeight = if (filter == NotificationFilter.ALL) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.notif_empty), color = Color.Gray)
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
                    if (isUnread) GrayBlue.copy(alpha = 0.08f)
                    else Color(0xFFF8FAFC)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = itemIcon,
                contentDescription = null,
                tint = if (isUnread) GrayBlue else Color(0xFF94A3B8),
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
                text = localizeNotificationTitle(notification.title),
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
                        .background(GrayBlue, CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.size(8.dp))
            }

            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.common_options),
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
            title = "New Order",
            message = "A new service order has been received.",
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
                title = "New Order",
                message = "Express service order has been received.",
                readBy = emptyList()
            ),
            Notifications(
                notificationId = "2",
                title = "System Update",
                message = "Scheduled system maintenance tonight at 23:00.",
                readBy = listOf("user123")
            )
        ),
        currentUid = "user123",
        filter = NotificationFilter.UNREAD,
        onFilterChange = {},
        onMarkAllAsRead = {},
        onDismiss = {},
        onNotificationClick = {}
    )
}
