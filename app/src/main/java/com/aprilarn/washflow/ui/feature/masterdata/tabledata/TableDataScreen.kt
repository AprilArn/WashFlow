package com.aprilarn.washflow.ui.feature.masterdata.tabledata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.navigation.AppNavigation

@Composable
fun TableDataScreen(
    uiState: TableDataUiState,
    onNavigate: (route: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp) // Beri padding di atas dan bawah
    ) {
        // Kartu untuk Customers
        item {
            TableDataCard(
                text = stringResource(R.string.master_customers_title),
                subText = stringResource(R.string.table_subtitle_customers),
                count = uiState.customerCount,
                onClick = { onNavigate(AppNavigation.Customers.route) }
            )
        }

        // Kartu untuk Services
        item {
            TableDataCard(
                text = stringResource(R.string.master_services_title),
                subText = stringResource(R.string.table_subtitle_services),
                count = uiState.serviceCount,
                onClick = { onNavigate(AppNavigation.Services.route) }
            )
        }

        // Kartu untuk Items
        item {
            TableDataCard(
                text = stringResource(R.string.master_items_title),
                subText = stringResource(R.string.table_subtitle_items),
                count = uiState.itemCount,
                onClick = { onNavigate(AppNavigation.Items.route) }
            )
        }
        // Anda bisa menambahkan item lain di sini jika perlu
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 500)
@Composable
fun TableDataScreenPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        com.aprilarn.washflow.ui.core.components.WeatherBackgroundIllustration(
            weather = "Sunny",
            greeting = "Good Morning!",
            modifier = Modifier.matchParentSize()
        )
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