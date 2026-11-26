package com.aprilarn.washflow.ui.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.domain.usecase.ManageCustomerUseCase
import com.aprilarn.washflow.domain.usecase.ManageItemUseCase
import com.aprilarn.washflow.domain.usecase.ManageOrderUseCase
import com.aprilarn.washflow.domain.usecase.ManageServiceUseCase
import com.aprilarn.washflow.ui.core.utils.UiText
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class OrdersViewModel(
    private val customerUseCase: ManageCustomerUseCase,
    private val serviceUseCase: ManageServiceUseCase,
    private val itemUseCase: ManageItemUseCase,
    private val orderUseCase: ManageOrderUseCase
) : ViewModel() {

    companion object {
        const val MIN_CUSTOMER_SEARCH_CHARS = 2
        private const val CUSTOMER_SEARCH_DEBOUNCE_MS = 1000L
    }

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState = _uiState.asStateFlow()

    private var hasLoadedCustomers = false
    private var customerSearchJob: Job? = null

    init {
        listenForDataChanges()
    }

    private fun listenForDataChanges() {
        viewModelScope.launch {
            val servicesFlow = serviceUseCase.observeServices()
            val itemsFlow = itemUseCase.observeItems()

            combine(servicesFlow, itemsFlow) { services, items ->
                _uiState.update {
                    it.copy(
                        services = services,
                        items = items,
                        // Set tab aktif pertama kali jika belum ada
                        activeServiceTabId = it.activeServiceTabId ?: services.firstOrNull()?.serviceId,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update {
                    it.copy(
                        errorMessage = UiText.StringResource(R.string.orders_msg_load_data_failed),
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun onCustomerQueryChanged(query: String) {
        val trimmed = query.trim()
        customerSearchJob?.cancel()

        if (trimmed.length < MIN_CUSTOMER_SEARCH_CHARS) {
            _uiState.update {
                it.copy(
                    customerSearchQuery = query,
                    debouncedCustomerQuery = "",
                    filteredCustomers = emptyList(),
                    isSearchingCustomers = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                customerSearchQuery = query,
                isSearchingCustomers = true
            )
        }

        customerSearchJob = viewModelScope.launch {
            delay(CUSTOMER_SEARCH_DEBOUNCE_MS.milliseconds)
            val sourceCustomers = if (hasLoadedCustomers) {
                _uiState.value.customers
            } else {
                val fetched = customerUseCase.getCustomersOnce()
                hasLoadedCustomers = true
                _uiState.update { it.copy(customers = fetched) }
                fetched
            }

            val matched = sourceCustomers.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                        (it.contact?.contains(trimmed, ignoreCase = true) == true)
            }

            _uiState.update {
                it.copy(
                    debouncedCustomerQuery = trimmed,
                    filteredCustomers = matched,
                    isSearchingCustomers = false
                )
            }
        }
    }

    fun onCustomerSelected(customer: Customers) {
        customerSearchJob?.cancel()
        _uiState.update {
            it.copy(
                selectedCustomer = customer,
                customerSearchQuery = customer.name, // Update text field juga
                debouncedCustomerQuery = "",
                filteredCustomers = emptyList(),
                isSearchingCustomers = false
            )
        }
    }

    fun onClearSelectedCustomer() {
        customerSearchJob?.cancel()
        _uiState.update {
            it.copy(
                selectedCustomer = null,
                customerSearchQuery = "",
                debouncedCustomerQuery = "",
                filteredCustomers = emptyList(),
                isSearchingCustomers = false
            )
        }
    }

    fun onDueDateChanged(timestamp: Timestamp?) {
        _uiState.update { it.copy(dueDate = timestamp) }
    }

    fun onServiceTabSelected(serviceId: String) {
        _uiState.update { it.copy(activeServiceTabId = serviceId) }
    }

    fun handleItemClick(item: Items) {
        val currentSelected = _uiState.value.selectedItems
        if (currentSelected.containsKey(item.itemId)) {
            // Jika sudah ada, hapus dari daftar
            removeSelectedItem(item.itemId)
        } else {
            // Jika belum ada, tampilkan dialog untuk input kuantitas
            _uiState.update { it.copy(itemForQuantityInput = item) }
        }
    }

    fun removeSelectedItem(itemId: String) {
        val currentSelected = _uiState.value.selectedItems
        if (!currentSelected.containsKey(itemId)) return
        val updatedMap = currentSelected.toMutableMap()
        updatedMap.remove(itemId)
        _uiState.update { it.copy(selectedItems = updatedMap) }
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
        customerSearchJob?.cancel()
        if (orderId == null) {
            // Mode Buat Baru
            _uiState.update {
                it.copy(
                    editingOrderId = null,
                    selectedCustomer = null,
                    customerSearchQuery = "",
                    debouncedCustomerQuery = "",
                    filteredCustomers = emptyList(),
                    isSearchingCustomers = false,
                    dueDate = null,
                    selectedItems = emptyMap()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val order = orderUseCase.getOrderById(orderId)
            if (order != null) {
                // Konversi list OrderItem ke map
                val itemsMap = order.orderItems.associateBy { it.itemId }
                val customer = _uiState.value.customers.find { it.customerId == order.customerId }
                    ?: Customers(order.customerId, order.customerName ?: "Unnamed")

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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiText.StringResource(R.string.orders_msg_load_order_failed)
                    )
                }
            }
        }
    }

    fun createOrder() {
        val state = _uiState.value
        if (state.selectedCustomer == null || state.selectedItems.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.orders_msg_incomplete))
            }
            return
        }

        if (state.dueDate == null) {
            _uiState.update { it.copy(showNoDeadlineWarning = true) }
            return
        }

        proceedCreateOrder()
    }

    fun dismissNoDeadlineWarning() {
        _uiState.update { it.copy(showNoDeadlineWarning = false) }
    }

    fun proceedCreateOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(showNoDeadlineWarning = false, isCreatingOrder = true) }
            val state = _uiState.value

            // Langsung ambil daftar OrderItem dari state. Tidak perlu .map lagi.
            val orderItems = state.selectedItems.values.toList()

            // Jumlahkan subtotal yang sudah ada di setiap OrderItem.
            val totalPrice = orderItems.sumOf { it.subtotal ?: 0.0 }

            if (state.editingOrderId != null) {
                // Update Order
                val existingOrder = orderUseCase.getOrderById(state.editingOrderId)
                if (existingOrder == null) {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            errorMessage = UiText.StringResource(R.string.orders_msg_existing_order_failed)
                        )
                    }
                    return@launch
                }
                
                val updatedOrder = existingOrder.copy(
                    customerId = state.selectedCustomer!!.customerId,
                    customerName = state.selectedCustomer.name,
                    orderDueDate = state.dueDate,
                    orderItems = orderItems,
                    totalPrice = totalPrice,
                    // Pastikan orderFinishDate diperbarui jika statusnya Done
                    orderFinishDate = if (existingOrder.status == "Done") Timestamp.now() else null
                )

                val success = orderUseCase.updateOrder(updatedOrder)
                if (success) {
                    customerSearchJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            successMessage = UiText.StringResource(R.string.orders_msg_updated),
                            editingOrderId = null,
                            customerSearchQuery = "",
                            debouncedCustomerQuery = "",
                            filteredCustomers = emptyList(),
                            isSearchingCustomers = false,
                            selectedCustomer = null,
                            dueDate = null,
                            selectedItems = emptyMap()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            errorMessage = UiText.StringResource(R.string.orders_msg_update_failed)
                        )
                    }
                }
            } else {
                // Create New Order
                val newOrder = Orders(
                    orderId = "", // Akan dibuat oleh repository
                    customerId = state.selectedCustomer!!.customerId,
                    customerName = state.selectedCustomer.name,
                    orderDate = Timestamp.now(),
                    orderDueDate = state.dueDate,
                    orderItems = orderItems,
                    totalPrice = totalPrice,
                    status = "On Queue",
                    alreadyPaid = false
                )

                val success = orderUseCase.createOrder(newOrder)
                if (success) {
                    // Reset state setelah order berhasil
                    customerSearchJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            successMessage = UiText.StringResource(R.string.orders_msg_created),
                            customerSearchQuery = "",
                            debouncedCustomerQuery = "",
                            filteredCustomers = emptyList(),
                            isSearchingCustomers = false,
                            selectedCustomer = null,
                            dueDate = null,
                            selectedItems = emptyMap()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            errorMessage = UiText.StringResource(R.string.orders_msg_create_failed)
                        )
                    }
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}