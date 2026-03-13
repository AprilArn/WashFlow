package com.aprilarn.washflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.theme.MainFontBlack


@Composable
fun SettingsScreen(
    userData: UserData,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp), // Memberikan jarak dari tepi layar
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Card melengkung (Rounded)
        Card(
            modifier = Modifier.size(600.dp, 100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 24.dp), // Jarak konten di dalam Card
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Memisahkan Kiri (Teks) dan Kanan (Gambar)
            ) {

                // Bagian Kiri: Nama dan Email
                Column(
                    modifier = Modifier.weight(1f) // weight agar teks panjang tidak menabrak foto
                ) {
                    Text(
                        text = userData.displayName ?: "Nama Tidak Tersedia",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MainFontBlack,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userData.email ?: "Email Tidak Tersedia",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        ),

                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Bagian Kanan: Foto Profil
                if (userData.profilePictureUrl != null) {
                    AsyncImage(
                        model = userData.profilePictureUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 600)
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
                displayName = "John",
                email = "john.mckinley@examplepetstore.com",
                profilePictureUrl = null // Kosongkan atau isi dengan link gambar dummy
            ),
            onSignOut = {} // Fungsi kosong untuk preview
        )
    }
}