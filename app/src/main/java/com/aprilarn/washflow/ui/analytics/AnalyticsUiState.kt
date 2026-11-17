package com.aprilarn.washflow.ui.analytics

import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services

data class AnalyticsUiState(
    val selectedTab: String = "Order",
    val isLoading: Boolean = false,

    // Data koleksi order dan services dari Firestore untuk halaman Report / Buku Besar
    val orders: List<Orders> = emptyList(),
    val services: List<Services> = emptyList(),
    val selectedMonth: String = "Semua Bulan",
    val searchQuery: String = "",
    val selectedStatusFilter: String = "Semua",
    val sortColumn: ReportSortColumn = ReportSortColumn.DATE,
    val isAscending: Boolean = false,
    val toastMessage: String? = null
)
