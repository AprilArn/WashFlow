package com.aprilarn.washflow.ui.phone.analytics

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.analytics.AnalyticsUiState
import com.aprilarn.washflow.ui.analytics.RecentOrderFilter
import com.aprilarn.washflow.ui.analytics.ReportSortColumn
import com.aprilarn.washflow.ui.analytics.ThemeBgGray
import com.aprilarn.washflow.ui.analytics.buildOrderDashboardSummary
import com.aprilarn.washflow.ui.analytics.component.AnalyticsChartType
import com.aprilarn.washflow.ui.analytics.component.DeadlineCountBadge
import com.aprilarn.washflow.ui.analytics.component.GeneralDropdown
import com.aprilarn.washflow.ui.analytics.component.MonthlySummaryCard
import com.aprilarn.washflow.ui.analytics.component.RecentOrderItem
import com.aprilarn.washflow.ui.analytics.component.SpendingChart
import com.aprilarn.washflow.ui.analytics.component.UpcomingDeadlinesCard
import com.aprilarn.washflow.ui.analytics.filterRecentOrders
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import kotlinx.coroutines.launch

/**
 * Tampilan Analytics 1-Kolom Vertikal khusus HP biasa (Batang / Portrait).
 * Menggunakan Segmented Tab Bar horizontal di atas dan layout proporsional khusus mobile
 * untuk ketiga tab (Ringkasan, Buku Besar, dan Tertunda).
 */
@Composable
fun PhoneAnalyticsScreen(
    uiState: AnalyticsUiState,
    onTabClick: (String) -> Unit,
    onMonthSelected: (String) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onStatusFilterChanged: (String) -> Unit = {},
    onSortColumnClicked: (ReportSortColumn) -> Unit = {},
    onLoadMoreMonths: () -> Unit = {},
    onTogglePayment: (String, Boolean) -> Unit = { _, _ -> },
    onTogglePickup: (String, Boolean) -> Unit = { _, _ -> },
    onToastShown: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onToastShown()
        }
    }

    val tabs = remember {
        listOf(
            "Order" to "Ringkasan",
            "Report" to "Buku Besar",
            "Tertunda" to "Tertunda"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Horizontal Segmented Tab Bar untuk HP
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEach { (tabKey, tabLabel) ->
                    val isSelected = uiState.selectedTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) PastelBlue else PastelBlueSoftBg.copy(alpha = 0.5f))
                            .clickable { onTabClick(tabKey) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) Color.White else GrayBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Konten Berdasarkan Tab
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            when (uiState.selectedTab) {
                "Report" -> {
                    PhoneOrderReportTab(
                        orders = uiState.orders,
                        services = uiState.services,
                        isLoading = uiState.isLoading,
                        selectedMonth = uiState.selectedMonth,
                        searchQuery = uiState.searchQuery,
                        selectedStatusFilter = uiState.selectedStatusFilter,
                        sortColumn = uiState.sortColumn,
                        isAscending = uiState.isAscending,
                        onMonthSelected = onMonthSelected,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onStatusFilterChanged = onStatusFilterChanged,
                        onSortColumnClicked = onSortColumnClicked,
                        onLoadMoreMonths = onLoadMoreMonths
                    )
                }

                "Tertunda" -> {
                    PhonePendingOrdersTab(
                        activeOrders = uiState.activeOrders,
                        customers = uiState.customers,
                        isLoading = uiState.isLoading,
                        onTogglePayment = onTogglePayment,
                        onTogglePickup = onTogglePickup
                    )
                }

                else -> {
                    PhoneAnalyticsSummaryTab(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun PhoneAnalyticsSummaryTab(
    uiState: AnalyticsUiState
) {
    val summary = remember(uiState.orders) { buildOrderDashboardSummary(uiState.orders) }
    var recentFilter by rememberSaveable { mutableStateOf(RecentOrderFilter.ALL) }
    val recentOrders = remember(uiState.orders, recentFilter) {
        filterRecentOrders(uiState.orders, recentFilter)
    }
    var selectedChartPeriod by rememberSaveable { mutableStateOf("Day") }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // A. Editorial Header + Ringkasan Bulanan
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        text = "Ringkasan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "Performa pendapatan & status antrean",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            MonthlySummaryCard(
                summary = summary,
                containerColor = ThemeBgGray
            )
        }

        // B. Grafik Pendapatan & Pelanggan dengan Switcher Pill
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ThemeBgGray
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val chartTabs = listOf("Pendapatan", "Pelanggan")
                    chartTabs.forEachIndexed { index, label ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) PastelBlueSoftBg else Color.Transparent)
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) PastelBlue else PastelNeutralText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    if (page == 0) {
                        SpendingChart(
                            orders = uiState.orders,
                            selectedPeriod = selectedChartPeriod,
                            onPeriodChanged = { selectedChartPeriod = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        SpendingChart(
                            orders = uiState.orders,
                            selectedPeriod = selectedChartPeriod,
                            onPeriodChanged = { selectedChartPeriod = it },
                            modifier = Modifier.fillMaxWidth(),
                            chartType = AnalyticsChartType.CUSTOMER
                        )
                    }
                }
            }
        }

        // C. Deadline Terdekat
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .height(18.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = "Deadline Terdekat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    )
                }
                DeadlineCountBadge(deadlines = summary.upcomingDeadlines)
            }
            UpcomingDeadlinesCard(
                deadlines = summary.upcomingDeadlines,
                containerColor = ThemeBgGray
            )
        }

        // D. Order Terbaru
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .height(18.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = "Order Terbaru",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    )
                }
                GeneralDropdown(
                    selectedText = recentFilter.label,
                    items = RecentOrderFilter.entries.map { it.label },
                    onItemSelected = { recentFilter = RecentOrderFilter.fromLabel(it) },
                    icon = Icons.Default.FilterList,
                    dropdownWidth = 140.dp,
                    backgroundColor = PastelBlueSoftBg
                )
            }

            if (uiState.isLoading && uiState.orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PastelBlue)
                }
            } else if (recentOrders.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = ThemeBgGray
                ) {
                    Text(
                        text = "Belum ada order untuk filter ini",
                        fontSize = 13.sp,
                        color = PastelNeutralText,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recentOrders.take(15).forEachIndexed { index, order ->
                        RecentOrderItem(order)
                        if (index < recentOrders.take(15).lastIndex) {
                            HorizontalDivider(color = PastelBlueBg.copy(alpha = 0.65f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
