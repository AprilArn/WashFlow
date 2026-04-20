// com/aprilarn/washflow/ui/components/NotificationDropdown.kt
package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.theme.MainFontBlack
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NotificationDropdown(
    expanded: Boolean,
    notifications: List<Notifications>,
    currentUid: String,
    popupOffset: IntOffset,
    onDismiss: () -> Unit,
    onNotificationClick: (Notifications) -> Unit
) {
    if (expanded) {
        Popup(
            alignment = Alignment.TopEnd,
            offset = popupOffset,
            properties = PopupProperties(focusable = true),
            onDismissRequest = onDismiss
        ) {
            Surface(
                modifier = Modifier
                    .width(350.dp) // Lebar tetap untuk pop-up notifikasi
                    .padding(top = 8.dp)
                    .heightIn(max = 400.dp), // Bisa di-scroll jika banyak
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Dropdown
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MainFontBlack,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()

                    if (notifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada notifikasi", color = Color.Gray)
                        }
                    } else {
                        LazyColumn {
                            items(notifications) { notif ->
                                // Cek apakah notifikasi ini belum dibaca oleh user yang sedang login
                                val isUnread = currentUid !in notif.readBy

                                NotificationItem(
                                    notification = notif,
                                    isUnread = isUnread,
                                    onClick = {
                                        onNotificationClick(notif)
                                        // Jangan dismiss otomatis jika ingin user bisa klik banyak
                                    }
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notifications,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = notification.timestamp.toDate().let { formatter.format(it) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) Color(0xFFF0F8FF) else Color.White) // Biru muda jika belum dibaca
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Ikon Lonceng Bulat
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isUnread) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Konten Teks
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                ),
                color = MainFontBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        // Titik Merah kecil di kanan jika unread
        if (isUnread) {
            Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
        }
    }
}