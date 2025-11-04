// com/aprilarn/washflow/ui/tabledata/TableDataScreen.kt
package com.aprilarn.washflow.ui.tabledata

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.AppNavigation

@Composable
fun TableDataScreen(
    uiState: TableDataUiState,
    onNavigate: (route: String) -> Unit // Callback untuk navigasi
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp) // Beri padding di atas dan bawah
    ) {
        // Kartu untuk Customers
        item {
            // Hapus Box pembungkus, panggil Card langsung
            TableDataCard(
                text = "Customers",
                subText = "Pelanggan",
                count = uiState.customerCount,
                onClick = { onNavigate(AppNavigation.Customers.route) }
            )
        }

        // Kartu untuk Services
        item {
            TableDataCard(
                text = "Services",
                subText = "Layanan",
                count = uiState.serviceCount,
                onClick = { onNavigate(AppNavigation.Services.route) }
            )
        }

        // Kartu untuk Items
        item {
            TableDataCard(
                text = "Items",
                subText = "Barang",
                count = uiState.itemCount,
                onClick = { onNavigate(AppNavigation.Items.route) }
            )
        }
        // Anda bisa menambahkan item lain di sini jika perlu
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 500)
@Composable
fun PreviewTableDataScreen() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        TableDataScreen(
            uiState = TableDataUiState(
                customerCount = 12,
                serviceCount = 5,
                itemCount = 34,
                isLoading = false
            ),
            onNavigate = {}
        )
    }
}