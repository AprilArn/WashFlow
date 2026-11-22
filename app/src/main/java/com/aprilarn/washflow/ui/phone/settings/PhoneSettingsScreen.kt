package com.aprilarn.washflow.ui.phone.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.components.LogoutConfirmationDialog
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.settings.AccountSection
import com.aprilarn.washflow.ui.settings.PreferencesSection
import com.aprilarn.washflow.ui.settings.ProfileInfoCard
import com.aprilarn.washflow.ui.settings.SettingsUiState

/**
 * Tampilan Settings khusus HP biasa (Batang / Portrait) dengan lebar penuh responsif.
 */
@Composable
fun PhoneSettingsScreen(
    userData: UserData,
    settingsUiState: SettingsUiState,
    onSetLocationClicked: () -> Unit,
    onSignOut: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE) }
    var isSoundEnabled by rememberSaveable { mutableStateOf(sharedPrefs.getBoolean("SOUND_ENABLED", true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileInfoCard(
            userData = userData,
            modifier = Modifier.fillMaxWidth()
        )

        PreferencesSection(
            locationName = settingsUiState.locationName,
            onSetLocationClicked = onSetLocationClicked,
            isSoundEnabled = isSoundEnabled,
            onSoundToggled = { newValue ->
                isSoundEnabled = newValue
                sharedPrefs.edit().putBoolean("SOUND_ENABLED", newValue).apply()
            }
        )

        AccountSection(
            onSignOut = { showLogoutDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onLogoutConfirmed = {
                showLogoutDialog = false
                onSignOut()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
