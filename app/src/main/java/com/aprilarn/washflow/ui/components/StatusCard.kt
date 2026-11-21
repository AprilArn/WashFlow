package com.example.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelGreenDark

private enum class StatusCardKind {
    ON_QUEUE,
    ON_PROCESS,
    DONE
}

private data class StatusCardTheme(
    val kind: StatusCardKind,
    val skyTop: Color,
    val skyBottom: Color,
    val labelColor: Color,
    val countColor: Color
)

private fun resolveStatusCardTheme(text: String): StatusCardTheme {
    val normalized = text.trim().lowercase()
    return when {
        normalized.contains("process") || normalized.contains("progress") -> StatusCardTheme(
            kind = StatusCardKind.ON_PROCESS,
            skyTop = Color(0xFFEEF3FB),
            skyBottom = PastelBlueBg,
            labelColor = PastelBlue,
            countColor = PastelBlue
        )
        normalized.contains("done") || normalized.contains("selesai") -> StatusCardTheme(
            kind = StatusCardKind.DONE,
            skyTop = Color(0xFFEEF7F2),
            skyBottom = PastelGreenBg,
            labelColor = PastelGreen,
            countColor = PastelGreenDark
        )
        else -> StatusCardTheme(
            kind = StatusCardKind.ON_QUEUE,
            skyTop = Color(0xFFFBF5EB),
            skyBottom = PastelAmberBg,
            labelColor = PastelAmber,
            countColor = PastelAmber
        )
    }
}

@Composable
fun StatusCard(
    text: String,
    count: Int,
    onClick: () -> Unit
) {
    val theme = resolveStatusCardTheme(text)

    Surface(
        modifier = Modifier.size(width = 140.dp, height = 180.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = Color.White,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ilustrasi bergaya Google Calendar (Material Editorial) di bagian atas kartu
            StatusCardIllustration(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
            )

            // Label status & jumlah order di bagian bawah
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.labelColor
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = theme.countColor
                )
            }
        }
    }
}

@Composable
private fun StatusCardIllustration(
    theme: StatusCardTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height

        // Latar langit pastel lembut
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.skyTop, theme.skyBottom)
            )
        )

        when (theme.kind) {
            StatusCardKind.ON_QUEUE -> drawOnQueueWorkflow()
            StatusCardKind.ON_PROCESS -> drawOnProcessWorkflow()
            StatusCardKind.DONE -> drawDoneWorkflow()
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

// ------------------------------------------------------------------
// 1. ON QUEUE — Clipboard Daftar Antrean & Jam Tunggu di Lanskap Pagi
// ------------------------------------------------------------------
private fun DrawScope.drawOnQueueWorkflow() {
    val w = size.width
    val h = size.height

    // Lingkaran matahari pagi lembut
    drawCircle(
        color = Color(0xFFFBE3B5).copy(alpha = 0.65f),
        radius = h * 0.28f,
        center = Offset(w * 0.25f, h * 0.28f)
    )

    // Awan datar kecil
    drawEditorialCloud(
        center = Offset(w * 0.76f, h * 0.22f),
        scale = h * 0.011f,
        color = Color.White.copy(alpha = 0.85f)
    )

    // Bukit hangat berlapis ala Google Calendar
    drawEditorialHill(0f, w * 0.82f, h, w * 0.34f, h * 0.60f, Color(0xFFF0DEC2))
    drawEditorialHill(w * 0.20f, w, h, w * 0.72f, h * 0.56f, Color(0xFFE4CA9F))

    // Clipboard / Papan Daftar Antrean (sedikit miring agar dinamis)
    val boardCenter = Offset(w * 0.44f, h * 0.54f)
    val boardW = w * 0.34f
    val boardH = h * 0.48f

    rotate(degrees = -5f, pivot = boardCenter) {
        val boardLeft = boardCenter.x - boardW * 0.5f
        val boardTop = boardCenter.y - boardH * 0.5f

        // Bayangan lembut papan
        drawRoundRect(
            color = Color(0xFFD5B583).copy(alpha = 0.45f),
            topLeft = Offset(boardLeft + 3f, boardTop + 4f),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Papan belakang
        drawRoundRect(
            color = Color(0xFFC99B5B),
            topLeft = Offset(boardLeft, boardTop),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Kertas putih di atas clipboard
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
        // Penjepit atas clipboard
        val clipW = boardW * 0.42f
        val clipH = boardH * 0.12f
        drawRoundRect(
            color = Color(0xFF8C6B3F),
            topLeft = Offset(boardCenter.x - clipW * 0.5f, boardTop - clipH * 0.35f),
            size = Size(clipW, clipH),
            cornerRadius = CornerRadius(5f, 5f)
        )

        // 3 Baris item antrean di dalam kertas
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

    // Lencana Jam Antrean di kanan-bawah clipboard
    val clockCenter = Offset(w * 0.65f, h * 0.61f)
    val clockR = h * 0.165f
    drawCircle(Color(0xFFD5B583).copy(alpha = 0.45f), clockR, Offset(clockCenter.x + 2f, clockCenter.y + 3f))
    drawCircle(Color(0xFFE09A4F), clockR, clockCenter)
    drawCircle(Color(0xFFFFFDF9), clockR * 0.78f, clockCenter)
    // Jarum jam (menunjuk pukul 10:15)
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
// 2. ON PROCESS — Roda Gigi Presisi (*Interlocking Gears*) & Dinamika Kerja
// ------------------------------------------------------------------
private fun DrawScope.drawOnProcessWorkflow() {
    val w = size.width
    val h = size.height

    // Lingkaran cahaya latar lembut
    drawCircle(
        color = Color.White.copy(alpha = 0.65f),
        radius = h * 0.30f,
        center = Offset(w * 0.46f, h * 0.44f)
    )

    // Awan lembut di kiri atas
    drawEditorialCloud(
        center = Offset(w * 0.25f, h * 0.22f),
        scale = h * 0.010f,
        color = Color.White.copy(alpha = 0.82f)
    )

    // Bukit biru-slate berlapis ala Google Calendar
    drawEditorialHill(0f, w * 0.78f, h, w * 0.32f, h * 0.62f, Color(0xFFC9D9F0))
    drawEditorialHill(w * 0.22f, w, h, w * 0.74f, h * 0.56f, Color(0xFFB1C7E8))

    // Busur orbit / aliran proses dinamis di sekitar roda gigi
    val arcRectSize = Size(h * 0.74f, h * 0.74f)
    val arcTopLeft = Offset(w * 0.43f - arcRectSize.width * 0.5f, h * 0.49f - arcRectSize.height * 0.5f)
    drawArc(
        color = Color(0xFF7FA3D1).copy(alpha = 0.55f),
        startAngle = 195f,
        sweepAngle = 110f,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcRectSize,
        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
    )

    // Roda gigi utama (biru medium)
    drawEditorialGear(
        center = Offset(w * 0.42f, h * 0.49f),
        outerRadius = h * 0.24f,
        toothCount = 8,
        gearColor = Color(0xFF5A82B8),
        innerRingColor = Color(0xFFDCE7F7),
        holeColor = Color(0xFFEEF3FB),
        rotationDeg = 12f
    )

    // Roda gigi pendamping (aksen coral-amber lembut agar kontras seperti palet Google)
    drawEditorialGear(
        center = Offset(w * 0.66f, h * 0.36f),
        outerRadius = h * 0.165f,
        toothCount = 6,
        gearColor = Color(0xFFEB9478),
        innerRingColor = Color(0xFFFCE4DC),
        holeColor = Color(0xFFEEF3FB),
        rotationDeg = 28f
    )

    // Kilau aktivitas (*sparkle*)
    drawSparkle(Offset(w * 0.74f, h * 0.62f), radius = h * 0.055f, color = Color(0xFF5A82B8))
    drawSparkle(Offset(w * 0.23f, h * 0.52f), radius = h * 0.045f, color = Color.White)
}

// ------------------------------------------------------------------
// 3. DONE — Paket/Pesanan Selesai Rapi, Lencana Centang & Bintang Segar
// ------------------------------------------------------------------
private fun DrawScope.drawDoneWorkflow() {
    val w = size.width
    val h = size.height

    // Halo cahaya lembut di belakang paket
    drawCircle(
        color = Color.White.copy(alpha = 0.70f),
        radius = h * 0.30f,
        center = Offset(w * 0.48f, h * 0.46f)
    )

    // Bukit hijau mint berlapis ala Google Calendar
    drawEditorialHill(0f, w * 0.80f, h, w * 0.34f, h * 0.60f, Color(0xFFC5E3D2))
    drawEditorialHill(w * 0.20f, w, h, w * 0.72f, h * 0.55f, Color(0xFFAAD3BC))

    // Kotak Paket / Hasil Layanan Rapi di tengah
    val boxW = w * 0.36f
    val boxH = h * 0.36f
    val boxLeft = w * 0.45f - boxW * 0.5f
    val boxTop = h * 0.40f

    // Bayangan kotak
    drawRoundRect(
        color = Color(0xFF83B89A).copy(alpha = 0.45f),
        topLeft = Offset(boxLeft + 3f, boxTop + 4f),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Badan kotak (krem hangat bersih)
    drawRoundRect(
        color = Color(0xFFFFFCF5),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Tutup atas kotak
    val lidH = boxH * 0.26f
    drawRoundRect(
        color = Color(0xFFF3E6CE),
        topLeft = Offset(boxLeft - 2f, boxTop),
        size = Size(boxW + 4f, lidH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Pita vertikal hijau-teal di tengah paket
    val ribbonW = boxW * 0.20f
    drawRect(
        color = Color(0xFF63A682),
        topLeft = Offset(boxLeft + (boxW - ribbonW) * 0.5f, boxTop),
        size = Size(ribbonW, boxH)
    )
    // Simpul pita kecil di atas tutup paket
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

    // Lencana Centang Hijau (Verified / Selesai) di kanan bawah paket
    val badgeCenter = Offset(w * 0.66f, h * 0.60f)
    val badgeR = h * 0.16f
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

    // Kilau bintang (*sparkles*) tanda tuntas & bersih
    drawSparkle(Offset(w * 0.24f, h * 0.32f), radius = h * 0.070f, color = Color(0xFFF2C15A))
    drawSparkle(Offset(w * 0.73f, h * 0.27f), radius = h * 0.060f, color = Color(0xFFF2C15A))
    drawSparkle(Offset(w * 0.30f, h * 0.20f), radius = h * 0.042f, color = Color.White)
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

@Preview(showBackground = true, widthDp = 490, heightDp = 220)
@Composable
fun StatusCardPreview() {
    Row(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StatusCard(text = "On Queue", count = 3, onClick = {})
        StatusCard(text = "On Process", count = 3, onClick = {})
        StatusCard(text = "Done", count = 3, onClick = {})
    }
}