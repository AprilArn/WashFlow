package com.aprilarn.washflow.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

                LongPressProceedButton(
                    onProceedConfirmed = onConfirm
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

@Composable
private fun LongPressProceedButton(
    onProceedConfirmed: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var triggerAction by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) {
            tween(
                durationMillis = 1500, 
                easing = androidx.compose.animation.core.LinearOutSlowInEasing // Cepat di awal, semakin ke kanan semakin melambat
            )
        } else {
            tween(
                durationMillis = 250, 
                easing = LinearEasing
            )
        },
        label = "ProceedProgress",
        finishedListener = { finalValue ->
            if (finalValue == 1f) {
                triggerAction = true
            }
        }
    )

    LaunchedEffect(triggerAction) {
        if (triggerAction) {
            onProceedConfirmed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE3F2FD)) // Warna background biru muda (GrayBlue theme)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .background(com.aprilarn.washflow.ui.theme.GrayBlue)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (progress >= 1f) "Memproses..." else "Hold to Proceed (1s)",
                color = if (progress > 0.5f) Color.White else com.aprilarn.washflow.ui.theme.GrayBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
