package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.analytics.ReportSortColumn
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderReportPanel(
    orders: List<Orders>,
    isLoading: Boolean,
    selectedMonth: String,
    searchQuery: String,
    selectedStatusFilter: String,
    sortColumn: ReportSortColumn,
    isAscending: Boolean,
    onMonthSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterChanged: (String) -> Unit,
    onSortColumnClicked: (ReportSortColumn) -> Unit,
) {
    // 1. Ekstrak daftar bulan unik dari data order
    val monthList = remember(orders) {
        val months = orders.map { it.orderDate.toMonthYearString() }.distinct()
        listOf("Semua Bulan") + months
    }

    // 2. Filter berdasarkan kata kunci pencarian
    val searchFiltered = orders.filter { order ->
        if (searchQuery.isBlank()) true
        else {
            val customerNameMatch = (order.customerName ?: "").contains(searchQuery, ignoreCase = true)
            val itemsMatch = order.orderItems.any { (it.itemName ?: "").contains(searchQuery, ignoreCase = true) }
            val idMatch = order.orderId.contains(searchQuery, ignoreCase = true)
            customerNameMatch || itemsMatch || idMatch
        }
    }

    // 3. Filter berdasarkan status
    val statusFiltered = searchFiltered.filter { order ->
        if (selectedStatusFilter == "Semua") true
        else (order.status ?: "").equals(selectedStatusFilter, ignoreCase = true)
    }

    // 4. Filter berdasarkan bulan
    val monthFiltered = statusFiltered.filter { order ->
        if (selectedMonth == "Semua Bulan") true
        else order.orderDate.toMonthYearString().equals(selectedMonth, ignoreCase = true)
    }

    // 5. Urutkan data berdasarkan kolom yang dipilih
    val sortedOrders = remember(monthFiltered, sortColumn, isAscending) {
        val comparator = when (sortColumn) {
            ReportSortColumn.DATE -> compareBy<Orders> { it.orderDate.toDate().time }
            ReportSortColumn.TIME -> compareBy<Orders> { it.orderDate.toDate().time }
            ReportSortColumn.CUSTOMER_NAME -> compareBy<Orders> { (it.customerName ?: "").lowercase() }
            ReportSortColumn.ORDER_ITEMS -> compareBy<Orders> { order ->
                order.orderItems.joinToString { it.itemName ?: "" }
            }
            ReportSortColumn.TOTAL_PRICE -> compareBy<Orders> { it.totalPrice ?: 0.0 }
            ReportSortColumn.STATUS -> compareBy<Orders> { (it.status ?: "").lowercase() }
        }
        if (isAscending) monthFiltered.sortedWith(comparator)
        else monthFiltered.sortedWith(comparator.reversed())
    }

    // Summary ringkasan
    val totalRevenue = sortedOrders.sumOf { it.totalPrice ?: 0.0 }
    val doneCount = sortedOrders.count { (it.status ?: "").lowercase() == "done" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // --- Header Title & Controls ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Buku Besar / Log Laporan Order",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayBlue
                )
                Text(
                    text = "Catatan histori digital transaksi order laundry",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Dropdown Filter Bulan
            var monthDropdownExpanded by remember { mutableStateOf(value = false) }
            ExposedDropdownMenuBox(
                expanded = monthDropdownExpanded,
                onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded }
            ) {
                Surface(
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF3F4F6),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = GrayBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedMonth,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = GrayBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = GrayBlue
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = monthDropdownExpanded,
                    onDismissRequest = { monthDropdownExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    monthList.forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month,
                                    fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                    color = GrayBlue
                                )
                            },
                            onClick = {
                                onMonthSelected(month)
                                monthDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Metric Summary Cards & Search/Filter Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Summary Card 1: Total Transaksi
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Receipt, contentDescription = null, tint = GrayBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Total Transaksi", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${sortedOrders.size} Order",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayBlue
                        )
                    }
                }
            }

            // Summary Card 2: Total Pendapatan
            Card(
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Payments, contentDescription = null, tint = Color(0xFF1D4ED8))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Total Pendapatan", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = CurrencyUtils.formatRupiahWithSymbol(totalRevenue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            // Summary Card 3: Order Selesai
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Order Selesai", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "$doneCount Selesai",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Filter Status & Search Input ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val statusOptions = listOf("Semua", "On Queue", "On Process", "Done")
                statusOptions.forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusFilterChanged(status) },
                        label = {
                            Text(
                                text = status,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrayBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = GrayBlue
                        )
                    )
                }
            }

            // Search Text Field
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Cari nama/order...", fontSize = 12.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .width(240.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Digital Logbook / Ledger Table Container ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header Row with Column Sorting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("Tanggal", ReportSortColumn.DATE, sortColumn, isAscending, 1.2f, onSortColumnClicked)
                    TableHeaderCell("Waktu", ReportSortColumn.TIME, sortColumn, isAscending, 0.8f, onSortColumnClicked)
                    TableHeaderCell("Nama Pelanggan", ReportSortColumn.CUSTOMER_NAME, sortColumn, isAscending, 1.5f, onSortColumnClicked)
                    TableHeaderCell("Order", ReportSortColumn.ORDER_ITEMS, sortColumn, isAscending, 2.2f, onSortColumnClicked)
                    TableHeaderCell("Total", ReportSortColumn.TOTAL_PRICE, sortColumn, isAscending, 1.2f, onSortColumnClicked)
                    TableHeaderCell("Status Order", ReportSortColumn.STATUS, sortColumn, isAscending, 1.2f, onSortColumnClicked)
                    TableHeaderCell("Bayar", null, sortColumn, isAscending, 1.0f, onSortColumnClicked)
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Table Rows
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GrayBlue, strokeWidth = 3.dp)
                    }
                } else if (sortedOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada data order ditemukan", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    // Grouping berdasarkan bulan jika "Semua Bulan" dipilih
                    if (selectedMonth == "Semua Bulan") {
                        val groupedByMonth = sortedOrders.groupBy { it.orderDate.toMonthYearString() }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            groupedByMonth.forEach { (monthName, orderList) ->
                                // Month Section Banner
                                item {
                                    val monthTotal = orderList.sumOf { it.totalPrice ?: 0.0 }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFEDF2F7))
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = monthName.uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GrayBlue,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${orderList.size} Order  •  Subtotal: ${CurrencyUtils.formatRupiahWithSymbol(monthTotal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GrayBlue.copy(alpha = 0.8f)
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
                                }

                                items(orderList) { order ->
                                    OrderLedgerRow(order)
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    } else {
                        // Jika memilih bulan spesifik, tampilkan baris langsung
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(sortedOrders) { order ->
                                OrderLedgerRow(order)
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableHeaderCell(
    title: String,
    column: ReportSortColumn?,
    activeSortColumn: ReportSortColumn,
    isAscending: Boolean,
    weight: Float,
    onSortColumnClicked: (ReportSortColumn) -> Unit
) {
    val isCurrentSort = column == activeSortColumn
    Row(
        modifier = Modifier
            .weight(weight)
            .then(
                if (column != null) Modifier.clickable { onSortColumnClicked(column) }
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentSort) GrayBlue else Color.Gray
        )
        if (isCurrentSort) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (isAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = GrayBlue
            )
        }
    }
}

@Composable
private fun OrderLedgerRow(order: Orders) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Tanggal
        Text(
            text = order.orderDate.toDateString(),
            fontSize = 12.sp,
            color = GrayBlue,
            modifier = Modifier.weight(1.2f)
        )

        // 2. Waktu
        Text(
            text = order.orderDate.toTimeString(),
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.8f)
        )

        // 3. Nama Pelanggan
        Text(
            text = order.customerName ?: "Tanpa Nama",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GrayBlue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f)
        )

        // 4. Order (Daftar barang & kuantitas)
        val itemsSummary = if (order.orderItems.isEmpty()) "-"
        else order.orderItems.joinToString(", ") { item ->
            val qty = item.itemQuantity ?: 1
            "${item.itemName ?: "Item"} ($qty)"
        }
        Text(
            text = itemsSummary,
            fontSize = 12.sp,
            color = Color(0xFF475569),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2.2f)
        )

        // 5. Total Price
        Text(
            text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GrayBlue,
            modifier = Modifier.weight(1.2f)
        )

        // 6. Status Order Badge
        Box(modifier = Modifier.weight(1.2f)) {
            StatusBadge(status = order.status ?: "On Queue")
        }

        // 7. Status Pembayaran Badge
        Box(modifier = Modifier.weight(1.0f)) {
            PaymentBadge(alreadyPaid = order.alreadyPaid)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "done", "completed" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Done")
        "on process", "in progress" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "On Process")
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "On Queue")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PaymentBadge(alreadyPaid: Boolean) {
    val bgColor = if (alreadyPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val textColor = if (alreadyPaid) Color(0xFF15803D) else Color(0xFFB91C1C)
    val label = if (alreadyPaid) "Lunas" else "Belum"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Helper formatting extensions
private fun Timestamp.toDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(this.toDate())
}

private fun Timestamp.toTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    return sdf.format(this.toDate())
}

private fun Timestamp.toMonthYearString(): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    return sdf.format(this.toDate())
}
