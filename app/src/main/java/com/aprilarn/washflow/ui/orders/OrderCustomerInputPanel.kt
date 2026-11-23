package com.aprilarn.washflow.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.ui.components.HapticSwitch
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelSwitchOffTrack
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
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = GrayBlue,
        unfocusedTextColor = GrayBlue,
        focusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.55f),
        unfocusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.35f),
        focusedBorderColor = PastelBlue,
        unfocusedBorderColor = PastelBlueBg,
        focusedLabelColor = PastelBlue,
        unfocusedLabelColor = PastelNeutralText,
        cursorColor = PastelBlue,
        focusedLeadingIconColor = PastelBlue,
        unfocusedLeadingIconColor = PastelNeutralText,
        focusedTrailingIconColor = PastelBlue,
        unfocusedTrailingIconColor = PastelNeutralText
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = borderRadius,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(animationSpec = androidx.compose.animation.core.tween(durationMillis = 250))
                .padding(vertical = 18.dp, horizontal = 20.dp)
        ) {
            // Header bergaya Material Editorial dengan aksen vertikal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .background(PastelBlue, RoundedCornerShape(2.dp))
                )
                Column {
                    Text(
                        text = "Customer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = "Pelanggan",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PastelNeutralText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = PastelBlueBg,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.customerSearchQuery,
                    onValueChange = {
                        viewModel.onCustomerQueryChanged(it)
                        isDropdownExpanded = true
                    },
                    label = { Text("Nama Pelanggan") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
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
                    shape = RoundedCornerShape(14.dp)
                )

                if (isDropdownExpanded && uiState.debouncedCustomerQuery.length >= OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS) {
                    val filteredCustomers = uiState.filteredCustomers

                    if (filteredCustomers.isNotEmpty()) {
                        Popup(
                            onDismissRequest = { isDropdownExpanded = false },
                            offset = IntOffset(x = 0, y = textFieldSize.height.toInt()),
                            properties = PopupProperties(focusable = false)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                                    .padding(top = 6.dp)
                                    .heightIn(max = 180.dp),
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 6.dp,
                                color = Color.White
                            ) {
                                LazyColumn {
                                    items(filteredCustomers) { customer ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = customer.name,
                                                        color = GrayBlue,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    )
                                                    if (!customer.contact.isNullOrBlank()) {
                                                        Text(
                                                            text = customer.contact,
                                                            color = PastelNeutralText,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.onCustomerSelected(customer)
                                                isDropdownExpanded = false
                                            }
                                        )
                                        HorizontalDivider(color = PastelBlueBg.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.selectedCustomer?.contact ?: "",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("No WA/Telp") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gunakan Batas Waktu",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = GrayBlue
                    )
                )
                HapticSwitch(
                    checked = uiState.dueDate != null,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            val defaultDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            viewModel.onDueDateChanged(Timestamp(defaultDate.time))
                        } else {
                            viewModel.onDueDateChanged(null)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PastelBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = PastelSwitchOffTrack,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            AnimatedVisibility(visible = uiState.dueDate != null) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Batas Waktu:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PastelBlue,
                            containerColor = PastelBlueSoftBg
                        ),
                        border = BorderStroke(1.dp, PastelBlueBg)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Event,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateText = uiState.dueDate?.toDate()?.let {
                            SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(it)
                        } ?: ""
                        Text(text = dateText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // --- DIALOG BARU UNTUK DATE PICKER ---
    if (showDatePicker) {
        val initialDateMillis = uiState.dueDate?.toDate()?.time ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val currentDue = uiState.dueDate?.toDate()
                                ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
                            val timeCal = Calendar.getInstance().apply { time = currentDue }

                            calendar.timeInMillis = millis
                            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))

                            viewModel.onDueDateChanged(Timestamp(calendar.time))
                        }
                        showDatePicker = false
                        showTimePicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = GrayBlue)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- DIALOG BARU UNTUK TIME PICKER ---
    if (showTimePicker) {
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
            title = { Text("Set Batas Waktu", color = GrayBlue, fontWeight = FontWeight.Bold) },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)

                        viewModel.onDueDateChanged(Timestamp(calendar.time))
                        showTimePicker = false
                    }
                ) {
                    Text("OK", color = PastelBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = GrayBlue)
                }
            }
        )
    }
}
