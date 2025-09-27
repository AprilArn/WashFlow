package com.aprilarn.washflow.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val customerRepository: CustomerRepository,
    private val serviceRepository: ServiceRepository,
    private val itemRepository: ItemRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForDataChanges()
    }

    private fun listenForDataChanges() {
        viewModelScope.launch {
            val customersFlow = customerRepository.getCustomersRealtime()
            val servicesFlow = serviceRepository.getServicesRealtime()
            val itemsFlow = itemRepository.getItemsRealtime()

            combine(customersFlow, servicesFlow, itemsFlow) { customers, services, items ->
                _uiState.update {
                    it.copy(
                        customers = customers,
                        services = services,
                        items = items,
                        // Set tab aktif pertama kali jika belum ada
                        activeServiceTabId = it.activeServiceTabId ?: services.firstOrNull()?.serviceId,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(errorMessage = "Failed to load data.", isLoading = false) }
            }.collect {}
        }
    }

    fun onCustomerQueryChanged(query: String) {
        _uiState.update { it.copy(customerSearchQuery = query) }
    }

    fun onCustomerSelected(customer: Customers) {
        _uiState.update {
            it.copy(
                selectedCustomer = customer,
                customerSearchQuery = customer.name // Update text field juga
            )
        }
    }

    fun onDueDateChanged(timestamp: Timestamp) {
        _uiState.update { it.copy(dueDate = timestamp) }
    }

    fun onServiceTabSelected(serviceId: String) {
        _uiState.update { it.copy(activeServiceTabId = serviceId) }
    }

    fun handleItemClick(item: Items) {
        val currentSelected = _uiState.value.selectedItems
        if (currentSelected.containsKey(item.itemId)) {
            // Jika sudah ada, hapus dari daftar
            val updatedMap = currentSelected.toMutableMap()
            updatedMap.remove(item.itemId)
            _uiState.update { it.copy(selectedItems = updatedMap) }
        } else {
            // Jika belum ada, tampilkan dialog untuk input kuantitas
            _uiState.update { it.copy(itemForQuantityInput = item) }
        }
    }

    fun onQuantityConfirmed(quantity: Int) {
        val item = _uiState.value.itemForQuantityInput ?: return
        if (quantity <= 0) {
            onDismissQuantityDialog()
            return
        }

        val subtotal = item.itemPrice * quantity
        val orderItem = OrderItem(
            itemId = item.itemId,
            itemName = item.itemName,
            itemPrice = item.itemPrice,
            serviceId = item.serviceId,
            itemQuantity = quantity,
            subtotal = subtotal
        )

        val updatedMap = _uiState.value.selectedItems.toMutableMap()
        updatedMap[item.itemId] = orderItem

        _uiState.update {
            it.copy(
                selectedItems = updatedMap,
                itemForQuantityInput = null // Tutup dialog
            )
        }
    }

    fun onDismissQuantityDialog() {
        _uiState.update { it.copy(itemForQuantityInput = null) }
    }

    fun createOrder() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedCustomer == null || state.selectedItems.isEmpty() || state.dueDate == null) {
                _uiState.update { it.copy(errorMessage = "Please complete the order details.") }
                return@launch
            }

            _uiState.update { it.copy(isCreatingOrder = true) }

            // --- PERBAIKAN DI SINI ---
            // Langsung ambil daftar OrderItem dari state. Tidak perlu .map lagi.
            val orderItems = state.selectedItems.values.toList()

            // Jumlahkan subtotal yang sudah ada di setiap OrderItem.
            val totalPrice = orderItems.sumOf { it.subtotal ?: 0.0 }

            val newOrder = Orders(
                orderId = "", // Akan dibuat oleh repository
                customerId = state.selectedCustomer.customerId,
                customerName = state.selectedCustomer.name,
                orderDate = Timestamp.now(),
                orderDueDate = state.dueDate,
                orderItems = orderItems, // Gunakan list yang sudah benar
                totalPrice = totalPrice, // Gunakan total harga yang sudah benar
                status = "On Queue"
            )

            val success = orderRepository.createOrder(newOrder)
            if (success) {
                // Reset state setelah order berhasil
                _uiState.update {
                    it.copy(
                        isCreatingOrder = false,
                        successMessage = "Order created successfully!",
                        customerSearchQuery = "",
                        selectedCustomer = null,
                        dueDate = null,
                        selectedItems = emptyMap()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isCreatingOrder = false, errorMessage = "Failed to create order.")
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}