package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Wrapper terpusat untuk Material 3 [Switch] yang otomatis memberikan
 * haptic feedback (`ToggleOn` / `ToggleOff`) saat status toggle berubah.
 */
@Composable
fun HapticSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource? = null
) {
    val haptic = LocalHapticFeedback.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange?.let { callback ->
            { newChecked ->
                haptic.performHapticFeedback(
                    if (newChecked) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                )
                callback(newChecked)
            }
        },
        modifier = modifier,
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}
