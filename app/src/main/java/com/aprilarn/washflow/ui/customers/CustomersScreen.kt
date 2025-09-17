package com.aprilarn.washflow.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.ui.components.AddNewDataInputField
import com.aprilarn.washflow.ui.components.AddNewDataPanel
import com.aprilarn.washflow.ui.components.ColumnConfig
import com.aprilarn.washflow.ui.components.DataTablePanel
import com.aprilarn.washflow.ui.components.EditDataPanel
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun CustomersScreen(
    uiState: CustomersUiState,
    onAddCustomerClick: (String, String) -> Unit,
    onEditCustomerClick: (Customers) -> Unit,
    onDeleteCustomerClick: (Customers) -> Unit,
    onCustomerSelected: (Customers) -> Unit,
    onDismissDialog: () -> Unit
) {
    // State untuk pencarian (search) data
    // var searchQuery by remember { mutableStateOf("") }
    // State untuk panel "Add New"
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }

    val customerCount = uiState.customers.size
    val filteredCustomers = uiState.customers.filter {
        it.name.contains(newCustomerName, ignoreCase = true)
                || it.contact?.contains(newCustomerName, ignoreCase = true) == true
    }

    // Dialog untuk Edit/Delete
    uiState.selectedCustomer?.let { customerToEdit ->
        // State untuk field di dalam dialog edit
        var editedName by remember { mutableStateOf(customerToEdit.name) }
        var editedContact by remember { mutableStateOf(customerToEdit.contact ?: "") }

        // LaunchedEffect untuk mereset state jika customer yang dipilih berganti
        LaunchedEffect(customerToEdit) {
            editedName = customerToEdit.name
            editedContact = customerToEdit.contact ?: ""
        }

        Dialog(onDismissRequest = onDismissDialog) {
            val editInputFields = listOf(
                AddNewDataInputField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = "Customer Name"
                ),
                AddNewDataInputField(
                    value = editedContact,
                    onValueChange = { editedContact = it },
                    label = "Contact"
                )
            )

            EditDataPanel(
                title = "Edit ${customerToEdit.name}",
                inputFields = editInputFields,
                onDoneClick = {
                    val updatedCustomer = customerToEdit.copy(
                        name = editedName,
                        contact = editedContact
                    )
                    onEditCustomerClick(updatedCustomer)
                },
                onDeleteClick = {
                    onDeleteCustomerClick(customerToEdit)
                }
            )
        }
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel: DataTablePanel
        Box(modifier = Modifier.weight(2f)) {
            val customerColumns = listOf(
                ColumnConfig<Customers>(
                    header = "Name",
                    weight = 0.7f,
                    content = { customer -> Text(customer.name, color = GrayBlue) }
                ),
                ColumnConfig<Customers>(
                    header = "Contact",
                    weight = 0.4f,
                    content = { customer -> customer.contact?.let { Text(it, color = GrayBlue) } }
                ),
                ColumnConfig<Customers>(
                    header = "Edit", // Ubah header menjadi "Actions"
                    weight = 0.1f,
                    content = { customer ->
                        // Hapus tombol dari sini karena aksi sekarang via onRowClick
                        // Bisa diganti dengan ikon atau indikator lain jika perlu
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Customer",
                            tint = GrayBlue.copy(alpha = 0.7f)
                        )
                    }
                )
            )

            DataTablePanel(
                title = "Customer",
                itemCount = customerCount,
                searchQuery = newCustomerName,
                onSearchQueryChange = { newCustomerName = it },
                searchPlaceholder = "Search by Name/Contact",
                columns = customerColumns,
                data = filteredCustomers, // Gunakan data yang sudah difilter
                isLoading = uiState.isLoading,
                onRowClick = { customer -> onCustomerSelected(customer) }
            )
        }

        // Right Panel: AddNewDataPanel
        Box(modifier = Modifier.weight(1f)) {
            val inputFields = listOf(
                AddNewDataInputField(
                    value = newCustomerName,
                    onValueChange = { newCustomerName = it },
                    label = "Nama Pelanggan"
                ),
                AddNewDataInputField(
                    value = newCustomerPhone,
                    onValueChange = { newCustomerPhone = it },
                    label = "No. WA"
                )
            )

            AddNewDataPanel(
                title = "Add New Customer",
                inputFields = inputFields,
                onAddClick = {
                    onAddCustomerClick(newCustomerName, newCustomerPhone)
                    newCustomerName = ""
                    newCustomerPhone = ""
                },
                addButtonText = "Add Customer" // Teks lebih deskriptif
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 600)
@Composable
fun CustomersScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        val previewState = CustomersUiState(
            customers = listOf(
                Customers("1", "Pelanggan Satu", "081234567890"),
                Customers("2", "Pelanggan Dua", "089876543210")
            ),
            // Contoh saat dialog edit muncul untuk pelanggan pertama
            selectedCustomer = Customers("1", "Pelanggan Satu", "081234567890")
        )
        CustomersScreen(
            uiState = previewState,
            onAddCustomerClick = { _, _ -> },
            onEditCustomerClick = {},
            onDeleteCustomerClick = {},
            onCustomerSelected = {},
            onDismissDialog = {}
        )
    }
}