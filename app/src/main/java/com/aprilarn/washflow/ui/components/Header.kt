// File: ui/components/WashFlowTopBar.kt (Updated)

package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Komponen Top Bar yang menampilkan judul aplikasi "WashFlow".
 */
@Composable
fun Header(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth() // Memastikan Row memenuhi lebar layar
            // Padding agar tidak terlalu mepet ke tepi layar
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Mendorong item ke ujung kiri dan kanan
    ) {
        // Kelompokkan teks dalam satu Row agar tetap bersama
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Wash",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = GrayBlue,
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Normal,
                ),
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = "Flow",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic
                ),
                fontWeight = FontWeight.Normal,
            )
        }

        // icon notification
        // Ikon notifikasi sekarang menjadi sebuah tombol
        IconButton(onClick = { /* TODO: Tambahkan aksi saat tombol diklik di sini */ }) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifikasi", // Penting untuk aksesibilitas
                tint = Color.White // Warna ikon
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun HeaderPreview() {
    Header()
}