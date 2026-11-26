package com.aprilarn.washflow.ui.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark

enum class StatusWorkflowKind {
    ON_QUEUE,
    ON_PROCESS,
    DONE
}

data class StatusWorkflowTheme(
    val kind: StatusWorkflowKind,
    val skyTop: Color,
    val skyBottom: Color,
    val haloColor: Color,
    val accentColor: Color,
    val labelColor: Color,
    val primaryTextColor: Color,
    val subtitleColor: Color,
    val countColor: Color,
    val highlightBg: Color
)

fun resolveStatusWorkflowTheme(statusText: String): StatusWorkflowTheme {
    val normalized = statusText.trim().lowercase()
    return when {
        normalized.contains("process") || normalized.contains("progress") || normalized.contains("proses") -> StatusWorkflowTheme(
            kind = StatusWorkflowKind.ON_PROCESS,
            skyTop = Color(0xFFEEF3FB),
            skyBottom = PastelBlueBg,
            haloColor = Color(0xFFEBF2FC),
            accentColor = PastelBlue,
            labelColor = PastelBlue,
            primaryTextColor = Color(0xFF2F4266),
            subtitleColor = Color(0xFF6B7B9C),
            countColor = PastelBlue,
            highlightBg = Color(0xFFF3F7FD)
        )
        normalized.contains("done") || normalized.contains("selesai") -> StatusWorkflowTheme(
            kind = StatusWorkflowKind.DONE,
            skyTop = Color(0xFFEEF7F2),
            skyBottom = PastelGreenBg,
            haloColor = Color(0xFFEAF5EF),
            accentColor = PastelGreen,
            labelColor = PastelGreen,
            primaryTextColor = Color(0xFF2D523D),
            subtitleColor = Color(0xFF6B8575),
            countColor = PastelGreenDark,
            highlightBg = Color(0xFFF2FAF5)
        )
        else -> StatusWorkflowTheme(
            kind = StatusWorkflowKind.ON_QUEUE,
            skyTop = Color(0xFFFBF5EB),
            skyBottom = PastelAmberBg,
            haloColor = Color(0xFFFCF3E4),
            accentColor = PastelAmber,
            labelColor = PastelAmber,
            primaryTextColor = Color(0xFF6B502A),
            subtitleColor = Color(0xFF947A59),
            countColor = PastelAmber,
            highlightBg = Color(0xFFFDF8F0)
        )
    }
}

/**
 * Ilustrasi alur kerja vertikal untuk kartu ringkasan status (`StatusCard`)
 * dengan latar langit pastel lembut dan gelombang transisi putih di bawah.
 */
@Composable
fun StatusWorkflowCardIllustration(
    theme: StatusWorkflowTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.skyTop, theme.skyBottom)
            )
        )

        val heroCenter = Offset(w * 0.50f, h * 0.50f)

        when (theme.kind) {
            StatusWorkflowKind.ON_QUEUE -> {
                drawCircle(
                    color = Color(0xFFFBE3B5).copy(alpha = 0.65f),
                    radius = h * 0.28f,
                    center = Offset(w * 0.25f, h * 0.28f)
                )
                drawEditorialCloud(
                    center = Offset(w * 0.76f, h * 0.22f),
                    scale = h * 0.011f,
                    color = Color.White.copy(alpha = 0.85f)
                )
                drawEditorialHill(0f, w * 0.82f, h, w * 0.34f, h * 0.60f, Color(0xFFF0DEC2))
                drawEditorialHill(w * 0.20f, w, h, w * 0.72f, h * 0.56f, Color(0xFFE4CA9F))

                drawOnQueueHero(center = heroCenter, scale = h)
            }
            StatusWorkflowKind.ON_PROCESS -> {
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = h * 0.30f,
                    center = Offset(w * 0.46f, h * 0.44f)
                )
                drawEditorialCloud(
                    center = Offset(w * 0.25f, h * 0.22f),
                    scale = h * 0.010f,
                    color = Color.White.copy(alpha = 0.82f)
                )
                drawEditorialHill(0f, w * 0.78f, h, w * 0.32f, h * 0.62f, Color(0xFFC9D9F0))
                drawEditorialHill(w * 0.22f, w, h, w * 0.74f, h * 0.56f, Color(0xFFB1C7E8))

                drawOnProcessHero(center = heroCenter, scale = h, holeColor = Color(0xFFEEF3FB))
            }
            StatusWorkflowKind.DONE -> {
                drawCircle(
                    color = Color.White.copy(alpha = 0.70f),
                    radius = h * 0.30f,
                    center = Offset(w * 0.48f, h * 0.46f)
                )
                drawEditorialHill(0f, w * 0.80f, h, w * 0.34f, h * 0.60f, Color(0xFFC5E3D2))
                drawEditorialHill(w * 0.20f, w, h, w * 0.72f, h * 0.55f, Color(0xFFAAD3BC))

                drawDoneHero(center = heroCenter, scale = h, whiteSparkleColor = Color.White)
            }
        }

        // Gelombang transisi putih di tepi paling bawah agar menyatu mulus dengan body kartu
        val bottomFade = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.90f)
            cubicTo(
                w * 0.30f, h * 0.98f,
                w * 0.70f, h * 0.84f,
                w, h * 0.92f
            )
            lineTo(w, h)
            close()
        }
        drawPath(bottomFade, Color.White)
    }
}

/**
 * Ilustrasi banner horizontal di atas latar putih solid (gaya Google Calendar editorial),
 * cocok untuk header kolom `ManageOrder` atau kartu horizontal lainnya.
 */
@Composable
fun StatusWorkflowBannerIllustration(
    theme: StatusWorkflowTheme,
    modifier: Modifier = Modifier,
    baseColor: Color = Color.White
) {
    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height

        drawRect(color = baseColor)

        val heroCenter = Offset(w * 0.64f, h * 0.50f)
        val heroScale = h * 0.94f

        // Halo pastel lembut di belakang objek utama
        drawCircle(
            color = theme.haloColor,
            radius = h * 0.42f,
            center = heroCenter
        )

        when (theme.kind) {
            StatusWorkflowKind.ON_QUEUE -> {
                drawCircle(
                    color = Color(0xFFFBE3B5).copy(alpha = 0.70f),
                    radius = h * 0.16f,
                    center = Offset(heroCenter.x - h * 0.48f, h * 0.28f)
                )
                drawEditorialCloud(
                    center = Offset(heroCenter.x + h * 0.52f, h * 0.23f),
                    scale = h * 0.011f,
                    color = Color(0xFFF7EBD6)
                )
                drawEditorialHill(w * 0.24f, w * 0.82f, h, w * 0.52f, h * 0.64f, Color(0xFFF6E9D5))
                drawEditorialHill(w * 0.42f, w * 0.96f, h, w * 0.71f, h * 0.58f, Color(0xFFEDD9BB))
                drawEditorialHill(w * 0.32f, w * 0.90f, h, w * 0.62f, h * 0.74f, Color(0xFFE3C8A0))

                drawOnQueueHero(center = heroCenter, scale = heroScale)
            }
            StatusWorkflowKind.ON_PROCESS -> {
                drawEditorialCloud(
                    center = Offset(heroCenter.x - h * 0.52f, h * 0.24f),
                    scale = h * 0.010f,
                    color = Color(0xFFE6EFFB)
                )
                drawEditorialCloud(
                    center = Offset(heroCenter.x + h * 0.54f, h * 0.23f),
                    scale = h * 0.010f,
                    color = Color(0xFFE6EFFB)
                )
                drawEditorialHill(w * 0.24f, w * 0.82f, h, w * 0.52f, h * 0.64f, Color(0xFFE4ECF8))
                drawEditorialHill(w * 0.42f, w * 0.96f, h, w * 0.71f, h * 0.58f, Color(0xFFD5E2F5))
                drawEditorialHill(w * 0.32f, w * 0.90f, h, w * 0.62f, h * 0.74f, Color(0xFFC3D5EF))

                drawOnProcessHero(center = heroCenter, scale = heroScale, holeColor = theme.haloColor)
            }
            StatusWorkflowKind.DONE -> {
                drawEditorialCloud(
                    center = Offset(heroCenter.x - h * 0.50f, h * 0.24f),
                    scale = h * 0.010f,
                    color = Color(0xFFE4F2EA)
                )
                drawEditorialCloud(
                    center = Offset(heroCenter.x + h * 0.54f, h * 0.23f),
                    scale = h * 0.010f,
                    color = Color(0xFFE4F2EA)
                )
                drawEditorialHill(w * 0.24f, w * 0.82f, h, w * 0.52f, h * 0.64f, Color(0xFFDFEFE6))
                drawEditorialHill(w * 0.42f, w * 0.96f, h, w * 0.71f, h * 0.58f, Color(0xFFCDE7D9))
                drawEditorialHill(w * 0.32f, w * 0.90f, h, w * 0.62f, h * 0.74f, Color(0xFFB8DCC9))

                drawDoneHero(center = heroCenter, scale = heroScale, whiteSparkleColor = Color(0xFF8CC4A6))
            }
        }

        // Scrim lembut di kiri & kanan agar judul kolom serta angka tetap bersih & kontras
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to baseColor,
                    0.34f to baseColor.copy(alpha = 0.88f),
                    0.47f to Color.Transparent,
                    0.81f to Color.Transparent,
                    0.91f to baseColor.copy(alpha = 0.88f),
                    1.0f to baseColor
                )
            )
        )
    }
}

// ------------------------------------------------------------------
// 1. ON QUEUE — Clipboard Daftar Antrean & Jam Tunggu
// ------------------------------------------------------------------
private fun DrawScope.drawOnQueueHero(
    center: Offset,
    scale: Float
) {
    val boardCenter = Offset(center.x - scale * 0.09f, center.y + scale * 0.04f)
    val boardW = scale * 0.50f
    val boardH = scale * 0.48f

    rotate(degrees = -5f, pivot = boardCenter) {
        val boardLeft = boardCenter.x - boardW * 0.5f
        val boardTop = boardCenter.y - boardH * 0.5f

        drawRoundRect(
            color = Color(0xFFD5B583).copy(alpha = 0.45f),
            topLeft = Offset(boardLeft + 3f, boardTop + 4f),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = Color(0xFFC99B5B),
            topLeft = Offset(boardLeft, boardTop),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        val pad = boardW * 0.11f
        val paperLeft = boardLeft + pad
        val paperTop = boardTop + pad * 1.25f
        val paperW = boardW - pad * 2f
        val paperH = boardH - pad * 2.1f
        drawRoundRect(
            color = Color(0xFFFFFDF9),
            topLeft = Offset(paperLeft, paperTop),
            size = Size(paperW, paperH),
            cornerRadius = CornerRadius(6f, 6f)
        )
        val clipW = boardW * 0.42f
        val clipH = boardH * 0.12f
        drawRoundRect(
            color = Color(0xFF8C6B3F),
            topLeft = Offset(boardCenter.x - clipW * 0.5f, boardTop - clipH * 0.35f),
            size = Size(clipW, clipH),
            cornerRadius = CornerRadius(5f, 5f)
        )

        listOf(0.26f, 0.52f, 0.76f).forEachIndexed { idx, ratioY ->
            val rowY = paperTop + paperH * ratioY
            val dotColor = if (idx == 0) Color(0xFFE09145) else Color(0xFFDCC7A7)
            drawCircle(
                color = dotColor,
                radius = paperW * 0.065f,
                center = Offset(paperLeft + paperW * 0.20f, rowY)
            )
            drawLine(
                color = if (idx == 0) Color(0xFFB58A52) else Color(0xFFE5D6BE),
                start = Offset(paperLeft + paperW * 0.36f, rowY),
                end = Offset(paperLeft + paperW * (if (idx == 1) 0.74f else 0.84f), rowY),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
        }
    }

    val clockCenter = Offset(center.x + scale * 0.22f, center.y + scale * 0.11f)
    val clockR = scale * 0.165f
    drawCircle(Color(0xFFD5B583).copy(alpha = 0.45f), clockR, Offset(clockCenter.x + 2f, clockCenter.y + 3f))
    drawCircle(Color(0xFFE09A4F), clockR, clockCenter)
    drawCircle(Color(0xFFFFFDF9), clockR * 0.78f, clockCenter)
    drawLine(
        color = Color(0xFF8C683A),
        start = clockCenter,
        end = Offset(clockCenter.x, clockCenter.y - clockR * 0.46f),
        strokeWidth = 2.6f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFD9823B),
        start = clockCenter,
        end = Offset(clockCenter.x + clockR * 0.42f, clockCenter.y + clockR * 0.12f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )
    drawCircle(Color(0xFF8C683A), radius = 2.4f, center = clockCenter)
}

// ------------------------------------------------------------------
// 2. ON PROCESS — Roda Gigi Presisi (Interlocking Gears) & Dinamika Kerja
// ------------------------------------------------------------------
private fun DrawScope.drawOnProcessHero(
    center: Offset,
    scale: Float,
    holeColor: Color
) {
    val mainGearCenter = Offset(center.x - scale * 0.12f, center.y - scale * 0.01f)
    val arcRectSize = Size(scale * 0.74f, scale * 0.74f)
    val arcTopLeft = Offset(
        mainGearCenter.x - arcRectSize.width * 0.5f,
        mainGearCenter.y - arcRectSize.height * 0.5f
    )
    drawArc(
        color = Color(0xFF7FA3D1).copy(alpha = 0.55f),
        startAngle = 195f,
        sweepAngle = 110f,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcRectSize,
        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
    )

    drawEditorialGear(
        center = mainGearCenter,
        outerRadius = scale * 0.24f,
        toothCount = 8,
        gearColor = Color(0xFF5A82B8),
        innerRingColor = Color(0xFFDCE7F7),
        holeColor = holeColor,
        rotationDeg = 12f
    )

    drawEditorialGear(
        center = Offset(center.x + scale * 0.24f, center.y - scale * 0.14f),
        outerRadius = scale * 0.165f,
        toothCount = 6,
        gearColor = Color(0xFFEB9478),
        innerRingColor = Color(0xFFFCE4DC),
        holeColor = holeColor,
        rotationDeg = 28f
    )

    drawSparkle(
        center = Offset(center.x + scale * 0.36f, center.y + scale * 0.12f),
        radius = scale * 0.055f,
        color = Color(0xFF5A82B8)
    )
    drawSparkle(
        center = Offset(center.x - scale * 0.40f, center.y + scale * 0.02f),
        radius = scale * 0.045f,
        color = Color(0xFF93B4DF)
    )
}

// ------------------------------------------------------------------
// 3. DONE — Paket Selesai Rapi, Lencana Centang & Kilau Bintang
// ------------------------------------------------------------------
private fun DrawScope.drawDoneHero(
    center: Offset,
    scale: Float,
    whiteSparkleColor: Color
) {
    val boxW = scale * 0.535f
    val boxH = scale * 0.36f
    val boxLeft = center.x - scale * 0.075f - boxW * 0.5f
    val boxTop = center.y - scale * 0.10f

    drawRoundRect(
        color = Color(0xFF83B89A).copy(alpha = 0.45f),
        topLeft = Offset(boxLeft + 3f, boxTop + 4f),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color(0xFFFFFCF5),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    val lidH = boxH * 0.26f
    drawRoundRect(
        color = Color(0xFFF3E6CE),
        topLeft = Offset(boxLeft - 2f, boxTop),
        size = Size(boxW + 4f, lidH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    val ribbonW = boxW * 0.20f
    drawRect(
        color = Color(0xFF63A682),
        topLeft = Offset(boxLeft + (boxW - ribbonW) * 0.5f, boxTop),
        size = Size(ribbonW, boxH)
    )
    val bowCenterX = boxLeft + boxW * 0.5f
    drawOval(
        color = Color(0xFF63A682),
        topLeft = Offset(bowCenterX - ribbonW * 1.15f, boxTop - lidH * 0.55f),
        size = Size(ribbonW * 1.1f, lidH * 0.65f)
    )
    drawOval(
        color = Color(0xFF63A682),
        topLeft = Offset(bowCenterX + ribbonW * 0.05f, boxTop - lidH * 0.55f),
        size = Size(ribbonW * 1.1f, lidH * 0.65f)
    )

    val badgeCenter = Offset(center.x + scale * 0.24f, center.y + scale * 0.10f)
    val badgeR = scale * 0.16f
    drawCircle(Color(0xFF78AF91).copy(alpha = 0.45f), badgeR, Offset(badgeCenter.x + 2f, badgeCenter.y + 3f))
    drawCircle(Color.White, badgeR, badgeCenter)
    drawCircle(Color(0xFF4F8F6B), badgeR * 0.82f, badgeCenter)

    val checkPath = Path().apply {
        moveTo(badgeCenter.x - badgeR * 0.34f, badgeCenter.y + badgeR * 0.02f)
        lineTo(badgeCenter.x - badgeR * 0.08f, badgeCenter.y + badgeR * 0.28f)
        lineTo(badgeCenter.x + badgeR * 0.38f, badgeCenter.y - badgeR * 0.24f)
    }
    drawPath(
        path = checkPath,
        color = Color.White,
        style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    drawSparkle(
        center = Offset(center.x - scale * 0.39f, center.y - scale * 0.18f),
        radius = scale * 0.070f,
        color = Color(0xFFF2C15A)
    )
    drawSparkle(
        center = Offset(center.x + scale * 0.34f, center.y - scale * 0.23f),
        radius = scale * 0.060f,
        color = Color(0xFFF2C15A)
    )
    drawSparkle(
        center = Offset(center.x - scale * 0.30f, center.y - scale * 0.30f),
        radius = scale * 0.042f,
        color = whiteSparkleColor
    )
}

// ------------------------------------------------------------------
// Shared Editorial Vector Helpers
// ------------------------------------------------------------------

private fun DrawScope.drawEditorialHill(
    startX: Float,
    endX: Float,
    baseY: Float,
    peakX: Float,
    peakY: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(startX, baseY)
        cubicTo(
            startX + (peakX - startX) * 0.45f, peakY,
            peakX + (endX - peakX) * 0.55f, peakY,
            endX, baseY
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawEditorialCloud(
    center: Offset,
    scale: Float,
    color: Color
) {
    val r = 10f * scale
    drawCircle(color, r * 0.85f, Offset(center.x - r * 0.9f, center.y + r * 0.15f))
    drawCircle(color, r * 1.1f, Offset(center.x, center.y - r * 0.15f))
    drawCircle(color, r * 0.8f, Offset(center.x + r * 0.95f, center.y + r * 0.2f))
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - r * 1.6f, center.y + r * 0.1f),
        size = Size(r * 3.2f, r * 0.95f),
        cornerRadius = CornerRadius(r * 0.5f, r * 0.5f)
    )
}

private fun DrawScope.drawEditorialGear(
    center: Offset,
    outerRadius: Float,
    toothCount: Int,
    gearColor: Color,
    innerRingColor: Color,
    holeColor: Color,
    rotationDeg: Float
) {
    val bodyRadius = outerRadius * 0.78f
    val toothW = (outerRadius * 0.38f).coerceAtLeast(4f)
    val toothH = outerRadius * 0.34f

    for (i in 0 until toothCount) {
        val angle = rotationDeg + i * (360f / toothCount)
        rotate(degrees = angle, pivot = center) {
            drawRoundRect(
                color = gearColor,
                topLeft = Offset(center.x - toothW * 0.5f, center.y - outerRadius),
                size = Size(toothW, toothH),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )
        }
    }

    drawCircle(color = gearColor, radius = bodyRadius, center = center)
    drawCircle(color = innerRingColor, radius = bodyRadius * 0.56f, center = center)
    drawCircle(color = holeColor, radius = bodyRadius * 0.28f, center = center)
}

private fun DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val star = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(star, color)
}

@Preview(showBackground = true, widthDp = 380, heightDp = 320)
@Composable
private fun StatusWorkflowBannerPreview() {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFF4FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("On Queue", "On Process", "Done").forEach { title ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                StatusWorkflowBannerIllustration(
                    theme = resolveStatusWorkflowTheme(title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                )
            }
        }
    }
}
