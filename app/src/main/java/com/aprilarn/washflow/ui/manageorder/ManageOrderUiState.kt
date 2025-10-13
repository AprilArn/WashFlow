package com.aprilarn.washflow.ui.manage_order

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services

data class ManageOrderUiState(
    val ordersOnQueue: List<Orders> = emptyList(),
    val ordersOnProcess: List<Orders> = emptyList(),
    val ordersDone: List<Orders> = emptyList(),
    val customers: List<Customers> = emptyList(),
    val services: List<Services> = emptyList(),
    val selectedOrderForDetail: Orders? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)