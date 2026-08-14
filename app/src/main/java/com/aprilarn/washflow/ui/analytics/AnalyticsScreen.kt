package com.aprilarn.washflow.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.analytics.component.*
import com.aprilarn.washflow.ui.theme.GrayBlue

// --- Konstanta Warna Tema ---
val ThemeBgGray = Color(0xFFF3F4F6)

// --- Main Screen ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalyticsScreen() {
    var selectedTab by remember { mutableStateOf("Order") }

    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 1. Sidebar Menu (Left)
        SidebarMenu(
            selectedTab = selectedTab,
            onTabClick = { selectedTab = it }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Main White Container
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(40.dp),
            color = Color.White
        ) {
            if (selectedTab == "Report") {
                // Blank White Page for Report
                Box(modifier = Modifier.fillMaxSize())
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp), // Padding disesuaikan sedikit agar lebih seimbang
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    // Left Column (Dashboard & Transactions)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(text = "Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                                Spacer(modifier = Modifier.height(24.dp))
                                CreditCardView()
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Upcoming deadlines", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                                Spacer(modifier = Modifier.height(24.dp))
                                BoxWithConstraints {
                                    val state = rememberLazyListState()
                                    val flingBehavior = rememberSnapFlingBehavior(
                                        lazyListState = state,
                                        snapPosition = SnapPosition.Start
                                    )
                                    LazyRow(
                                        state = state,
                                        flingBehavior = flingBehavior,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(ThemeBgGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                            .clip(RoundedCornerShape(24.dp)),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                                    ) {
                                        items(mockUpcomingDeadlines) { payment ->
                                            UpcomingPaymentCard(payment)
                                        }
                                        // Trailing spacer to allow the last item to snap to the left (16dp from edge)
                                        item {
                                            val itemWidth = 130.dp
                                            val startPadding = 16.dp
                                            val spacing = 16.dp
                                            // The goal is to have enough space so the last item can reach the snap point (16dp)
                                            // Space needed after the last item = maxWidth - startPadding - itemWidth - spacing - endPadding
                                            val spacerWidth = (maxWidth - startPadding - itemWidth - spacing - startPadding).coerceAtLeast(0.dp)
                                            Spacer(modifier = Modifier.width(spacerWidth))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Recent orders", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Sort by", fontSize = 12.sp, color = GrayBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = GrayBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(mockRecentOrders) { transaction ->
                                TransactionItem(transaction)
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                            }
                        }
                    }

                    // Right Column (Chart & Cards) - DIBUNGKUS DENGAN SURFACE BARU
                    Surface(
                        modifier = Modifier
                            .width(320.dp) // Sedikit dilebarkan untuk memberi ruang pada padding dalam
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(32.dp), // Radius melengkung
                        color = ThemeBgGray // Latar belakang abu-abu
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp) // Padding internal di dalam kontainer abu-abu
                        ) {
                            SpendingChart(modifier = Modifier.fillMaxWidth())

                            Spacer(modifier = Modifier.height(48.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Available cards", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                                Text(text = "View all", color = Color.Gray, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                mockCards.forEach { card ->
                                    MiniCardItem(card)
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
fun AnalyticsScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        AnalyticsScreen()
    }
}
