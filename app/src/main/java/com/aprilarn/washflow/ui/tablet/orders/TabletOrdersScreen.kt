package com.aprilarn.washflow.ui.tablet.orders

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.orders.OrdersScreen
import com.aprilarn.washflow.ui.orders.OrdersUiState
import com.aprilarn.washflow.ui.orders.OrdersViewModel

/**
 * Tampilan Kasir / Orders 2-Panel khusus Tablet (Customer & Preview di kiri + Item Grid di kanan).
 */
@Composable
fun TabletOrdersScreen(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    OrdersScreen(
        uiState = uiState,
        viewModel = viewModel
    )
}
