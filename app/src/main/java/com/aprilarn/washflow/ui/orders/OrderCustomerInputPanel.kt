package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCustomerInputPanel(uiState: OrdersUiState, viewModel: OrdersViewModel) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Customer", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded && uiState.customerSearchQuery.isNotEmpty(),
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.customerSearchQuery,
                    onValueChange = {
                        viewModel.onCustomerQueryChanged(it)
                        isDropdownExpanded = true
                    },
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded && uiState.customerSearchQuery.isNotEmpty(),
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    val filteredCustomers = uiState.customers.filter {
                        it.name.contains(uiState.customerSearchQuery, ignoreCase = true)
                    }
                    filteredCustomers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text(customer.name) },
                            onClick = {
                                viewModel.onCustomerSelected(customer)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.selectedCustomer?.contact ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("No WA/Telp") },
                modifier = Modifier.fillMaxWidth()
            )

            // Untuk Batas Waktu, idealnya menggunakan DatePickerDialog.
            // Di sini kita gunakan TextField sederhana untuk demonstrasi.
            OutlinedTextField(
                value = uiState.dueDate?.toDate()?.let {
                    SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(it)
                } ?: "",
                onValueChange = {},
                readOnly = true, // Ganti dengan event onClick untuk memunculkan picker
                label = { Text("Batas Waktu") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}