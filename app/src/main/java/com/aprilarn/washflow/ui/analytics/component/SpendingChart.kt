package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Orders
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.utils.CurrencyUtils
import kotlin.math.roundToInt

enum class AnalyticsChartType {
    INCOME, CUSTOMER
}

@Composable
fun SpendingChart(
    orders: List<Orders>,
    selectedPeriod: String,
    onPeriodChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    chartType: AnalyticsChartType = AnalyticsChartType.INCOME,
    onInteractionStateChange: (Boolean) -> Unit = {}
) {
    // 1. Generate lists based on selectedPeriod
    val daysList = mutableListOf<String>()
    val dateList = mutableListOf<String>() // Digunakan untuk mencocokkan format dengan Order
    val weekRanges = mutableListOf<Pair<Long, Long>>() // Khusus untuk Week: batas awal dan akhir milidetik
    
    if (selectedPeriod == "Day") {
        val daysFormat = SimpleDateFormat("EEE", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // 7 Hari terakhir
        for (i in 6 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            daysList.add(daysFormat.format(tempCal.time))
            dateList.add(dateFormat.format(tempCal.time))
        }
    } else if (selectedPeriod == "Week") {
        // 7 Minggu terakhir (Termasuk minggu ini sampai hari ini)
        for (i in 6 downTo 0) {
            val endCal = Calendar.getInstance()
            endCal.add(Calendar.DAY_OF_YEAR, -(i * 7)) // Titik akhirnya
            
            val startCal = Calendar.getInstance()
            startCal.timeInMillis = endCal.timeInMillis
            startCal.add(Calendar.DAY_OF_YEAR, -6) // 6 hari sebelumnya (total 7 hari)
            
            // Atur waktu agar presisi di jam 00:00 - 23:59
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            
            endCal.set(Calendar.HOUR_OF_DAY, 23)
            endCal.set(Calendar.MINUTE, 59)
            endCal.set(Calendar.SECOND, 59)
            
            val dateFormat = SimpleDateFormat("dd MMM", Locale.US)
            // Labelnya: "12 Oct", dll (menunjukkan awal minggu)
            daysList.add(dateFormat.format(startCal.time)) 
            weekRanges.add(Pair(startCal.timeInMillis, endCal.timeInMillis))
        }
    } else { // "Month"
        val monthFormat = SimpleDateFormat("MMM", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        // 7 Bulan terakhir
        for (i in 6 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.MONTH, -i)
            daysList.add(monthFormat.format(tempCal.time))
            dateList.add(dateFormat.format(tempCal.time))
        }
    }

    // 2. Calculate values
    val values = DoubleArray(7) { 0.0 }
    val uniqueCustomers = Array(7) { mutableSetOf<String>() }
    
    orders.forEach { order ->
        val orderTime = order.orderDate.toDate().time
        
        if (selectedPeriod == "Week") {
            // Logika pencarian untuk range mingguan
            for (i in 0..6) {
                val (start, end) = weekRanges[i]
                if (orderTime in start..end) {
                    if (chartType == AnalyticsChartType.INCOME) {
                        if (order.totalPrice != null && order.totalPrice > 0) {
                            values[i] += order.totalPrice
                        }
                    } else {
                        if (order.customerId.isNotBlank()) {
                            uniqueCustomers[i].add(order.customerId)
                        }
                    }
                    break // Sudah ketemu tempatnya, lanjut order berikutnya
                }
            }
        } else {
            // Logika harian / bulanan (pencocokan string)
            val formatStr = if (selectedPeriod == "Day") "yyyy-MM-dd" else "yyyy-MM"
            val orderDateStr = SimpleDateFormat(formatStr, Locale.getDefault()).format(order.orderDate.toDate())
            val index = dateList.indexOf(orderDateStr)
            
            if (index != -1) {
                if (chartType == AnalyticsChartType.INCOME) {
                    if (order.totalPrice != null && order.totalPrice > 0) {
                        values[index] += order.totalPrice
                    }
                } else {
                    if (order.customerId.isNotBlank()) {
                        uniqueCustomers[index].add(order.customerId)
                    }
                }
            }
        }
    }

    if (chartType == AnalyticsChartType.CUSTOMER) {
        for (i in 0..6) {
            values[i] = uniqueCustomers[i].size.toDouble()
        }
    }

    // Calculate chart points (0f to 1f based on max value)
    val maxValue = values.maxOrNull() ?: 0.0
    val points = if (maxValue > 0) {
        values.map { (it / maxValue).toFloat() }
    } else {
        List(7) { 0f }
    }
    
    val dayAmounts = values.map { 
        if (chartType == AnalyticsChartType.INCOME) CurrencyUtils.formatRupiahWithSymbol(it) 
        else "${it.toInt()} User"
    }
    val days = daysList

    // Set default selected index to today/this month (index 6, the last item)
    var selectedIndex by remember { mutableIntStateOf(6) }
    // Update selectedIndex automatically if it exceeds list size (though it's fixed at 7)
    if (selectedIndex >= days.size) selectedIndex = days.size - 1
    
    // Hitung tren (naik/turun/tetap)
    val currentIncome = values[selectedIndex]
    val previousIncome = if (selectedIndex > 0) values[selectedIndex - 1] else 0.0
    val trendIsUp = currentIncome > previousIncome
    val trendIsDown = currentIncome < previousIncome
    val trendColor = if (trendIsUp) Color(0xFF059669) else if (trendIsDown) Color(0xFFDC2626) else Color.Gray
    val trendIcon = if (trendIsUp) Icons.AutoMirrored.Rounded.TrendingUp else if (trendIsDown) Icons.AutoMirrored.Rounded.TrendingDown else Icons.AutoMirrored.Rounded.TrendingFlat
    
    // Kalkulasi persentase perbedaan
    val percentageDiff = if (previousIncome > 0) {
        ((currentIncome - previousIncome) / previousIncome) * 100
    } else if (currentIncome > 0) {
        100.0 // Anggap kenaikan 100% jika sebelumnya 0
    } else {
        0.0
    }
    
    val formattedPercentage = if (percentageDiff % 1 == 0.0) {
        "${kotlin.math.abs(percentageDiff).toInt()}%"
    } else {
        String.format(Locale.US, "%.1f%%", kotlin.math.abs(percentageDiff))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val titlePrefix = when (selectedPeriod) {
                        "Day" -> "Daily"
                        "Week" -> "Weekly"
                        else -> "Monthly"
                    }
                    val titleSuffix = if (chartType == AnalyticsChartType.INCOME) "income" else "customers"
                    
                    Text(
                        text = "$titlePrefix $titleSuffix", 
                        fontSize = 12.sp, color = Color.Gray
                    )
                    
                    if (selectedIndex > 0 && (trendIsUp || trendIsDown)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formattedPercentage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
                Text(text = dayAmounts[selectedIndex], fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = GrayBlue)
            }

            GeneralDropdown(
                selectedText = selectedPeriod,
                items = listOf("Day", "Week", "Month"),
                onItemSelected = { 
                    onPeriodChanged(it)
                    selectedIndex = 6 // Reset to right-most
                },
                onExpandedChange = { expanded ->
                    onInteractionStateChange(expanded)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onInteractionStateChange(true) },
                        onDragEnd = { onInteractionStateChange(false) },
                        onDragCancel = { onInteractionStateChange(false) }
                    ) { change, _ ->
                        val x = change.position.x - 15.dp.toPx() 
                        val stepXPx = (size.width - 30.dp.toPx()) / (points.size - 1)
                        val index = (x / stepXPx).roundToInt().coerceIn(0, points.size - 1)
                        selectedIndex = index
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                androidx.compose.ui.input.pointer.PointerEventType.Press -> {
                                    onInteractionStateChange(true)
                                    val position = event.changes.first().position
                                    val x = position.x - 15.dp.toPx()
                                    val stepXPx = (size.width - 30.dp.toPx()) / (points.size - 1)
                                    val index = (x / stepXPx).roundToInt().coerceIn(0, points.size - 1)
                                    selectedIndex = index
                                }
                                androidx.compose.ui.input.pointer.PointerEventType.Release -> {
                                    onInteractionStateChange(false)
                                }
                            }
                        }
                    }
                }
        ) {
            val topPadding = 40.dp
            val chartHeight = maxHeight - topPadding
            val horizontalPadding = 15.dp
            val innerWidth = maxWidth - (horizontalPadding * 2)
            val stepX = innerWidth / (points.size - 1)

            val animatedX by animateFloatAsState(
                targetValue = (selectedIndex * stepX.value),
                animationSpec = tween(durationMillis = 300),
                label = "indicatorX"
            )
            
            val animatedYFactor by animateFloatAsState(
                targetValue = points[selectedIndex],
                animationSpec = tween(durationMillis = 300),
                label = "indicatorYFactor"
            )

            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding)) {
                val stepXCanvas = size.width / (points.size - 1)
                val chartHeightCanvas = size.height - topPadding.toPx()

                // Draw horizontal grid lines
                val gridLines = 4
                val stepY = chartHeightCanvas / gridLines
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
                    val xPos = index * stepXCanvas
                    val yPos = topPadding.toPx() + chartHeightCanvas * (1 - y)

                    if (index == 0) path.moveTo(xPos, yPos)
                    else {
                        val prevX = (index - 1) * stepXCanvas
                        val prevY = topPadding.toPx() + chartHeightCanvas * (1 - points[index - 1])
                        val controlX1 = prevX + (xPos - prevX) / 2
                        val controlX2 = prevX + (xPos - prevX) / 2
                        path.cubicTo(controlX1, prevY, controlX2, yPos, xPos, yPos)
                    }
                }

                drawPath(
                    path = path,
                    color = GrayBlue,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Indicator
                val indicatorX = animatedX.dp.toPx()
                val indicatorY = topPadding.toPx() + chartHeightCanvas * (1 - animatedYFactor)

                // Shadow/Gradient line under dot
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(GrayBlue.copy(alpha = 0.5f), Color.Transparent),
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
                    color = GrayBlue,
                    start = Offset(indicatorX, indicatorY),
                    end = Offset(indicatorX, indicatorY - lineDistancePx),
                    strokeWidth = 1.dp.toPx()
                )

                // Active Dot
                drawCircle(
                    color = GrayBlue,
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
            val indicatorY = topPadding + chartHeight * (1 - animatedYFactor)
            val labelGap = 20.dp
            
            // Flip label to the left if it's near the right edge
            val isFarRight = selectedIndex >= days.size - 2
            val targetTranslationXFactor = if (isFarRight) -1f else 0f
            val animatedTranslationXFactor by animateFloatAsState(
                targetValue = targetTranslationXFactor,
                animationSpec = tween(durationMillis = 300),
                label = "labelTranslationX"
            )

            val bottomStartRadius by animateDpAsState(
                targetValue = if (isFarRight) 8.dp else 0.dp,
                animationSpec = tween(durationMillis = 300),
                label = "bottomStartRadius"
            )
            val bottomEndRadius by animateDpAsState(
                targetValue = if (isFarRight) 0.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "bottomEndRadius"
            )

            Surface(
                modifier = Modifier
                    .offset(
                        // Karena label digambar SEBELUM padding canvas, kita tambahkan manual padding-nya ke offset pop-up ini
                        x = animatedX.dp + horizontalPadding + if (isFarRight) 0.5.dp else (-0.5).dp, 
                        y = indicatorY - labelGap
                    )
                    .graphicsLayer {
                        translationX = size.width * animatedTranslationXFactor
                        translationY = -size.height
                        transformOrigin = TransformOrigin(if (isFarRight) 1f else 0f, 1f)
                    },
                shape = RoundedCornerShape(
                    topStart = 8.dp, 
                    topEnd = 8.dp, 
                    bottomStart = bottomStartRadius, 
                    bottomEnd = bottomEndRadius
                ),
                color = GrayBlue
            ) {
                Text(
                    text = dayAmounts[selectedIndex],
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
            days.forEachIndexed { index, day ->
                val isSelected = index == selectedIndex
                Text(
                    text = day,
                    fontSize = 11.sp,
                    color = if (isSelected) GrayBlue else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(30.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
