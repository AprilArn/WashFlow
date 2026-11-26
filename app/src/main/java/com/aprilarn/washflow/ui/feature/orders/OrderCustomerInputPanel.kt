package com.aprilarn.washflow.ui.feature.orders

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.ui.core.components.HapticSwitch
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelSwitchOffTrack
import com.aprilarn.washflow.ui.core.theme.WashFlowTheme
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun OrderCustomerInputPanel(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    OrderCustomerInputPanelContent(
        uiState = uiState,
        onCustomerQueryChanged = { viewModel.onCustomerQueryChanged(it) },
        onCustomerSelected = { viewModel.onCustomerSelected(it) },
        onClearSelectedCustomer = { viewModel.onClearSelectedCustomer() },
        onDueDateChanged = { viewModel.onDueDateChanged(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderCustomerInputPanelContent(
    uiState: OrdersUiState,
    onCustomerQueryChanged: (String) -> Unit,
    onCustomerSelected: (Customers) -> Unit,
    onClearSelectedCustomer: () -> Unit,
    onDueDateChanged: (Timestamp?) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var shouldFocusSearch by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val searchFocusRequester = remember { FocusRequester() }
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
                .animateContentSize(animationSpec = tween(durationMillis = 250))
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
                        text = stringResource(R.string.common_customer),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = stringResource(R.string.orders_customer_subtitle),
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

            AnimatedContent(
                targetState = uiState.selectedCustomer,
                transitionSpec = {
                    (fadeIn(tween(200)) + expandVertically(tween(220)))
                        .togetherWith(fadeOut(tween(150)) + shrinkVertically(tween(180)))
                },
                label = "tabletCustomerSection"
            ) { selectedCustomer ->
                if (selectedCustomer != null) {
                    SelectedCustomerCard(
                        customer = selectedCustomer,
                        onClearCustomer = {
                            isDropdownExpanded = false
                            shouldFocusSearch = true
                            onClearSelectedCustomer()
                        },
                        shadowElevation = 0.dp
                    )
                } else {
                    LaunchedEffect(shouldFocusSearch) {
                        if (shouldFocusSearch) {
                            runCatching { searchFocusRequester.requestFocus() }
                            shouldFocusSearch = false
                        }
                    }

                    val trimmedQuery = uiState.customerSearchQuery.trim()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.customerSearchQuery,
                            onValueChange = {
                                onCustomerQueryChanged(it)
                                isDropdownExpanded = it.isNotBlank()
                            },
                            label = { Text(stringResource(R.string.master_customers_name_label)) },
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
                                .focusRequester(searchFocusRequester)
                                .onGloballyPositioned { coordinates ->
                                    textFieldSize = coordinates.size.toSize()
                                },
                            trailingIcon = {
                                when {
                                    uiState.isSearchingCustomers -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = PastelBlue
                                        )
                                    }
                                    uiState.customerSearchQuery.isNotEmpty() -> {
                                        IconButton(
                                            onClick = {
                                                onCustomerQueryChanged("")
                                                isDropdownExpanded = false
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = stringResource(R.string.common_clear_search),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                            Icon(
                                                imageVector = if (isDropdownExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            },
                            colors = textFieldColors,
                            shape = RoundedCornerShape(14.dp)
                        )

                        AnimatedVisibility(
                            visible = trimmedQuery.isNotEmpty() && trimmedQuery.length < OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.orders_phone_min_chars_hint,
                                    OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS
                                ),
                                color = PastelNeutralText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }

                        if (isDropdownExpanded &&
                            !uiState.isSearchingCustomers &&
                            uiState.debouncedCustomerQuery.length >= OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS
                        ) {
                            val filteredCustomers = uiState.filteredCustomers

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
                                    border = BorderStroke(1.dp, PastelBlueBg),
                                    shadowElevation = 6.dp,
                                    color = Color.White
                                ) {
                                    if (filteredCustomers.isEmpty()) {
                                        Text(
                                            text = stringResource(
                                                R.string.orders_phone_customer_not_found,
                                                uiState.debouncedCustomerQuery
                                            ),
                                            color = PastelNeutralText,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 14.dp)
                                        )
                                    } else {
                                        LazyColumn {
                                            items(filteredCustomers, key = { it.customerId }) { customer ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(
                                                                text = customer.name,
                                                                color = GrayBlue,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 14.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (!customer.contact.isNullOrBlank()) {
                                                                Text(
                                                                    text = customer.contact,
                                                                    color = PastelNeutralText,
                                                                    fontSize = 11.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        onCustomerSelected(customer)
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
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.orders_use_due_date),
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
                            onDueDateChanged(Timestamp(defaultDate.time))
                        } else {
                            onDueDateChanged(null)
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
                        text = stringResource(R.string.orders_due_date_label),
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

                            onDueDateChanged(Timestamp(calendar.time))
                        }
                        showDatePicker = false
                        showTimePicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                ) {
                    Text(stringResource(R.string.common_ok), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel), color = GrayBlue)
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
            title = { Text(stringResource(R.string.orders_set_due_time_title), color = GrayBlue, fontWeight = FontWeight.Bold) },
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

                        onDueDateChanged(Timestamp(calendar.time))
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.common_ok), color = PastelBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.common_cancel), color = GrayBlue)
                }
            }
        )
    }
}

/**
 * Kartu detail pelanggan terpilih dengan tombol "Ganti" di kanan (dipakai bersama di Tablet & HP).
 */
@Composable
internal fun SelectedCustomerCard(
    customer: Customers,
    onClearCustomer: () -> Unit,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 0.dp
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PastelBlueSoftBg,
        border = BorderStroke(
            width = 1.dp,
            color = PastelBlue.copy(alpha = 0.45f)
        ),
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = PastelBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!customer.contact.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = PastelNeutralText,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = customer.contact,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PastelNeutralText,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Surface(
                onClick = onClearCustomer,
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = BorderStroke(1.dp, PastelBlue.copy(alpha = 0.25f))
            ) {
                Text(
                    text = stringResource(R.string.common_change),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PastelBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Tablet Customer Panel - Search")
@Composable
private fun OrderCustomerInputPanelSearchPreview() {
    WashFlowTheme {
        Box(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp)
        ) {
            OrderCustomerInputPanelContent(
                uiState = OrdersUiState(
                    customerSearchQuery = "Ve"
                ),
                onCustomerQueryChanged = {},
                onCustomerSelected = {},
                onClearSelectedCustomer = {},
                onDueDateChanged = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, name = "Tablet Customer Panel - Selected")
@Composable
private fun OrderCustomerInputPanelSelectedPreview() {
    WashFlowTheme {
        Box(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp)
        ) {
            OrderCustomerInputPanelContent(
                uiState = OrdersUiState(
                    selectedCustomer = Customers(
                        customerId = "c1",
                        name = "Max Verstappen",
                        contact = "0812-3456-7890"
                    ),
                    customerSearchQuery = "Max Verstappen"
                ),
                onCustomerQueryChanged = {},
                onCustomerSelected = {},
                onClearSelectedCustomer = {},
                onDueDateChanged = {}
            )
        }
    }
}
