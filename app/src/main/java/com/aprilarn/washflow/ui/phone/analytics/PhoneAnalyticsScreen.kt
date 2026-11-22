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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.aprilarn.washflow.ui.analytics.component.OrderReportPanel
import com.aprilarn.washflow.ui.analytics.component.PendingOrdersPanel
import com.aprilarn.washflow.ui.analytics.component.RecentOrderItem
import com.aprilarn.washflow.ui.analytics.component.SpendingChart
import com.aprilarn.washflow.ui.analytics.component.UpcomingDeadlinesCard
import com.aprilarn.washflow.ui.analytics.filterRecentOrders
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg

/**
 * Tampilan Analytics 1-Kolom Vertikal khusus HP biasa (Batang / Portrait).
 * Menggantikan SidebarMenu vertikal dengan Segmented Tab Bar horizontal di atas.
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
                            fontWeight = FontWeight.Bold
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
            color = Color.White
        ) {
            when (uiState.selectedTab) {
                "Report" -> {
                    OrderReportPanel(
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
                    PendingOrdersPanel(
                        activeOrders = uiState.activeOrders,
                        customers = uiState.customers,
                        isLoading = uiState.isLoading,
                        onTogglePayment = onTogglePayment,
                        onTogglePickup = onTogglePickup
                    )
                }
                else -> {
                    val summary = remember(uiState.orders) { buildOrderDashboardSummary(uiState.orders) }
                    var recentFilter by rememberSaveable { mutableStateOf(RecentOrderFilter.ALL) }
                    val recentOrders = remember(uiState.orders, recentFilter) {
                        filterRecentOrders(uiState.orders, recentFilter)
                    }
                    var selectedChartPeriod by rememberSaveable { mutableStateOf("Day") }
                    val pagerState = rememberPagerState(pageCount = { 2 })

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // A. Ringkasan Bulanan
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Ringkasan",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue
                            )
                            MonthlySummaryCard(
                                summary = summary,
                                containerColor = ThemeBgGray
                            )
                        }

                        // B. Grafik Pendapatan & Pelanggan
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = ThemeBgGray
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
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
                                Text(
                                    text = "Deadline Terdekat",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrayBlue
                                )
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
                                Text(
                                    text = "Order Terbaru",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrayBlue
                                )
                                GeneralDropdown(
                                    selectedText = recentFilter.label,
                                    items = RecentOrderFilter.entries.map { it.label },
                                    onItemSelected = { recentFilter = RecentOrderFilter.fromLabel(it) },
                                    icon = Icons.Default.FilterList,
                                    dropdownWidth = 140.dp,
                                    backgroundColor = ThemeBgGray
                                )
                            }

                            if (uiState.isLoading && uiState.orders.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GrayBlue)
                                }
                            } else if (recentOrders.isEmpty()) {
                                Text(
                                    text = "Belum ada order untuk filter ini",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    recentOrders.take(15).forEach { order ->
                                        RecentOrderItem(order)
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
