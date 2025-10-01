package com.aprilarn.washflow.ui.orders

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Services
import com.google.firebase.Timestamp

data class OrdersUiState(
    // Data dari Firestore
    val customers: List<Customers> = emptyList(),
    val services: List<Services> = emptyList(),
    val items: List<Items> = emptyList(),

    // State untuk interaksi UI
    val customerSearchQuery: String = "",
    val selectedCustomer: Customers? = null,
    val dueDate: Timestamp? = null,
    val activeServiceTabId: String? = null,
    // val selectedItems: Map<String, Items> = emptyMap(), // Map<ItemId, Item>
    val selectedItems: Map<String, OrderItem> = emptyMap(), // Sekarang menyimpan OrderItem
    val itemForQuantityInput: Items? = null, // Untuk mengontrol dialog kuantitas

    // State umum
    val isCreatingOrder: Boolean = false,
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null
)