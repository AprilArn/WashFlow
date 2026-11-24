package com.aprilarn.washflow.ui.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.data.model.Notifications
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
    isDarkBackground: Boolean = false,
    notificationPreviews: List<Notifications> = emptyList(),
    onWorkspaceClick: () -> Unit,
    onNotifClick: () -> Unit,
    onAiAgentClick: () -> Unit = {},
    onAiAgentLongClick: () -> Unit = {},
    isAiVoiceActive: Boolean = false,
    onRemovePreview: (String, Boolean) -> Unit,
    workspaceDropdown: @Composable (IntOffset) -> Unit
) {
    // Warna aksen teks & elemen sekunder di Header menyesuaikan gelap/terangnya langit secara dinamis:
    // - Saat background gelap (malam/badai/hujan): menjadi biru-es pastel yang cerah agar kontras & tajam.
    // - Saat background terang (siang cerah/berawan): menjadi slate-navy yang lebih gelap & tegas.
    val brandAccentColor by animateColorAsState(
        targetValue = if (isDarkBackground) Color(0xFFD8E6FF) else Color(0xFF3B4870),
        animationSpec = tween(durationMillis = 700),
        label = "headerBrandAccentColor"
    )
    val subTextColor by animateColorAsState(
        targetValue = if (isDarkBackground) Color(0xFFD4E3FC) else Color(0xFF44527C),
        animationSpec = tween(durationMillis = 700),
        label = "headerSubTextColor"
    )
    val primaryTextShadow = remember(isDarkBackground) {
        Shadow(
            color = Color(0xFF2B3F63).copy(alpha = if (isDarkBackground) 0.14f else 0.22f),
            offset = Offset(0f, 1.5f),
            blurRadius = 4f
        )
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LOGO WASHFLOW ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Wash",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = brandAccentColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "Flow",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic,
                    shadow = primaryTextShadow
                )
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // --- NAVIGATION BAR ---
        NavigationBar(
            navController = navController,
            isDarkBackground = isDarkBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- WORKSPACE DROPDOWN TRIGGER ---
        var wsTriggerHeight by remember { mutableIntStateOf(0) }
        val wsArrowRotationState = animateFloatAsState(
            targetValue = if (isWorkspaceExpanded) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "wsArrowRotation"
        )
        val wsBgAlpha by animateFloatAsState(
            targetValue = if (isWorkspaceExpanded) 0.24f else 0.12f,
            animationSpec = tween(durationMillis = 250),
            label = "wsBgAlpha"
        )
        Box(modifier = Modifier.onSizeChanged { wsTriggerHeight = it.height }) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = wsBgAlpha))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (isWorkspaceExpanded) 0.45f else 0.24f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(onClick = onWorkspaceClick)
                    .padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workspaceName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        shadow = primaryTextShadow
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Workspace Options",
                    tint = Color.White,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = wsArrowRotationState.value
                    }
                )
            }

            // 2. MASUKKAN LAMBDA TEPAT DI DALAM BOX INI
            workspaceDropdown(IntOffset(0, wsTriggerHeight))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- CLOCK & UPTIME (Diisolasi agar detak tiap detik tidak me-recompose Header/NavigationBar) ---
        HeaderClockAndUptime(
            subTextColor = subTextColor,
            primaryTextShadow = primaryTextShadow
        )

        Spacer(modifier = Modifier.width(8.dp))

        // --- AI AGENT BUTTON ---
        @OptIn(ExperimentalFoundationApi::class)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onAiAgentClick,
                    onLongClick = if (isAiVoiceActive) null else onAiAgentLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "AI Agent",
                tint = Color.White
            )
        }

        // --- NOTIFICATION DROPDOWN TRIGGER ---
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
                Icon(Icons.Rounded.Notifications, contentDescription = "Notifikasi", tint = Color.White)
            }
        }
    }
}

@Composable
private fun HeaderClockAndUptime(
    subTextColor: Color,
    primaryTextShadow: Shadow
) {
    val startTime = remember { System.currentTimeMillis() }
    var currentTime by remember { mutableLongStateOf(startTime) }
    var uptimeMillis by remember { mutableLongStateOf(0L) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            currentTime = now
            uptimeMillis = now - startTime
            delay(1000L)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        AnalogClockIcon(
            millis = currentTime,
            secondHandColor = subTextColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeFormatter.format(Date(currentTime)),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    shadow = primaryTextShadow
                )
            )
            Text(
                text = "active ${formatDuration(uptimeMillis)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = subTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
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
fun AnalogClockIcon(
    millis: Long,
    modifier: Modifier = Modifier,
    secondHandColor: Color = Color(0xFFD4E3FC)
) {
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

        // Draw Second Hand (dinamis mengikuti kontras background)
        val secondAngle = seconds * 6f - 90f
        drawLine(
            color = secondHandColor,
            start = center,
            end = Offset(
                center.x + (radius * 0.85f) * cos(Math.toRadians(secondAngle.toDouble())).toFloat(),
                center.y + (radius * 0.85f) * sin(Math.toRadians(secondAngle.toDouble())).toFloat()
            ),
            strokeWidth = 1.2.dp.toPx()
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