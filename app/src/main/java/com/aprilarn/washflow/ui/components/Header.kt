// com/aprilarn/washflow/ui/components/Header.kt
package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun Header(
    modifier: Modifier = Modifier,
    navController: NavController,
    workspaceName: String,
    unreadCount: Int,
    notificationPreviews: List<Notifications> = emptyList(),
    onWorkspaceClick: () -> Unit,
    onNotifClick: () -> Unit,
    onRemovePreview: (String, Boolean) -> Unit,
    workspaceDropdown: @Composable (IntOffset) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LOGO WASHFLOW ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Wash",
                style = MaterialTheme.typography.headlineMedium.copy(color = GrayBlue, fontSize = 22.sp)
            )
            Text(
                text = "Flow",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontSize = 22.sp, fontStyle = FontStyle.Italic)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // --- NAVIGATION BAR ---
        NavigationBar(navController = navController, modifier = Modifier.height(44.dp))

        Spacer(modifier=Modifier.weight(1f))

        // --- WORKSPACE DROPDOWN TRIGGER ---
        var wsTriggerSize by remember { mutableStateOf(IntSize.Zero) }
        Box(modifier = Modifier.onGloballyPositioned { wsTriggerSize = it.size }) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onWorkspaceClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                    text = workspaceName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Light)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Workspace Options", tint = Color.White)
            }

            // 2. MASUKKAN LAMBDA TEPAT DI DALAM BOX INI
            workspaceDropdown(IntOffset(0, wsTriggerSize.height))
        }

        // --- NOTIFICATION DROPDOWN TRIGGER ---
        var notifTriggerSize by remember { mutableStateOf(IntSize.Zero) }
        Box(modifier = Modifier.onGloballyPositioned { notifTriggerSize = it.size }) {
            IconButton(onClick = onNotifClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$unreadCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = Color.White)
                }
            }
            // --- AREA PREVIEW NOTIFIKASI ---
            if (notificationPreviews.isNotEmpty()) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(0, notifTriggerSize.height),
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnClickOutside = false,
                        dismissOnBackPress = false,
                        clippingEnabled = false // Mencegah popup bergeser ke atas saat animasi jatuh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp), // Hanya padding atas agar ada jarak dari lonceng
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        notificationPreviews.forEach { notif ->
                            NotificationPreviewItem(
                                notification = notif,
                                onRemove = { wasSwiped ->
                                    onRemovePreview(notif.notificationId, wasSwiped)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun HeaderPreview() {
    Header(
        workspaceName = "Example",
        unreadCount = 3,
        notificationPreviews = emptyList(),
        navController = rememberNavController(),
        onWorkspaceClick = {},
        onNotifClick = {},
        onRemovePreview = { _, _ -> },
        workspaceDropdown = { _ -> }
    )
}