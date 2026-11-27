package com.aprilarn.washflow.ui.feature.masterdata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.navigation.AppNavigation
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.components.AddNewDataInputField
import com.aprilarn.washflow.ui.core.components.AddNewDataPanel
import com.aprilarn.washflow.ui.core.components.ColumnConfig
import com.aprilarn.washflow.ui.core.components.DataTablePanel
import com.aprilarn.washflow.ui.core.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.core.components.EditDataPanel
import com.aprilarn.washflow.ui.feature.masterdata.customers.CustomersUiState
import com.aprilarn.washflow.ui.feature.masterdata.items.EditItemDialog
import com.aprilarn.washflow.ui.feature.masterdata.items.ItemsUiState
import com.aprilarn.washflow.ui.feature.masterdata.items.ServiceDropdown
import com.aprilarn.washflow.ui.feature.masterdata.services.ServicesUiState
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.TableDataCard
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.TableDataUiState
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRoseText
import com.aprilarn.washflow.utils.CurrencyUtils

/**
 * Layar Pilihan Master Data (Customers, Services, Items) khusus HP biasa (Batang / Portrait).
 */
@Composable
fun PhoneTableDataScreen(
    uiState: TableDataUiState,
    onNavigate: (route: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 20.dp)
    ) {
        item {
            TableDataCard(
                text = stringResource(R.string.master_customers_title),
                subText = stringResource(R.string.table_subtitle_customers),
                count = uiState.customerCount,
                onClick = { onNavigate(AppNavigation.Customers.route) }
            )
        }
        item {
            TableDataCard(
                text = stringResource(R.string.master_services_title),
                subText = stringResource(R.string.table_subtitle_services),
                count = uiState.serviceCount,
                onClick = { onNavigate(AppNavigation.Services.route) }
            )
        }
        item {
            TableDataCard(
                text = stringResource(R.string.master_items_title),
                subText = stringResource(R.string.table_subtitle_items),
                count = uiState.itemCount,
                onClick = { onNavigate(AppNavigation.Items.route) }
            )
        }
    }
}

/**
 * Layar Master Data Customers khusus HP biasa:
 * Tabel/daftar penuh 1 kolom + FAB (+ Tambah Customer) yang membuka ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCustomersScreen(
    uiState: CustomersUiState,
    onAddCustomerClick: (String, String) -> Unit,
    onEditCustomerClick: (Customers) -> Unit,
    onDeleteCustomerClick: (Customers) -> Unit,
    onCustomerSelected: (Customers) -> Unit,
    onDismissDialog: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var newCustomerName by rememberSaveable { mutableStateOf("") }
    var newCustomerPhone by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val filteredCustomers = uiState.customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.contact?.contains(searchQuery, ignoreCase = true) == true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        val customerColumns = listOf(
            ColumnConfig<Customers>(
                header = stringResource(R.string.master_customers_col_name),
                weight = 0.50f,
                content = { customer ->
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }
            ),
            ColumnConfig<Customers>(
                header = stringResource(R.string.master_customers_col_contact),
                weight = 0.35f,
                content = { customer ->
                    Text(
                        text = customer.contact?.takeIf { it.isNotBlank() } ?: "-",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontSize = 12.sp
                        )
                    )
                }
            ),
            ColumnConfig<Customers>(
                header = stringResource(R.string.common_edit),
                weight = 0.15f,
                content = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PastelBlueBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.common_edit),
                            tint = PastelBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        )

        DataTablePanel(
            title = stringResource(R.string.master_customers_title),
            itemCount = uiState.customers.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = stringResource(R.string.master_customers_search_placeholder),
            columns = customerColumns,
            data = filteredCustomers,
            isLoading = uiState.isLoading,
            onRowClick = onCustomerSelected
        )

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = PastelBlue,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.master_customers_add_button)) },
            text = { Text(stringResource(R.string.master_customers_fab), fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                AddNewDataPanel(
                    title = stringResource(R.string.master_customers_add_title),
                    subTitle = stringResource(R.string.master_customers_add_subtitle),
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = newCustomerName,
                            onValueChange = { newCustomerName = it },
                            label = stringResource(R.string.master_customers_name_label)
                        ),
                        AddNewDataInputField(
                            value = newCustomerPhone,
                            onValueChange = { newCustomerPhone = it },
                            label = stringResource(R.string.master_customers_phone_label),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    ),
                    onAddClick = {
                        onAddCustomerClick(newCustomerName, newCustomerPhone)
                        newCustomerName = ""
                        newCustomerPhone = ""
                        showAddSheet = false
                    },
                    addButtonText = stringResource(R.string.master_customers_add_button)
                )
            }
        }
    }

    uiState.selectedCustomer?.let { customerToEdit ->
        var editedName by rememberSaveable(customerToEdit.customerId) { mutableStateOf(customerToEdit.name) }
        var editedContact by rememberSaveable(customerToEdit.customerId) { mutableStateOf(customerToEdit.contact ?: "") }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                itemName = customerToEdit.name,
                onConfirm = {
                    onDeleteCustomerClick(customerToEdit)
                    showDeleteConfirmation = false
                    onDismissDialog()
                },
                onDismiss = { showDeleteConfirmation = false }
            )
        } else {
            Dialog(onDismissRequest = onDismissDialog) {
                EditDataPanel(
                    title = stringResource(R.string.master_edit_item_title, customerToEdit.name),
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = stringResource(R.string.master_customers_name_label)
                        ),
                        AddNewDataInputField(
                            value = editedContact,
                            onValueChange = { editedContact = it },
                            label = stringResource(R.string.master_customers_contact_label),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    ),
                    onDoneClick = {
                        onEditCustomerClick(customerToEdit.copy(name = editedName, contact = editedContact))
                    },
                    onDeleteClick = { showDeleteConfirmation = true },
                    accentHint = "Customer"
                )
            }
        }
    }
}

/**
 * Layar Master Data Services khusus HP biasa:
 * Tabel penuh 1 kolom + FAB (+ Tambah Layanan) yang membuka ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneServicesScreen(
    uiState: ServicesUiState,
    onAddServiceClick: (String, String) -> Unit,
    onEditServiceClick: (Services) -> Unit,
    onDeleteServiceClick: (Services) -> Unit,
    onServiceSelected: (Services) -> Unit,
    onDismissDialog: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var newServiceName by rememberSaveable { mutableStateOf("") }
    var newServiceId by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val filteredServices = uiState.services.filter {
        it.serviceName.contains(searchQuery, ignoreCase = true) ||
                it.serviceId.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        val serviceColumns = listOf(
            ColumnConfig<Services>(
                header = stringResource(R.string.master_services_col_id),
                weight = 0.28f,
                content = { service ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PastelGreenBg
                    ) {
                        Text(
                            text = service.serviceId,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PastelGreenDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            ),
            ColumnConfig<Services>(
                header = stringResource(R.string.master_services_col_name),
                weight = 0.57f,
                content = { service ->
                    Text(
                        text = service.serviceName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }
            ),
            ColumnConfig<Services>(
                header = stringResource(R.string.common_edit),
                weight = 0.15f,
                content = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PastelGreenBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.master_services_edit_cd),
                            tint = PastelGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        )

        DataTablePanel(
            title = stringResource(R.string.master_services_title),
            itemCount = uiState.services.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = stringResource(R.string.master_services_search_placeholder),
            columns = serviceColumns,
            data = filteredServices,
            isLoading = uiState.isLoading,
            onRowClick = onServiceSelected
        )

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = PastelGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.master_services_add_button)) },
            text = { Text(stringResource(R.string.master_services_fab), fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                AddNewDataPanel(
                    title = stringResource(R.string.master_services_add_title),
                    subTitle = stringResource(R.string.master_services_add_subtitle),
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = newServiceName,
                            onValueChange = { newServiceName = it },
                            label = stringResource(R.string.master_services_name_label)
                        ),
                        AddNewDataInputField(
                            value = newServiceId,
                            onValueChange = { newServiceId = it },
                            label = stringResource(R.string.master_services_id_label)
                        )
                    ),
                    onAddClick = {
                        onAddServiceClick(newServiceId, newServiceName)
                        newServiceId = ""
                        newServiceName = ""
                        showAddSheet = false
                    },
                    addButtonText = stringResource(R.string.master_services_add_button)
                )
            }
        }
    }

    uiState.selectedService?.let { serviceToEdit ->
        var editedName by rememberSaveable(serviceToEdit.serviceId) { mutableStateOf(serviceToEdit.serviceName) }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                itemName = serviceToEdit.serviceName,
                onConfirm = {
                    onDeleteServiceClick(serviceToEdit)
                    showDeleteConfirmation = false
                    onDismissDialog()
                },
                onDismiss = { showDeleteConfirmation = false }
            )
        } else {
            Dialog(onDismissRequest = onDismissDialog) {
                EditDataPanel(
                    title = stringResource(R.string.master_edit_item_title, serviceToEdit.serviceName),
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = stringResource(R.string.master_services_name_label)
                        )
                    ),
                    onDoneClick = {
                        onEditServiceClick(serviceToEdit.copy(serviceName = editedName))
                    },
                    onDeleteClick = { showDeleteConfirmation = true },
                    accentHint = "Service"
                )
            }
        }
    }
}

/**
 * Layar Master Data Items khusus HP biasa:
 * Tabel penuh 1 kolom + FAB (+ Tambah Barang) yang membuka ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneItemsScreen(
    uiState: ItemsUiState,
    onAddItemClick: (String, String, Double) -> Unit,
    onEditItemClick: (Items) -> Unit,
    onDeleteItemClick: (Items) -> Unit,
    onItemSelected: (Items) -> Unit,
    onDismissDialog: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var newItemService by rememberSaveable { mutableStateOf("") }
    var newItemName by rememberSaveable { mutableStateOf("") }
    var newItemPrice by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val filteredItems = uiState.items.filter {
        it.itemName.contains(searchQuery, ignoreCase = true) ||
                it.serviceId.contains(searchQuery, ignoreCase = true) ||
                it.itemPrice.toString().contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        val itemColumns = listOf(
            ColumnConfig<Items>(
                header = stringResource(R.string.master_items_col_svc_short),
                weight = 0.22f,
                content = { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PastelAmberBg
                    ) {
                        Text(
                            text = item.serviceId,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            ),
            ColumnConfig<Items>(
                header = stringResource(R.string.master_items_col_item_short),
                weight = 0.40f,
                content = { item ->
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }
            ),
            ColumnConfig<Items>(
                header = stringResource(R.string.master_items_col_price_short),
                weight = 0.25f,
                content = { item ->
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(item.itemPrice),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            ),
            ColumnConfig<Items>(
                header = stringResource(R.string.common_edit),
                weight = 0.13f,
                content = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PastelAmberBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.master_items_edit_cd),
                            tint = PastelAmber,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        )

        DataTablePanel(
            title = stringResource(R.string.master_items_title),
            itemCount = uiState.items.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = stringResource(R.string.master_items_search_placeholder),
            columns = itemColumns,
            data = filteredItems,
            isLoading = uiState.isLoading,
            onRowClick = onItemSelected
        )

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = PastelAmber,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.master_items_add_button)) },
            text = { Text(stringResource(R.string.master_items_fab), fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.master_items_add_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
                ServiceDropdown(
                    services = uiState.services,
                    selectedServiceId = newItemService,
                    onServiceSelected = { newItemService = it.serviceId },
                    label = stringResource(R.string.master_items_select_service),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text(stringResource(R.string.master_items_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = newItemPrice,
                    onValueChange = { newItemPrice = it },
                    label = { Text(stringResource(R.string.master_items_price_rp_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                Button(
                    onClick = {
                        val price = newItemPrice.toDoubleOrNull() ?: 0.0
                        onAddItemClick(newItemService, newItemName, price)
                        newItemService = ""
                        newItemName = ""
                        newItemPrice = ""
                        showAddSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelAmber)
                ) {
                    Text(stringResource(R.string.master_items_save_button), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    uiState.selectedItem?.let { itemToEdit ->
        var editedService by rememberSaveable(itemToEdit.itemId) { mutableStateOf(itemToEdit.serviceId) }
        var editedName by rememberSaveable(itemToEdit.itemId) { mutableStateOf(itemToEdit.itemName) }
        var editedPrice by rememberSaveable(itemToEdit.itemId) {
            mutableStateOf(itemToEdit.itemPrice.toString().replace(".0", ""))
        }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                itemName = itemToEdit.itemName,
                onConfirm = {
                    onDeleteItemClick(itemToEdit)
                    showDeleteConfirmation = false
                    onDismissDialog()
                },
                onDismiss = { showDeleteConfirmation = false }
            )
        } else {
            EditItemDialog(
                itemToEdit = itemToEdit,
                services = uiState.services,
                editedService = editedService,
                onEditedServiceChange = { editedService = it },
                editedName = editedName,
                onEditedNameChange = { editedName = it },
                editedPrice = editedPrice,
                onEditedPriceChange = { editedPrice = it },
                onDismiss = onDismissDialog,
                onDeleteClick = { showDeleteConfirmation = true },
                onDoneClick = {
                    val price = editedPrice.toDoubleOrNull() ?: itemToEdit.itemPrice
                    onEditItemClick(
                        itemToEdit.copy(
                            serviceId = editedService,
                            itemName = editedName,
                            itemPrice = price
                        )
                    )
                }
            )
        }
    }
}
