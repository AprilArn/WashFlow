package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.LocalLaundryService
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
import androidx.compose.ui.graphics.vector.ImageVector
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

private val OverdueColor = Color(0xFFDC2626)
private val TodayColor = Color(0xFFD97706)
private val GrowthUpColor = Color(0xFF6EE7B7)
private val GrowthDownColor = Color(0xFFFCA5A5)

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
                        fontSize = 12.sp
                    )
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
            Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
// Kartu deadline order
// ------------------------------------------------------------------

@Composable
fun DeadlineOrderCard(order: Orders, modifier: Modifier = Modifier) {
    val due = order.orderDueDate?.toDate() ?: return
    val info = deadlineInfo(due)
    val accentColor = when (info.urgency) {
        DeadlineUrgency.OVERDUE -> OverdueColor
        DeadlineUrgency.TODAY -> TodayColor
        DeadlineUrgency.UPCOMING -> GrayBlue
    }
    val isOnProcess = order.status.equals("On Process", ignoreCase = true)
    val statusIcon: ImageVector = if (isOnProcess) Icons.Rounded.LocalLaundryService else Icons.Rounded.HourglassTop

    Card(
        modifier = modifier
            .width(130.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    .background(accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, contentDescription = order.status, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(
                    text = order.customerName?.takeIf { it.isNotBlank() } ?: "Tanpa nama",
                    fontWeight = FontWeight.Bold,
                    color = GrayBlue,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = info.label,
                    fontSize = 10.sp,
                    fontWeight = if (info.urgency == DeadlineUrgency.UPCOMING) FontWeight.Normal else FontWeight.Bold,
                    color = if (info.urgency == DeadlineUrgency.UPCOMING) Color.Gray else accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = GrayBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DeadlineEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(192.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.EventAvailable,
            contentDescription = null,
            tint = GrayBlue.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tidak ada order aktif dengan deadline",
            fontSize = 12.sp,
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
