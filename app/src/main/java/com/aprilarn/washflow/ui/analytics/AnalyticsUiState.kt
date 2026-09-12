package com.aprilarn.washflow.ui.analytics

data class AnalyticsUiState(
    val selectedTab: String = "Order",
    val recentOrders: List<Transaction> = emptyList(),
    val upcomingDeadlines: List<UpcomingPayment> = emptyList(),
    val cards: List<CardInfo> = emptyList(),
    val isLoading: Boolean = false
)
