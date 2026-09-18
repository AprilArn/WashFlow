package com.aprilarn.washflow.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                recentOrders = mockRecentOrders,
                upcomingDeadlines = mockUpcomingDeadlines,
                cards = mockCards
            )
        }
        listenForOrders()
    }

    private fun listenForOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.getOrdersRealtime()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { orders ->
                    _uiState.update {
                        it.copy(
                            orders = orders,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onTabSelected(tab: String) {
        _uiState.update {
            it.copy(selectedTab = tab)
        }
    }

    fun onMonthSelected(month: String) {
        _uiState.update {
            it.copy(selectedMonth = month)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
    }

    fun onStatusFilterChanged(status: String) {
        _uiState.update {
            it.copy(selectedStatusFilter = status)
        }
    }

    fun onSortColumnClicked(column: ReportSortColumn) {
        _uiState.update { state ->
            if (state.sortColumn == column) {
                state.copy(isAscending = !state.isAscending)
            } else {
                val defaultAsc = when (column) {
                    ReportSortColumn.DATE, ReportSortColumn.TIME, ReportSortColumn.TOTAL_PRICE -> false
                    else -> true
                }
                state.copy(sortColumn = column, isAscending = defaultAsc)
            }
        }
    }
}
