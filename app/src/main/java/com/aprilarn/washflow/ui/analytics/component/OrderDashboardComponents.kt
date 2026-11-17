package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.analytics.DeadlineUrgency
import com.aprilarn.washflow.ui.analytics.OrderDashboardSummary
import com.aprilarn.washflow.ui.analytics.ServicePerformance
import com.aprilarn.washflow.ui.analytics.deadlineInfo
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

private val OverdueColor = Color(0xFFB06868)      // Dusty rose pastel (tidak terlalu merah terang)
private val OverdueBgColor = Color(0xFFF7EAEA)
private val TodayColor = Color(0xFFA68049)        // Warm ochre pastel
private val TodayBgColor = Color(0xFFF8F0E2)
private val UpcomingBgColor = Color(0xFFEDF0F7)   // Soft slate-blue pastel
private val GrowthUpColor = Color(0xFFA7DFC6)     // Soft mint pastel
private val GrowthDownColor = Color(0xFFF2B8B5)   // Soft rose pastel

// ------------------------------------------------------------------
// Kartu ringkasan pendapatan bulan ini
// ------------------------------------------------------------------

@Composable
fun MonthlySummaryCard(
    summary: OrderDashboardSummary,
    modifier: Modifier = Modifier
) {
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
                        colors = listOf(Color(0xFF3B4570), GrayBlue, Color(0xFF7F8BB8))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pendapatan bulan ini",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GrowthChip(summary.revenueGrowthPercent)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = CurrencyUtils.formatRupiahWithSymbol(summary.monthRevenue),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${summary.monthOrderCount} order masuk bulan ini",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryStat(
                        label = "Diproses",
                        value = summary.activeOrderCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatDivider()
                    SummaryStat(
                        label = "Siap diambil",
                        value = summary.readyForPickupCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatDivider()
                    SummaryStat(
                        label = "Belum lunas",
                        value = summary.unpaidCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthChip(growthPercent: Double?) {
    if (growthPercent == null) return

    val (icon, color) = when {
        growthPercent > 0 -> Icons.AutoMirrored.Rounded.TrendingUp to GrowthUpColor
        growthPercent < 0 -> Icons.AutoMirrored.Rounded.TrendingDown to GrowthDownColor
        else -> Icons.AutoMirrored.Rounded.TrendingFlat to Color.White.copy(alpha = 0.8f)
    }
    val text = if (growthPercent % 1 == 0.0) {
        "${abs(growthPercent).toInt()}%"
    } else {
        String.format(Locale.US, "%.1f%%", abs(growthPercent))
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color.White.copy(alpha = 0.25f))
    )
}

// ------------------------------------------------------------------
// Panel & kartu deadline order
// ------------------------------------------------------------------

@Composable
fun DeadlineCountBadge(
    deadlines: List<Orders>,
    modifier: Modifier = Modifier
) {
    if (deadlines.isEmpty()) return

    val overdueCount = deadlines.count { order ->
        val due = order.orderDueDate?.toDate() ?: return@count false
        deadlineInfo(due).urgency == DeadlineUrgency.OVERDUE
    }

    val (textColor, label) = if (overdueCount > 0) {
        OverdueColor to "$overdueCount terlambat"
    } else {
        Color.Gray to "${deadlines.size} aktif"
    }

    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}

@Composable
fun UpcomingDeadlinesCard(
    deadlines: List<Orders>,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor
    ) {
        if (deadlines.isEmpty()) {
            DeadlineEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deadlines, key = { it.orderId }) { order ->
                    DeadlineOrderRow(order)
                }
            }
        }
    }
}

@Composable
fun DeadlineOrderRow(order: Orders, modifier: Modifier = Modifier) {
    val due = order.orderDueDate?.toDate() ?: return
    val info = deadlineInfo(due)
    val (accentColor, badgeBgColor) = when (info.urgency) {
        DeadlineUrgency.OVERDUE -> OverdueColor to OverdueBgColor
        DeadlineUrgency.TODAY -> TodayColor to TodayBgColor
        DeadlineUrgency.UPCOMING -> GrayBlue to UpcomingBgColor
    }
    val customerName = order.customerName?.takeIf { it.isNotBlank() } ?: "Tanpa nama"
    val isOnProcess = order.status.equals("On Process", ignoreCase = true)
    val statusIcon = if (isOnProcess) Icons.Rounded.LocalLaundryService else Icons.Rounded.HourglassTop

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(badgeBgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = order.status,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customerName,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBgColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = info.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeadlineEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.EventAvailable,
                contentDescription = null,
                tint = GrayBlue.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Semua deadline aman",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GrayBlue,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Tidak ada order aktif dengan deadline",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// ------------------------------------------------------------------
// Item daftar order terbaru
// ------------------------------------------------------------------

@Composable
fun RecentOrderItem(order: Orders, modifier: Modifier = Modifier) {
    val customerName = order.customerName?.takeIf { it.isNotBlank() } ?: "Tanpa nama"
    val totalQty = order.orderItems.sumOf { it.itemQuantity ?: 0 }
    val dateText = remember(order.orderDate) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(order.orderDate.toDate())
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = customerName.first().uppercase(),
                color = GrayBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = customerName,
                fontWeight = FontWeight.Bold,
                color = GrayBlue,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$dateText · $totalQty item",
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
                fontWeight = FontWeight.Bold,
                color = GrayBlue,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge(status = order.status ?: "On Queue")
                PaymentBadge(alreadyPaid = order.alreadyPaid)
            }
        }
    }
}

// ------------------------------------------------------------------
// Item layanan terlaris (panel kanan)
// ------------------------------------------------------------------

@Composable
fun TopServiceItem(
    rank: Int,
    service: ServicePerformance,
    modifier: Modifier = Modifier
) {
    val percentText = "${(service.share * 100).toInt()}%"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (rank == 1) GrayBlue else GrayBlue.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        color = if (rank == 1) Color.White else GrayBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.serviceName,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${service.quantity} item · $percentText",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = CurrencyUtils.formatRupiahWithSymbol(service.revenue),
                    fontWeight = FontWeight.Bold,
                    color = GrayBlue,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { service.share },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
                color = GrayBlue,
                trackColor = GrayBlue.copy(alpha = 0.1f),
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
fun TopServicesEmptyState(
    periodLabel: String = "Bulan ini",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Text(
            text = "Belum ada transaksi layanan ${periodLabel.lowercase()}",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}
