package com.aprilarn.washflow.ui.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.R
import com.aprilarn.washflow.domain.usecase.ManageCustomerUseCase
import com.aprilarn.washflow.domain.usecase.ManageOrderUseCase
import com.aprilarn.washflow.domain.usecase.ManageServiceUseCase
import com.aprilarn.washflow.ui.core.utils.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val orderUseCase: ManageOrderUseCase,
    private val serviceUseCase: ManageServiceUseCase,
    private val customerUseCase: ManageCustomerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    private var listenJob: kotlinx.coroutines.Job? = null
    private var currentMonthsBack = 3

    init {
        listenForData()
        listenForActiveAndReferenceData()
    }

    private fun listenForData() {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Mulai dari tanggal 1 bulan ke-N ke belakang agar bulan pembanding (M-2 dst.) selalu utuh
            val calendar = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MONTH, -currentMonthsBack)
                set(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startDate = com.google.firebase.Timestamp(calendar.time)

            val ordersFlow = orderUseCase.observeOrders(startDate = startDate)
            val servicesFlow = serviceUseCase.observeServices()

            combine(ordersFlow, servicesFlow) { orders, services ->
                _uiState.update {
                    it.copy(
                        orders = orders,
                        services = services,
                        loadedMonthsCount = currentMonthsBack,
                        isLoading = false
                    )
                }
            }.catch {
                _uiState.update { it.copy(isLoading = false) }
            }.collect {}
        }
    }

    private fun listenForActiveAndReferenceData() {
        viewModelScope.launch {
            val activeOrdersFlow = orderUseCase.observeOrders(activeOnly = true)
            val customersFlow = customerUseCase.observeCustomers()

            combine(activeOrdersFlow, customersFlow) { activeOrders, customers ->
                _uiState.update {
                    it.copy(
                        activeOrders = activeOrders,
                        customers = customers
                    )
                }
            }.catch {
                // Abaikan error sekunder agar tidak memblokir data utama
            }.collect {}
        }
    }

    fun loadMoreMonths() {
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MONTH, -currentMonthsBack)
                set(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val currentStartDate = com.google.firebase.Timestamp(calendar.time)

            val hasMore = orderUseCase.hasOrdersBefore(currentStartDate)
            if (hasMore) {
                currentMonthsBack += 1
                _uiState.update { it.copy(loadedMonthsCount = currentMonthsBack) }
                listenForData()
            } else {
                _uiState.update {
                    it.copy(toastMessage = UiText.StringResource(R.string.analytics_msg_no_older_orders))
                }
            }
        }
    }

    fun toggleOrderPaymentStatus(orderId: String, isPaid: Boolean) {
        viewModelScope.launch {
            val success = orderUseCase.togglePayment(orderId, isPaid)
            _uiState.update {
                it.copy(
                    toastMessage = UiText.StringResource(
                        if (success) {
                            if (isPaid) R.string.analytics_msg_paid_marked
                            else R.string.analytics_msg_unpaid_marked
                        } else {
                            R.string.analytics_msg_payment_update_failed
                        }
                    )
                )
            }
        }
    }

    fun toggleOrderPickupStatus(orderId: String, isPickedUp: Boolean) {
        viewModelScope.launch {
            val success = orderUseCase.togglePickup(orderId, isPickedUp)
            _uiState.update {
                it.copy(
                    toastMessage = UiText.StringResource(
                        if (success) {
                            if (isPickedUp) R.string.analytics_msg_picked_up_marked
                            else R.string.analytics_msg_pickup_reverted
                        } else {
                            R.string.analytics_msg_pickup_update_failed
                        }
                    )
                )
            }
        }
    }

    fun onToastShown() {
        _uiState.update { it.copy(toastMessage = null) }
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
