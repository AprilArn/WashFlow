// com/aprilarn/washflow/ui/contributors/ContributorDetailDialog.kt
package com.aprilarn.washflow.ui.contributors

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun ContributorDetailDialog(
    contributor: ContributorUiModel,
    showKickButton: Boolean,
    onDismiss: () -> Unit,
    onKick: () -> Unit
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
                // Foto Profil Besar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (contributor.photoUrl != null) {
                        AsyncImage(
                            model = contributor.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nama
                Text(
                    text = contributor.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    )
                )

                // Email
                Text(
                    text = contributor.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )

                // Role Badge
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (contributor.role == "owner") Color(0xFFFFCE74) else Color(0xFF77A4FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = contributor.role.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (showKickButton) {
                    LongPressKickButton(
                        onKickConfirmed = onKick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tombol Cancel
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

// --- KOMPONEN TOMBOL BARU (HOLD 4 DETIK) ---
@Composable
fun LongPressKickButton(
    onKickConfirmed: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var triggerAction by remember { mutableStateOf(false) }

    // Animasi progress bar:
    // Jika ditekan: Durasi 4000ms (4 detik)
    // Jika dilepas: Durasi 250ms (Reset cepat)
    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 4000, easing = LinearEasing)
        } else {
            tween(durationMillis = 250, easing = LinearEasing)
        },
        label = "KickProgress",
        finishedListener = { finalValue ->
            if (finalValue == 1f) {
                triggerAction = true
            }
        }
    )

    // Efek samping ketika progress mencapai 100%
    LaunchedEffect(triggerAction) {
        if (triggerAction) {
            onKickConfirmed()
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
                // Ubah teks sesuai status progress
                text = if (progress >= 1f) "Kicking User..." else "Hold to Kick (4s)",
                color = if (progress > 0.5f) Color.White else Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
        }
    }
}