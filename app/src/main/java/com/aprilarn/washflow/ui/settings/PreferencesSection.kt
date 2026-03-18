package com.aprilarn.washflow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack

@Composable
fun PreferencesSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = GrayBlue,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // 1. Notifications and sounds (Switch)
                var notificationsEnabled by remember { mutableStateOf(false) }
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications and sounds",
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                )

                Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 2. Language
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("English", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 3. Set location
                SettingsItem(
                    icon = Icons.Default.MyLocation,
                    title = "Set location",
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Boyolali, Jawa Tengah", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 3. Theme
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Light", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}