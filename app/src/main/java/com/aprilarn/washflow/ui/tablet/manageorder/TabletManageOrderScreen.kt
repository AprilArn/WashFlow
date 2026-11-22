package com.aprilarn.washflow.ui.tablet.manageorder

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.manage_order.ManageOrderScreen
import com.aprilarn.washflow.ui.manage_order.ManageOrderUiState

/**
 * Tampilan Kanban 3-Kolom (On Queue, On Process, Done) dengan Drag-and-Drop khusus Tablet.
 */
@Composable
fun TabletManageOrderScreen(
    uiState: ManageOrderUiState,
    onDrop: (orderId: String, newStatus: String) -> Unit,
    onOrderClick: (Orders) -> Unit,
    onTogglePayment: (String, Boolean) -> Unit,
    onTogglePickup: (String, Boolean) -> Unit,
    onDismissDialog: () -> Unit,
    onDeleteOrder: (String) -> Unit,
    onEditOrder: (String) -> Unit
) {
    ManageOrderScreen(
        uiState = uiState,
        onDrop = onDrop,
        onOrderClick = onOrderClick,
        onTogglePayment = onTogglePayment,
        onTogglePickup = onTogglePickup,
        onDismissDialog = onDismissDialog,
        onDeleteOrder = onDeleteOrder,
        onEditOrder = onEditOrder
    )
}
