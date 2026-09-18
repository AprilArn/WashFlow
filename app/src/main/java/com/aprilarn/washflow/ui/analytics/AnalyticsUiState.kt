package com.aprilarn.washflow.ui.analytics

import com.aprilarn.washflow.data.model.Orders

data class AnalyticsUiState(
    val selectedTab: String = "Order",
    val recentOrders: List<Transaction> = emptyList(),
    val upcomingDeadlines: List<UpcomingPayment> = emptyList(),
    val cards: List<CardInfo> = emptyList(),
    val isLoading: Boolean = false,

    // Data koleksi order dari Firestore untuk halaman Report / Buku Besar
    val orders: List<Orders> = emptyList(),
    val selectedMonth: String = "Semua Bulan",
    val searchQuery: String = "",
    val selectedStatusFilter: String = "Semua",
    val sortColumn: ReportSortColumn = ReportSortColumn.DATE,
    val isAscending: Boolean = false
)
