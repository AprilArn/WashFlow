package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCustomerInputPanel(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val calendar = remember { Calendar.getInstance() }

    val borderRadius = RoundedCornerShape(24.dp)
    val borderColor = Color.White
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Gray,
        focusedBorderColor = GrayBlue,
        focusedLabelColor = GrayBlue,
        cursorColor = GrayBlue
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White.copy(alpha = 0.25f), shape = borderRadius)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = borderRadius
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical=24.dp, horizontal=22.dp)
        ) {
            Text(
                text = "Customer",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = GrayBlue
                )
            )
            Text(
                text = "Pelanggan",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayBlue
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.customerSearchQuery,
                    onValueChange = {
                        viewModel.onCustomerQueryChanged(it)
                        isDropdownExpanded = true
                    },
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        },
                    trailingIcon = {
                        IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                            Icon(
                                imageVector = if (isDropdownExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    },
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )

                if (isDropdownExpanded && uiState.customerSearchQuery.isNotEmpty()) {
                    val filteredCustomers = uiState.customers.filter {
                        it.name.contains(uiState.customerSearchQuery, ignoreCase = true)
                    }

                    if (filteredCustomers.isNotEmpty()) {
                        Popup(
                            onDismissRequest = { isDropdownExpanded = false },
                            offset = IntOffset(x = 0, y = textFieldSize.height.toInt()),
                            properties = PopupProperties(focusable = false)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                                    .padding(top = 8.dp)
                                    .heightIn(max = 180.dp),
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 4.dp,
                                color = Color.White
                            ) {
                                LazyColumn {
                                    items(filteredCustomers) { customer ->
                                        DropdownMenuItem(
                                            text = { Text(customer.name) },
                                            onClick = {
                                                viewModel.onCustomerSelected(customer)
                                                isDropdownExpanded = false
                                            }
                                        )
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.selectedCustomer?.contact ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("No WA/Telp") },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
            ){
                OutlinedTextField(
                    value = uiState.dueDate?.toDate()?.let {
                        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Batas Waktu") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp),
                )
                // Kotak transparan di atasnya untuk menangkap klik (MASIH ADA KECACATAN UI)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            if (uiState.dueDate == null) {
                                // Jika belum ada tanggal, set default ke 1 hari setelah sekarang
                                val defaultDate = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                                viewModel.onDueDateChanged(Timestamp(defaultDate.time))
                            }
                            showDatePicker = true
                        }
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }

    // --- DIALOG BARU UNTUK DATE PICKER ---
    if (showDatePicker) {
        // Gunakan tanggal yang ada di uiState atau default 1 hari ke depan
        val initialDateMillis = uiState.dueDate?.toDate()?.time ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Ambil jam/menit yang sudah ada di uiState atau default sekarang
                        val currentDue = uiState.dueDate?.toDate() ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
                        val timeCal = Calendar.getInstance().apply { time = currentDue }
                        
                        calendar.timeInMillis = millis
                        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        
                        viewModel.onDueDateChanged(Timestamp(calendar.time))
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
        // Ambil jam dan menit dari uiState atau default dari calendar yang sudah diset di DatePicker
        val initialHour = uiState.dueDate?.toDate()?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.HOUR_OF_DAY)
        } ?: calendar.get(Calendar.HOUR_OF_DAY)

        val initialMinute = uiState.dueDate?.toDate()?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.MINUTE)
        } ?: calendar.get(Calendar.MINUTE)

        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set Batas Waktu") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Pastikan kita menggunakan tanggal yang sudah dipilih di DatePicker (yang ada di 'calendar')
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
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
