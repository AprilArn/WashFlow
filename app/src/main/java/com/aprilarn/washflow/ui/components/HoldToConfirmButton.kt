package com.aprilarn.washflow.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun HoldToConfirmButton(
    modifier: Modifier = Modifier,
    defaultText: String,
    progressText: String,
    durationMillis: Int = 1500,
    cornerRadius: Dp = 12.dp,
    containerColor: Color = Color(0xFFFFEBEE), // Default: merah sangat muda
    progressColor: Color = Color(0xFFD32F2F),  // Default: merah gelap
    defaultTextColor: Color = Color(0xFFD32F2F), // Default: merah
    progressTextColor: Color = Color.White,      // Default: putih
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current

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
                durationMillis = 300,
                easing = androidx.compose.animation.core.FastOutLinearInEasing // Animasi mundur ke kiri: dari lambat ke cepat (Ease-In)
            )
        },
        label = "HoldToConfirmProgress",
        finishedListener = { finalValue ->
            if (finalValue == 1f) {
                triggerAction = true
            }
        }
    )

    // Efek getaran bertahap (semakin ke kanan progress, getaran semakin rapat/banyak)
    LaunchedEffect(isPressed) {
        if (isPressed) {
            performLightHaptic(view, context)

            while (isPressed && (progress < 1f)) {
                val currentProgress = progress
                // Interval berkurang dari ~160ms (awal) sampai ~30ms (mendekati 100%)
                val delayMs = (160f - (currentProgress * 130f)).toLong().coerceAtLeast(30L)
                delay(delayMs)

                if (isPressed) {
                    performLightHaptic(view, context)
                }
            }
        }
    }

    // Efek hentakan keras 1x ketika progress mencapai 100%
    LaunchedEffect(triggerAction) {
        if (triggerAction) {
            performHeavyHaptic(view, context)
            onConfirm()
        }
    }

    val buttonShape = RoundedCornerShape(cornerRadius)

    // Bentuk dinamis untuk progress fill:
    // - Sisi kiri (topStart & bottomStart) SELALU tetap cornerRadius (12.dp) agar selalu pas/presisi dengan bingkai kiri container.
    // - Sisi kanan (topEnd & bottomEnd) bergerak dinamis dari 25.dp (kapsul bulat di awal) memudar hingga 0.dp saat 100%.
    val maxEndRadius = 25.dp
    val dynamicEndRadius = maxEndRadius * (1f - progress)

    val progressShape = RoundedCornerShape(
        topStart = cornerRadius,
        bottomStart = cornerRadius,
        topEnd = dynamicEndRadius,
        bottomEnd = dynamicEndRadius
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(buttonShape)
            .background(containerColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        triggerAction = false
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false // Reset jika dilepas sebelum selesai
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Progress Fill (dengan kelengkungan kanan yang berkurang secara dinamis saat bergerak ke kanan)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .clip(progressShape)
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

private fun performLightHaptic(view: View, context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                return
            }
        }
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun performHeavyHaptic(view: View, context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun HoldToConfirmButtonStatesPreview() {
    val buttonShape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Progress 0% (Idle)", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(buttonShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Text("Hold to Delete (1s)", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Progress 5% (Just Starting - Flush Left, Capsule Right)", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        val p05 = 0.05f
        val shape05 = RoundedCornerShape(
            topStart = 12.dp,
            bottomStart = 12.dp,
            topEnd = 25.dp * (1f - p05),
            bottomEnd = 25.dp * (1f - p05)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(buttonShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(p05)
                    .clip(shape05)
                    .background(Color(0xFFD32F2F))
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Hold to Delete (1s)", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Progress 50% (Transitioning)", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        val p50 = 0.5f
        val shape50 = RoundedCornerShape(
            topStart = 12.dp,
            bottomStart = 12.dp,
            topEnd = 25.dp * (1f - p50),
            bottomEnd = 25.dp * (1f - p50)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(buttonShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(p50)
                    .clip(shape50)
                    .background(Color(0xFFD32F2F))
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Hold to Delete (1s)", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Progress 100% (Confirmed)", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        val p100 = 1.0f
        val shape100 = RoundedCornerShape(
            topStart = 12.dp,
            bottomStart = 12.dp,
            topEnd = 25.dp * (1f - p100),
            bottomEnd = 25.dp * (1f - p100)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(buttonShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(p100)
                    .clip(shape100)
                    .background(Color(0xFFD32F2F))
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Deleting...", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
