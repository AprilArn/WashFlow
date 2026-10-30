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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R

@Composable
fun NoDeadlineConfirmationDialog(
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
                    text = "Batas Waktu Kosong",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Pesanan ini akan dibuat tanpa batas waktu pengerjaan.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Apakah Anda yakin ingin melanjutkan?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                HoldToConfirmButton(
                    defaultText = "Hold to Proceed (1s)",
                    progressText = "Memproses...",
                    durationMillis = 1500,
                    containerColor = Color(0xFFE3F2FD),
                    progressColor = com.aprilarn.washflow.ui.theme.GrayBlue,
                    defaultTextColor = com.aprilarn.washflow.ui.theme.GrayBlue,
                    onConfirm = onConfirm
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = Color.Gray)
                }
            }
        }
    }
}

