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

    fun setEditingOrder(orderId: String?) {
        if (orderId == null) {
            // Mode Buat Baru
            _uiState.update {
                it.copy(
                    editingOrderId = null,
                    selectedCustomer = null,
                    customerSearchQuery = "",
                    dueDate = null,
                    selectedItems = emptyMap()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val order = orderRepository.getOrderById(orderId)
            if (order != null) {
                // Konversi list OrderItem ke map
                val itemsMap = order.orderItems.associateBy { it.itemId }
                val customer = _uiState.value.customers.find { it.customerId == order.customerId }
                    ?: Customers(order.customerId, order.customerName ?: "Tanpa Nama")

                _uiState.update {
                    it.copy(
                        editingOrderId = order.orderId,
                        selectedCustomer = customer,
                        customerSearchQuery = customer.name,
                        dueDate = order.orderDueDate,
                        selectedItems = itemsMap,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load order.") }
            }
        }
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

            if (state.editingOrderId != null) {
                // Update Order
                val existingOrder = orderRepository.getOrderById(state.editingOrderId) ?: return@launch
                
                val updatedOrder = existingOrder.copy(
                    customerId = state.selectedCustomer.customerId,
                    customerName = state.selectedCustomer.name,
                    orderDueDate = state.dueDate,
                    orderItems = orderItems,
                    totalPrice = totalPrice,
                    // Pastikan orderFinishDate diperbarui jika statusnya Done
                    orderFinishDate = if (existingOrder.status == "Done") Timestamp.now() else null
                )

                val success = orderRepository.updateOrder(updatedOrder)
                if (success) {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            successMessage = "Order updated successfully!",
                            editingOrderId = null,
                            customerSearchQuery = "",
                            selectedCustomer = null,
                            dueDate = null,
                            selectedItems = emptyMap()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isCreatingOrder = false, errorMessage = "Failed to update order.")
                    }
                }
            } else {
                // Create New Order
                val newOrder = Orders(
                    orderId = "", // Akan dibuat oleh repository
                    customerId = state.selectedCustomer.customerId,
                    customerName = state.selectedCustomer.name,
                    orderDate = Timestamp.now(),
                    orderDueDate = state.dueDate,
                    orderItems = orderItems,
                    totalPrice = totalPrice,
                    status = "On Queue",
                    alreadyPaid = false
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
    }

    fun onMessageShown() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}