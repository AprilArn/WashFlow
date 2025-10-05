// com/aprilarn/washflow/ui/manage_order/ManageOrderViewModel.kt
package com.aprilarn.washflow.ui.manage_order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageOrderViewModel(
    private val orderRepository: OrderRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageOrderUiState())
    val uiState = _uiState.asStateFlow()

    init {
//        listenForOrders()
        listenForDataChanges()
    }

    private fun listenForDataChanges() {
//        viewModelScope.launch {
//            Log.d("ManageOrderVM", "Starting to listen for real-time order updates.")
//            orderRepository.getOrdersRealtime()
//                .catch { e ->
//                    Log.e("ManageOrderVM", "Error listening for orders", e)
//                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
//                }
//                .collect { orders ->
//                    Log.d("ManageOrderVM", "Received ${orders.size} orders from Firestore.")
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            ordersOnQueue = orders.filter { o -> o.status == "On Queue" },
//                            ordersOnProcess = orders.filter { o -> o.status == "On Process" },
//                            ordersDone = orders.filter { o -> o.status == "Done" }
//                        )
//                    }
//                    Log.d("ManageOrderVM", "UI state updated with new order lists.")
//                }
//        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val ordersFlow = orderRepository.getOrdersRealtime()
            val servicesFlow = serviceRepository.getServicesRealtime()

            // Gabungkan data orders dan services secara real-time
            combine(ordersFlow, servicesFlow) { orders, services ->
                val groupedOrders = orders.groupBy { it.status }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        ordersOnQueue = groupedOrders["On Queue"] ?: emptyList(),
                        ordersOnProcess = groupedOrders["On Process"] ?: emptyList(),
                        ordersDone = groupedOrders["Done"] ?: emptyList(),
                        services = services // Simpan daftar services ke state
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }.collect {}
        }
    }

    fun changeOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            // 1. Dapatkan status saat ini untuk mencegah pembaruan yang tidak perlu
            val currentState = _uiState.value
            val allOrders = currentState.ordersOnQueue + currentState.ordersOnProcess + currentState.ordersDone
            val orderToUpdate = allOrders.find { it.orderId == orderId }

            // 2. Jangan lakukan apa-apa jika status tidak berubah
            if (orderToUpdate?.status == newStatus) {
                Log.d("ManageOrderVM", "Order status is already '$newStatus'. No update needed.")
                return@launch
            }

            Log.d("ManageOrderVM", "Attempting to change order '$orderId' to status '$newStatus'.")

            // 3. HAPUS SEMUA LOGIKA OPTIMISTIC UPDATE.
            //    Langsung panggil repository untuk mengubah data di Firestore.
            //    Listener real-time akan secara otomatis menangani pembaruan UI.
            val success = orderRepository.updateOrderStatus(orderId, newStatus)

            if (success) {
                Log.d("ManageOrderVM", "Successfully requested status update for order '$orderId'. Waiting for listener to reflect changes.")
            } else {
                Log.e("ManageOrderVM", "Failed to update status for order '$orderId'.")
                // Tampilkan pesan error. UI akan otomatis sinkron kembali dengan
                // data server yang lama pada pembaruan listener berikutnya.
                _uiState.update { it.copy(errorMessage = "Failed to update order status.") }
            }
        }
    }

    fun onOrderCardClicked(order: Orders) {
        _uiState.update { it.copy(selectedOrderForDetail = order) }
    }

    fun onDismissOrderDetailDialog() {
        _uiState.update { it.copy(selectedOrderForDetail = null) }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            val success = orderRepository.deleteOrder(orderId)
            if (success) {
                // Tutup dialog setelah berhasil dihapus
                _uiState.update { it.copy(selectedOrderForDetail = null) }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to delete order.") }
            }
        }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}