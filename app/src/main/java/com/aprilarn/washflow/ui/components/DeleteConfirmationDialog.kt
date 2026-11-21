package com.aprilarn.washflow.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aprilarn.washflow.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)
        val isSoundEnabled = prefs.getBoolean("SOUND_ENABLED", true)

        if (isSoundEnabled) {
            try {
                val mediaPlayer = MediaPlayer.create(context, R.raw.confirmation)
                mediaPlayer.start()

                // Clear memory when done
                mediaPlayer.setOnCompletionListener { it.release() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Confirmation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Are you sure you want to delete '$itemName'?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Delete Khusus (Long Press)
                HoldToConfirmButton(
                    defaultText = "Hold to Delete (1s)",
                    progressText = "Deleting...",
                    durationMillis = 1500,
                    onConfirm = onConfirm
                )

                Spacer(modifier = Modifier.height(12.dp))

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
