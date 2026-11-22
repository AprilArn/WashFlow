package com.aprilarn.washflow.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aprilarn.washflow.ui.components.AddNewDataInputField
import com.aprilarn.washflow.ui.components.AddNewDataPanel
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.components.EditDataPanel
import com.aprilarn.washflow.ui.components.ExitConfirmationDialog
import com.aprilarn.washflow.ui.components.KickedDialog
import com.aprilarn.washflow.ui.components.LeaveWorkspaceDialog
import com.aprilarn.washflow.ui.components.NoDeadlineConfirmationDialog
import com.aprilarn.washflow.ui.components.OfflineBanner
import com.aprilarn.washflow.ui.components.WeatherBackgroundIllustration
import com.aprilarn.washflow.ui.components.isDarkWeatherScene
import com.aprilarn.washflow.ui.components.rememberConnectivityState
import com.example.app.ui.components.StatusCard

/**
 * Shared UI Layer (ui/common)
 * Menyediakan komponen UI yang dipakai bersama oleh tampilan Phone (ui/phone) maupun Tablet (ui/tablet).
 */
@Composable
fun SharedWeatherBackground(
    weather: String,
    greeting: String = "",
    weatherType: String = "",
    isDaytime: Boolean? = null,
    modifier: Modifier = Modifier
) {
    WeatherBackgroundIllustration(
        weather = weather,
        greeting = greeting,
        weatherType = weatherType,
        isDaytime = isDaytime,
        modifier = modifier
    )
}

fun isSharedDarkWeatherScene(
    weatherText: String,
    greetingText: String,
    weatherType: String = "",
    isDaytime: Boolean? = null
): Boolean = isDarkWeatherScene(
    weatherText = weatherText,
    greetingText = greetingText,
    weatherType = weatherType,
    isDaytime = isDaytime
)

@Composable
fun SharedStatusCard(
    text: String,
    count: Int,
    onClick: () -> Unit
) {
    StatusCard(text = text, count = count, onClick = onClick)
}
