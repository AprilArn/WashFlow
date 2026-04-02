package com.aprilarn.washflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.login.UserData

@Composable
fun SettingsScreen(
    userData: UserData,
    settingsUiState: SettingsUiState,
    onSetLocationClicked: () -> Unit,
    onSignOut: () -> Unit
) {
    // Scroll state untuk bagian konten bawah
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally // Menengahkan konten (600.dp)
    ) {
        // --- BAGIAN ATAS (FIXED / TIDAK SCROLL) ---
        ProfileInfoCard(
            userData = userData,
            modifier = Modifier.width(600.dp) // Membatasi lebar sesuai request
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- BAGIAN BAWAH (SCROLLABLE) ---
        // weight(1f) memastikan bagian ini memenuhi sisa ruang layar ke bawah
        Column(
            modifier = Modifier
                .width(600.dp) // Membatasi lebar bagian list
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Jarak antar section
        ) {

            PreferencesSection(
                locationName = settingsUiState.locationName,
                onSetLocationClicked = onSetLocationClicked
            )

            AccountSection(onSignOut = onSignOut)
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
fun SettingsScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        SettingsScreen(
            userData = UserData(
                userId = "user_id_123",
                displayName = "Dianne Russell",
                email = "diannerussel@mail.com",
                profilePictureUrl = null
            ),
            settingsUiState = SettingsUiState(locationName = "Boyolali, Central Java", latitude = -7.797068, longitude = 110.370529),
            onSetLocationClicked = {},
            onSignOut = {}
        )
    }
}