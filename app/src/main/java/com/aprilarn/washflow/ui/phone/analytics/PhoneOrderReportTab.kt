package com.aprilarn.washflow.ui.phone.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.analytics.ReportSortColumn
import com.aprilarn.washflow.ui.analytics.component.GeneralDropdown
import com.aprilarn.washflow.ui.analytics.isDone
import com.aprilarn.washflow.ui.orders.ReceiptDashedDivider
import com.aprilarn.washflow.ui.orders.ReceiptServiceSection
import com.aprilarn.washflow.ui.orders.groupOrderItemsByService
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseText
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Tampilan Buku Besar / Log Laporan Order yang proporsional khusus HP biasa (Portrait).
 * Menggantikan tabel multi-kolom lebar dengan kartu KPI full-width, filter chip horizontal scrollable,
 * dan daftar kartu transaksi yang dapat diklik untuk membuka Nota Pesanan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneOrderReportTab(
    orders: List<Orders>,
    services: List<Services>,
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
    onLoadMoreMonths: () -> Unit
) {
    var selectedOrderForReceipt by remember { mutableStateOf<Orders?>(null) }

    val monthList = remember(orders) {
        val months = orders.map { it.orderDate.toReportMonthYearString() }.distinct()
        listOf("Semua Bulan") + months
    }

    val filteredAndSortedOrders = remember(
        orders,
        services,
        searchQuery,
        selectedStatusFilter,
        selectedMonth,
        sortColumn,
        isAscending
    ) {
        val filtered = orders.filter { order ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val customerMatch = (order.customerName ?: "").contains(searchQuery, ignoreCase = true)
                val itemsMatch = order.orderItems.any { (it.itemName ?: "").contains(searchQuery, ignoreCase = true) }
                val idMatch = order.orderId.contains(searchQuery, ignoreCase = true)
                val serviceMatch = order.orderItems.any { item ->
                    val sName = services.find { it.serviceId == item.serviceId }?.serviceName ?: item.serviceId ?: ""
                    sName.contains(searchQuery, ignoreCase = true)
                }
                customerMatch || itemsMatch || idMatch || serviceMatch
            }
            if (!matchesSearch) return@filter false

            val matchesStatus = if (selectedStatusFilter == "Semua") true else {
                (order.status ?: "").equals(selectedStatusFilter, ignoreCase = true)
            }
            if (!matchesStatus) return@filter false

            if (selectedMonth == "Semua Bulan") true else {
                order.orderDate.toReportMonthYearString().equals(selectedMonth, ignoreCase = true)
            }
        }

        val comparator = when (sortColumn) {
            ReportSortColumn.DATE, ReportSortColumn.TIME -> compareBy<Orders> { it.orderDate.toDate().time }
            ReportSortColumn.CUSTOMER_NAME -> compareBy<Orders> { (it.customerName ?: "").lowercase() }
            ReportSortColumn.ORDER_ITEMS -> compareBy<Orders> { order ->
                order.orderItems.joinToString { it.itemName ?: "" }
            }
            ReportSortColumn.TOTAL_PRICE -> compareBy<Orders> { it.totalPrice ?: 0.0 }
            ReportSortColumn.STATUS -> compareBy<Orders> { (it.status ?: "").lowercase() }
            ReportSortColumn.FINISH_DATE -> compareBy<Orders> { it.orderFinishDate?.toDate()?.time ?: 0L }
        }
        if (isAscending) filtered.sortedWith(comparator) else filtered.sortedWith(comparator.reversed())
    }

    val comparison = remember(orders, selectedMonth) {
        buildPhoneReportComparison(orders = orders, selectedMonth = selectedMonth)
    }

    val statusFilters = remember { listOf("Semua", "On Queue", "On Process", "Done") }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Editorial Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(34.dp)
                        .background(PastelBlue, RoundedCornerShape(2.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Buku Besar Order",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "Catatan histori digital transaksi laundry",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 2. Filter Bulan + Sort Cepat
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GeneralDropdown(
                    selectedText = selectedMonth,
                    items = monthList,
                    onItemSelected = onMonthSelected,
                    icon = Icons.Rounded.CalendarToday,
                    modifier = Modifier.weight(1f),
                    dropdownWidth = 175.dp,
                    backgroundColor = PastelBlueSoftBg,
                    shapeRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSortColumnClicked(ReportSortColumn.DATE) },
                    shape = RoundedCornerShape(14.dp),
                    color = PastelNeutralBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "Urutkan",
                            tint = GrayBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAscending && sortColumn == ReportSortColumn.DATE) "Terlama" else "Terbaru",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // 3. KPI Ringkasan Proporsional (Full-width agar angka & persentase tidak pernah terpotong)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PhoneReportKpiCard(
                    icon = Icons.Rounded.Payments,
                    label = "Pendapatan (${comparison.targetShortLabel})",
                    value = CurrencyUtils.formatRupiahWithSymbol(comparison.targetRevenue),
                    compareLabel = "vs ${comparison.compareShortLabel}",
                    trendPercent = comparison.revenueTrendPercent
                )
                PhoneReportKpiCard(
                    icon = Icons.Rounded.Receipt,
                    label = "Jumlah Order (${comparison.targetShortLabel})",
                    value = "${comparison.targetOrderCount} Order",
                    compareLabel = "vs ${comparison.compareShortLabel}",
                    trendPercent = comparison.orderTrendPercent
                )
            }
        }

        // 4. Search Bar Full-Width
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Cari nama, layanan, item, atau ID...", fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Hapus pencarian",
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
                    focusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.5f),
                    unfocusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.35f),
                    focusedBorderColor = PastelBlue,
                    unfocusedBorderColor = PastelBlueBg,
                    focusedLeadingIconColor = PastelBlue,
                    unfocusedLeadingIconColor = PastelNeutralText,
                    focusedTrailingIconColor = PastelBlue,
                    unfocusedTrailingIconColor = PastelNeutralText,
                    cursorColor = PastelBlue
                )
            )
        }

        // 5. Status Filter Chips (Horizontal Scrollable agar tidak wrap vertikal)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(statusFilters) { status ->
                    val isSelected = selectedStatusFilter.equals(status, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onStatusFilterChanged(status) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PastelBlue else PastelBlueSoftBg.copy(alpha = 0.55f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PastelBlue else PastelBlueBg
                        )
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else GrayBlue,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Info jumlah transaksi tampil
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Transaksi",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${filteredAndSortedOrders.size} transaksi",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PastelNeutralText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // 6. Daftar Kartu Transaksi
        if (isLoading && orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PastelBlue)
                }
            }
        } else if (filteredAndSortedOrders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = PastelNeutralBg
                ) {
                    Text(
                        text = "Tidak ada transaksi yang sesuai filter.",
                        color = PastelNeutralText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 28.dp, horizontal = 16.dp)
                    )
                }
            }
        } else {
            items(filteredAndSortedOrders, key = { it.orderId }) { order ->
                PhoneReportOrderCard(
                    order = order,
                    services = services,
                    formattedDate = dateFormat.format(order.orderDate.toDate()),
                    onClick = { selectedOrderForReceipt = order }
                )
            }
        }

        // 7. Tombol Muat Bulan Lebih Lama
        item {
            OutlinedButton(
                onClick = onLoadMoreMonths,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PastelBlueBg)
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = PastelBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Muat Data Bulan Sebelumnya",
                    color = PastelBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }

    // BottomSheet Detail Nota untuk transaksi yang diklik
    selectedOrderForReceipt?.let { order ->
        PhoneReportReceiptBottomSheet(
            order = order,
            services = services,
            onDismiss = { selectedOrderForReceipt = null }
        )
    }
}

@Composable
private fun PhoneReportKpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    compareLabel: String,
    trendPercent: Double?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PastelBlueSoftBg.copy(alpha = 0.55f),
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PastelBlueIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PastelBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PastelNeutralText,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (trendPercent != null) {
                    val isPositive = trendPercent >= 0.0
                    val badgeBg = if (isPositive) PastelGreenBg else PastelRoseBg
                    val badgeColor = if (isPositive) PastelGreen else PastelRoseText
                    val sign = if (isPositive) "+" else "-"
                    val formattedPercent = String.format(Locale.getDefault(), "%s%.1f%%", sign, abs(trendPercent))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = formattedPercent,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
                Text(
                    text = compareLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PastelNeutralText,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PhoneReportOrderCard(
    order: Orders,
    services: List<Services>,
    formattedDate: String,
    onClick: () -> Unit
) {
    val status = order.status ?: "On Queue"
    val (statusBg, statusColor) = when {
        status.equals("Done", ignoreCase = true) -> PastelGreenBg to PastelGreen
        status.equals("On Process", ignoreCase = true) -> PastelBlueBg to PastelBlue
        else -> PastelAmberBg to PastelAmber
    }

    val totalQty = remember(order.orderItems) {
        order.orderItems.sumOf { it.itemQuantity ?: 0 }
    }
    val serviceSummary = remember(order.orderItems, services) {
        order.orderItems
            .mapNotNull { item -> services.find { it.serviceId == item.serviceId }?.serviceName }
            .distinct()
            .joinToString(", ")
            .ifBlank { "${order.orderItems.size} item" }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, PastelBlueBg),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.customerName ?: "Pelanggan",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$serviceSummary • $totalQty pcs",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayBlue.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PastelNeutralText
                    )
                )
            }

            HorizontalDivider(color = PastelBlueSoftBg)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (order.alreadyPaid) PastelGreenBg else PastelAmberBg
                    ) {
                        Text(
                            text = if (order.alreadyPaid) "Lunas" else "Belum Bayar",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (order.alreadyPaid) PastelGreen else PastelAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    if (order.isDone()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (order.alreadyPickedUp) PastelGreenBg else PastelBlueSoftBg
                        ) {
                            Text(
                                text = if (order.alreadyPickedUp) "Diambil" else "Siap Diambil",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (order.alreadyPickedUp) PastelGreen else PastelBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice ?: 0.0),
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = PastelBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneReportReceiptBottomSheet(
    order: Orders,
    services: List<Services>,
    onDismiss: () -> Unit
) {
    val groupedItems = remember(order.orderItems, services) {
        groupOrderItemsByService(order.orderItems, services)
    }
    val totalQty = remember(order.orderItems) {
        order.orderItems.sumOf { it.itemQuantity ?: 0 }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
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
                            .height(32.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = "Nota Pesanan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        )
                        Text(
                            text = "Order #${order.orderId.take(8)}",
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
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${order.orderItems.size} item",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = order.customerName ?: "Pelanggan",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = PastelNeutralText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Masuk: ${dateFormat.format(order.orderDate.toDate())}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelNeutralText
                            )
                        )
                    }
                }
            }

            ReceiptDashedDivider(color = PastelBlueBg)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedItems.forEach { (serviceName, itemsInService) ->
                    ReceiptServiceSection(
                        serviceName = serviceName,
                        items = itemsInService
                    )
                }
            }

            ReceiptDashedDivider(color = PastelBlueBg)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Pembayaran",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "$totalQty pcs",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice ?: 0.0),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PastelBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

private fun Timestamp.toReportMonthYearString(): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
    return sdf.format(this.toDate())
}

private fun Timestamp.toReportYearMonthKey(): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply { time = toDate() }
    return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
}

private data class PhoneReportMonthlyComparison(
    val targetShortLabel: String,
    val compareShortLabel: String,
    val targetOrderCount: Int,
    val compareOrderCount: Int,
    val targetRevenue: Double,
    val compareRevenue: Double
) {
    val orderTrendPercent: Double?
        get() = if (compareOrderCount > 0) {
            ((targetOrderCount - compareOrderCount).toDouble() / compareOrderCount) * 100.0
        } else null

    val revenueTrendPercent: Double?
        get() = if (compareRevenue > 0.0) {
            ((targetRevenue - compareRevenue) / compareRevenue) * 100.0
        } else null
}

private fun buildPhoneReportComparison(
    orders: List<Orders>,
    selectedMonth: String,
    now: Date = Date()
): PhoneReportMonthlyComparison {
    val idLocale = Locale.forLanguageTag("id-ID")
    val fullMonthFormat = SimpleDateFormat("MMMM yyyy", idLocale)
    val shortMonthFormat = SimpleDateFormat("MMM yyyy", idLocale)

    val targetCal = Calendar.getInstance().apply {
        time = now
        set(Calendar.DAY_OF_MONTH, 1)
    }
    if (selectedMonth == "Semua Bulan") {
        targetCal.add(Calendar.MONTH, -1)
    } else {
        val parsed = runCatching { fullMonthFormat.parse(selectedMonth) }.getOrNull()
        if (parsed != null) {
            targetCal.time = parsed
        } else {
            targetCal.add(Calendar.MONTH, -1)
        }
    }

    val compareCal = (targetCal.clone() as Calendar).apply {
        add(Calendar.MONTH, -1)
    }

    val targetKey = targetCal.get(Calendar.YEAR) to targetCal.get(Calendar.MONTH)
    val compareKey = compareCal.get(Calendar.YEAR) to compareCal.get(Calendar.MONTH)

    var targetCount = 0
    var compareCount = 0
    var targetRev = 0.0
    var compareRev = 0.0

    orders.forEach { order ->
        val key = order.orderDate.toReportYearMonthKey()
        val price = order.totalPrice ?: 0.0
        when (key) {
            targetKey -> {
                targetCount++
                targetRev += price
            }
            compareKey -> {
                compareCount++
                compareRev += price
            }
        }
    }

    return PhoneReportMonthlyComparison(
        targetShortLabel = shortMonthFormat.format(targetCal.time),
        compareShortLabel = shortMonthFormat.format(compareCal.time),
        targetOrderCount = targetCount,
        compareOrderCount = compareCount,
        targetRevenue = targetRev,
        compareRevenue = compareRev
    )
}
