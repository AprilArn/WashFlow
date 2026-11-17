package com.aprilarn.washflow.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.aprilarn.washflow.ui.analytics.component.*
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import com.aprilarn.washflow.ui.theme.GrayBlue

// --- Konstanta Warna Tema ---
val ThemeBgGray = Color(0xFFF3F4F6)

// --- Main Screen ---

@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onTabClick: (String) -> Unit,
    onMonthSelected: (String) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onStatusFilterChanged: (String) -> Unit = {},
    onSortColumnClicked: (ReportSortColumn) -> Unit = {},
    onLoadMoreMonths: () -> Unit = {},
    onToastShown: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onToastShown()
        }
    }

    val selectedTab = uiState.selectedTab

    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 1. Sidebar Menu (Left)
        SidebarMenu(
            selectedTab = selectedTab,
            onTabClick = onTabClick
        )

        // 2. Main White Container
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            if (selectedTab == "Report") {
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
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp), // Padding disesuaikan sedikit agar lebih seimbang
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    // Left Column (Ringkasan, Deadline & Order Terbaru) - dihitung dari data order realtime
                    val summary = remember(uiState.orders) { buildOrderDashboardSummary(uiState.orders) }
                    var recentFilter by rememberSaveable { mutableStateOf(RecentOrderFilter.ALL) }
                    val recentOrders = remember(uiState.orders, recentFilter) {
                        filterRecentOrders(uiState.orders, recentFilter)
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.02f)) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(26.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Ringkasan",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrayBlue,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                MonthlySummaryCard(summary = summary)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(26.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Deadline terdekat",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrayBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DeadlineCountBadge(deadlines = summary.upcomingDeadlines)
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                UpcomingDeadlinesCard(
                                    deadlines = summary.upcomingDeadlines,
                                    containerColor = ThemeBgGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Order terbaru", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                            GeneralDropdown(
                                selectedText = recentFilter.label,
                                items = RecentOrderFilter.entries.map { it.label },
                                onItemSelected = { recentFilter = RecentOrderFilter.fromLabel(it) },
                                icon = Icons.Default.FilterList,
                                dropdownWidth = 150.dp,
                                backgroundColor = ThemeBgGray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                            uiState.isLoading && uiState.orders.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GrayBlue)
                                }
                            }
                            recentOrders.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (recentFilter == RecentOrderFilter.ALL) "Belum ada order"
                                        else "Tidak ada order \"${recentFilter.label}\"",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(recentOrders, key = { it.orderId }) { order ->
                                        RecentOrderItem(order)
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }

                    // Right Column (Chart & Cards) - DIBUNGKUS DENGAN SURFACE BARU
                    Surface(
                        modifier = Modifier
                            .width(320.dp) // Sedikit dilebarkan untuk memberi ruang pada padding dalam
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(24.dp), // Radius melengkung disesuaikan dengan tema
                        color = ThemeBgGray // Latar belakang abu-abu
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp) // Padding internal di dalam kontainer abu-abu
                        ) {
                            val pagerState = rememberPagerState(
                                pageCount = { 2 }
                            )
                            
                            // State untuk melacak apakah pengguna sedang berinteraksi dengan grafik (drag/tap label)
                            var isInteractingWithChart by remember { mutableStateOf(false) }
                            
                            // Shared state untuk sinkronisasi periode (Day/Week/Month) di semua chart Pager
                            var selectedChartPeriod by remember { mutableStateOf("Day") }

                            // Gunakan coroutine scope dari luar komponen yang tidak akan tercancel saat recompose
                            val coroutineScope = rememberCoroutineScope()
                            
                            // State untuk animasi progress bar timer
                            val slideProgress = remember { androidx.compose.animation.core.Animatable(0f) }

                            // Efek auto-slide memutar balik (Rewind)
                            LaunchedEffect(pagerState.isScrollInProgress, isInteractingWithChart) {
                                // Hanya jalankan auto-slide jika tidak sedang digeser (scroll) DAN tidak sedang berinteraksi (tap/drag) dengan grafik
                                if (pagerState.isScrollInProgress || isInteractingWithChart) {
                                    slideProgress.snapTo(0f) // Reset progress bar
                                    return@LaunchedEffect
                                }
                                
                                while (true) {
                                    slideProgress.snapTo(0f)
                                    // Animasi progress bar selama 6 detik
                                    slideProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = 6000, 
                                            easing = androidx.compose.animation.core.LinearEasing
                                        )
                                    )
                                    
                                    // Pindah ke Coroutine Scope UI agar tidak tercancel oleh LaunchedEffect
                                    val nextPage = if (pagerState.currentPage == 1) 0 else pagerState.currentPage + 1

                                    // Jangan di-try/catch dalam LaunchedEffect scope, biarkan coroutineScope yang handle
                                    coroutineScope.launch {
                                        try {
                                            pagerState.animateScrollToPage(
                                                page = nextPage,
                                                animationSpec = androidx.compose.animation.core.tween(800)
                                            )
                                        } catch (e: Exception) {
                                            // Abaikan error cancellation jika ada
                                        }
                                    }
                                }
                            }

                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                                if (page == 0) {
                                    SpendingChart(
                                        orders = uiState.orders, 
                                        selectedPeriod = selectedChartPeriod,
                                        onPeriodChanged = { 
                                            selectedChartPeriod = it
                                            isInteractingWithChart = false // Reset timer otomatis setelah milih 
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        onInteractionStateChange = { isInteractingWithChart = it }
                                    )
                                } else {
                                    SpendingChart(
                                        orders = uiState.orders, 
                                        selectedPeriod = selectedChartPeriod,
                                        onPeriodChanged = { 
                                            selectedChartPeriod = it
                                            isInteractingWithChart = false // Reset timer otomatis setelah milih 
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        chartType = AnalyticsChartType.CUSTOMER,
                                        onInteractionStateChange = { isInteractingWithChart = it }
                                    )
                                }
                            }
                            
                            // Pager Indicator dengan Progress Bar Menyatu (Instagram Stories style)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(2) { iteration ->
                                    val isCurrentPage = pagerState.currentPage == iteration
                                    
                                    // Animasi lebar bar (smooth transisi antara titik bulat dan garis memanjang)
                                    val width by androidx.compose.animation.core.animateDpAsState(
                                        targetValue = if (isCurrentPage) 32.dp else 8.dp,
                                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .width(width)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50)) // 50% radius memastikan bentuk selalu pil/bulat sempurna
                                            .background(Color.LightGray.copy(alpha = 0.5f))
                                    ) {
                                        if (isCurrentPage) {
                                            // Isi bar hanya untuk halaman yang sedang aktif (dengan ujung rounded)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(slideProgress.value)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(GrayBlue)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            val topServices = remember(uiState.orders, uiState.services, selectedChartPeriod) {
                                buildTopServices(uiState.orders, uiState.services, period = selectedChartPeriod)
                            }
                            val topServicesPeriodText = topServicesPeriodLabel(selectedChartPeriod)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Layanan terlaris", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                                Text(text = topServicesPeriodText, color = Color.Gray, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (topServices.isEmpty()) {
                                TopServicesEmptyState(periodLabel = topServicesPeriodText)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(topServices, key = { _, it -> it.serviceId }) { index, service ->
                                        TopServiceItem(rank = index + 1, service = service)
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

@Preview(showBackground = true, widthDp = 960, heightDp = 600)
@Composable
fun AnalyticsScreenOrderPreview() {
    val hour = 60 * 60 * 1000L
    val now = System.currentTimeMillis()
    fun ts(offsetHours: Long) = Timestamp(java.util.Date(now + offsetHours * hour))

    val previewOrders = listOf(
        Orders(orderId = "1", customerId = "c1", customerName = "Budi Santoso", orderDate = ts(-2), orderDueDate = ts(-1),
            status = "On Process", totalPrice = 55000.0, orderItems = listOf(OrderItem(serviceId = "s1", itemQuantity = 4, subtotal = 55000.0))),
        Orders(orderId = "2", customerId = "c2", customerName = "Siti Aminah", orderDate = ts(-5), orderDueDate = ts(5),
            status = "On Queue", totalPrice = 120000.0, alreadyPaid = true, orderItems = listOf(OrderItem(serviceId = "s2", itemQuantity = 2, subtotal = 120000.0))),
        Orders(orderId = "3", customerId = "c3", customerName = "Citra Lestari", orderDate = ts(-20), orderDueDate = ts(30),
            status = "On Queue", totalPrice = 70000.0, orderItems = listOf(OrderItem(serviceId = "s1", itemQuantity = 6, subtotal = 70000.0))),
        Orders(orderId = "4", customerId = "c4", customerName = "Dewi Anggraini", orderDate = ts(-30),
            status = "Done", totalPrice = 30750.0, alreadyPaid = true, orderItems = listOf(OrderItem(serviceId = "s3", itemQuantity = 3, subtotal = 30750.0))),
        Orders(orderId = "5", customerId = "c5", customerName = "Eko Prasetyo", orderDate = ts(-24 * 31),
            status = "Done", totalPrice = 100000.0, alreadyPaid = true, alreadyPickedUp = true, orderItems = listOf(OrderItem(serviceId = "s1", itemQuantity = 5, subtotal = 100000.0)))
    )

    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        AnalyticsScreen(
            uiState = AnalyticsUiState(
                orders = previewOrders,
                services = listOf(
                    Services("s1", "Cuci Kiloan"),
                    Services("s2", "Dry Clean"),
                    Services("s3", "Setrika Saja")
                )
            ),
            onTabClick = {},
            onLoadMoreMonths = {},
            onToastShown = {}
        )
    }
}
