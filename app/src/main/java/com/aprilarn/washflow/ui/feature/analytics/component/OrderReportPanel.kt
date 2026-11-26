package com.aprilarn.washflow.ui.feature.analytics.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.feature.analytics.ANALYTICS_ALL_MONTHS_KEY
import com.aprilarn.washflow.ui.feature.analytics.ANALYTICS_ALL_STATUS_KEY
import com.aprilarn.washflow.ui.feature.analytics.ReportSortColumn
import com.aprilarn.washflow.ui.feature.analytics.isDone
import com.aprilarn.washflow.ui.core.theme.Gray
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.core.theme.PastelRoseText
import com.aprilarn.washflow.ui.core.theme.PastelSlateBg
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
    val allMonthsLabel = stringResource(R.string.analytics_all_months)
    val currentLocale = Locale.getDefault()

    // 1. Ekstrak daftar bulan unik (key = "yyyy-MM", label = localized "MMMM yyyy")
    val monthOptions = remember(orders, allMonthsLabel, currentLocale) {
        val months = orders
            .map { it.orderDate.toMonthKey() to it.orderDate.toMonthYearString(currentLocale) }
            .distinctBy { it.first }
        listOf(ANALYTICS_ALL_MONTHS_KEY to allMonthsLabel) + months
    }
    val isAllMonthsSelected = selectedMonth == ANALYTICS_ALL_MONTHS_KEY || selectedMonth == "Semua Bulan"
    val selectedMonthDisplayLabel = monthOptions.firstOrNull {
        it.first == selectedMonth || it.second.equals(selectedMonth, ignoreCase = true)
    }?.second ?: allMonthsLabel

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
        if (selectedStatusFilter == ANALYTICS_ALL_STATUS_KEY || selectedStatusFilter == "Semua") true
        else (order.status ?: "").equals(selectedStatusFilter, ignoreCase = true)
    }

    // 4. Filter berdasarkan bulan
    val monthFiltered = statusFiltered.filter { order ->
        if (isAllMonthsSelected) true
        else order.orderDate.toMonthKey() == selectedMonth ||
            order.orderDate.toMonthYearString(currentLocale).equals(selectedMonth, ignoreCase = true)
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

    // Ringkasan perbandingan bulanan
    val reportSummary = remember(orders, selectedMonth, currentLocale) {
        buildReportMonthlyComparison(orders = orders, selectedMonth = selectedMonth, locale = currentLocale)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .background(PastelBlue, RoundedCornerShape(2.dp))
                )
                Column {
                    Text(
                        text = stringResource(R.string.analytics_report_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    )
                    Text(
                        text = stringResource(R.string.analytics_report_subtitle),
                        fontSize = 13.sp,
                        color = PastelNeutralText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Dropdown Filter Bulan Menggunakan GeneralDropdown
            GeneralDropdown(
                selectedText = selectedMonthDisplayLabel,
                items = monthOptions.map { it.second },
                onItemSelected = { pickedLabel ->
                    val pickedKey = monthOptions.firstOrNull { it.second == pickedLabel }?.first
                        ?: ANALYTICS_ALL_MONTHS_KEY
                    onMonthSelected(pickedKey)
                },
                icon = Icons.Rounded.CalendarToday,
                modifier = Modifier,
                dropdownWidth = 175.dp,
                backgroundColor = PastelBlueSoftBg,
                shapeRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
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
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = PastelBlueSoftBg.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, PastelBlueBg)
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
                                .background(PastelBlueIconBg, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Receipt, contentDescription = null, tint = PastelBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.analytics_report_orders_card_label, reportSummary.targetShortLabel),
                                    fontSize = 11.sp,
                                    color = PastelNeutralText,
                                    fontWeight = FontWeight.Medium,
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
                                text = stringResource(R.string.analytics_report_order_count_value, reportSummary.targetOrderCount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (reportSummary.compareOrderCount > 0) {
                                    stringResource(
                                        R.string.analytics_report_vs_orders,
                                        reportSummary.compareShortLabel,
                                        reportSummary.compareOrderCount
                                    )
                                } else {
                                    stringResource(R.string.analytics_report_no_comparison, reportSummary.compareShortLabel)
                                },
                                fontSize = 10.sp,
                                color = PastelNeutralText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Summary Card 2: Total Pendapatan (Semua Order Bulan Lalu / Bulan Terpilih vs Bulan Sebelumnya)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = PastelBlueSoftBg.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, PastelBlueBg)
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
                                .background(PastelBlueIconBg, RoundedCornerShape(12.dp)),
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
                                    text = stringResource(R.string.analytics_report_revenue_card_label, reportSummary.targetShortLabel),
                                    fontSize = 11.sp,
                                    color = PastelNeutralText,
                                    fontWeight = FontWeight.Medium,
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
                                    stringResource(
                                        R.string.analytics_report_vs_revenue,
                                        reportSummary.compareShortLabel,
                                        CurrencyUtils.formatRupiahWithSymbol(reportSummary.compareRevenue)
                                    )
                                } else {
                                    stringResource(R.string.analytics_report_no_comparison, reportSummary.compareShortLabel)
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
                val statusOptions = listOf(
                    ANALYTICS_ALL_STATUS_KEY to stringResource(R.string.analytics_filter_all),
                    "On Queue" to stringResource(R.string.home_status_on_queue),
                    "On Process" to stringResource(R.string.home_status_on_process),
                    "Done" to stringResource(R.string.home_status_done)
                )
                statusOptions.forEach { (statusKey, statusLabel) ->
                    val isSelected = selectedStatusFilter == statusKey ||
                        (statusKey == ANALYTICS_ALL_STATUS_KEY && selectedStatusFilter == "Semua")
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onStatusFilterChanged(statusKey) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PastelBlue else PastelBlueSoftBg.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (isSelected) PastelBlue else PastelBlueBg)
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else GrayBlue,
                            maxLines = 1
                        )
                    }
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
                            .height(42.dp)
                            .background(PastelBlueSoftBg.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .border(1.dp, PastelBlueBg, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PastelNeutralText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    stringResource(R.string.analytics_report_search_placeholder),
                                    fontSize = 13.sp,
                                    color = PastelNeutralText
                                )
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
                                    contentDescription = stringResource(R.string.analytics_pending_clear_search_cd),
                                    modifier = Modifier.size(16.dp),
                                    tint = PastelNeutralText
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Digital Logbook / Ledger List Container ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, PastelBlueBg, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Compact Sort Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PastelBlueSoftBg.copy(alpha = 0.45f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.analytics_report_sort_by),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = PastelNeutralText
                            )
                            ReportSortPill(stringResource(R.string.analytics_sort_date), ReportSortColumn.DATE, sortColumn, isAscending, onSortColumnClicked)
                            ReportSortPill(stringResource(R.string.analytics_sort_customer_short), ReportSortColumn.CUSTOMER_NAME, sortColumn, isAscending, onSortColumnClicked)
                            ReportSortPill(stringResource(R.string.analytics_sort_total), ReportSortColumn.TOTAL_PRICE, sortColumn, isAscending, onSortColumnClicked)
                            ReportSortPill(stringResource(R.string.analytics_sort_finish_date_short), ReportSortColumn.FINISH_DATE, sortColumn, isAscending, onSortColumnClicked)
                            ReportSortPill(stringResource(R.string.analytics_sort_status_short), ReportSortColumn.STATUS, sortColumn, isAscending, onSortColumnClicked)
                        }

                        Text(
                            text = stringResource(R.string.analytics_order_count_badge, sortedOrders.size),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PastelNeutralText
                        )
                    }

                    HorizontalDivider(color = PastelBlueBg)

                    // List Content
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PastelBlue, strokeWidth = 3.dp)
                        }
                    } else if (sortedOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(PastelBlueSoftBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Inbox,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = PastelBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.analytics_report_empty_title),
                                    color = GrayBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.analytics_report_empty_subtitle),
                                    color = PastelNeutralText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        // Grouping berdasarkan bulan jika "All Months" dipilih
                        if (isAllMonthsSelected) {
                            val groupedByMonth = sortedOrders.groupBy { it.orderDate.toMonthYearString(currentLocale) }
                            val monthEntries = groupedByMonth.entries.toList()

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState,
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                monthEntries.forEachIndexed { groupIndex, (monthName, orderList) ->
                                    stickyHeader(key = "header_$monthName") {
                                        val monthTotal = orderList.sumOf { it.totalPrice ?: 0.0 }
                                        val monthIndex = orderList.firstOrNull()?.orderDate?.toYearMonthKey()?.second
                                            ?: Calendar.JANUARY
                                        ReportMonthHeader(
                                            monthName = monthName,
                                            monthIndex = monthIndex,
                                            orderCount = orderList.size,
                                            monthTotal = monthTotal,
                                            showTopDivider = groupIndex > 0
                                        )
                                    }

                                    itemsIndexed(orderList, key = { _, it -> it.orderId }) { itemIndex, order ->
                                        val topPadding = if (itemIndex == 0) 12.dp else 10.dp
                                        val bottomPadding = if (itemIndex == orderList.lastIndex) 14.dp else 0.dp
                                        OrderLedgerRow(
                                            order = order,
                                            services = services,
                                            modifier = Modifier.padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = topPadding,
                                                bottom = bottomPadding
                                            )
                                        )
                                    }
                                }
                                item(key = "load_more") {
                                    LoadMoreMonthsButton(onLoadMoreMonths = onLoadMoreMonths)
                                }
                            }
                        } else {
                            // Jika memilih bulan spesifik, tampilkan baris langsung
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(sortedOrders, key = { it.orderId }) { order ->
                                    OrderLedgerRow(order = order, services = services)
                                }
                                item(key = "load_more") {
                                    LoadMoreMonthsButton(onLoadMoreMonths = onLoadMoreMonths)
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
private fun ReportSortPill(
    title: String,
    column: ReportSortColumn,
    activeSortColumn: ReportSortColumn,
    isAscending: Boolean,
    onSortColumnClicked: (ReportSortColumn) -> Unit
) {
    val isCurrentSort = column == activeSortColumn
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isCurrentSort) PastelBlueBg else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSortColumnClicked(column) }
            .padding(
                start = 8.dp,
                end = if (isCurrentSort) 4.dp else 8.dp,
                top = 5.dp,
                bottom = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isCurrentSort) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrentSort) PastelBlue else PastelNeutralText
        )
        if (isCurrentSort) {
            Icon(
                imageVector = if (isAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PastelBlue
            )
        }
    }
}

@Composable
private fun ReportMonthHeader(
    monthName: String,
    monthIndex: Int,
    orderCount: Int,
    monthTotal: Double,
    showTopDivider: Boolean = false
) {
    val theme = monthBannerTheme(monthIndex)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showTopDivider) {
            HorizontalDivider(color = theme.dividerColor)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            MonthBannerIllustration(
                monthIndex = monthIndex,
                modifier = Modifier.matchParentSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .background(theme.accentColor, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = monthName.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.titleColor,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, theme.dividerColor)
                    ) {
                        Text(
                            text = stringResource(R.string.analytics_report_order_count_value, orderCount),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.titleColor.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, theme.accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = CurrencyUtils.formatRupiahWithSymbol(monthTotal),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.totalPillText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = theme.dividerColor)
    }
}

@Composable
private fun LoadMoreMonthsButton(onLoadMoreMonths: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(
            onClick = onLoadMoreMonths,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, PastelBlueBg)
        ) {
            Text(
                text = stringResource(R.string.analytics_report_load_more),
                color = PastelBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun OrderLedgerRow(
    order: Orders,
    services: List<Services>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(order.orderId) { mutableStateOf(false) }

    val customerName = order.customerName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.orders_unnamed_customer)
    val initial = customerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val currentLocale = Locale.getDefault()
    val orderDateText = remember(order.orderDate, currentLocale) {
        "${order.orderDate.toDateString(currentLocale)} • ${order.orderDate.toTimeString(currentLocale)}"
    }
    val finishDateText = remember(order.orderFinishDate, currentLocale) {
        order.orderFinishDate?.toDateString(currentLocale)
    }

    val defaultServiceLabel = stringResource(R.string.analytics_report_default_service)
    val defaultItemLabel = stringResource(R.string.analytics_report_default_item)
    val groupedItems = remember(order.orderItems, services, defaultServiceLabel) {
        order.orderItems.groupBy { item ->
            val matchedService = services.find { it.serviceId == item.serviceId }
            matchedService?.serviceName?.ifBlank { null }
                ?: item.serviceId?.ifBlank { null }
                ?: defaultServiceLabel
        }
    }
    val totalItemQty = remember(order.orderItems) {
        order.orderItems.sumOf { it.itemQuantity ?: 1 }
    }
    val serviceSummaryLabel = if (groupedItems.isEmpty()) null else {
        stringResource(R.string.analytics_report_service_count, groupedItems.size)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PastelNeutralBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = order.orderItems.isNotEmpty()) { isExpanded = !isExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GrayBlue
                    )
                }

                // Info Pelanggan, Tanggal, Ringkasan Item & Badge Status
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MainFontBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(shape = RoundedCornerShape(6.dp), color = PastelSlateBg) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = GrayBlue,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = orderDateText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrayBlue,
                                    maxLines = 1
                                )
                            }
                        }
                        if (finishDateText != null) {
                            Surface(shape = RoundedCornerShape(6.dp), color = PastelGreenBg) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = PastelGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.analytics_report_completed_prefix, finishDateText),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PastelGreen,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Badge Ringkasan Layanan
                    if (serviceSummaryLabel != null) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color.White) {
                            Text(
                                text = serviceSummaryLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Baris Status Order, Pembayaran & Pengambilan
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusBadge(status = order.status ?: "On Queue")
                        PaymentBadge(alreadyPaid = order.alreadyPaid)
                        if (order.isDone()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (order.alreadyPickedUp) PastelGreenBg else PastelAmberBg
                            ) {
                                Text(
                                    text = stringResource(
                                        if (order.alreadyPickedUp) R.string.manage_order_picked_up
                                        else R.string.manage_order_not_picked_up
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.alreadyPickedUp) PastelGreen else PastelAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Nominal + Tombol Rincian Item
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = GrayBlue
                    )

                    if (order.orderItems.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) {
                                        stringResource(R.string.analytics_report_close_details)
                                    } else {
                                        stringResource(R.string.analytics_report_item_count, totalItemQty)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrayBlue
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = stringResource(
                                        if (isExpanded) R.string.analytics_report_close_details_cd
                                        else R.string.analytics_report_view_details_cd
                                    ),
                                    tint = GrayBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Rincian Item per Layanan (Expandable saat kartu diklik)
            AnimatedVisibility(
                visible = isExpanded && groupedItems.isNotEmpty(),
                enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(150)),
                exit = shrinkVertically(animationSpec = tween(160)) + fadeOut(animationSpec = tween(120))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val entries = groupedItems.entries.toList()
                        entries.forEachIndexed { index, (serviceName, items) ->
                            val categoryTotal = items.sumOf {
                                it.subtotal ?: ((it.itemPrice ?: 0.0) * (it.itemQuantity ?: 1))
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = PastelSlateBg) {
                                        Text(
                                            text = serviceName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PastelBlue,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (categoryTotal > 0) {
                                        Text(
                                            text = CurrencyUtils.formatRupiahWithSymbol(categoryTotal),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrayBlue
                                        )
                                    }
                                }

                                items.forEach { item ->
                                    val qty = item.itemQuantity ?: 1
                                    val itemTotal = item.subtotal ?: ((item.itemPrice ?: 0.0) * qty)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${item.itemName ?: defaultItemLabel} ($qty)",
                                            fontSize = 12.sp,
                                            color = MainFontBlack,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (itemTotal > 0) {
                                            Text(
                                                text = CurrencyUtils.formatRupiahWithSymbol(itemTotal),
                                                fontSize = 11.sp,
                                                color = Gray
                                            )
                                        }
                                    }
                                }
                            }

                            if (index < entries.size - 1) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }

                        if (order.orderFinishDate != null) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.analytics_report_received_colon, orderDateText),
                                    fontSize = 11.sp,
                                    color = Gray
                                )
                                Text(
                                    text = stringResource(
                                        R.string.analytics_report_completed_colon,
                                        "${order.orderFinishDate.toDateString(currentLocale)} • ${order.orderFinishDate.toTimeString(currentLocale)}"
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PastelGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatusBadge(status: String) {
    val (bgColor, textColor, labelRes) = when (status.lowercase()) {
        "done", "completed" -> Triple(PastelGreenBg, PastelGreen, R.string.home_status_done)
        "on process", "in progress" -> Triple(PastelBlueBg, PastelBlue, R.string.home_status_on_process)
        else -> Triple(PastelAmberBg, PastelAmber, R.string.home_status_on_queue)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = stringResource(labelRes),
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
    val labelRes = if (alreadyPaid) R.string.manage_order_paid else R.string.manage_order_unpaid

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = stringResource(labelRes),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Helper formatting extensions
private fun Timestamp.toDateString(locale: Locale = Locale.getDefault()): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", locale)
    return sdf.format(this.toDate())
}

private fun Timestamp.toTimeString(locale: Locale = Locale.getDefault()): String {
    val sdf = SimpleDateFormat("HH:mm", locale)
    return sdf.format(this.toDate())
}

private fun Timestamp.toMonthYearString(locale: Locale = Locale.getDefault()): String {
    val sdf = SimpleDateFormat("MMMM yyyy", locale)
    return sdf.format(this.toDate())
}

private fun Timestamp.toMonthKey(): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
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
    locale: Locale = Locale.getDefault(),
    now: Date = Date()
): ReportMonthlyComparison {
    val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    val fullMonthFormat = SimpleDateFormat("MMMM yyyy", locale)
    val shortMonthFormat = SimpleDateFormat("MMM yyyy", locale)

    val targetCal = Calendar.getInstance().apply {
        time = now
        set(Calendar.DAY_OF_MONTH, 1)
    }
    if (selectedMonth == ANALYTICS_ALL_MONTHS_KEY || selectedMonth == "Semua Bulan") {
        targetCal.add(Calendar.MONTH, -1)
    } else {
        val parsed = runCatching { keyFormat.parse(selectedMonth) }.getOrNull()
            ?: runCatching { fullMonthFormat.parse(selectedMonth) }.getOrNull()
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
                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = stringResource(R.string.analytics_report_scroll_top_cd))
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
                    serviceId = "s3",
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
            orderFinishDate = Timestamp(Date(oneMonthAgo.time + 2L * 24 * 60 * 60 * 1000)),
            status = "Done",
            alreadyPaid = true,
            alreadyPickedUp = true,
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
            orderFinishDate = Timestamp(Date(oneMonthAgo.time + 1L * 24 * 60 * 60 * 1000)),
            status = "Done",
            alreadyPaid = true,
            alreadyPickedUp = false,
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
