package com.aprilarn.washflow.ui.feature.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.ui.core.components.NoDeadlineConfirmationDialog
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRoseText
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Tampilan Kasir / Create Order bertahap khusus HP biasa (Batang / Portrait):
 * - Tahap 1: User memilih pelanggan terlebih dahulu.
 * - Tahap 2: Panel pelanggan mengecil (shrink) menjadi kartu nama pelanggan dengan tombol silang (X)
 *   untuk mengganti pelanggan, diikuti tombol Batas Waktu di bawahnya, katalog layanan/item,
 *   dan Sticky Cart Bar di bagian bawah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneOrdersScreen(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    var showPreviewSheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }

    val selectedCustomer = uiState.selectedCustomer
    val totalItemTypes = uiState.selectedItems.size
    val totalQty = uiState.selectedItems.values.sumOf { it.itemQuantity ?: 0 }
    val totalPrice = uiState.selectedItems.values.sumOf { it.subtotal ?: 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(animationSpec = tween(durationMillis = 260)),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (selectedCustomer == null) {
            // TAHAP 1: Pilih Pelanggan terlebih dahulu (minimal 2 karakter + debounce 1 detik)
            PhoneCustomerSelectionStep(
                filteredCustomers = uiState.filteredCustomers,
                searchQuery = uiState.customerSearchQuery,
                debouncedQuery = uiState.debouncedCustomerQuery,
                isSearching = uiState.isSearchingCustomers,
                onSearchQueryChange = { viewModel.onCustomerQueryChanged(it) },
                onSelectCustomer = { viewModel.onCustomerSelected(it) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // TAHAP 2: Panel Pelanggan mengecil (shrink) + tombol "Ganti" untuk ganti pelanggan
            SelectedCustomerCard(
                customer = selectedCustomer,
                onClearCustomer = { viewModel.onClearSelectedCustomer() },
                shadowElevation = 4.dp
            )

            // Tombol Batas Waktu tepat di bawah kartu nama pelanggan
            PhoneDueDateButton(
                dueDate = uiState.dueDate,
                onClick = { showDatePicker = true },
                onClearDueDate = { viewModel.onDueDateChanged(null) }
            )

            // Katalog Layanan & Item (mengisi ruang utama)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(220)) + expandVertically(tween(240)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                OrderPanel(
                    modifier = Modifier.fillMaxSize(),
                    uiState = uiState,
                    viewModel = viewModel,
                    showFooter = false,
                    isCompact = true
                )
            }

            // Sticky Bar Ringkasan Nota di Bawah
            Surface(
                onClick = { showPreviewSheet = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelBlueSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ReceiptLong,
                                contentDescription = stringResource(R.string.orders_phone_preview_receipt_cd),
                                tint = PastelBlue
                            )
                        }
                        Column {
                            Text(
                                text = if (totalItemTypes > 0) {
                                    stringResource(R.string.orders_phone_cart_summary, totalItemTypes, totalQty)
                                } else {
                                    stringResource(R.string.orders_phone_no_items_selected)
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PastelNeutralText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = CurrencyUtils.formatRupiahWithSymbol(totalPrice),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GrayBlue,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showPreviewSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.orders_phone_view_receipt),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // --- DATE PICKER DIALOG ---
    if (showDatePicker) {
        val initialDateMillis = uiState.dueDate?.toDate()?.time
            ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
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

    // --- TIME PICKER DIALOG ---
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

                        viewModel.onDueDateChanged(Timestamp(calendar.time))
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

    if (showPreviewSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPreviewSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderPreviewPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    uiState = uiState,
                    onRemoveItem = { viewModel.removeSelectedItem(it) }
                )
                Button(
                    onClick = {
                        showPreviewSheet = false
                        viewModel.createOrder()
                    },
                    enabled = !uiState.isCreatingOrder && uiState.selectedCustomer != null && uiState.selectedItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                ) {
                    Text(
                        text = if (uiState.editingOrderId != null) {
                            stringResource(R.string.orders_save_changes_button)
                        } else {
                            stringResource(R.string.orders_create_now_button)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    uiState.itemForQuantityInput?.let { item ->
        PhoneQuantityInputDialog(
            item = item,
            onDismiss = { viewModel.onDismissQuantityDialog() },
            onConfirm = { quantity -> viewModel.onQuantityConfirmed(quantity) }
        )
    }

    if (uiState.showNoDeadlineWarning) {
        NoDeadlineConfirmationDialog(
            onConfirm = { viewModel.proceedCreateOrder() },
            onDismiss = { viewModel.dismissNoDeadlineWarning() }
        )
    }
}

/**
 * Tahap 1: Panel pencarian pelanggan. Daftar pelanggan hanya dimuat & difilter
 * setelah user mengetik minimal 2 karakter dengan jeda debounce 1 detik agar hemat Firestore read.
 */
@Composable
private fun PhoneCustomerSelectionStep(
    filteredCustomers: List<Customers>,
    searchQuery: String,
    debouncedQuery: String,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCustomer: (Customers) -> Unit,
    modifier: Modifier = Modifier
) {
    val trimmedQuery = searchQuery.trim()

    Surface(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 220))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(34.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.orders_phone_select_customer_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = GrayBlue
                            )
                        )
                        Text(
                            text = stringResource(R.string.orders_phone_select_customer_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = PastelBlueSoftBg
                ) {
                    Text(
                        text = stringResource(R.string.orders_phone_step_1_of_2),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PastelBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            HorizontalDivider(color = PastelBlueBg, thickness = 1.dp)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.orders_phone_search_customer_placeholder),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PastelNeutralText,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.common_clear_search),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GrayBlue,
                    unfocusedTextColor = GrayBlue,
                    focusedPlaceholderColor = PastelNeutralText,
                    unfocusedPlaceholderColor = PastelNeutralText,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = PastelBlueSoftBg,
                    focusedBorderColor = PastelBlue,
                    unfocusedBorderColor = PastelBlueBg,
                    focusedLeadingIconColor = PastelBlue,
                    unfocusedLeadingIconColor = PastelNeutralText,
                    focusedTrailingIconColor = PastelBlue,
                    unfocusedTrailingIconColor = PastelNeutralText,
                    cursorColor = PastelBlue
                )
            )

            when {
                trimmedQuery.isEmpty() -> Unit

                trimmedQuery.length < OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS -> {
                    Text(
                        text = stringResource(
                            R.string.orders_phone_min_chars_hint,
                            OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS
                        ),
                        color = PastelNeutralText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                isSearching -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PastelBlue
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.orders_phone_searching_customers),
                            color = PastelNeutralText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                debouncedQuery.length >= OrdersViewModel.MIN_CUSTOMER_SEARCH_CHARS && filteredCustomers.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.orders_phone_customer_not_found, debouncedQuery),
                        color = PastelNeutralText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }

                filteredCustomers.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 4.dp)
                        ) {
                            items(filteredCustomers, key = { it.customerId }) { customer ->
                                Surface(
                                    onClick = { onSelectCustomer(customer) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = PastelBlueSoftBg.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, PastelBlueBg)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(PastelBlueBg),
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
                                                style = MaterialTheme.typography.bodyLarge.copy(
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
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = customer.contact,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = PastelNeutralText,
                                                            fontSize = 12.sp
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}

/**
 * Tombol Batas Waktu tepat di bawah kartu nama pelanggan.
 */
@Composable
private fun PhoneDueDateButton(
    dueDate: Timestamp?,
    onClick: () -> Unit,
    onClearDueDate: () -> Unit
) {
    val formattedDueDate = remember(dueDate) {
        dueDate?.toDate()?.let {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(it)
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (dueDate != null) PastelBlueSoftBg else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (dueDate != null) PastelBlue.copy(alpha = 0.45f) else PastelBlueBg
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Event,
                    contentDescription = null,
                    tint = PastelBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (formattedDueDate != null) {
                        stringResource(R.string.orders_phone_due_date_value, formattedDueDate)
                    } else {
                        stringResource(R.string.orders_phone_set_due_date)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (dueDate != null) GrayBlue else PastelBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (dueDate != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onClearDueDate),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.orders_phone_clear_due_date_cd),
                        tint = PastelRoseText,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneQuantityInputDialog(
    item: Items,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantityText by rememberSaveable(item.itemId) { mutableStateOf("1") }
    val currentQty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val subtotal = item.itemPrice * currentQty

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.orders_qty_dialog_title),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = PastelNeutralText,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            R.string.orders_price_per_unit,
                            CurrencyUtils.formatRupiahWithSymbol(item.itemPrice)
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                HorizontalDivider(color = PastelBlueBg, thickness = 1.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val btnShape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(btnShape)
                            .background(PastelBlueSoftBg)
                            .border(1.dp, PastelBlueBg, btnShape)
                            .clickable {
                                val next = (currentQty - 1).coerceAtLeast(1)
                                quantityText = next.toString()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = stringResource(R.string.orders_qty_decrease_cd),
                            tint = PastelBlue
                        )
                    }

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                        label = { Text(stringResource(R.string.orders_qty_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = GrayBlue
                        ),
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GrayBlue,
                            unfocusedTextColor = GrayBlue,
                            focusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.45f),
                            unfocusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.25f),
                            focusedBorderColor = PastelBlue,
                            unfocusedBorderColor = PastelBlueBg,
                            focusedLabelColor = PastelBlue,
                            unfocusedLabelColor = PastelNeutralText,
                            cursorColor = PastelBlue
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(btnShape)
                            .background(PastelBlueSoftBg)
                            .border(1.dp, PastelBlueBg, btnShape)
                            .clickable {
                                val next = currentQty + 1
                                quantityText = next.toString()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.orders_qty_increase_cd),
                            tint = PastelBlue
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelBlueSoftBg.copy(alpha = 0.65f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.orders_subtotal),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = CurrencyUtils.formatRupiahWithSymbol(subtotal),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_cancel), color = GrayBlue, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(currentQty) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                    ) {
                        Text(stringResource(R.string.common_save), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
