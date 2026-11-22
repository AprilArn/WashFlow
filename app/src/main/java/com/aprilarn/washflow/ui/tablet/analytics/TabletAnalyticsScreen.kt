package com.aprilarn.washflow.ui.tablet.analytics

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.analytics.AnalyticsScreen
import com.aprilarn.washflow.ui.analytics.AnalyticsUiState
import com.aprilarn.washflow.ui.analytics.ReportSortColumn

/**
 * Tampilan Analytics Multi-Kolom dengan SidebarMenu vertikal di kiri khusus Tablet.
 */
@Composable
fun TabletAnalyticsScreen(
    uiState: AnalyticsUiState,
    onTabClick: (String) -> Unit,
    onMonthSelected: (String) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onStatusFilterChanged: (String) -> Unit = {},
    onSortColumnClicked: (ReportSortColumn) -> Unit = {},
    onLoadMoreMonths: () -> Unit = {},
    onTogglePayment: (String, Boolean) -> Unit = { _, _ -> },
    onTogglePickup: (String, Boolean) -> Unit = { _, _ -> },
    onToastShown: () -> Unit = {}
) {
    AnalyticsScreen(
        uiState = uiState,
        onTabClick = onTabClick,
        onMonthSelected = onMonthSelected,
        onSearchQueryChanged = onSearchQueryChanged,
        onStatusFilterChanged = onStatusFilterChanged,
        onSortColumnClicked = onSortColumnClicked,
        onLoadMoreMonths = onLoadMoreMonths,
        onTogglePayment = onTogglePayment,
        onTogglePickup = onTogglePickup,
        onToastShown = onToastShown
    )
}
