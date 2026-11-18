package com.aprilarn.washflow.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val serviceRepository: ServiceRepository = ServiceRepository(),
    private val customerRepository: CustomerRepository = CustomerRepository()
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

            val ordersFlow = orderRepository.getOrdersRealtime(startDate = startDate)
            val servicesFlow = serviceRepository.getServicesRealtime()

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
            val activeOrdersFlow = orderRepository.getOrdersRealtime(activeOnly = true)
            val customersFlow = customerRepository.getCustomersRealtime()

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

            val hasMore = orderRepository.hasOrdersBefore(currentStartDate)
            if (hasMore) {
                currentMonthsBack += 1
                _uiState.update { it.copy(loadedMonthsCount = currentMonthsBack) }
                listenForData()
            } else {
                _uiState.update { it.copy(toastMessage = "Tidak ada data order yang lebih lama") }
            }
        }
    }

    fun toggleOrderPaymentStatus(orderId: String, isPaid: Boolean) {
        viewModelScope.launch {
            val success = orderRepository.updateOrderPaymentStatus(orderId, isPaid)
            _uiState.update {
                it.copy(
                    toastMessage = if (success) {
                        if (isPaid) "Pembayaran ditandai lunas" else "Status pembayaran diubah ke belum lunas"
                    } else {
                        "Gagal memperbarui status pembayaran"
                    }
                )
            }
        }
    }

    fun toggleOrderPickupStatus(orderId: String, isPickedUp: Boolean) {
        viewModelScope.launch {
            val success = orderRepository.updateOrderPickupStatus(orderId, isPickedUp)
            _uiState.update {
                it.copy(
                    toastMessage = if (success) {
                        if (isPickedUp) "Order ditandai sudah diambil" else "Status pengambilan dibatalkan"
                    } else {
                        "Gagal memperbarui status pengambilan"
                    }
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
