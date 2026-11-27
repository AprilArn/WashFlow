package com.aprilarn.washflow.ui.feature.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.core.utils.UiText
import com.aprilarn.washflow.ui.feature.login.UserData

/**
 * Tampilan Settings khusus HP biasa (Batang / Portrait) dengan lebar penuh responsif.
 */
@Composable
fun PhoneSettingsScreen(
    userData: UserData,
    settingsUiState: SettingsUiState,
    onSetLocationClicked: () -> Unit,
    onSignOut: () -> Unit,
    workspaceName: String? = null,
    isOwner: Boolean? = null
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE) }
    var isSoundEnabled by rememberSaveable { mutableStateOf(sharedPrefs.getBoolean("SOUND_ENABLED", true)) }
    val isAutoLocationMode = remember(settingsUiState.isAutoLocationMode, settingsUiState.hasFetchedLocation) {
        if (settingsUiState.hasFetchedLocation) {
            settingsUiState.isAutoLocationMode
        } else {
            sharedPrefs.getString("LOCATION_MODE", "AUTO") != "STATIC"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileInfoCard(
            userData = userData,
            workspaceName = workspaceName,
            isOwner = isOwner,
            modifier = Modifier.fillMaxWidth()
        )

        PreferencesSection(
            locationName = settingsUiState.locationName.asString(),
            isAutoLocationMode = isAutoLocationMode,
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

@Preview(name = "Phone Settings", device = Devices.PHONE, showBackground = true)
@Composable
private fun PhoneSettingsScreenPreview() {
    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
    ) {
        PhoneSettingsScreen(
            userData = UserData(
                userId = "user_id_123",
                displayName = "Dianne Russell",
                email = "diannerussel@mail.com",
                profilePictureUrl = null
            ),
            settingsUiState = SettingsUiState(
                locationName = UiText.DynamicString("Boyolali, Central Java"),
                hasFetchedLocation = true,
                isAutoLocationMode = false,
                latitude = -7.797068,
                longitude = 110.370529
            ),
            workspaceName = "WashFlow Express",
            isOwner = false,
            onSetLocationClicked = {},
            onSignOut = {}
        )
    }
}
