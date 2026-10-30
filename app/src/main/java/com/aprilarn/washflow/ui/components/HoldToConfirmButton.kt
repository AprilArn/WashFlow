package com.aprilarn.washflow.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HoldToConfirmButton(
    modifier: Modifier = Modifier,
    defaultText: String,
    progressText: String,
    durationMillis: Int = 1500,
    containerColor: Color = Color(0xFFFFEBEE), // Default: merah sangat muda
    progressColor: Color = Color(0xFFD32F2F),  // Default: merah gelap
    defaultTextColor: Color = Color(0xFFD32F2F), // Default: merah
    progressTextColor: Color = Color.White,      // Default: putih
    onConfirm: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var triggerAction by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) {
            tween(
                durationMillis = durationMillis,
                easing = androidx.compose.animation.core.LinearOutSlowInEasing // Cepat di awal, semakin ke kanan semakin melambat
            )
        } else {
            tween(
                durationMillis = 250,
                easing = LinearEasing
            )
        },
        label = "HoldToConfirmProgress",
        finishedListener = { finalValue ->
            if (finalValue == 1f) {
                triggerAction = true
            }
        }
    )

    // Efek samping ketika progress mencapai 100%
    LaunchedEffect(triggerAction) {
        if (triggerAction) {
            onConfirm()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
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
        // Progress Fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .background(progressColor)
        )

        // Teks Label (di tengah)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (progress >= 1f) progressText else defaultText,
                color = if (progress > 0.5f) progressTextColor else defaultTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
