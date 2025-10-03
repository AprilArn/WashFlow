// com/aprilarn/washflow/ui/manage_order/ManageOrderUiState.kt
package com.aprilarn.washflow.ui.manage_order

import com.aprilarn.washflow.data.model.Orders

data class ManageOrderUiState(
    val ordersOnQueue: List<Orders> = emptyList(),
    val ordersOnProcess: List<Orders> = emptyList(),
    val ordersDone: List<Orders> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)