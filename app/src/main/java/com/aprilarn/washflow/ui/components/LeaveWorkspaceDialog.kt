package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun LeaveWorkspaceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Judul
                Text(
                    text = "Leave Workspace?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Peringatan
                Text(
                    text = "Anda akan kehilangan akses ke semua data di workspace ini. Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tombol Hold-to-Leave (4 Detik)
                HoldToConfirmButton(
                    defaultText = "Hold to Leave (4s)",
                    progressText = "Leaving...",
                    durationMillis = 4000,
                    onConfirm = onConfirm
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Cancel
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}
