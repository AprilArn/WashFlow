package com.aprilarn.washflow.ui.analytics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Inisialisasi dengan data mock untuk saat ini, serupa dengan pola di halaman lain
        // Jika nanti ada Repository, data ini akan diambil dari sana.
        _uiState.update {
            it.copy(
                recentOrders = mockRecentOrders,
                upcomingDeadlines = mockUpcomingDeadlines,
                cards = mockCards
            )
        }
    }

    fun onTabSelected(tab: String) {
        _uiState.update {
            it.copy(selectedTab = tab)
        }
    }
}
