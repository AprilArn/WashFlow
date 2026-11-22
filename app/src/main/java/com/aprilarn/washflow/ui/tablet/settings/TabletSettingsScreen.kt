package com.aprilarn.washflow.ui.tablet.settings

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.settings.SettingsScreen
import com.aprilarn.washflow.ui.settings.SettingsUiState

/**
 * Tampilan Settings khusus Tablet (kontainer 600.dp di tengah).
 */
@Composable
fun TabletSettingsScreen(
    userData: UserData,
    settingsUiState: SettingsUiState,
    onSetLocationClicked: () -> Unit,
    onSignOut: () -> Unit
) {
    SettingsScreen(
        userData = userData,
        settingsUiState = settingsUiState,
        onSetLocationClicked = onSetLocationClicked,
        onSignOut = onSignOut
    )
}
