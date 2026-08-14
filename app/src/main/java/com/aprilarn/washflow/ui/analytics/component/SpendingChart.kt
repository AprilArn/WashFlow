package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.aprilarn.washflow.ui.theme.GrayBlue
import kotlin.math.roundToInt

@Composable
fun SpendingChart(modifier: Modifier = Modifier) {
    val points = listOf(0.6f, 0.4f, 0.8f, 0.3f, 0.45f, 0.2f, 0.4f)
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayAmounts = listOf("$194.80", "$129.85", "$259.75", "$97.40", "$146.10", "$64.90", "$129.85")
    
    var selectedIndex by remember { mutableIntStateOf(2) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(text = "Daily income", fontSize = 12.sp, color = Color.Gray)
                Text(text = dayAmounts[selectedIndex], fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = GrayBlue)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Week", fontSize = 12.sp, color = GrayBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = GrayBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x
                        val stepX = size.width / (points.size - 1)
                        val index = (x / stepX).roundToInt().coerceIn(0, points.size - 1)
                        selectedIndex = index
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val x = offset.x
                        val stepX = size.width / (points.size - 1)
                        val index = (x / stepX).roundToInt().coerceIn(0, points.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            val topPadding = 40.dp
            val chartHeight = maxHeight - topPadding
            val stepX = maxWidth / (points.size - 1)

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

            Canvas(modifier = Modifier.fillMaxSize()) {
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
            val indicatorX = animatedX.dp
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
                        x = indicatorX + if (isFarRight) 0.5.dp else (-0.5).dp, 
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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
