package com.aprilarn.washflow.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.ui.theme.SoftBlue

// --- Konstanta Warna Tema ---
val ThemeNavy = Color(0xFF2D265A)
val ThemeBgGray = Color(0xFFF3F4F6)

// --- Mock Data Models ---

data class Transaction(
    val icon: ImageVector,
    val title: String,
    val date: String,
    val amount: String,
    val category: String
)

data class UpcomingPayment(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val amount: String,
    val color: Color
)

data class CardInfo(
    val balance: String,
    val currency: String,
    val lastDigits: String,
    val type: String
)

val mockTransactions = listOf(
    Transaction(Icons.Rounded.LocalTaxi, "Taxi Trips", "03 Aug 2022, 15:43", "$56.50", "Transport"),
    Transaction(Icons.Rounded.DirectionsBus, "Public Transport", "01 Aug 2022, 12:58", "$2.50", "Transport"),
    Transaction(Icons.Rounded.Flight, "Plane Tickets", "28 Jul 2022, 21:40", "$70", "Travel"),
    Transaction(Icons.Rounded.LocalGasStation, "Gas Station", "28 Jul 2022, 09:28", "$30.75", "Utilities"),
    Transaction(Icons.Rounded.FitnessCenter, "Gym", "26 Jul 2022, 18:25", "$100.00", "Health")
)

val mockUpcomingPayments = listOf(
    UpcomingPayment(Icons.Rounded.Chair, "Freelance", "Unregular payment", "$1,500", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.Savings, "Salary", "Regular payment", "$4,000", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.ShoppingCart, "Shopping", "Monthly groceries", "$500", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.ElectricBolt, "Electricity", "Utility bill", "$120", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.PhoneAndroid, "Internet", "Fiber optic", "$60", Color(0xFFF4F5F8))
)

val mockCards = listOf(
    CardInfo("98,500", "USD", "4141", "Mastercard"),
    CardInfo("76,280", "EUR", "8345", "VISA")
)

// --- Components ---

@Composable
fun SidebarMenu() {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active item (Weather)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.ShoppingCart, contentDescription = null, tint = ThemeNavy)
            Spacer(Modifier.width(16.dp))
            Text("Order", fontWeight = FontWeight.Bold, color = ThemeNavy, fontSize = 14.sp)
        }

        // Inactive item (Order)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Cloud, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Text("TBA", fontWeight = FontWeight.Medium, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun CreditCardView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2D265A), Color(0xFF5A4B81), Color(0xFF8E6DA8))
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = "Chip",
                        tint = Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "Contactless",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "2984  5633  7859  4141",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(text = "CARD HOLDER", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                        Text(text = "Derrick Fisher", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                    // Mock Mastercard Logo
                    Row {
                        Box(modifier = Modifier.size(20.dp).background(Color(0xFFEB001B), CircleShape))
                        Box(modifier = Modifier.offset(x = (-8).dp).size(20.dp).background(Color(0xFFF79E1B).copy(alpha = 0.8f), CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingPaymentCard(payment: UpcomingPayment) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = payment.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ThemeNavy, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(payment.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(text = payment.title, fontWeight = FontWeight.Bold, color = ThemeNavy, fontSize = 14.sp)
                Text(text = payment.subtitle, fontSize = 9.sp, color = Color.Gray)
            }

            Text(text = payment.amount, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = ThemeNavy)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(transaction.icon, contentDescription = null, tint = ThemeNavy, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.title, fontWeight = FontWeight.Bold, color = ThemeNavy, fontSize = 14.sp)
            Text(text = transaction.date, fontSize = 11.sp, color = Color.Gray)
        }

        Text(text = transaction.amount, fontWeight = FontWeight.Bold, color = ThemeNavy, fontSize = 14.sp)

        Spacer(modifier = Modifier.width(16.dp))

        Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun SpendingChart(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(text = "Spent this day", fontSize = 12.sp, color = Color.Gray)
                Text(text = "$259.75", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = ThemeNavy)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White, // Tambahkan warna putih agar kontras dengan background abu-abu
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Week", fontSize = 12.sp, color = ThemeNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = ThemeNavy)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val points = listOf(0.6f, 0.4f, 0.8f, 0.3f, 0.45f, 0.2f, 0.4f)
            val topPadding = 40.dp
            val chartHeight = maxHeight - topPadding
            val stepX = maxWidth / (points.size - 1)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepX = size.width / (points.size - 1)
                val chartHeight = size.height - topPadding.toPx()

                // Draw horizontal grid lines
                val gridLines = 4
                val stepY = chartHeight / gridLines
                for (i in 0..gridLines) {
                    val y = topPadding.toPx() + i * stepY
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val path = Path()
                // Draw the line chart
                points.forEachIndexed { index, y ->
                    val xPos = index * stepX
                    val yPos = topPadding.toPx() + chartHeight * (1 - y)

                    if (index == 0) path.moveTo(xPos, yPos)
                    else {
                        val prevX = (index - 1) * stepX
                        val prevY = topPadding.toPx() + chartHeight * (1 - points[index - 1])
                        val controlX1 = prevX + (xPos - prevX) / 2
                        val controlX2 = prevX + (xPos - prevX) / 2
                        path.cubicTo(controlX1, prevY, controlX2, yPos, xPos, yPos)
                    }
                }

                drawPath(
                    path = path,
                    color = ThemeNavy,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Indicator for Tuesday (index 2)
                val indicatorX = 2 * stepX
                val indicatorY = topPadding.toPx() + chartHeight * (1 - points[2])

                // Shadow/Gradient line under dot
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(ThemeNavy.copy(alpha = 0.5f), Color.Transparent),
                        startY = indicatorY,
                        endY = size.height
                    ),
                    start = Offset(indicatorX, indicatorY),
                    end = Offset(indicatorX, size.height),
                    strokeWidth = 16.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Connector Line to Label
                val lineDistancePx = 20.dp.toPx()
                drawLine(
                    color = ThemeNavy,
                    start = Offset(indicatorX, indicatorY),
                    end = Offset(indicatorX, indicatorY - lineDistancePx),
                    strokeWidth = 1.dp.toPx()
                )

                // Active Dot
                drawCircle(
                    color = ThemeNavy,
                    radius = 5.dp.toPx(),
                    center = Offset(indicatorX, indicatorY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(indicatorX, indicatorY)
                )
            }

            // Pop-up label for the active dot
            val indicatorX = stepX * 2
            val indicatorY = topPadding + chartHeight * (1 - points[2])
            val labelGap = 20.dp

            Surface(
                modifier = Modifier
                    .offset(indicatorX, indicatorY - labelGap)
                    .graphicsLayer {
                        translationY = -size.height
                        transformOrigin = TransformOrigin(0f, 1f)
                    },
                shape = RoundedCornerShape(8.dp),
                color = ThemeNavy
            ) {
                Text(
                    text = "$259.75",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    color = if (day == "Tue") ThemeNavy else Color.Gray,
                    fontWeight = if (day == "Tue") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun MiniCardItem(card: CardInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White // Diubah menjadi putih agar menonjol di atas latar abu-abu
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = card.balance, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ThemeNavy)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = card.currency, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "...${card.lastDigits}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                if (card.type == "Mastercard") {
                    Row {
                        Box(modifier = Modifier.size(16.dp).background(ThemeNavy, CircleShape))
                        Box(modifier = Modifier.offset(x = (-6).dp).size(16.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
                    }
                } else {
                    Text(text = card.type, fontWeight = FontWeight.Black, color = ThemeNavy, fontSize = 14.sp)
                }
            }
        }
    }
}

// --- Main Screen ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalyticsScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 1. Sidebar Menu (Left)
        SidebarMenu()

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Main White Container
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(40.dp),
            color = Color.White
        ) {
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
                            Text(text = "Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeNavy)
                            Spacer(modifier = Modifier.height(24.dp))
                            CreditCardView()
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Upcoming deadlines", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeNavy)
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
                                    items(mockUpcomingPayments) { payment ->
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
                        Text(text = "Recent orders", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ThemeNavy)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Sort by", fontSize = 12.sp, color = ThemeNavy)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = ThemeNavy)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(mockTransactions) { transaction ->
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
                            Text(text = "Available cards", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeNavy)
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



@Preview(showBackground = true, widthDp = 960, heightDp = 500)
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