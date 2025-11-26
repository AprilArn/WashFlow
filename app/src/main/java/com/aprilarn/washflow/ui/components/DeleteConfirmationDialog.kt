package com.aprilarn.washflow.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                LongPressDeleteButton(
                    onDeleteConfirmed = onConfirm
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

@Composable
fun LongPressDeleteButton(
    onDeleteConfirmed: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var triggerAction by remember { mutableStateOf(false) }

    // Animasi progress bar dari 0f ke 1f selama 2000ms (2 detik)
    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) {
            // Jika ditekan (MAJU ke kanan): Durasi 2 detik (lambat/beban berat)
            tween(durationMillis = 2000, easing = LinearEasing)
        } else {
            // Jika dilepas/batal (MUNDUR ke kiri): Durasi 0.25 detik (cepat/snappy)
            tween(durationMillis = 250, easing = LinearEasing)
        },
        label = "DeleteProgress",
        finishedListener = { finalValue ->
            if (finalValue == 1f) {
                triggerAction = true
            }
        }
    )

    // Efek samping ketika progress mencapai 100%
    LaunchedEffect(triggerAction) {
        if (triggerAction) {
            onDeleteConfirmed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFEBEE)) // Warna background merah sangat muda
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false // Reset jika dilepas sebelum selesai
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Progress Fill (Merah Gelap)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .background(Color(0xFFD32F2F))
        )

        // Teks Label (di tengah)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (progress >= 1f) "Deleting..." else "Hold to Delete (2s)",
                color = if (progress > 0.5f) Color.White else Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
        }
    }
}