package com.aprilarn.washflow.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.ui.components.AddNewDataInputField
import com.aprilarn.washflow.ui.components.AddNewDataPanel
import com.aprilarn.washflow.ui.components.ColumnConfig
import com.aprilarn.washflow.ui.components.DataTablePanel
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.components.EditDataPanel
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText

@Composable
fun CustomersScreen(
    uiState: CustomersUiState,
    onAddCustomerClick: (String, String) -> Unit,
    onEditCustomerClick: (Customers) -> Unit,
    onDeleteCustomerClick: (Customers) -> Unit,
    onCustomerSelected: (Customers) -> Unit,
    onDismissDialog: () -> Unit
) {
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val customerCount = uiState.customers.size
    val filteredCustomers = uiState.customers.filter {
        it.name.contains(newCustomerName, ignoreCase = true)
                || it.contact?.contains(newCustomerName, ignoreCase = true) == true
    }

    // Dialog untuk Edit/Delete
    uiState.selectedCustomer?.let { customerToEdit ->
        LaunchedEffect(customerToEdit) {
            showDeleteConfirmation = false
        }
        var editedName by remember { mutableStateOf(customerToEdit.name) }
        var editedContact by remember { mutableStateOf(customerToEdit.contact ?: "") }

        LaunchedEffect(customerToEdit) {
            editedName = customerToEdit.name
            editedContact = customerToEdit.contact ?: ""
        }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                itemName = customerToEdit.name,
                onConfirm = {
                    onDeleteCustomerClick(customerToEdit)
                    showDeleteConfirmation = false
                    onDismissDialog()
                },
                onDismiss = {
                    showDeleteConfirmation = false
                }
            )
        } else {
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
                        label = "Contact",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                        showDeleteConfirmation = true
                    },
                    accentHint = "Customer"
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel: DataTablePanel
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            val customerColumns = listOf(
                ColumnConfig<Customers>(
                    header = "Name",
                    weight = 0.65f,
                    content = { customer ->
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                ),
                ColumnConfig<Customers>(
                    header = "Contact",
                    weight = 0.45f,
                    content = { customer ->
                        val contactText = customer.contact?.takeIf { it.isNotBlank() }
                        Text(
                            text = contactText ?: "-",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (contactText != null) PastelBlue else PastelNeutralText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                ),
                ColumnConfig<Customers>(
                    header = "Edit",
                    weight = 0.12f,
                    content = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(PastelBlueBg, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Customer",
                                tint = PastelBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            )

            DataTablePanel(
                title = "Customers",
                itemCount = customerCount,
                searchQuery = newCustomerName,
                onSearchQueryChange = { newCustomerName = it },
                searchPlaceholder = "Cari nama atau nomor kontak pelanggan...",
                columns = customerColumns,
                data = filteredCustomers,
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
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            newCustomerPhone = newValue
                        }
                    },
                    label = "No. WA",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            )

            AddNewDataPanel(
                title = "Add New Customer",
                subTitle = "Tambah pelanggan baru",
                inputFields = inputFields,
                onAddClick = {
                    onAddCustomerClick(newCustomerName, newCustomerPhone)
                    newCustomerName = ""
                    newCustomerPhone = ""
                },
                addButtonText = "Add Customer"
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
