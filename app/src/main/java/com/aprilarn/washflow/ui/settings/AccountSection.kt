package com.aprilarn.washflow.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun AccountSection(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Account",
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
                // 1. Support
                SettingsItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Support",
                    trailingContent = {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 2. Logout
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Logout",
                    titleColor = MaterialTheme.colorScheme.error, // Warna merah
                    iconTint = MaterialTheme.colorScheme.error, // Icon merah
                    onClick = onSignOut,
                    trailingContent = {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                )
            }
        }
    }
}