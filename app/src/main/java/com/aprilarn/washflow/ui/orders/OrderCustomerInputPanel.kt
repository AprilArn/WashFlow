package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCustomerInputPanel(uiState: OrdersUiState, viewModel: OrdersViewModel) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }

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

            Box {
                OutlinedTextField(
                    value = uiState.dueDate?.toDate()?.let {
                        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Batas Waktu") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Kotak transparan di atasnya untuk menangkap klik
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true } // Saat diklik, tampilkan Date Picker
                )
            }
        }
    }

    // --- DIALOG BARU UNTUK DATE PICKER ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Simpan tanggal yang dipilih ke Calendar
                        calendar.timeInMillis = millis
                    }
                    showDatePicker = false
                    showTimePicker = true // Setelah tanggal dipilih, tampilkan Time Picker
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- DIALOG BARU UNTUK TIME PICKER ---
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        TimePickerDialog( // Ini adalah implementasi custom kecil untuk Time Picker Dialog
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        // Gabungkan tanggal yang sudah ada di Calendar dengan waktu yang baru dipilih
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)

                        // Kirim timestamp yang sudah lengkap ke ViewModel
                        viewModel.onDueDateChanged(Timestamp(calendar.time))
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

// Composable helper untuk Time Picker Dialog (karena tidak ada bawaan di M3)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}