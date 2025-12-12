// File: ui/components/WashFlowTopBar.kt (Updated)

package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Komponen Top Bar yang menampilkan judul aplikasi "WashFlow".
 */
@Composable
fun Header(
    modifier: Modifier = Modifier,
    navController: NavController,
    workspaceName: String,
    onWorkspaceClick: () -> Unit,
    dropdownContent: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // 1. Judul WashFlow
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

        // Jarak antara Judul dan Navigasi
        Spacer(modifier = Modifier.width(32.dp))

        // 2. Navigation Bar
        NavigationBar(
            navController = navController,
            modifier = Modifier.height(44.dp) // Tinggi yang pas untuk header
        )

        // Jarak antara Navigasi dan Workspace
        Spacer(modifier=Modifier.weight(1f)) // Spacer fleksibel untuk mendorong item ke ujung kanan

        // 3. Nama Workspace & Dropdown
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onWorkspaceClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                    text = workspaceName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                    )
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Workspace Options",
                    tint = Color.White
                )
            }

            // --- KONTEN DROPDOWN AKAN DIRENDER DI SINI ---
            // Posisinya akan relatif terhadap Box di atas
            dropdownContent()
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
    Header(
        workspaceName = "Example",
        navController = rememberNavController(),
        onWorkspaceClick = {},
        dropdownContent = {}
    )
}