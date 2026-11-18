package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.analytics.ReportSortColumn
import com.aprilarn.washflow.ui.theme.Gray
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
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseText
import com.aprilarn.washflow.ui.theme.PastelSlateBg
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OrderReportPanel(
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
            val serviceMatch = order.orderItems.any { item ->
                val sName = services.find { it.serviceId == item.serviceId }?.serviceName ?: item.serviceId ?: ""
                sName.contains(searchQuery, ignoreCase = true)
            }
            customerNameMatch || itemsMatch || idMatch || serviceMatch
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
            ReportSortColumn.FINISH_DATE -> compareBy<Orders> { it.orderFinishDate?.toDate()?.time ?: 0L }
        }
        if (isAscending) monthFiltered.sortedWith(comparator)
        else monthFiltered.sortedWith(comparator.reversed())
    }

    // Ringkasan perbandingan bulanan (Opsi 2: Semua Bulan -> bulan lalu vs 2 bulan lalu;
    // bulan terpilih -> bulan tersebut vs 1 bulan sebelumnya), dihitung dari seluruh order bulan terkait.
    val reportSummary = remember(orders, selectedMonth) {
        buildReportMonthlyComparison(orders = orders, selectedMonth = selectedMonth)
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var summaryHeightPx by remember { mutableIntStateOf(0) }
    var summaryOffsetPx by remember { mutableFloatStateOf(0f) }

    val collapsibleScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (summaryHeightPx <= 0) return Offset.Zero
                val minOffset = -summaryHeightPx.toFloat()

                // Scroll ke bawah -> sembunyikan summary bertahap
                if (available.y < 0f && listState.canScrollForward && summaryOffsetPx > minOffset) {
                    val prev = summaryOffsetPx
                    summaryOffsetPx = (summaryOffsetPx + available.y).coerceIn(minOffset, 0f)
                    return Offset(0f, summaryOffsetPx - prev)
                }

                // Scroll ke atas -> tampilkan summary kembali bertahap
                if (available.y > 0f && summaryOffsetPx < 0f) {
                    val prev = summaryOffsetPx
                    summaryOffsetPx = (summaryOffsetPx + available.y).coerceIn(minOffset, 0f)
                    return Offset(0f, summaryOffsetPx - prev)
                }

                return Offset.Zero
            }
        }
    }

    // Snap ke terbuka/tertutup penuh setelah scroll berhenti, atau buka kembali jika tabel tidak bisa discroll
    LaunchedEffect(
        listState.isScrollInProgress,
        listState.canScrollForward,
        listState.canScrollBackward,
        sortedOrders.size
    ) {
        if (!listState.isScrollInProgress) {
            val minOffset = -summaryHeightPx.toFloat()
            val canScroll = listState.canScrollForward || listState.canScrollBackward
            val targetOffset = when {
                !canScroll -> 0f
                summaryHeightPx > 0 && summaryOffsetPx < 0f && summaryOffsetPx > minOffset -> {
                    if (summaryOffsetPx < minOffset / 2f) minOffset else 0f
                }
                else -> null
            }
            if (targetOffset != null && targetOffset != summaryOffsetPx) {
                AnimationState(initialValue = summaryOffsetPx).animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(durationMillis = 180)
                ) {
                    summaryOffsetPx = value
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .nestedScroll(collapsibleScrollConnection)
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

            // Dropdown Filter Bulan Menggunakan GeneralDropdown
            GeneralDropdown(
                selectedText = selectedMonth,
                items = monthList,
                onItemSelected = { onMonthSelected(it) },
                icon = Icons.Rounded.CalendarToday, // Menambahkan icon custom jika diperlukan oleh GeneralDropdown
                modifier = Modifier,
                dropdownWidth = 160.dp, // Sedikit lebih lebar untuk nama bulan
                backgroundColor = Color(0xFFF3F4F6),
                shapeRadius = 20.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // --- Collapsible Metric Summary Cards (Pembukuan & Cakupan Data) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    if (placeable.height > 0) {
                        summaryHeightPx = placeable.height
                    }
                    val offsetInt = summaryOffsetPx.roundToInt()
                    val visibleHeight = (placeable.height + offsetInt).coerceIn(0, placeable.height)
                    val alpha = if (placeable.height > 0) {
                        (visibleHeight.toFloat() / placeable.height.toFloat()).coerceIn(0f, 1f)
                    } else 1f
                    layout(placeable.width, visibleHeight) {
                        placeable.placeRelativeWithLayer(0, offsetInt) {
                            this.alpha = alpha
                        }
                    }
                }
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Summary Card 1: Jumlah Order (Bulan Lalu / Bulan Terpilih vs Bulan Sebelumnya)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PastelNeutralBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PastelSlateBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Receipt, contentDescription = null, tint = GrayBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order ${reportSummary.targetShortLabel}",
                                    fontSize = 11.sp,
                                    color = Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                GrowthChip(
                                    growthPercent = reportSummary.orderTrendPercent,
                                    showSign = true
                                )
                            }
                            Text(
                                text = "${reportSummary.targetOrderCount} Order",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (reportSummary.compareOrderCount > 0) {
                                    "vs ${reportSummary.compareShortLabel} (${reportSummary.compareOrderCount} order)"
                                } else {
                                    "Belum ada pembanding (${reportSummary.compareShortLabel})"
                                },
                                fontSize = 10.sp,
                                color = Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Summary Card 2: Total Pendapatan (Semua Order Bulan Lalu / Bulan Terpilih vs Bulan Sebelumnya)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PastelBlueSoftBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PastelBlueIconBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Payments, contentDescription = null, tint = PastelBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pendapatan ${reportSummary.targetShortLabel}",
                                    fontSize = 11.sp,
                                    color = Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                GrowthChip(
                                    growthPercent = reportSummary.revenueTrendPercent,
                                    showSign = true
                                )
                            }
                            Text(
                                text = CurrencyUtils.formatRupiahWithSymbol(reportSummary.targetRevenue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (reportSummary.compareRevenue > 0.0) {
                                    "vs ${reportSummary.compareShortLabel} (${CurrencyUtils.formatRupiahWithSymbol(reportSummary.compareRevenue)})"
                                } else {
                                    "Belum ada pembanding (${reportSummary.compareShortLabel})"
                                },
                                fontSize = 10.sp,
                                color = PastelBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = GrayBlue
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .width(260.dp)
                            .height(44.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text("Cari nama/order...", fontSize = 13.sp, color = Color.Gray)
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onSearchQueryChanged("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Table Header Row with Column Sorting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("Tanggal Masuk", ReportSortColumn.DATE, sortColumn, isAscending, 1.3f, onSortColumnClicked)
                    TableHeaderCell("Nama Pelanggan", ReportSortColumn.CUSTOMER_NAME, sortColumn, isAscending, 1.3f, onSortColumnClicked)
                    TableHeaderCell("Order", ReportSortColumn.ORDER_ITEMS, sortColumn, isAscending, 2.7f, onSortColumnClicked)
                    TableHeaderCell("Total", ReportSortColumn.TOTAL_PRICE, sortColumn, isAscending, 1.2f, onSortColumnClicked)
                    TableHeaderCell("Tgl. Selesai", ReportSortColumn.FINISH_DATE, sortColumn, isAscending, 1.1f, onSortColumnClicked)
                    TableHeaderCell("Status Order", ReportSortColumn.STATUS, sortColumn, isAscending, 1.1f, onSortColumnClicked)
                    TableHeaderCell("Bayar", null, sortColumn, isAscending, 0.9f, onSortColumnClicked)
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

                        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                            groupedByMonth.forEach { (monthName, orderList) ->
                                // Month Section Banner (Sticky Header)
                                stickyHeader {
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
                                    OrderLedgerRow(order = order, services = services)
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(onClick = onLoadMoreMonths) {
                                        Text("Load 1 more month data")
                                    }
                                }
                            }
                        }
                    } else {
                        // Jika memilih bulan spesifik, tampilkan baris langsung
                        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                            items(sortedOrders) { order ->
                                OrderLedgerRow(order = order, services = services)
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(onClick = onLoadMoreMonths) {
                                        Text("Load 1 more month data")
                                    }
                                }
                            }
                        }
                    }
                }
            } // Close Column
                
            // FAB for Scroll to Top
            ScrollToTopFab(
                listState = listState,
                onScrollToTop = {
                    coroutineScope.launch {
                        if (summaryOffsetPx < 0f) {
                            AnimationState(initialValue = summaryOffsetPx).animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 220)
                            ) {
                                summaryOffsetPx = value
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 24.dp)
            )
        } // Close Box
        } // Close Card
    } // Close outer Column
} // Close OrderReportPanel function

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
private fun OrderLedgerRow(
    order: Orders,
    services: List<Services>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 1. Tanggal Masuk (Digabung dengan Waktu)
        Text(
            text = order.orderDate.toDateString() + "\n" + order.orderDate.toTimeString(),
            fontSize = 12.sp,
            color = GrayBlue,
            lineHeight = 16.sp,
            modifier = Modifier
                .weight(1.3f)
                .padding(top = 2.dp)
        )

        // 2. Nama Pelanggan
        Text(
            text = order.customerName ?: "Tanpa Nama",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GrayBlue,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1.3f)
                .padding(top = 2.dp)
        )

        // 3. Order (List Barang memanjang ke bawah per Kategori & New Line)
        Column(
            modifier = Modifier
                .weight(2.7f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (order.orderItems.isEmpty()) {
                Text("-", fontSize = 12.sp, color = Color.Gray)
            } else {
                val groupedItems = remember(order.orderItems, services) {
                    order.orderItems.groupBy { item ->
                        val matchedService = services.find { it.serviceId == item.serviceId }
                        matchedService?.serviceName?.ifBlank { null }
                            ?: item.serviceId?.ifBlank { null }
                            ?: "Layanan Utama"
                    }
                }

                val entries = groupedItems.entries.toList()
                entries.forEachIndexed { index, entry ->
                    val (serviceName, items) = entry
                    val categoryTotal = items.sumOf { it.subtotal ?: ((it.itemPrice ?: 0.0) * (it.itemQuantity ?: 1)) }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Label Header Kategori dan Total Harga
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PastelSlateBg
                            ) {
                                Text(
                                    text = serviceName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            
                            if (categoryTotal > 0) {
                                Text(
                                    text = CurrencyUtils.formatRupiahWithSymbol(categoryTotal),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrayBlue.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // List Barang di Bawah Kategori
                        items.forEach { item ->
                            val qty = item.itemQuantity ?: 1
                            val itemTotal = item.subtotal ?: ((item.itemPrice ?: 0.0) * qty)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${item.itemName ?: "Item"} ($qty)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                                
                                if (itemTotal > 0) {
                                    Text(
                                        text = CurrencyUtils.formatRupiahWithSymbol(itemTotal),
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                    )
                                }
                            }
                        }
                    }

                    if (index < entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // 5. Total Price
        Text(
            text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GrayBlue,
            modifier = Modifier
                .weight(1.2f)
                .padding(top = 2.dp)
        )

        // 6. Tanggal Selesai
        Text(
            text = order.orderFinishDate?.let { 
                it.toDateString() + "\n" + it.toTimeString() 
            } ?: "-",
            fontSize = 11.sp,
            color = if (order.orderFinishDate != null) PastelGreen else Color.Gray,
            lineHeight = 16.sp,
            modifier = Modifier
                .weight(1.1f)
                .padding(top = 2.dp)
        )

        // 7. Status Order Badge
        Box(
            modifier = Modifier
                .weight(1.1f)
                .padding(top = 2.dp)
        ) {
            StatusBadge(status = order.status ?: "On Queue")
        }

        // 8. Status Pembayaran Badge
        Box(
            modifier = Modifier
                .weight(0.9f)
                .padding(top = 2.dp)
        ) {
            PaymentBadge(alreadyPaid = order.alreadyPaid)
        }
    }
}

@Composable
internal fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "done", "completed" -> Triple(PastelGreenBg, PastelGreen, "Done")
        "on process", "in progress" -> Triple(PastelBlueBg, PastelBlue, "On Process")
        else -> Triple(PastelAmberBg, PastelAmber, "On Queue")
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
internal fun PaymentBadge(alreadyPaid: Boolean) {
    val bgColor = if (alreadyPaid) PastelGreenBg else PastelRoseBg
    val textColor = if (alreadyPaid) PastelGreen else PastelRoseText
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
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
    return sdf.format(this.toDate())
}

private fun Timestamp.toYearMonthKey(): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply { time = toDate() }
    return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
}

private data class ReportMonthlyComparison(
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

private fun buildReportMonthlyComparison(
    orders: List<Orders>,
    selectedMonth: String,
    now: Date = Date()
): ReportMonthlyComparison {
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
        val key = order.orderDate.toYearMonthKey()
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

    return ReportMonthlyComparison(
        targetShortLabel = shortMonthFormat.format(targetCal.time),
        compareShortLabel = shortMonthFormat.format(compareCal.time),
        targetOrderCount = targetCount,
        compareOrderCount = compareCount,
        targetRevenue = targetRev,
        compareRevenue = compareRev
    )
}

@Composable
fun ScrollToTopFab(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onScrollToTop: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    val progress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo.size
            val maxScrollable = maxOf(0, totalItems - visibleItems)
            if (maxScrollable == 0) 0f
            else {
                val currentItem = listState.firstVisibleItemIndex
                (currentItem.toFloat() / maxScrollable).coerceIn(0f, 1f)
            }
        }
    }
    
    val isNotAtTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(listState.isScrollInProgress, isNotAtTop) {
        if (isNotAtTop) {
            isVisible = true
            if (!listState.isScrollInProgress) {
                delay(2000)
                isVisible = false
            }
        } else {
            isVisible = false
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 150),
        label = "progressAnimation"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = GrayBlue,
                trackColor = Color.LightGray.copy(alpha = 0.5f),
                strokeWidth = 3.dp
            )
            FloatingActionButton(
                onClick = { 
                    onScrollToTop()
                    coroutineScope.launch { 
                        listState.animateScrollToItem(0) 
                    } 
                },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                containerColor = Color(0xFFEDEEF4), // Warna GrayBlue yang sangat terang (mendekati putih)
                contentColor = GrayBlue,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 700)
@Composable
fun OrderReportPanelPreview() {
    val mockServices = listOf(
        Services("s1", "Laundry Satuan"),
        Services("s2", "Dry Clean"),
        Services("s3", "Setrika Saja")
    )

    val nowCal = Calendar.getInstance()
    val oneMonthAgo = (nowCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.time
    val twoMonthsAgo = (nowCal.clone() as Calendar).apply { add(Calendar.MONTH, -2) }.time

    val mockOrders = listOf(
        Orders(
            orderId = "1",
            customerId = "c1",
            customerName = "Budi Santoso",
            orderDate = Timestamp.now(),
            status = "On Process",
            alreadyPaid = false,
            totalPrice = 55000.0,
            orderItems = listOf(
                com.aprilarn.washflow.data.model.OrderItem(
                    itemId = "i1",
                    itemName = "Kemeja",
                    serviceId = "s1",
                    itemQuantity = 3,
                    itemPrice = 15000.0,
                    subtotal = 45000.0
                ),
                com.aprilarn.washflow.data.model.OrderItem(
                    itemId = "i2",
                    itemName = "Celana Jeans",
                    serviceId = "s1",
                    itemQuantity = 1,
                    itemPrice = 10000.0,
                    subtotal = 10000.0
                )
            )
        ),
        Orders(
            orderId = "2",
            customerId = "c2",
            customerName = "Siti Aminah",
            orderDate = Timestamp(oneMonthAgo),
            status = "Done",
            alreadyPaid = true,
            totalPrice = 120000.0,
            orderItems = listOf(
                com.aprilarn.washflow.data.model.OrderItem(
                    itemId = "i3",
                    itemName = "Jas Pria",
                    serviceId = "s2",
                    itemQuantity = 1,
                    itemPrice = 120000.0,
                    subtotal = 120000.0
                )
            )
        ),
        Orders(
            orderId = "3",
            customerId = "c3",
            customerName = "Andi Pratama",
            orderDate = Timestamp(oneMonthAgo),
            status = "Done",
            alreadyPaid = true,
            totalPrice = 80000.0,
            orderItems = listOf(
                com.aprilarn.washflow.data.model.OrderItem(
                    itemId = "i4",
                    itemName = "Bed Cover",
                    serviceId = "s1",
                    itemQuantity = 2,
                    itemPrice = 40000.0,
                    subtotal = 80000.0
                )
            )
        ),
        Orders(
            orderId = "4",
            customerId = "c4",
            customerName = "Dewi Lestari",
            orderDate = Timestamp(twoMonthsAgo),
            status = "Done",
            alreadyPaid = true,
            totalPrice = 160000.0,
            orderItems = listOf(
                com.aprilarn.washflow.data.model.OrderItem(
                    itemId = "i5",
                    itemName = "Gaun Pesta",
                    serviceId = "s2",
                    itemQuantity = 2,
                    itemPrice = 80000.0,
                    subtotal = 160000.0
                )
            )
        )
    )

    MaterialTheme {
        Box(modifier = Modifier.background(Color.White)) {
            OrderReportPanel(
                orders = mockOrders,
                services = mockServices,
                isLoading = false,
                selectedMonth = "Semua Bulan",
                searchQuery = "",
                selectedStatusFilter = "Semua",
                sortColumn = ReportSortColumn.DATE,
                isAscending = false,
                onMonthSelected = {},
                onSearchQueryChanged = {},
                onStatusFilterChanged = {},
                onSortColumnClicked = {},
                onLoadMoreMonths = {}
            )
        }
    }
}
