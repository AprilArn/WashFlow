package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.utils.CurrencyUtils
import java.util.Calendar

internal data class MonthBannerTheme(
    val skyStart: Color,
    val skyEnd: Color,
    val titleColor: Color,
    val accentColor: Color,
    val totalPillBg: Color,
    val totalPillText: Color,
    val dividerColor: Color
)

internal fun monthBannerTheme(monthIndex: Int): MonthBannerTheme = when (monthIndex) {
    Calendar.JANUARY -> MonthBannerTheme(
        skyStart = Color(0xFFEAF3FC),
        skyEnd = Color(0xFFD6E7F8),
        titleColor = Color(0xFF2F4869),
        accentColor = Color(0xFF5D87B8),
        totalPillBg = Color(0xFFDCEBFA),
        totalPillText = Color(0xFF2F4F75),
        dividerColor = Color(0xFFD5E2F0)
    )
    Calendar.FEBRUARY -> MonthBannerTheme(
        skyStart = Color(0xFFF6EDF7),
        skyEnd = Color(0xFFEADCF0),
        titleColor = Color(0xFF4B3861),
        accentColor = Color(0xFF9370B3),
        totalPillBg = Color(0xFFEBDCF2),
        totalPillText = Color(0xFF563D73),
        dividerColor = Color(0xFFE2D4EA)
    )
    Calendar.MARCH -> MonthBannerTheme(
        skyStart = Color(0xFFEAF6F3),
        skyEnd = Color(0xFFD7EEE8),
        titleColor = Color(0xFF2B564D),
        accentColor = Color(0xFF57A18C),
        totalPillBg = Color(0xFFD7EFE8),
        totalPillText = Color(0xFF2B5E52),
        dividerColor = Color(0xFFD0E5DF)
    )
    Calendar.APRIL -> MonthBannerTheme(
        skyStart = Color(0xFFEDF2FB),
        skyEnd = Color(0xFFDDE7F8),
        titleColor = Color(0xFF354B70),
        accentColor = Color(0xFFD987A1),
        totalPillBg = Color(0xFFE0EAFA),
        totalPillText = Color(0xFF3A527A),
        dividerColor = Color(0xFFD6E0F0)
    )
    Calendar.MAY -> MonthBannerTheme(
        skyStart = Color(0xFFF2F8EC),
        skyEnd = Color(0xFFE2F1D7),
        titleColor = Color(0xFF33593B),
        accentColor = Color(0xFF6BA66E),
        totalPillBg = Color(0xFFDFEFD5),
        totalPillText = Color(0xFF35603E),
        dividerColor = Color(0xFFD4E6CC)
    )
    Calendar.JUNE -> MonthBannerTheme(
        skyStart = Color(0xFFE8F5FD),
        skyEnd = Color(0xFFCFEAFB),
        titleColor = Color(0xFF285170),
        accentColor = Color(0xFF4D9AC9),
        totalPillBg = Color(0xFFD4ECFA),
        totalPillText = Color(0xFF2B597A),
        dividerColor = Color(0xFFCDE2F0)
    )
    Calendar.JULY -> MonthBannerTheme(
        skyStart = Color(0xFFE8F7F6),
        skyEnd = Color(0xFFCFECEB),
        titleColor = Color(0xFF25545B),
        accentColor = Color(0xFF4AA3A8),
        totalPillBg = Color(0xFFD2EFEF),
        totalPillText = Color(0xFF275C63),
        dividerColor = Color(0xFFCBE3E3)
    )
    Calendar.AUGUST -> MonthBannerTheme(
        skyStart = Color(0xFFFDF2E8),
        skyEnd = Color(0xFFF8E2CE),
        titleColor = Color(0xFF6B402A),
        accentColor = Color(0xFFD47F52),
        totalPillBg = Color(0xFFF7DFC8),
        totalPillText = Color(0xFF75452D),
        dividerColor = Color(0xFFEBD7C5)
    )
    Calendar.SEPTEMBER -> MonthBannerTheme(
        skyStart = Color(0xFFF8F3E8),
        skyEnd = Color(0xFFEEE2CC),
        titleColor = Color(0xFF5A4930),
        accentColor = Color(0xFFC29555),
        totalPillBg = Color(0xFFEEE0C8),
        totalPillText = Color(0xFF634F32),
        dividerColor = Color(0xFFE4D8C3)
    )
    Calendar.OCTOBER -> MonthBannerTheme(
        skyStart = Color(0xFFFBF0E5),
        skyEnd = Color(0xFFF3DBC4),
        titleColor = Color(0xFF693C28),
        accentColor = Color(0xFFC96B4B),
        totalPillBg = Color(0xFFF2D8C0),
        totalPillText = Color(0xFF73402A),
        dividerColor = Color(0xFFE9D2BC)
    )
    Calendar.NOVEMBER -> MonthBannerTheme(
        skyStart = Color(0xFFEEF2F8),
        skyEnd = Color(0xFFDCE4F1),
        titleColor = Color(0xFF394A6B),
        accentColor = Color(0xFF6A82AB),
        totalPillBg = Color(0xFFDDE5F2),
        totalPillText = Color(0xFF3D5075),
        dividerColor = Color(0xFFD5DEE9)
    )
    else -> MonthBannerTheme( // DECEMBER
        skyStart = Color(0xFFEBEFFA),
        skyEnd = Color(0xFFD8E2F6),
        titleColor = Color(0xFF314269),
        accentColor = Color(0xFF5E78B3),
        totalPillBg = Color(0xFFD9E3F6),
        totalPillText = Color(0xFF354975),
        dividerColor = Color(0xFFD0DAEE)
    )
}

/**
 * Ilustrasi lanskap vektor datar bergaya Google Calendar (Material Editorial)
 * yang unik untuk setiap bulan (Januari–Desember).
 * Komposisi sengaja memusatkan detail ilustrasi di area tengah hingga kanan
 * agar judul bulan di sisi kiri dan pill ringkasan di kanan tetap mudah dibaca.
 */
@Composable
internal fun MonthBannerIllustration(
    monthIndex: Int,
    modifier: Modifier = Modifier
) {
    val theme = monthBannerTheme(monthIndex)

    Canvas(modifier = modifier.clipToBounds()) {
        // Latar langit gradien lembut
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(theme.skyStart, theme.skyEnd, theme.skyStart)
            )
        )

        when (monthIndex) {
            Calendar.JANUARY -> drawJanuaryWinter()
            Calendar.FEBRUARY -> drawFebruaryTwilight()
            Calendar.MARCH -> drawMarchSpringBreeze()
            Calendar.APRIL -> drawAprilBlossoms()
            Calendar.MAY -> drawMayMeadow()
            Calendar.JUNE -> drawJuneSummerCoast()
            Calendar.JULY -> drawJulySailboats()
            Calendar.AUGUST -> drawAugustBalloons()
            Calendar.SEPTEMBER -> drawSeptemberEarlyAutumn()
            Calendar.OCTOBER -> drawOctoberHarvest()
            Calendar.NOVEMBER -> drawNovemberRainyMist()
            else -> drawDecemberStarryWinter()
        }

        // Scrim lembut di sisi kiri agar teks nama bulan selalu tajam & kontras
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to theme.skyStart.copy(alpha = 0.92f),
                    0.22f to theme.skyStart.copy(alpha = 0.55f),
                    0.36f to Color.Transparent
                )
            )
        )
    }
}

// ------------------------------------------------------------------
// 1. JANUARI — Pegunungan Salju & Cemara Musim Dingin
// ------------------------------------------------------------------
private fun DrawScope.drawJanuaryWinter() {
    val w = size.width
    val h = size.height

    // Matahari musim dingin pucat
    drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.75f), radius = h * 0.24f, center = Offset(w * 0.62f, h * 0.30f))

    // Pegunungan belakang
    drawMountain(w * 0.30f, w * 0.44f, w * 0.58f, h * 0.22f, Color(0xFFB5CBE4), Color(0xFFF5F9FE))
    drawMountain(w * 0.50f, w * 0.66f, w * 0.84f, h * 0.16f, Color(0xFF9CB8D8), Color(0xFFFFFFFF))
    drawMountain(w * 0.72f, w * 0.86f, w * 1.02f, h * 0.28f, Color(0xFFABC3DF), Color(0xFFF5F9FE))

    // Bukit salju depan
    drawHillWave(startX = 0f, endX = w, baseY = h, peakX = w * 0.55f, peakY = h * 0.68f, color = Color(0xFFEAF2FB))
    drawHillWave(startX = w * 0.25f, endX = w, baseY = h, peakX = w * 0.76f, peakY = h * 0.72f, color = Color(0xFFD8E7F6))

    // Pohon cemara bersalju
    drawPineTree(w * 0.36f, h * 0.94f, h * 0.42f, Color(0xFF6B8FB5))
    drawPineTree(w * 0.40f, h * 0.96f, h * 0.30f, Color(0xFF7FA2C6))
    drawPineTree(w * 0.72f, h * 0.95f, h * 0.46f, Color(0xFF5D81A8))
    drawPineTree(w * 0.76f, h * 0.96f, h * 0.34f, Color(0xFF7598BD))

    // Butiran salju halus
    val flakes = listOf(
        0.33f to 0.28f, 0.41f to 0.18f, 0.49f to 0.36f, 0.56f to 0.22f,
        0.69f to 0.24f, 0.78f to 0.32f, 0.85f to 0.20f, 0.92f to 0.36f
    )
    flakes.forEach { (fx, fy) ->
        drawCircle(Color.White.copy(alpha = 0.85f), radius = h * 0.028f, center = Offset(w * fx, h * fy))
    }
}

// ------------------------------------------------------------------
// 2. FEBRUARI — Senja Lavender, Bulan Sabit & Kabin Hangat
// ------------------------------------------------------------------
private fun DrawScope.drawFebruaryTwilight() {
    val w = size.width
    val h = size.height

    // Bulan sabit pastel
    drawCrescentMoon(Offset(w * 0.58f, h * 0.28f), radius = h * 0.16f, moonColor = Color(0xFFFBE6B5), cutColor = Color(0xFFEEDFF4))

    // Bintang kecil
    listOf(0.42f to 0.22f, 0.49f to 0.34f, 0.65f to 0.20f, 0.74f to 0.30f, 0.83f to 0.22f).forEach { (sx, sy) ->
        drawSparkleStar(Offset(w * sx, h * sy), radius = h * 0.045f, color = Color(0xFFFFFFFF).copy(alpha = 0.9f))
    }

    // Bukit senja berlapis
    drawHillWave(0f, w, h, w * 0.46f, h * 0.56f, Color(0xFFD7C4E6))
    drawHillWave(w * 0.28f, w, h, w * 0.72f, h * 0.52f, Color(0xFFC3A8D8))
    drawHillWave(w * 0.18f, w * 0.96f, h, w * 0.56f, h * 0.72f, Color(0xFFB394CA))

    // Kabin kecil hangat dengan jendela menyala
    val cabinX = w * 0.52f
    val cabinW = h * 0.44f
    val cabinH = h * 0.30f
    val cabinTop = h * 0.66f
    // Asap cerobong halus
    drawCircle(Color.White.copy(alpha = 0.55f), h * 0.055f, Offset(cabinX + cabinW * 0.82f, cabinTop - h * 0.14f))
    drawCircle(Color.White.copy(alpha = 0.42f), h * 0.075f, Offset(cabinX + cabinW * 0.95f, cabinTop - h * 0.25f))
    // Badan kabin
    drawRoundRect(
        color = Color(0xFF7B5E96),
        topLeft = Offset(cabinX, cabinTop),
        size = Size(cabinW, cabinH),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Atap kabin
    val roof = Path().apply {
        moveTo(cabinX - cabinW * 0.12f, cabinTop + 2f)
        lineTo(cabinX + cabinW * 0.5f, cabinTop - cabinH * 0.58f)
        lineTo(cabinX + cabinW * 1.12f, cabinTop + 2f)
        close()
    }
    drawPath(roof, Color(0xFF63487D))
    // Jendela hangat menyala
    drawRoundRect(
        color = Color(0xFFFCE09B),
        topLeft = Offset(cabinX + cabinW * 0.28f, cabinTop + cabinH * 0.25f),
        size = Size(cabinW * 0.24f, cabinH * 0.38f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Pohon cemara lavender
    drawPineTree(w * 0.44f, h * 0.96f, h * 0.40f, Color(0xFF8C6EA8))
    drawPineTree(w * 0.70f, h * 0.94f, h * 0.44f, Color(0xFF8667A1))
    drawPineTree(w * 0.74f, h * 0.96f, h * 0.32f, Color(0xFF9C7EB8))
}

// ------------------------------------------------------------------
// 3. MARET — Angin Awal Musim Semi, Awan & Layang-layang
// ------------------------------------------------------------------
private fun DrawScope.drawMarchSpringBreeze() {
    val w = size.width
    val h = size.height

    // Awan lembut bergaya datar
    drawFlatCloud(Offset(w * 0.44f, h * 0.28f), scale = h * 0.015f, Color.White.copy(alpha = 0.85f))
    drawFlatCloud(Offset(w * 0.78f, h * 0.24f), scale = h * 0.012f, Color.White.copy(alpha = 0.75f))

    // Bukit hijau muda berlapis
    drawHillWave(0f, w, h, w * 0.42f, h * 0.62f, Color(0xFFB7DFD2))
    drawHillWave(w * 0.28f, w, h, w * 0.74f, h * 0.56f, Color(0xFF95CCB9))
    drawHillWave(w * 0.10f, w * 0.95f, h, w * 0.55f, h * 0.74f, Color(0xFF7CBBA5))

    // Layang-layang 1 (coral lembut)
    drawKite(Offset(w * 0.57f, h * 0.32f), sizePx = h * 0.20f, color = Color(0xFFEB9B8E), tailColor = Color(0xFF5D9684))
    // Layang-layang 2 (kuning pastel)
    drawKite(Offset(w * 0.68f, h * 0.42f), sizePx = h * 0.15f, color = Color(0xFFF2C97D), tailColor = Color(0xFF5D9684))

    // Pohon bulat musim semi
    drawRoundTree(w * 0.38f, h * 0.96f, h * 0.36f, Color(0xFF63A690), Color(0xFF7B6B5F))
    drawRoundTree(w * 0.74f, h * 0.95f, h * 0.40f, Color(0xFF5A9D87), Color(0xFF7B6B5F))
}

// ------------------------------------------------------------------
// 4. APRIL — Hujan Ringan & Mekar Bunga Sakura
// ------------------------------------------------------------------
private fun DrawScope.drawAprilBlossoms() {
    val w = size.width
    val h = size.height

    // Awan hujan lembut
    drawFlatCloud(Offset(w * 0.52f, h * 0.22f), scale = h * 0.014f, Color.White.copy(alpha = 0.82f))
    drawFlatCloud(Offset(w * 0.74f, h * 0.20f), scale = h * 0.013f, Color.White.copy(alpha = 0.78f))

    // Garis hujan diagonal halus
    val rainColor = Color(0xFF8FB0DF).copy(alpha = 0.45f)
    listOf(0.46f, 0.51f, 0.56f, 0.69f, 0.74f, 0.79f).forEachIndexed { idx, rx ->
        val startY = h * (0.30f + (idx % 2) * 0.08f)
        drawLine(
            color = rainColor,
            start = Offset(w * rx, startY),
            end = Offset(w * rx - h * 0.09f, startY + h * 0.20f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }

    // Bukit hijau-biru lembut
    drawHillWave(0f, w, h, w * 0.45f, h * 0.64f, Color(0xFFBDD8C6))
    drawHillWave(w * 0.30f, w, h, w * 0.76f, h * 0.58f, Color(0xFFA1C8B0))

    // Pohon sakura pastel
    drawBlossomTree(w * 0.42f, h * 0.96f, h * 0.46f, Color(0xFFF2B6C6), Color(0xFFF8D0DB))
    drawBlossomTree(w * 0.63f, h * 0.95f, h * 0.52f, Color(0xFFECA5B9), Color(0xFFF7C9D6))
    drawBlossomTree(w * 0.80f, h * 0.96f, h * 0.40f, Color(0xFFF2B6C6), Color(0xFFF8D0DB))

    // Kelopak melayang
    listOf(0.49f to 0.54f, 0.56f to 0.42f, 0.70f to 0.50f, 0.75f to 0.64f).forEach { (px, py) ->
        drawOval(
            color = Color(0xFFF4B6C7),
            topLeft = Offset(w * px, h * py),
            size = Size(h * 0.07f, h * 0.04f)
        )
    }
}

// ------------------------------------------------------------------
// 5. MEI — Padang Rumput Cerah & Bunga Bermekaran
// ------------------------------------------------------------------
private fun DrawScope.drawMayMeadow() {
    val w = size.width
    val h = size.height

    // Matahari hangat lembut
    drawCircle(Color(0xFFFBE6A2).copy(alpha = 0.50f), radius = h * 0.30f, center = Offset(w * 0.62f, h * 0.28f))
    drawCircle(Color(0xFFF6D478), radius = h * 0.18f, center = Offset(w * 0.62f, h * 0.28f))

    // Awan kecil
    drawFlatCloud(Offset(w * 0.44f, h * 0.24f), scale = h * 0.012f, Color.White.copy(alpha = 0.85f))

    // Bukit padang rumput berlapis
    drawHillWave(0f, w, h, w * 0.40f, h * 0.60f, Color(0xFFC1E0B4))
    drawHillWave(w * 0.26f, w, h, w * 0.72f, h * 0.54f, Color(0xFF9CCB8F))
    drawHillWave(w * 0.18f, w * 0.96f, h, w * 0.54f, h * 0.72f, Color(0xFF7EB878))

    // Bunga-bunga geometris kecil
    val flowers = listOf(
        Triple(0.42f, 0.76f, Color(0xFFF29E9E)),
        Triple(0.47f, 0.82f, Color(0xFFF7D57E)),
        Triple(0.54f, 0.74f, Color(0xFFA5B5E8)),
        Triple(0.60f, 0.84f, Color(0xFFF29E9E)),
        Triple(0.69f, 0.72f, Color(0xFFF7D57E)),
        Triple(0.75f, 0.80f, Color(0xFFEBA8C4)),
        Triple(0.82f, 0.76f, Color(0xFFFFFFFF))
    )
    flowers.forEach { (fx, fy, color) ->
        val cx = w * fx
        val cy = h * fy
        drawLine(Color(0xFF568C58), Offset(cx, cy), Offset(cx, h), strokeWidth = 2f)
        drawCircle(color, radius = h * 0.055f, center = Offset(cx, cy))
        drawCircle(Color(0xFFFFF6D6), radius = h * 0.022f, center = Offset(cx, cy))
    }
}

// ------------------------------------------------------------------
// 6. JUNI — Awal Musim Panas, Ombak Laut & Mercusuar
// ------------------------------------------------------------------
private fun DrawScope.drawJuneSummerCoast() {
    val w = size.width
    val h = size.height

    // Matahari musim panas
    drawCircle(Color(0xFFFBE197).copy(alpha = 0.45f), radius = h * 0.30f, center = Offset(w * 0.56f, h * 0.30f))
    drawCircle(Color(0xFFF5C96B), radius = h * 0.18f, center = Offset(w * 0.56f, h * 0.30f))

    // Awan tipis
    drawFlatCloud(Offset(w * 0.40f, h * 0.24f), scale = h * 0.011f, Color.White.copy(alpha = 0.85f))

    // Lapisan laut tenang
    drawHillWave(0f, w, h, w * 0.46f, h * 0.58f, Color(0xFF9AD2EE))
    drawHillWave(w * 0.24f, w, h, w * 0.70f, h * 0.64f, Color(0xFF72B9DE))
    drawHillWave(0f, w, h, w * 0.60f, h * 0.78f, Color(0xFF57A3CC))

    // Pantai pasir melengkung di sisi kanan tengah
    drawHillWave(w * 0.58f, w * 0.96f, h, w * 0.78f, h * 0.68f, Color(0xFFF2D9A7))

    // Payung pantai geometris kecil
    val umbX = w * 0.74f
    val umbY = h * 0.56f
    drawLine(Color(0xFF8C735B), Offset(umbX, umbY), Offset(umbX, h * 0.84f), strokeWidth = 2.5f)
    drawArc(
        color = Color(0xFFEB8B7C),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(umbX - h * 0.16f, umbY - h * 0.12f),
        size = Size(h * 0.32f, h * 0.24f)
    )
    drawArc(
        color = Color.White,
        startAngle = 215f,
        sweepAngle = 110f,
        useCenter = true,
        topLeft = Offset(umbX - h * 0.16f, umbY - h * 0.12f),
        size = Size(h * 0.32f, h * 0.24f)
    )
}

// ------------------------------------------------------------------
// 7. JULI — Laut Tropis & Perahu Layar
// ------------------------------------------------------------------
private fun DrawScope.drawJulySailboats() {
    val w = size.width
    val h = size.height

    // Matahari tropis
    drawCircle(Color(0xFFF7D57E), radius = h * 0.17f, center = Offset(w * 0.76f, h * 0.26f))

    // Pulau jauh pastel
    drawHillWave(w * 0.30f, w * 0.62f, h * 0.72f, w * 0.45f, h * 0.46f, Color(0xFFA7D9CE))

    // Lapisan air toska
    drawRect(color = Color(0xFF8ED0CE), topLeft = Offset(0f, h * 0.66f), size = Size(w, h * 0.34f))
    drawHillWave(0f, w, h, w * 0.52f, h * 0.74f, Color(0xFF6BB7B8))
    drawHillWave(w * 0.25f, w, h, w * 0.76f, h * 0.80f, Color(0xFF56A4A8))

    // Dua perahu layar geometris
    drawSailboat(Offset(w * 0.54f, h * 0.72f), scale = h * 0.36f, sailColor = Color.White, accentSail = Color(0xFFEF9A8A))
    drawSailboat(Offset(w * 0.69f, h * 0.68f), scale = h * 0.26f, sailColor = Color.White, accentSail = Color(0xFFF5CA76))

    // Burung camar kecil
    drawBirdV(Offset(w * 0.46f, h * 0.26f), h * 0.06f, Color(0xFF4F878C))
    drawBirdV(Offset(w * 0.51f, h * 0.21f), h * 0.05f, Color(0xFF4F878C))
}

// ------------------------------------------------------------------
// 8. AGUSTUS — Senja Hangat & Balon Udara
// ------------------------------------------------------------------
private fun DrawScope.drawAugustBalloons() {
    val w = size.width
    val h = size.height

    // Matahari senja rendah
    drawCircle(Color(0xFFFAD8A8).copy(alpha = 0.55f), radius = h * 0.32f, center = Offset(w * 0.58f, h * 0.64f))
    drawCircle(Color(0xFFF6C27D), radius = h * 0.20f, center = Offset(w * 0.58f, h * 0.64f))

    // Bukit hangat terakota
    drawHillWave(0f, w, h, w * 0.42f, h * 0.62f, Color(0xFFECC19B))
    drawHillWave(w * 0.30f, w, h, w * 0.74f, h * 0.58f, Color(0xFFDF9F73))
    drawHillWave(w * 0.14f, w * 0.96f, h, w * 0.56f, h * 0.76f, Color(0xFFCF885D))

    // 3 Balon udara pastel
    drawHotAirBalloon(Offset(w * 0.45f, h * 0.30f), radius = h * 0.15f, bodyColor = Color(0xFFE37E6E), stripeColor = Color(0xFFF9D9C6))
    drawHotAirBalloon(Offset(w * 0.63f, h * 0.25f), radius = h * 0.18f, bodyColor = Color(0xFF6B99C7), stripeColor = Color(0xFFF5D085))
    drawHotAirBalloon(Offset(w * 0.77f, h * 0.36f), radius = h * 0.12f, bodyColor = Color(0xFFEBB660), stripeColor = Color(0xFFFFFFFF))
}

// ------------------------------------------------------------------
// 9. SEPTEMBER — Transisi Musim Gugur, Bukit Okre & Daun Melayang
// ------------------------------------------------------------------
private fun DrawScope.drawSeptemberEarlyAutumn() {
    val w = size.width
    val h = size.height

    // Matahari lembut
    drawCircle(Color(0xFFF5D89C), radius = h * 0.17f, center = Offset(w * 0.48f, h * 0.28f))

    // Bukit okre & hijau zaitun
    drawHillWave(0f, w, h, w * 0.40f, h * 0.60f, Color(0xFFE2CCA5))
    drawHillWave(w * 0.28f, w, h, w * 0.72f, h * 0.56f, Color(0xFFC5C99E))
    drawHillWave(w * 0.16f, w * 0.96f, h, w * 0.56f, h * 0.74f, Color(0xFFCCA774))

    // Pohon musim gugur awal
    drawRoundTree(w * 0.42f, h * 0.95f, h * 0.44f, Color(0xFF93AA78), Color(0xFF7D6552))
    drawRoundTree(w * 0.62f, h * 0.94f, h * 0.48f, Color(0xFFDFA35F), Color(0xFF7D6552))
    drawRoundTree(w * 0.75f, h * 0.96f, h * 0.38f, Color(0xFFB3BA80), Color(0xFF7D6552))

    // Garis angin melengkung lembut + daun
    val breeze = Path().apply {
        moveTo(w * 0.48f, h * 0.44f)
        cubicTo(w * 0.56f, h * 0.32f, w * 0.64f, h * 0.54f, w * 0.72f, h * 0.40f)
    }
    drawPath(breeze, color = Color(0xFFC7A87A).copy(alpha = 0.55f), style = Stroke(width = 2.2f, cap = StrokeCap.Round))

    listOf(0.53f to 0.38f, 0.66f to 0.46f, 0.71f to 0.34f).forEach { (lx, ly) ->
        drawOval(Color(0xFFD99252), Offset(w * lx, h * ly), Size(h * 0.08f, h * 0.045f))
    }
}

// ------------------------------------------------------------------
// 10. OKTOBER — Musim Panen & Pepohonan Musim Gugur Keemasan
// ------------------------------------------------------------------
private fun DrawScope.drawOctoberHarvest() {
    val w = size.width
    val h = size.height

    // Bulan panen / matahari senja
    drawCircle(Color(0xFFFBE2B4).copy(alpha = 0.5f), radius = h * 0.28f, center = Offset(w * 0.68f, h * 0.30f))
    drawCircle(Color(0xFFF5CA7C), radius = h * 0.17f, center = Offset(w * 0.68f, h * 0.30f))

    // Bukit hangat berlapis
    drawHillWave(0f, w, h, w * 0.46f, h * 0.60f, Color(0xFFEAC298))
    drawHillWave(w * 0.26f, w, h, w * 0.74f, h * 0.56f, Color(0xFFDC9C6D))
    drawHillWave(w * 0.12f, w * 0.96f, h, w * 0.54f, h * 0.74f, Color(0xFFC98055))

    // Rumpun pohon warna gugur (mustard, coral, terakota)
    drawRoundTree(w * 0.39f, h * 0.96f, h * 0.44f, Color(0xFFE6B55E), Color(0xFF7A5342))
    drawRoundTree(w * 0.45f, h * 0.96f, h * 0.34f, Color(0xFFD97D5E), Color(0xFF7A5342))
    drawRoundTree(w * 0.58f, h * 0.95f, h * 0.50f, Color(0xFFC96A4D), Color(0xFF7A5342))
    drawRoundTree(w * 0.64f, h * 0.96f, h * 0.38f, Color(0xFFE6B55E), Color(0xFF7A5342))
    drawRoundTree(w * 0.78f, h * 0.96f, h * 0.42f, Color(0xFFD97D5E), Color(0xFF7A5342))

    // Daun gugur beterbangan
    listOf(0.50f to 0.36f, 0.54f to 0.48f, 0.71f to 0.52f).forEach { (lx, ly) ->
        drawOval(Color(0xFFD97855), Offset(w * lx, h * ly), Size(h * 0.075f, h * 0.042f))
    }
}

// ------------------------------------------------------------------
// 11. NOVEMBER — Kabut Hujan Sejuk & Pegunungan Biru-Slate
// ------------------------------------------------------------------
private fun DrawScope.drawNovemberRainyMist() {
    val w = size.width
    val h = size.height

    // Awan kabut lembut
    drawFlatCloud(Offset(w * 0.46f, h * 0.24f), scale = h * 0.014f, Color.White.copy(alpha = 0.80f))
    drawFlatCloud(Offset(w * 0.71f, h * 0.22f), scale = h * 0.013f, Color.White.copy(alpha = 0.75f))

    // Tetes hujan diagonal lembut
    val rainColor = Color(0xFF7E96BA).copy(alpha = 0.40f)
    listOf(0.42f, 0.47f, 0.52f, 0.66f, 0.71f, 0.76f).forEachIndexed { idx, rx ->
        val startY = h * (0.32f + (idx % 2) * 0.08f)
        drawLine(
            color = rainColor,
            start = Offset(w * rx, startY),
            end = Offset(w * rx - h * 0.08f, startY + h * 0.18f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }

    // Pegunungan kabut berlapis
    drawHillWave(0f, w, h, w * 0.44f, h * 0.54f, Color(0xFFC1CFE3))
    drawHillWave(w * 0.28f, w, h, w * 0.72f, h * 0.50f, Color(0xFFA2B6D1))
    drawHillWave(w * 0.16f, w * 0.98f, h, w * 0.56f, h * 0.70f, Color(0xFF879EC0))

    // Pohon pinus gelap lembut
    drawPineTree(w * 0.39f, h * 0.96f, h * 0.44f, Color(0xFF5D7499))
    drawPineTree(w * 0.43f, h * 0.96f, h * 0.32f, Color(0xFF6E86AB))
    drawPineTree(w * 0.73f, h * 0.96f, h * 0.46f, Color(0xFF566D91))

    // Siluet rumah mungil dengan lampu jendela kuning hangat
    val hx = w * 0.57f
    val hw = h * 0.36f
    val hh = h * 0.24f
    val hTop = h * 0.72f
    drawRoundRect(Color(0xFF536687), Offset(hx, hTop), Size(hw, hh), CornerRadius(3f, 3f))
    val roof = Path().apply {
        moveTo(hx - hw * 0.12f, hTop + 2f)
        lineTo(hx + hw * 0.5f, hTop - hh * 0.6f)
        lineTo(hx + hw * 1.12f, hTop + 2f)
        close()
    }
    drawPath(roof, Color(0xFF425475))
    drawRoundRect(
        color = Color(0xFFFAD98B),
        topLeft = Offset(hx + hw * 0.35f, hTop + hh * 0.26f),
        size = Size(hw * 0.28f, hh * 0.40f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

// ------------------------------------------------------------------
// 12. DESEMBER — Langit Bintang Akhir Tahun & Cemara Meriah
// ------------------------------------------------------------------
private fun DrawScope.drawDecemberStarryWinter() {
    val w = size.width
    val h = size.height

    // Bulan sabit & bintang berkilau
    drawCrescentMoon(Offset(w * 0.64f, h * 0.26f), radius = h * 0.15f, moonColor = Color(0xFFFBE39E), cutColor = Color(0xFFDFE7F8))

    listOf(0.41f to 0.24f, 0.50f to 0.18f, 0.56f to 0.34f, 0.73f to 0.22f, 0.82f to 0.30f).forEachIndexed { idx, (sx, sy) ->
        val starColor = if (idx % 2 == 0) Color(0xFFF7D57E) else Color.White
        drawSparkleStar(Offset(w * sx, h * sy), radius = h * 0.055f, color = starColor)
    }

    // Bukit malam akhir tahun
    drawHillWave(0f, w, h, w * 0.45f, h * 0.60f, Color(0xFFBCCAE8))
    drawHillWave(w * 0.28f, w, h, w * 0.74f, h * 0.56f, Color(0xFF9FB2DC))
    drawHillWave(w * 0.14f, w * 0.96f, h, w * 0.56f, h * 0.74f, Color(0xFFE6EEFA))

    // Deretan pohon cemara hijau-pinus dengan titik lampu hangat
    val trees = listOf(
        Triple(0.42f, 0.44f, Color(0xFF5D8579)),
        Triple(0.48f, 0.54f, Color(0xFF4B7368)),
        Triple(0.54f, 0.38f, Color(0xFF6B9488)),
        Triple(0.70f, 0.48f, Color(0xFF4F786C)),
        Triple(0.76f, 0.36f, Color(0xFF648D81))
    )
    trees.forEach { (tx, thRatio, color) ->
        drawPineTree(w * tx, h * 0.96f, h * thRatio, color)
        // Hiasan titik emas hangat di puncak cemara
        drawCircle(
            color = Color(0xFFF7D57E),
            radius = h * 0.032f,
            center = Offset(w * tx, h * (0.96f - thRatio))
        )
    }
}

// ------------------------------------------------------------------
// Primitive helpers (flat geometric Google Calendar illustration style)
// ------------------------------------------------------------------

private fun DrawScope.drawHillWave(
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
            peakX + (endX - peakX) * 0.25f, peakY,
            endX, baseY
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawMountain(
    leftX: Float,
    peakX: Float,
    rightX: Float,
    peakY: Float,
    bodyColor: Color,
    snowColor: Color
) {
    val baseY = size.height
    val body = Path().apply {
        moveTo(leftX, baseY)
        lineTo(peakX, peakY)
        lineTo(rightX, baseY)
        close()
    }
    drawPath(body, bodyColor)

    val snowRatio = 0.34f
    val snowLeftX = peakX - (peakX - leftX) * snowRatio
    val snowRightX = peakX + (rightX - peakX) * snowRatio
    val snowY = peakY + (baseY - peakY) * snowRatio
    val snow = Path().apply {
        moveTo(peakX, peakY)
        lineTo(snowRightX, snowY)
        lineTo(peakX + (snowRightX - peakX) * 0.35f, snowY - 5f)
        lineTo(peakX, snowY + 4f)
        lineTo(peakX - (peakX - snowLeftX) * 0.35f, snowY - 5f)
        lineTo(snowLeftX, snowY)
        close()
    }
    drawPath(snow, snowColor)
}

private fun DrawScope.drawPineTree(
    centerX: Float,
    bottomY: Float,
    treeHeight: Float,
    color: Color
) {
    val halfW = treeHeight * 0.34f
    val trunkH = treeHeight * 0.16f
    val canopyBottom = bottomY - trunkH
    val topY = bottomY - treeHeight

    drawRect(
        color = Color(0xFF7B6B63),
        topLeft = Offset(centerX - halfW * 0.16f, canopyBottom),
        size = Size(halfW * 0.32f, trunkH)
    )
    val canopy = Path().apply {
        moveTo(centerX, topY)
        lineTo(centerX + halfW, canopyBottom)
        lineTo(centerX - halfW, canopyBottom)
        close()
    }
    drawPath(canopy, color)
}

private fun DrawScope.drawRoundTree(
    centerX: Float,
    bottomY: Float,
    treeHeight: Float,
    canopyColor: Color,
    trunkColor: Color
) {
    val radius = treeHeight * 0.34f
    val trunkH = treeHeight * 0.45f
    drawLine(
        color = trunkColor,
        start = Offset(centerX, bottomY),
        end = Offset(centerX, bottomY - trunkH),
        strokeWidth = (treeHeight * 0.08f).coerceAtLeast(2.5f),
        cap = StrokeCap.Round
    )
    drawCircle(
        color = canopyColor,
        radius = radius,
        center = Offset(centerX, bottomY - trunkH - radius * 0.65f)
    )
}

private fun DrawScope.drawBlossomTree(
    centerX: Float,
    bottomY: Float,
    treeHeight: Float,
    mainPink: Color,
    lightPink: Color
) {
    val r = treeHeight * 0.28f
    val trunkTop = bottomY - treeHeight * 0.52f
    drawLine(
        color = Color(0xFF8C726B),
        start = Offset(centerX, bottomY),
        end = Offset(centerX, trunkTop),
        strokeWidth = (treeHeight * 0.08f).coerceAtLeast(2.5f),
        cap = StrokeCap.Round
    )
    drawCircle(mainPink, r, Offset(centerX - r * 0.55f, trunkTop - r * 0.2f))
    drawCircle(mainPink, r, Offset(centerX + r * 0.55f, trunkTop - r * 0.2f))
    drawCircle(lightPink, r * 1.05f, Offset(centerX, trunkTop - r * 0.72f))
}

private fun DrawScope.drawFlatCloud(
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

private fun DrawScope.drawKite(
    center: Offset,
    sizePx: Float,
    color: Color,
    tailColor: Color
) {
    val tail = Path().apply {
        moveTo(center.x, center.y + sizePx * 0.65f)
        cubicTo(
            center.x - sizePx * 0.5f, center.y + sizePx * 1.25f,
            center.x + sizePx * 0.35f, center.y + sizePx * 1.65f,
            center.x - sizePx * 0.25f, center.y + sizePx * 2.1f
        )
    }
    drawPath(tail, color = tailColor.copy(alpha = 0.65f), style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    val diamond = Path().apply {
        moveTo(center.x, center.y - sizePx * 0.65f)
        lineTo(center.x + sizePx * 0.48f, center.y)
        lineTo(center.x, center.y + sizePx * 0.65f)
        lineTo(center.x - sizePx * 0.48f, center.y)
        close()
    }
    drawPath(diamond, color)
}

private fun DrawScope.drawSailboat(
    baseCenter: Offset,
    scale: Float,
    sailColor: Color,
    accentSail: Color
) {
    val hullW = scale * 1.1f
    val hullH = scale * 0.24f
    val hull = Path().apply {
        moveTo(baseCenter.x - hullW * 0.5f, baseCenter.y)
        lineTo(baseCenter.x + hullW * 0.5f, baseCenter.y)
        lineTo(baseCenter.x + hullW * 0.36f, baseCenter.y + hullH)
        lineTo(baseCenter.x - hullW * 0.36f, baseCenter.y + hullH)
        close()
    }
    drawPath(hull, Color(0xFF477B82))

    // Layar utama
    val mainSail = Path().apply {
        moveTo(baseCenter.x - 1f, baseCenter.y - scale * 0.05f)
        lineTo(baseCenter.x - 1f, baseCenter.y - scale * 0.92f)
        lineTo(baseCenter.x - hullW * 0.44f, baseCenter.y - scale * 0.05f)
        close()
    }
    drawPath(mainSail, sailColor)

    // Layar depan aksen
    val frontSail = Path().apply {
        moveTo(baseCenter.x + 2f, baseCenter.y - scale * 0.05f)
        lineTo(baseCenter.x + 2f, baseCenter.y - scale * 0.72f)
        lineTo(baseCenter.x + hullW * 0.38f, baseCenter.y - scale * 0.05f)
        close()
    }
    drawPath(frontSail, accentSail)
}

private fun DrawScope.drawHotAirBalloon(
    center: Offset,
    radius: Float,
    bodyColor: Color,
    stripeColor: Color
) {
    // Tali & keranjang
    val basketTop = center.y + radius * 1.28f
    drawLine(Color(0xFF8C6954), Offset(center.x - radius * 0.25f, center.y + radius * 0.85f), Offset(center.x - radius * 0.16f, basketTop), 1.4f)
    drawLine(Color(0xFF8C6954), Offset(center.x + radius * 0.25f, center.y + radius * 0.85f), Offset(center.x + radius * 0.16f, basketTop), 1.4f)
    drawRoundRect(
        color = Color(0xFF8C6954),
        topLeft = Offset(center.x - radius * 0.24f, basketTop),
        size = Size(radius * 0.48f, radius * 0.32f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Balon
    drawCircle(bodyColor, radius, center)
    drawOval(
        color = stripeColor,
        topLeft = Offset(center.x - radius * 0.42f, center.y - radius * 0.98f),
        size = Size(radius * 0.84f, radius * 1.96f)
    )
}

private fun DrawScope.drawCrescentMoon(
    center: Offset,
    radius: Float,
    moonColor: Color,
    cutColor: Color
) {
    drawCircle(moonColor, radius, center)
    drawCircle(cutColor, radius * 0.88f, Offset(center.x + radius * 0.42f, center.y - radius * 0.15f))
}

private fun DrawScope.drawSparkleStar(
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

private fun DrawScope.drawBirdV(
    center: Offset,
    wingSize: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x - wingSize, center.y)
        quadraticTo(center.x - wingSize * 0.4f, center.y - wingSize * 0.55f, center.x, center.y)
        quadraticTo(center.x + wingSize * 0.4f, center.y - wingSize * 0.55f, center.x + wingSize, center.y)
    }
    drawPath(path, color = color, style = Stroke(width = 1.8f, cap = StrokeCap.Round))
}

@Preview(showBackground = true, widthDp = 820, heightDp = 920)
@Composable
fun MonthBannerGalleryPreview() {
    val monthNames = listOf(
        "Januari 2026", "Februari 2026", "Maret 2026", "April 2026",
        "Mei 2026", "Juni 2026", "Juli 2026", "Agustus 2026",
        "September 2026", "Oktober 2026", "November 2026", "Desember 2026"
    )
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            monthNames.forEachIndexed { idx, name ->
                val theme = monthBannerTheme(idx)
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (idx > 0) HorizontalDivider(color = theme.dividerColor)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                    ) {
                        MonthBannerIllustration(
                            monthIndex = idx,
                            modifier = Modifier.matchParentSize()
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(16.dp)
                                        .background(theme.accentColor, RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = name.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.titleColor,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.90f),
                                    border = BorderStroke(0.5.dp, theme.dividerColor)
                                ) {
                                    Text(
                                        text = "${(idx + 1) * 3} Order",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.titleColor.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.92f),
                                    border = BorderStroke(0.5.dp, theme.accentColor.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = CurrencyUtils.formatRupiahWithSymbol((idx + 1) * 425000.0),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.totalPillText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
