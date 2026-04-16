package com.aprilarn.washflow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack

@Composable
fun PreferencesSection(
    locationName: String,
    onSetLocationClicked: () -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
                SettingsItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Sound Notifications",
                    trailingContent = {
                        Switch(
                            modifier = Modifier.height(24.dp),
                            checked = isSoundEnabled,
                            onCheckedChange = { onSoundToggled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GrayBlue
                            )
                        )
                    }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

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

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 3. Set location
                SettingsItem(
                    icon = Icons.Default.MyLocation,
                    title = "Set location",
                    onClick = onSetLocationClicked,
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Gunakan locationName yang diteruskan dari State
                            Text(
                                text = locationName,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

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