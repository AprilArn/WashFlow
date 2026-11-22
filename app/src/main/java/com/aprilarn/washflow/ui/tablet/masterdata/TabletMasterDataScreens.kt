package com.aprilarn.washflow.ui.tablet.masterdata

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.customers.CustomersScreen
import com.aprilarn.washflow.ui.customers.CustomersUiState
import com.aprilarn.washflow.ui.items.ItemsScreen
import com.aprilarn.washflow.ui.items.ItemsUiState
import com.aprilarn.washflow.ui.services.ServicesScreen
import com.aprilarn.washflow.ui.services.ServicesUiState
import com.aprilarn.washflow.ui.tabledata.TableDataScreen
import com.aprilarn.washflow.ui.tabledata.TableDataUiState

/**
 * Kumpulan layar Master Data 2-Panel (Tabel di kiri + Form Tambah di kanan) khusus Tablet.
 */
@Composable
fun TabletTableDataScreen(
    uiState: TableDataUiState,
    onNavigate: (route: String) -> Unit
) {
    TableDataScreen(
        uiState = uiState,
        onNavigate = onNavigate
    )
}

@Composable
fun TabletCustomersScreen(
    uiState: CustomersUiState,
    onAddCustomerClick: (String, String) -> Unit,
    onEditCustomerClick: (Customers) -> Unit,
    onDeleteCustomerClick: (Customers) -> Unit,
    onCustomerSelected: (Customers) -> Unit,
    onDismissDialog: () -> Unit
) {
    CustomersScreen(
        uiState = uiState,
        onAddCustomerClick = onAddCustomerClick,
        onEditCustomerClick = onEditCustomerClick,
        onDeleteCustomerClick = onDeleteCustomerClick,
        onCustomerSelected = onCustomerSelected,
        onDismissDialog = onDismissDialog
    )
}

@Composable
fun TabletServicesScreen(
    uiState: ServicesUiState,
    onAddServiceClick: (String, String) -> Unit,
    onEditServiceClick: (Services) -> Unit,
    onDeleteServiceClick: (Services) -> Unit,
    onServiceSelected: (Services) -> Unit,
    onDismissDialog: () -> Unit
) {
    ServicesScreen(
        uiState = uiState,
        onAddServiceClick = onAddServiceClick,
        onEditServiceClick = onEditServiceClick,
        onDeleteServiceClick = onDeleteServiceClick,
        onServiceSelected = onServiceSelected,
        onDismissDialog = onDismissDialog
    )
}

@Composable
fun TabletItemsScreen(
    uiState: ItemsUiState,
    onAddItemClick: (String, String, Double) -> Unit,
    onEditItemClick: (Items) -> Unit,
    onDeleteItemClick: (Items) -> Unit,
    onItemSelected: (Items) -> Unit,
    onDismissDialog: () -> Unit
) {
    ItemsScreen(
        uiState = uiState,
        onAddItemClick = onAddItemClick,
        onEditItemClick = onEditItemClick,
        onDeleteItemClick = onDeleteItemClick,
        onItemSelected = onItemSelected,
        onDismissDialog = onDismissDialog
    )
}
