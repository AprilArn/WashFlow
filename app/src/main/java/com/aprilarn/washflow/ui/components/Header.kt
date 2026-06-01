package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.SoftBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Header(
    modifier: Modifier = Modifier,
    navController: NavController,
    workspaceName: String,
    unreadCount: Int,
    isWorkspaceExpanded: Boolean = false,
    notificationPreviews: List<Notifications> = emptyList(),
    onWorkspaceClick: () -> Unit,
    onNotifClick: () -> Unit,
    onRemovePreview: (String, Boolean) -> Unit,
    workspaceDropdown: @Composable (IntOffset) -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val startTime = remember { System.currentTimeMillis() }
    var uptimeMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            uptimeMillis = currentTime - startTime
            delay(1000)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LOGO WASHFLOW ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Wash",
                style = MaterialTheme.typography.headlineMedium.copy(color = GrayBlue, fontSize = 22.sp)
            )
            Text(
                text = "Flow",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontSize = 22.sp, fontStyle = FontStyle.Italic)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // --- NAVIGATION BAR ---
        NavigationBar(navController = navController, modifier = Modifier.height(44.dp))

        Spacer(modifier=Modifier.weight(1f))

        // --- WORKSPACE DROPDOWN TRIGGER ---
        var wsTriggerSize by remember { mutableStateOf(IntSize.Zero) }
        Box(modifier = Modifier.onGloballyPositioned { wsTriggerSize = it.size }) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onWorkspaceClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                    text = workspaceName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Light)
                )
                Icon(
                    imageVector = if (isWorkspaceExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Workspace Options",
                    tint = Color.White
                )
            }

            // 2. MASUKKAN LAMBDA TEPAT DI DALAM BOX INI
            workspaceDropdown(IntOffset(0, wsTriggerSize.height))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- CLOCK & UPTIME ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnalogClockIcon(millis = currentTime)
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeFormatter.format(Date(currentTime)),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = "active ${formatDuration(uptimeMillis)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SoftBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- NOTIFICATION DROPDOWN TRIGGER ---
        var notifTriggerSize by remember { mutableStateOf(IntSize.Zero) }
        Box(modifier = Modifier.onGloballyPositioned { notifTriggerSize = it.size }) {
            IconButton(onClick = onNotifClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$unreadCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = Color.White)
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun AnalogClockIcon(millis: Long, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(28.dp)) {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        // Draw face
        drawCircle(
            color = Color.White,
            radius = radius,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Draw Hour Hand
        val hourAngle = (hours + minutes / 60f) * 30f - 90f
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (radius * 0.55f) * cos(Math.toRadians(hourAngle.toDouble())).toFloat(),
                center.y + (radius * 0.55f) * sin(Math.toRadians(hourAngle.toDouble())).toFloat()
            ),
            strokeWidth = 2.dp.toPx()
        )

        // Draw Minute Hand
        val minuteAngle = minutes * 6f - 90f
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (radius * 0.8f) * cos(Math.toRadians(minuteAngle.toDouble())).toFloat(),
                center.y + (radius * 0.8f) * sin(Math.toRadians(minuteAngle.toDouble())).toFloat()
            ),
            strokeWidth = 1.5.dp.toPx()
        )

        // Draw Second Hand (Pinkish/Red)
        val secondAngle = seconds * 6f - 90f
        drawLine(
            color = SoftBlue,
            start = center,
            end = Offset(
                center.x + (radius * 0.85f) * cos(Math.toRadians(secondAngle.toDouble())).toFloat(),
                center.y + (radius * 0.85f) * sin(Math.toRadians(secondAngle.toDouble())).toFloat()
            ),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF, widthDp = 960)
@Composable
fun HeaderPreview() {
    Header(
        workspaceName = "Example",
        unreadCount = 3,
        isWorkspaceExpanded = false,
        notificationPreviews = emptyList(),
        navController = rememberNavController(),
        onWorkspaceClick = {},
        onNotifClick = {},
        onRemovePreview = { _, _ -> },
        workspaceDropdown = { _ -> }
    )
}