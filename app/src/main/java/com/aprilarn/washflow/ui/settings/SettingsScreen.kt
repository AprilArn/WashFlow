package com.aprilarn.washflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // State baru untuk mengontrol kemunculan dialog logout
    var showLogoutDialog by remember { mutableStateOf(false) }

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

            AccountSection(
                // 1. CEGAT KLIK LOGOUT: Ubah state menjadi true untuk memunculkan dialog
                onSignOut = { showLogoutDialog = true }
            )
        }
    }

    // --- DIALOG KONFIRMASI LOGOUT ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Konfirmasi Logout",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin keluar dari akun ini? Anda harus login kembali untuk mengakses workspace Anda.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // 2. JALANKAN LOGOUT: Panggil fungsi onSignOut asli dari MainActivity jika user menekan konfirmasi
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error // Warnai tombol konfirmasi dengan warna merah
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
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