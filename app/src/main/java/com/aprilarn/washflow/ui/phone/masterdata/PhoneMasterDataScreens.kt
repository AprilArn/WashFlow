package com.aprilarn.washflow.ui.phone.masterdata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.AppNavigation
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.components.AddNewDataInputField
import com.aprilarn.washflow.ui.components.AddNewDataPanel
import com.aprilarn.washflow.ui.components.ColumnConfig
import com.aprilarn.washflow.ui.components.DataTablePanel
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.components.EditDataPanel
import com.aprilarn.washflow.ui.customers.CustomersUiState
import com.aprilarn.washflow.ui.items.ItemsUiState
import com.aprilarn.washflow.ui.items.ServiceDropdown
import com.aprilarn.washflow.ui.services.ServicesUiState
import com.aprilarn.washflow.ui.tabledata.TableDataCard
import com.aprilarn.washflow.ui.tabledata.TableDataUiState
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelGreenDark
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRoseText
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
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            TableDataCard(
                text = "Customers",
                subText = "Pelanggan",
                count = uiState.customerCount,
                onClick = { onNavigate(AppNavigation.Customers.route) }
            )
        }
        item {
            TableDataCard(
                text = "Services",
                subText = "Layanan",
                count = uiState.serviceCount,
                onClick = { onNavigate(AppNavigation.Services.route) }
            )
        }
        item {
            TableDataCard(
                text = "Items",
                subText = "Barang",
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

    Box(modifier = Modifier.fillMaxSize()) {
        val customerColumns = listOf(
            ColumnConfig<Customers>(
                header = "Name",
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
                header = "Contact",
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
                header = "Edit",
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
                            contentDescription = "Edit",
                            tint = PastelBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        )

        DataTablePanel(
            title = "Customers",
            itemCount = uiState.customers.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = "Cari nama atau kontak pelanggan...",
            columns = customerColumns,
            data = filteredCustomers,
            isLoading = uiState.isLoading,
            onRowClick = onCustomerSelected
        )

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = PastelBlue,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = "Tambah Pelanggan") },
            text = { Text("Pelanggan", fontWeight = FontWeight.Bold) },
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
                    title = "Add New Customer",
                    subTitle = "Tambah pelanggan baru",
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = newCustomerName,
                            onValueChange = { newCustomerName = it },
                            label = "Nama Pelanggan"
                        ),
                        AddNewDataInputField(
                            value = newCustomerPhone,
                            onValueChange = { newCustomerPhone = it },
                            label = "Kontak / WhatsApp",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    ),
                    onAddClick = {
                        onAddCustomerClick(newCustomerName, newCustomerPhone)
                        newCustomerName = ""
                        newCustomerPhone = ""
                        showAddSheet = false
                    },
                    addButtonText = "Add Customer"
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
                    title = "Edit ${customerToEdit.name}",
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = "Customer Name"
                        ),
                        AddNewDataInputField(
                            value = editedContact,
                            onValueChange = { editedContact = it },
                            label = "Contact",
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

    Box(modifier = Modifier.fillMaxSize()) {
        val serviceColumns = listOf(
            ColumnConfig<Services>(
                header = "ID",
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
                header = "Service Name",
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
                header = "Edit",
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
                            contentDescription = "Edit Service",
                            tint = PastelGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        )

        DataTablePanel(
            title = "Services",
            itemCount = uiState.services.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = "Cari ID atau nama layanan...",
            columns = serviceColumns,
            data = filteredServices,
            isLoading = uiState.isLoading,
            onRowClick = onServiceSelected
        )

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = PastelGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = "Tambah Layanan") },
            text = { Text("Layanan", fontWeight = FontWeight.Bold) },
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
                    title = "Add New Service",
                    subTitle = "Tambah layanan baru",
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = newServiceName,
                            onValueChange = { newServiceName = it },
                            label = "Nama Service"
                        ),
                        AddNewDataInputField(
                            value = newServiceId,
                            onValueChange = { newServiceId = it },
                            label = "ID Service"
                        )
                    ),
                    onAddClick = {
                        onAddServiceClick(newServiceId, newServiceName)
                        newServiceId = ""
                        newServiceName = ""
                        showAddSheet = false
                    },
                    addButtonText = "Add Service"
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
                    title = "Edit ${serviceToEdit.serviceName}",
                    inputFields = listOf(
                        AddNewDataInputField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = "Service Name"
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

    Box(modifier = Modifier.fillMaxSize()) {
        val itemColumns = listOf(
            ColumnConfig<Items>(
                header = "Svc",
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
                header = "Item",
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
                header = "Price",
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
                header = "Edit",
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
                            contentDescription = "Edit Item",
                            tint = PastelAmber,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        )

        DataTablePanel(
            title = "Items",
            itemCount = uiState.items.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = "Cari nama barang, layanan, atau harga...",
            columns = itemColumns,
            data = filteredItems,
            isLoading = uiState.isLoading,
            onRowClick = onItemSelected
        )

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = PastelAmber,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = "Tambah Barang") },
            text = { Text("Barang", fontWeight = FontWeight.Bold) },
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
                    text = "Tambah Barang Baru",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
                ServiceDropdown(
                    services = uiState.services,
                    selectedServiceId = newItemService,
                    onServiceSelected = { newItemService = it.serviceId },
                    label = "Pilih Layanan",
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Nama Barang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = newItemPrice,
                    onValueChange = { newItemPrice = it },
                    label = { Text("Harga (Rp)") },
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
                    Text("Simpan Barang", color = Color.White, fontWeight = FontWeight.Bold)
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
            Dialog(onDismissRequest = onDismissDialog) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Edit ${itemToEdit.itemName}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        HorizontalDivider(color = PastelNeutralBg)
                        ServiceDropdown(
                            services = uiState.services,
                            selectedServiceId = editedService,
                            onServiceSelected = { editedService = it.serviceId },
                            label = "Service",
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Item Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = editedPrice,
                            onValueChange = { editedPrice = it },
                            label = { Text("Item Price") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { showDeleteConfirmation = true }) {
                                Text("Hapus", color = PastelRoseText, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                TextButton(onClick = onDismissDialog) {
                                    Text("Batal", color = GrayBlue)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        val price = editedPrice.toDoubleOrNull() ?: itemToEdit.itemPrice
                                        onEditItemClick(
                                            itemToEdit.copy(
                                                serviceId = editedService,
                                                itemName = editedName,
                                                itemPrice = price
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PastelAmber)
                                ) {
                                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
