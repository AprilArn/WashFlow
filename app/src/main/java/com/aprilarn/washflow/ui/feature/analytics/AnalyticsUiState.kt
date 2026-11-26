package com.aprilarn.washflow.ui.feature.analytics

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.utils.UiText

data class AnalyticsUiState(
    val selectedTab: String = "Order",
    val isLoading: Boolean = false,

    // Order & service collections from Firestore for Ledger / Report screen
    val orders: List<Orders> = emptyList(),
    // All active orders (archived == false) across months for Pending tab
    val activeOrders: List<Orders> = emptyList(),
    val customers: List<Customers> = emptyList(),
    val services: List<Services> = emptyList(),
    val selectedMonth: String = ANALYTICS_ALL_MONTHS_KEY,
    val searchQuery: String = "",
    val selectedStatusFilter: String = ANALYTICS_ALL_STATUS_KEY,
    val sortColumn: ReportSortColumn = ReportSortColumn.DATE,
    val isAscending: Boolean = false,
    val loadedMonthsCount: Int = 3,
    val toastMessage: UiText? = null
)
