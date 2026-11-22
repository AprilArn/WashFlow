package com.aprilarn.washflow.ui.tablet.home

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.home.HomeScreen
import com.aprilarn.washflow.ui.home.HomeUiState
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.home.LocationAwareHomePage

/**
 * Tampilan Home 2-Panel khusus Tablet (Weather Details Panel di kiri + Main Dashboard di kanan).
 */
@Composable
fun TabletLocationAwareHomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToManageOrder: () -> Unit,
    onLocationFetched: (Double, Double) -> Unit
) {
    LocationAwareHomePage(
        homeViewModel = homeViewModel,
        onNavigateToManageOrder = onNavigateToManageOrder,
        onLocationFetched = onLocationFetched
    )
}

@Composable
fun TabletHomeScreen(
    state: HomeUiState,
    onEnterDataClick: () -> Unit,
    onStatusCardClick: () -> Unit
) {
    HomeScreen(
        state = state,
        onEnterDataClick = onEnterDataClick,
        onStatusCardClick = onStatusCardClick
    )
}
