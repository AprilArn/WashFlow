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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.utils.CurrencyUtils
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

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
    drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.78f), radius = h * 0.24f, center = Offset(w * 0.61f, h * 0.28f))

    // Pegunungan belakang
    drawMountain(w * 0.28f, w * 0.43f, w * 0.58f, h * 0.20f, Color(0xFFB5CBE4), Color(0xFFF5F9FE))
    drawMountain(w * 0.49f, w * 0.65f, w * 0.83f, h * 0.14f, Color(0xFF9CB8D8), Color(0xFFFFFFFF))
    drawMountain(w * 0.71f, w * 0.85f, w * 1.02f, h * 0.26f, Color(0xFFABC3DF), Color(0xFFF5F9FE))

    // Bukit salju depan berlapis
    drawHillWave(startX = w * 0.10f, endX = w * 0.72f, baseY = h, peakX = w * 0.42f, peakY = h * 0.62f, color = Color(0xFFEAF2FB))
    drawHillWave(startX = w * 0.34f, endX = w * 0.98f, baseY = h, peakX = w * 0.68f, peakY = h * 0.64f, color = Color(0xFFD8E7F6))

    // Pohon cemara bersalju
    drawPineTree(w * 0.36f, h * 0.95f, h * 0.44f, Color(0xFF6B8FB5))
    drawPineTree(w * 0.40f, h * 0.97f, h * 0.32f, Color(0xFF7FA2C6))
    drawPineTree(w * 0.72f, h * 0.96f, h * 0.48f, Color(0xFF5D81A8))
    drawPineTree(w * 0.76f, h * 0.97f, h * 0.35f, Color(0xFF7598BD))

    // Butiran salju halus
    val flakes = listOf(
        0.33f to 0.28f, 0.41f to 0.18f, 0.49f to 0.36f, 0.56f to 0.22f,
        0.69f to 0.24f, 0.78f to 0.32f, 0.85f to 0.20f, 0.92f to 0.36f
    )
    flakes.forEach { (fx, fy) ->
        drawCircle(Color.White.copy(alpha = 0.88f), radius = h * 0.028f, center = Offset(w * fx, h * fy))
    }
}

// ------------------------------------------------------------------
// 2. FEBRUARI — Senja Lavender, Bulan Sabit & Kabin Hangat
// ------------------------------------------------------------------
private fun DrawScope.drawFebruaryTwilight() {
    val w = size.width
    val h = size.height

    // Bulan sabit pastel
    drawCrescentMoon(Offset(w * 0.58f, h * 0.26f), radius = h * 0.16f, moonColor = Color(0xFFFBE6B5), cutColor = Color(0xFFEEDFF4))

    // Bintang kecil
    listOf(0.42f to 0.22f, 0.49f to 0.34f, 0.65f to 0.20f, 0.74f to 0.28f, 0.83f to 0.22f).forEach { (sx, sy) ->
        drawSparkleStar(Offset(w * sx, h * sy), radius = h * 0.048f, color = Color(0xFFFFFFFF).copy(alpha = 0.9f))
    }

    // Bukit senja berlapis
    drawHillWave(w * 0.08f, w * 0.70f, h, w * 0.40f, h * 0.50f, Color(0xFFD7C4E6))
    drawHillWave(w * 0.36f, w * 0.98f, h, w * 0.70f, h * 0.46f, Color(0xFFC3A8D8))
    drawHillWave(w * 0.22f, w * 0.88f, h, w * 0.54f, h * 0.66f, Color(0xFFB394CA))

    // Kabin kecil hangat dengan jendela menyala
    val cabinX = w * 0.52f
    val cabinW = h * 0.46f
    val cabinH = h * 0.30f
    val cabinTop = h * 0.66f
    // Asap cerobong halus
    drawCircle(Color.White.copy(alpha = 0.58f), h * 0.055f, Offset(cabinX + cabinW * 0.82f, cabinTop - h * 0.14f))
    drawCircle(Color.White.copy(alpha = 0.42f), h * 0.075f, Offset(cabinX + cabinW * 0.95f, cabinTop - h * 0.26f))
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
        topLeft = Offset(cabinX + cabinW * 0.26f, cabinTop + cabinH * 0.25f),
        size = Size(cabinW * 0.22f, cabinH * 0.38f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = Color(0xFFFCE09B),
        topLeft = Offset(cabinX + cabinW * 0.56f, cabinTop + cabinH * 0.25f),
        size = Size(cabinW * 0.22f, cabinH * 0.38f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Pohon cemara lavender
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.42f, Color(0xFF8C6EA8))
    drawPineTree(w * 0.70f, h * 0.95f, h * 0.46f, Color(0xFF8667A1))
    drawPineTree(w * 0.74f, h * 0.97f, h * 0.34f, Color(0xFF9C7EB8))
}

// ------------------------------------------------------------------
// 3. MARET — Angin Awal Musim Semi, Awan & Layang-layang
// ------------------------------------------------------------------
private fun DrawScope.drawMarchSpringBreeze() {
    val w = size.width
    val h = size.height

    // Awan lembut bergaya datar
    drawFlatCloud(Offset(w * 0.43f, h * 0.26f), scale = h * 0.015f, Color.White.copy(alpha = 0.88f))
    drawFlatCloud(Offset(w * 0.77f, h * 0.22f), scale = h * 0.013f, Color.White.copy(alpha = 0.78f))

    // Bukit hijau muda berlapis yang lebih berlekuk
    drawHillWave(w * 0.06f, w * 0.66f, h, w * 0.38f, h * 0.54f, Color(0xFFB7DFD2))
    drawHillWave(w * 0.36f, w * 0.98f, h, w * 0.72f, h * 0.48f, Color(0xFF95CCB9))
    drawHillWave(w * 0.18f, w * 0.84f, h, w * 0.52f, h * 0.68f, Color(0xFF7CBBA5))

    // Layang-layang 1 (coral lembut, ukuran lebih ekspresif dengan pita ekor)
    drawKite(
        center = Offset(w * 0.56f, h * 0.29f),
        sizePx = h * 0.26f,
        primaryColor = Color(0xFFEC8F81),
        secondaryColor = Color(0xFFF6B5A8),
        tailColor = Color(0xFF4F8A78)
    )
    // Layang-layang 2 (kuning keemasan)
    drawKite(
        center = Offset(w * 0.68f, h * 0.39f),
        sizePx = h * 0.20f,
        primaryColor = Color(0xFFF2C26B),
        secondaryColor = Color(0xFFF9DC9C),
        tailColor = Color(0xFF4F8A78)
    )

    // Burung musim semi kecil
    drawBirdV(Offset(w * 0.49f, h * 0.22f), h * 0.055f, Color(0xFF4D8A78))

    // Pohon bulat musim semi
    drawRoundTree(w * 0.37f, h * 0.97f, h * 0.42f, Color(0xFF63A690), Color(0xFF798F7A), Color(0xFF7B6B5F))
    drawRoundTree(w * 0.74f, h * 0.96f, h * 0.46f, Color(0xFF569983), Color(0xFF6DB09A), Color(0xFF7B6B5F))
    drawRoundTree(w * 0.79f, h * 0.97f, h * 0.33f, Color(0xFF6DB09A), Color(0xFF83C2AD), Color(0xFF7B6B5F))
}

// ------------------------------------------------------------------
// 4. APRIL — Hujan Ringan & Mekar Bunga Sakura
// ------------------------------------------------------------------
private fun DrawScope.drawAprilBlossoms() {
    val w = size.width
    val h = size.height

    // Awan hujan lembut
    drawFlatCloud(Offset(w * 0.50f, h * 0.22f), scale = h * 0.015f, Color.White.copy(alpha = 0.85f))
    drawFlatCloud(Offset(w * 0.73f, h * 0.20f), scale = h * 0.013f, Color.White.copy(alpha = 0.80f))

    // Garis hujan diagonal halus
    val rainColor = Color(0xFF8FB0DF).copy(alpha = 0.48f)
    listOf(0.45f, 0.50f, 0.55f, 0.68f, 0.73f, 0.78f).forEachIndexed { idx, rx ->
        val startY = h * (0.30f + (idx % 2) * 0.08f)
        drawLine(
            color = rainColor,
            start = Offset(w * rx, startY),
            end = Offset(w * rx - h * 0.09f, startY + h * 0.20f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }

    // Bukit hijau-biru lembut berlapis
    drawHillWave(w * 0.08f, w * 0.68f, h, w * 0.40f, h * 0.54f, Color(0xFFBDD8C6))
    drawHillWave(w * 0.34f, w * 0.98f, h, w * 0.72f, h * 0.48f, Color(0xFFA1C8B0))
    drawHillWave(w * 0.20f, w * 0.88f, h, w * 0.56f, h * 0.70f, Color(0xFF8DB99E))

    // Pohon sakura pastel
    drawBlossomTree(w * 0.41f, h * 0.97f, h * 0.50f, Color(0xFFF0ACC0), Color(0xFFF9D1DC))
    drawBlossomTree(w * 0.62f, h * 0.96f, h * 0.56f, Color(0xFFE89AB0), Color(0xFFF6C4D2))
    drawBlossomTree(w * 0.78f, h * 0.97f, h * 0.44f, Color(0xFFF0ACC0), Color(0xFFF9D1DC))

    // Kelopak sakura miring melayang tertiup angin
    listOf(
        Triple(0.47f, 0.50f, -25f),
        Triple(0.54f, 0.38f, 18f),
        Triple(0.68f, 0.46f, -32f),
        Triple(0.73f, 0.60f, 22f)
    ).forEach { (px, py, angle) ->
        drawFloatingLeaf(
            center = Offset(w * px, h * py),
            length = h * 0.085f,
            width = h * 0.045f,
            angleDegrees = angle,
            color = Color(0xFFF0A7BC)
        )
    }
}

// ------------------------------------------------------------------
// 5. MEI — Padang Rumput Cerah, Kincir Angin & Bunga Bermekaran
// ------------------------------------------------------------------
private fun DrawScope.drawMayMeadow() {
    val w = size.width
    val h = size.height

    // Matahari hangat lembut
    drawCircle(Color(0xFFFBE6A2).copy(alpha = 0.55f), radius = h * 0.30f, center = Offset(w * 0.64f, h * 0.26f))
    drawCircle(Color(0xFFF6D478), radius = h * 0.18f, center = Offset(w * 0.64f, h * 0.26f))

    // Awan kecil
    drawFlatCloud(Offset(w * 0.45f, h * 0.23f), scale = h * 0.013f, Color.White.copy(alpha = 0.88f))

    // Bukit padang rumput berlapis yang ekspresif
    drawHillWave(w * 0.06f, w * 0.68f, h, w * 0.38f, h * 0.50f, Color(0xFFC1E0B4))
    drawHillWave(w * 0.34f, w * 0.98f, h, w * 0.70f, h * 0.44f, Color(0xFF9CCB8F))
    drawHillWave(w * 0.16f, w * 0.92f, h, w * 0.54f, h * 0.66f, Color(0xFF7EB878))

    // Kincir angin (Windmill) bergaya pedesaan musim semi sebagai focal point
    drawWindmill(
        baseCenter = Offset(w * 0.56f, h * 0.74f),
        towerHeight = h * 0.42f,
        bodyColor = Color(0xFFFDFBF6),
        roofColor = Color(0xFFD97E6A),
        bladeColor = Color(0xFF5D7A58)
    )

    // Pohon cemara/cypress padang rumput
    drawRoundTree(w * 0.39f, h * 0.96f, h * 0.38f, Color(0xFF609E64), Color(0xFF76B37A), Color(0xFF786452))
    drawRoundTree(w * 0.75f, h * 0.95f, h * 0.42f, Color(0xFF56945A), Color(0xFF6CA870), Color(0xFF786452))

    // Bunga-bunga berkelopak di padang rumput depan
    val flowers = listOf(
        Triple(0.45f, 0.78f, Color(0xFFF28D8D)),
        Triple(0.50f, 0.83f, Color(0xFFF7D06A)),
        Triple(0.63f, 0.80f, Color(0xFFFFFFFF)),
        Triple(0.69f, 0.76f, Color(0xFFE99BB9)),
        Triple(0.81f, 0.82f, Color(0xFFF7D06A))
    )
    flowers.forEach { (fx, fy, petalColor) ->
        drawMeadowFlower(
            center = Offset(w * fx, h * fy),
            bottomY = h,
            petalRadius = h * 0.068f,
            petalColor = petalColor,
            centerColor = Color(0xFFF5B841)
        )
    }
}

// ------------------------------------------------------------------
// 6. JUNI — Pesisir Musim Panas, Ombak Laut & Mercusuar Tanjung
// ------------------------------------------------------------------
private fun DrawScope.drawJuneSummerCoast() {
    val w = size.width
    val h = size.height

    // Matahari musim panas cerah
    drawCircle(Color(0xFFFBE197).copy(alpha = 0.48f), radius = h * 0.30f, center = Offset(w * 0.53f, h * 0.28f))
    drawCircle(Color(0xFFF5C96B), radius = h * 0.18f, center = Offset(w * 0.53f, h * 0.28f))

    // Awan tipis & burung camar pantai
    drawFlatCloud(Offset(w * 0.39f, h * 0.23f), scale = h * 0.012f, Color.White.copy(alpha = 0.88f))
    drawBirdV(Offset(w * 0.62f, h * 0.22f), h * 0.055f, Color(0xFF427E9E))
    drawBirdV(Offset(w * 0.66f, h * 0.17f), h * 0.045f, Color(0xFF427E9E))

    // Lapisan laut musim panas
    drawHillWave(w * 0.04f, w * 0.78f, h, w * 0.44f, h * 0.54f, Color(0xFF9AD2EE))
    drawHillWave(w * 0.22f, w * 0.96f, h, w * 0.62f, h * 0.60f, Color(0xFF72B9DE))
    drawHillWave(0f, w, h, w * 0.50f, h * 0.74f, Color(0xFF57A3CC))

    // Tanjung berpasir & tebing hijau di sisi kanan-tengah
    drawHillWave(w * 0.54f, w * 0.98f, h, w * 0.74f, h * 0.56f, Color(0xFFEED39F))
    drawHillWave(w * 0.58f, w * 0.94f, h, w * 0.73f, h * 0.64f, Color(0xFF86B88E))

    // Mercusuar merah-putih ikonik di puncak tanjung
    drawLighthouse(
        baseCenter = Offset(w * 0.72f, h * 0.68f),
        towerHeight = h * 0.48f,
        bodyColor = Color.White,
        stripeColor = Color(0xFFE07162),
        lightColor = Color(0xFFFBE285)
    )

    // Payung pantai di pesisir pasir
    val umbX = w * 0.63f
    val umbY = h * 0.64f
    drawLine(Color(0xFF7A634E), Offset(umbX, umbY), Offset(umbX, h * 0.88f), strokeWidth = 2.4f)
    drawArc(
        color = Color(0xFFEB8374),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(umbX - h * 0.18f, umbY - h * 0.13f),
        size = Size(h * 0.36f, h * 0.26f)
    )
    drawArc(
        color = Color.White,
        startAngle = 215f,
        sweepAngle = 110f,
        useCenter = true,
        topLeft = Offset(umbX - h * 0.18f, umbY - h * 0.13f),
        size = Size(h * 0.36f, h * 0.26f)
    )
}

// ------------------------------------------------------------------
// 7. JULI — Laut Tropis & Perahu Layar
// ------------------------------------------------------------------
private fun DrawScope.drawJulySailboats() {
    val w = size.width
    val h = size.height

    // Matahari tropis
    drawCircle(Color(0xFFFBE39C).copy(alpha = 0.45f), radius = h * 0.27f, center = Offset(w * 0.75f, h * 0.26f))
    drawCircle(Color(0xFFF7D57E), radius = h * 0.17f, center = Offset(w * 0.75f, h * 0.26f))

    // Pulau jauh pastel berlapis
    drawHillWave(w * 0.28f, w * 0.62f, h * 0.70f, w * 0.44f, h * 0.40f, Color(0xFFA7D9CE))
    drawHillWave(w * 0.38f, w * 0.66f, h * 0.70f, w * 0.52f, h * 0.48f, Color(0xFF8EC8BC))

    // Lapisan air toska
    drawRect(color = Color(0xFF8ED0CE), topLeft = Offset(0f, h * 0.66f), size = Size(w, h * 0.34f))
    drawHillWave(w * 0.05f, w * 0.85f, h, w * 0.48f, h * 0.70f, Color(0xFF6BB7B8))
    drawHillWave(w * 0.25f, w, h, w * 0.72f, h * 0.74f, Color(0xFF56A4A8))

    // Dua perahu layar geometris
    drawSailboat(Offset(w * 0.55f, h * 0.72f), scale = h * 0.40f, sailColor = Color.White, accentSail = Color(0xFFEF9A8A))
    drawSailboat(Offset(w * 0.69f, h * 0.68f), scale = h * 0.29f, sailColor = Color.White, accentSail = Color(0xFFF5CA76))

    // Burung camar kecil
    drawBirdV(Offset(w * 0.46f, h * 0.26f), h * 0.06f, Color(0xFF4F878C))
    drawBirdV(Offset(w * 0.51f, h * 0.20f), h * 0.05f, Color(0xFF4F878C))
}

// ------------------------------------------------------------------
// 8. AGUSTUS — Senja Hangat & Balon Udara
// ------------------------------------------------------------------
private fun DrawScope.drawAugustBalloons() {
    val w = size.width
    val h = size.height

    // Matahari senja rendah
    drawCircle(Color(0xFFFAD8A8).copy(alpha = 0.55f), radius = h * 0.34f, center = Offset(w * 0.58f, h * 0.60f))
    drawCircle(Color(0xFFF6C27D), radius = h * 0.21f, center = Offset(w * 0.58f, h * 0.60f))

    // Bukit hangat terakota berlapis
    drawHillWave(w * 0.06f, w * 0.68f, h, w * 0.39f, h * 0.52f, Color(0xFFECC19B))
    drawHillWave(w * 0.34f, w * 0.98f, h, w * 0.72f, h * 0.48f, Color(0xFFDF9F73))
    drawHillWave(w * 0.16f, w * 0.90f, h, w * 0.54f, h * 0.68f, Color(0xFFCF885D))

    // 3 Balon udara berbentuk teardrop klasik dengan pita horizontal & gore
    drawHotAirBalloon(
        center = Offset(w * 0.46f, h * 0.31f),
        radius = h * 0.16f,
        bodyColor = Color(0xFFE07664),
        stripeColor = Color(0xFFFCE2CA),
        bandColor = Color(0xFFF4B96B)
    )
    drawHotAirBalloon(
        center = Offset(w * 0.63f, h * 0.25f),
        radius = h * 0.20f,
        bodyColor = Color(0xFF6393C2),
        stripeColor = Color(0xFFF7D488),
        bandColor = Color(0xFFE3826B)
    )
    drawHotAirBalloon(
        center = Offset(w * 0.76f, h * 0.36f),
        radius = h * 0.13f,
        bodyColor = Color(0xFFE8B058),
        stripeColor = Color(0xFFFFFFFF),
        bandColor = Color(0xFFD9785F)
    )
}

// ------------------------------------------------------------------
// 9. SEPTEMBER — Transisi Musim Gugur, Bukit Okre & Daun Melayang
// ------------------------------------------------------------------
private fun DrawScope.drawSeptemberEarlyAutumn() {
    val w = size.width
    val h = size.height

    // Matahari lembut
    drawCircle(Color(0xFFF5D89C), radius = h * 0.17f, center = Offset(w * 0.47f, h * 0.27f))

    // Bukit okre & hijau zaitun
    drawHillWave(w * 0.06f, w * 0.66f, h, w * 0.38f, h * 0.52f, Color(0xFFE2CCA5))
    drawHillWave(w * 0.34f, w * 0.98f, h, w * 0.70f, h * 0.46f, Color(0xFFC5C99E))
    drawHillWave(w * 0.18f, w * 0.90f, h, w * 0.54f, h * 0.66f, Color(0xFFCCA774))

    // Pohon musim gugur awal
    drawRoundTree(w * 0.41f, h * 0.96f, h * 0.46f, Color(0xFF8EA672), Color(0xFFA3BA87), Color(0xFF7D6552))
    drawRoundTree(w * 0.62f, h * 0.95f, h * 0.50f, Color(0xFFDFA35F), Color(0xFFEBB678), Color(0xFF7D6552))
    drawRoundTree(w * 0.75f, h * 0.97f, h * 0.40f, Color(0xFFB3BA80), Color(0xFFC5CC95), Color(0xFF7D6552))

    // Hembusan angin pendek dengan ujung melingkar halus + daun melayang
    val windColor = Color(0xFFC2A274).copy(alpha = 0.58f)
    val windStroke = (h * 0.028f).coerceIn(1.6f, 2.6f)
    drawWindSwirl(
        startX = w * 0.53f,
        y = h * 0.33f,
        length = h * 0.85f,
        curlRadius = h * 0.095f,
        curlUp = true,
        color = windColor,
        strokeWidth = windStroke
    )
    drawWindSwirl(
        startX = w * 0.56f,
        y = h * 0.43f,
        length = h * 0.62f,
        curlRadius = h * 0.075f,
        curlUp = false,
        color = windColor.copy(alpha = 0.45f),
        strokeWidth = windStroke * 0.9f
    )

    listOf(
        Triple(0.55f, 0.25f, -22f),
        Triple(0.66f, 0.36f, 18f),
        Triple(0.70f, 0.24f, -30f)
    ).forEach { (lx, ly, angle) ->
        drawFloatingLeaf(
            center = Offset(w * lx, h * ly),
            length = h * 0.09f,
            width = h * 0.048f,
            angleDegrees = angle,
            color = Color(0xFFD99252)
        )
    }
}

// ------------------------------------------------------------------
// 10. OKTOBER — Musim Panen & Pepohonan Musim Gugur Keemasan
// ------------------------------------------------------------------
private fun DrawScope.drawOctoberHarvest() {
    val w = size.width
    val h = size.height

    // Bulan panen / matahari senja
    drawCircle(Color(0xFFFBE2B4).copy(alpha = 0.52f), radius = h * 0.28f, center = Offset(w * 0.68f, h * 0.28f))
    drawCircle(Color(0xFFF5CA7C), radius = h * 0.17f, center = Offset(w * 0.68f, h * 0.28f))

    // Bukit hangat berlapis
    drawHillWave(w * 0.06f, w * 0.68f, h, w * 0.40f, h * 0.50f, Color(0xFFEAC298))
    drawHillWave(w * 0.32f, w * 0.98f, h, w * 0.72f, h * 0.46f, Color(0xFFDC9C6D))
    drawHillWave(w * 0.14f, w * 0.90f, h, w * 0.54f, h * 0.66f, Color(0xFFC98055))

    // Rumpun pohon warna gugur (mustard, coral, terakota)
    drawRoundTree(w * 0.39f, h * 0.97f, h * 0.46f, Color(0xFFE3B054), Color(0xFFF0C36E), Color(0xFF7A5342))
    drawRoundTree(w * 0.45f, h * 0.97f, h * 0.36f, Color(0xFFD97D5E), Color(0xFFE89476), Color(0xFF7A5342))
    drawRoundTree(w * 0.58f, h * 0.96f, h * 0.52f, Color(0xFFC76649), Color(0xFFD97C5F), Color(0xFF7A5342))
    drawRoundTree(w * 0.64f, h * 0.97f, h * 0.40f, Color(0xFFE3B054), Color(0xFFF0C36E), Color(0xFF7A5342))
    drawRoundTree(w * 0.77f, h * 0.97f, h * 0.44f, Color(0xFFD97D5E), Color(0xFFE89476), Color(0xFF7A5342))

    // Daun gugur beterbangan miring
    listOf(
        Triple(0.50f, 0.34f, 24f),
        Triple(0.53f, 0.46f, -20f),
        Triple(0.71f, 0.48f, 28f)
    ).forEach { (lx, ly, angle) ->
        drawFloatingLeaf(
            center = Offset(w * lx, h * ly),
            length = h * 0.09f,
            width = h * 0.048f,
            angleDegrees = angle,
            color = Color(0xFFD67350)
        )
    }
}

// ------------------------------------------------------------------
// 11. NOVEMBER — Kabut Hujan Sejuk & Pegunungan Biru-Slate
// ------------------------------------------------------------------
private fun DrawScope.drawNovemberRainyMist() {
    val w = size.width
    val h = size.height

    // Awan kabut lembut
    drawFlatCloud(Offset(w * 0.46f, h * 0.23f), scale = h * 0.014f, Color.White.copy(alpha = 0.82f))
    drawFlatCloud(Offset(w * 0.71f, h * 0.21f), scale = h * 0.013f, Color.White.copy(alpha = 0.78f))

    // Tetes hujan diagonal lembut
    val rainColor = Color(0xFF7E96BA).copy(alpha = 0.42f)
    listOf(0.42f, 0.47f, 0.52f, 0.66f, 0.71f, 0.76f).forEachIndexed { idx, rx ->
        val startY = h * (0.31f + (idx % 2) * 0.08f)
        drawLine(
            color = rainColor,
            start = Offset(w * rx, startY),
            end = Offset(w * rx - h * 0.08f, startY + h * 0.18f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }

    // Pegunungan kabut berlapis
    drawHillWave(w * 0.06f, w * 0.68f, h, w * 0.39f, h * 0.48f, Color(0xFFC1CFE3))
    drawHillWave(w * 0.32f, w * 0.98f, h, w * 0.70f, h * 0.44f, Color(0xFFA2B6D1))
    drawHillWave(w * 0.16f, w * 0.92f, h, w * 0.54f, h * 0.64f, Color(0xFF879EC0))

    // Pohon pinus gelap lembut
    drawPineTree(w * 0.39f, h * 0.97f, h * 0.46f, Color(0xFF5D7499))
    drawPineTree(w * 0.43f, h * 0.97f, h * 0.34f, Color(0xFF6E86AB))
    drawPineTree(w * 0.73f, h * 0.97f, h * 0.48f, Color(0xFF566D91))

    // Siluet rumah mungil dengan lampu jendela kuning hangat
    val hx = w * 0.57f
    val hw = h * 0.38f
    val hh = h * 0.25f
    val hTop = h * 0.71f
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
    drawCrescentMoon(Offset(w * 0.64f, h * 0.25f), radius = h * 0.15f, moonColor = Color(0xFFFBE39E), cutColor = Color(0xFFDFE7F8))

    listOf(0.41f to 0.24f, 0.50f to 0.18f, 0.56f to 0.33f, 0.73f to 0.21f, 0.82f to 0.29f).forEachIndexed { idx, (sx, sy) ->
        val starColor = if (idx % 2 == 0) Color(0xFFF7D57E) else Color.White
        drawSparkleStar(Offset(w * sx, h * sy), radius = h * 0.055f, color = starColor)
    }

    // Bukit malam akhir tahun
    drawHillWave(w * 0.06f, w * 0.68f, h, w * 0.40f, h * 0.52f, Color(0xFFBCCAE8))
    drawHillWave(w * 0.32f, w * 0.98f, h, w * 0.72f, h * 0.48f, Color(0xFF9FB2DC))
    drawHillWave(w * 0.16f, w * 0.92f, h, w * 0.54f, h * 0.68f, Color(0xFFE6EEFA))

    // Deretan pohon cemara hijau-pinus dengan titik lampu hangat
    val trees = listOf(
        Triple(0.42f, 0.46f, Color(0xFF5D8579)),
        Triple(0.48f, 0.56f, Color(0xFF4B7368)),
        Triple(0.54f, 0.40f, Color(0xFF6B9488)),
        Triple(0.70f, 0.50f, Color(0xFF4F786C)),
        Triple(0.76f, 0.38f, Color(0xFF648D81))
    )
    trees.forEach { (tx, thRatio, color) ->
        drawPineTree(w * tx, h * 0.97f, h * thRatio, color)
        // Hiasan titik emas hangat di puncak cemara
        drawCircle(
            color = Color(0xFFF7D57E),
            radius = h * 0.034f,
            center = Offset(w * tx, h * (0.97f - thRatio))
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
            peakX + (endX - peakX) * 0.55f, peakY,
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
    val halfW = treeHeight * 0.36f
    val trunkH = treeHeight * 0.16f
    val canopyBottom = bottomY - trunkH
    val topY = bottomY - treeHeight
    val midY = topY + (canopyBottom - topY) * 0.52f

    drawRect(
        color = Color(0xFF7B6B63),
        topLeft = Offset(centerX - halfW * 0.16f, canopyBottom),
        size = Size(halfW * 0.32f, trunkH)
    )
    // Tajuk bawah
    val lowerCanopy = Path().apply {
        moveTo(centerX, topY + (canopyBottom - topY) * 0.22f)
        lineTo(centerX + halfW, canopyBottom)
        lineTo(centerX - halfW, canopyBottom)
        close()
    }
    drawPath(lowerCanopy, color)

    // Tajuk atas (memberi siluet bertingkat khas cemara)
    val upperCanopy = Path().apply {
        moveTo(centerX, topY)
        lineTo(centerX + halfW * 0.78f, midY)
        lineTo(centerX - halfW * 0.78f, midY)
        close()
    }
    drawPath(upperCanopy, color)
}

private fun DrawScope.drawRoundTree(
    centerX: Float,
    bottomY: Float,
    treeHeight: Float,
    canopyColor: Color,
    highlightColor: Color,
    trunkColor: Color
) {
    val radius = treeHeight * 0.33f
    val trunkH = treeHeight * 0.44f
    val canopyCenterY = bottomY - trunkH - radius * 0.55f
    drawLine(
        color = trunkColor,
        start = Offset(centerX, bottomY),
        end = Offset(centerX, canopyCenterY + radius * 0.2f),
        strokeWidth = (treeHeight * 0.10f).coerceAtLeast(2.8f),
        cap = StrokeCap.Round
    )
    // Rumpun daun berlapis agar siluet pohon lebih organik
    drawCircle(
        color = canopyColor,
        radius = radius * 0.85f,
        center = Offset(centerX - radius * 0.42f, canopyCenterY + radius * 0.18f)
    )
    drawCircle(
        color = canopyColor,
        radius = radius * 0.82f,
        center = Offset(centerX + radius * 0.42f, canopyCenterY + radius * 0.20f)
    )
    drawCircle(
        color = highlightColor,
        radius = radius * 0.95f,
        center = Offset(centerX, canopyCenterY - radius * 0.15f)
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
    val trunkTop = bottomY - treeHeight * 0.50f
    drawLine(
        color = Color(0xFF8C726B),
        start = Offset(centerX, bottomY),
        end = Offset(centerX, trunkTop),
        strokeWidth = (treeHeight * 0.10f).coerceAtLeast(2.8f),
        cap = StrokeCap.Round
    )
    drawCircle(mainPink, r * 0.95f, Offset(centerX - r * 0.58f, trunkTop - r * 0.15f))
    drawCircle(mainPink, r * 0.95f, Offset(centerX + r * 0.58f, trunkTop - r * 0.15f))
    drawCircle(lightPink, r * 1.08f, Offset(centerX, trunkTop - r * 0.72f))
}

private fun DrawScope.drawFloatingLeaf(
    center: Offset,
    length: Float,
    width: Float,
    angleDegrees: Float,
    color: Color
) {
    rotate(degrees = angleDegrees, pivot = center) {
        val leaf = Path().apply {
            moveTo(center.x - length * 0.5f, center.y)
            quadraticTo(center.x, center.y - width, center.x + length * 0.5f, center.y)
            quadraticTo(center.x, center.y + width, center.x - length * 0.5f, center.y)
            close()
        }
        drawPath(leaf, color)
    }
}

private fun DrawScope.drawMeadowFlower(
    center: Offset,
    bottomY: Float,
    petalRadius: Float,
    petalColor: Color,
    centerColor: Color
) {
    drawLine(
        color = Color(0xFF568C58),
        start = center,
        end = Offset(center.x, bottomY),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )
    val offset = petalRadius * 0.58f
    drawCircle(petalColor, petalRadius * 0.62f, Offset(center.x - offset, center.y))
    drawCircle(petalColor, petalRadius * 0.62f, Offset(center.x + offset, center.y))
    drawCircle(petalColor, petalRadius * 0.62f, Offset(center.x, center.y - offset))
    drawCircle(petalColor, petalRadius * 0.62f, Offset(center.x, center.y + offset))
    drawCircle(centerColor, petalRadius * 0.42f, center)
}

private fun DrawScope.drawWindmill(
    baseCenter: Offset,
    towerHeight: Float,
    bodyColor: Color,
    roofColor: Color,
    bladeColor: Color
) {
    val baseHalfW = towerHeight * 0.28f
    val topHalfW = towerHeight * 0.17f
    val topY = baseCenter.y - towerHeight

    // Menara trapesium
    val tower = Path().apply {
        moveTo(baseCenter.x - baseHalfW, baseCenter.y)
        lineTo(baseCenter.x - topHalfW, topY)
        lineTo(baseCenter.x + topHalfW, topY)
        lineTo(baseCenter.x + baseHalfW, baseCenter.y)
        close()
    }
    drawPath(tower, bodyColor)

    // Kubah atap
    val roof = Path().apply {
        moveTo(baseCenter.x - topHalfW * 1.15f, topY)
        lineTo(baseCenter.x, topY - towerHeight * 0.24f)
        lineTo(baseCenter.x + topHalfW * 1.15f, topY)
        close()
    }
    drawPath(roof, roofColor)

    // Poros & 4 bilah baling-baling diagonal
    val hub = Offset(baseCenter.x, topY + towerHeight * 0.08f)
    val bladeLen = towerHeight * 0.56f
    listOf(35f, 125f, 215f, 305f).forEach { deg ->
        val rad = Math.toRadians(deg.toDouble())
        val end = Offset(
            x = hub.x + (cos(rad) * bladeLen).toFloat(),
            y = hub.y + (sin(rad) * bladeLen).toFloat()
        )
        drawLine(bladeColor, hub, end, strokeWidth = 2.4f, cap = StrokeCap.Round)
    }
    drawCircle(roofColor, radius = towerHeight * 0.06f, center = hub)
}

private fun DrawScope.drawLighthouse(
    baseCenter: Offset,
    towerHeight: Float,
    bodyColor: Color,
    stripeColor: Color,
    lightColor: Color
) {
    val baseHalfW = towerHeight * 0.22f
    val topHalfW = towerHeight * 0.14f
    val topY = baseCenter.y - towerHeight

    // Sorot cahaya lembut dari puncak mercusuar ke arah laut
    val beam = Path().apply {
        moveTo(baseCenter.x - topHalfW, topY - towerHeight * 0.10f)
        lineTo(baseCenter.x - towerHeight * 1.25f, topY - towerHeight * 0.34f)
        lineTo(baseCenter.x - towerHeight * 1.25f, topY + towerHeight * 0.12f)
        close()
    }
    drawPath(beam, lightColor.copy(alpha = 0.32f))

    // Badan menara putih
    val tower = Path().apply {
        moveTo(baseCenter.x - baseHalfW, baseCenter.y)
        lineTo(baseCenter.x - topHalfW, topY)
        lineTo(baseCenter.x + topHalfW, topY)
        lineTo(baseCenter.x + baseHalfW, baseCenter.y)
        close()
    }
    drawPath(tower, bodyColor)

    // Sabuk merah tengah mercusuar
    val midY1 = baseCenter.y - towerHeight * 0.68f
    val midY2 = baseCenter.y - towerHeight * 0.38f
    val w1 = baseHalfW * 0.78f
    val w2 = baseHalfW * 0.88f
    val stripe = Path().apply {
        moveTo(baseCenter.x - w1, midY1)
        lineTo(baseCenter.x + w1, midY1)
        lineTo(baseCenter.x + w2, midY2)
        lineTo(baseCenter.x - w2, midY2)
        close()
    }
    drawPath(stripe, stripeColor)

    // Ruang lampu & kubah merah
    val lanternH = towerHeight * 0.16f
    drawRect(
        color = lightColor,
        topLeft = Offset(baseCenter.x - topHalfW * 0.9f, topY - lanternH),
        size = Size(topHalfW * 1.8f, lanternH)
    )
    val dome = Path().apply {
        moveTo(baseCenter.x - topHalfW * 1.2f, topY - lanternH)
        lineTo(baseCenter.x, topY - lanternH - towerHeight * 0.16f)
        lineTo(baseCenter.x + topHalfW * 1.2f, topY - lanternH)
        close()
    }
    drawPath(dome, stripeColor)
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
    primaryColor: Color,
    secondaryColor: Color,
    tailColor: Color
) {
    val tail = Path().apply {
        moveTo(center.x, center.y + sizePx * 0.65f)
        cubicTo(
            center.x - sizePx * 0.55f, center.y + sizePx * 1.20f,
            center.x + sizePx * 0.40f, center.y + sizePx * 1.60f,
            center.x - sizePx * 0.30f, center.y + sizePx * 2.05f
        )
    }
    drawPath(tail, color = tailColor.copy(alpha = 0.72f), style = Stroke(width = 2.0f, cap = StrokeCap.Round))

    // Pita kecil pada ekor layang-layang
    drawCircle(primaryColor, radius = sizePx * 0.09f, center = Offset(center.x - sizePx * 0.14f, center.y + sizePx * 1.12f))
    drawCircle(secondaryColor, radius = sizePx * 0.08f, center = Offset(center.x - sizePx * 0.05f, center.y + sizePx * 1.56f))

    // Sisi kiri berlian
    val leftHalf = Path().apply {
        moveTo(center.x, center.y - sizePx * 0.65f)
        lineTo(center.x, center.y + sizePx * 0.65f)
        lineTo(center.x - sizePx * 0.48f, center.y)
        close()
    }
    drawPath(leftHalf, primaryColor)

    // Sisi kanan berlian (dua nada warna agar berdimensi)
    val rightHalf = Path().apply {
        moveTo(center.x, center.y - sizePx * 0.65f)
        lineTo(center.x + sizePx * 0.48f, center.y)
        lineTo(center.x, center.y + sizePx * 0.65f)
        close()
    }
    drawPath(rightHalf, secondaryColor)
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
    stripeColor: Color,
    bandColor: Color
) {
    val neckY = center.y + radius * 1.05f
    val neckHalfW = radius * 0.30f
    val basketTop = center.y + radius * 1.24f

    // Tali & keranjang
    drawLine(Color(0xFF8C6954), Offset(center.x - neckHalfW * 0.75f, neckY), Offset(center.x - radius * 0.16f, basketTop), 1.5f)
    drawLine(Color(0xFF8C6954), Offset(center.x + neckHalfW * 0.75f, neckY), Offset(center.x + radius * 0.16f, basketTop), 1.5f)
    drawRoundRect(
        color = Color(0xFF8C6954),
        topLeft = Offset(center.x - radius * 0.22f, basketTop),
        size = Size(radius * 0.44f, radius * 0.28f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Kubah balon + leher meruncing (teardrop)
    drawCircle(bodyColor, radius, center)
    val skirt = Path().apply {
        moveTo(center.x - radius * 0.84f, center.y + radius * 0.52f)
        lineTo(center.x - neckHalfW, neckY)
        lineTo(center.x + neckHalfW, neckY)
        lineTo(center.x + radius * 0.84f, center.y + radius * 0.52f)
        close()
    }
    drawPath(skirt, bodyColor)

    // Panel tengah vertikal (gore) yang meruncing ke leher balon
    val gore = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(
            center.x + radius * 0.48f, center.y - radius * 0.5f,
            center.x + radius * 0.38f, center.y + radius * 0.5f,
            center.x + neckHalfW * 0.45f, neckY
        )
        lineTo(center.x - neckHalfW * 0.45f, neckY)
        cubicTo(
            center.x - radius * 0.38f, center.y + radius * 0.5f,
            center.x - radius * 0.48f, center.y - radius * 0.5f,
            center.x, center.y - radius
        )
        close()
    }
    drawPath(gore, stripeColor)

    // Pita aksen horizontal di bagian tengah balon
    drawRoundRect(
        color = bandColor,
        topLeft = Offset(center.x - radius * 0.96f, center.y - radius * 0.12f),
        size = Size(radius * 1.92f, radius * 0.24f),
        cornerRadius = CornerRadius(radius * 0.12f, radius * 0.12f)
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

private fun DrawScope.drawWindSwirl(
    startX: Float,
    y: Float,
    length: Float,
    curlRadius: Float,
    curlUp: Boolean,
    color: Color,
    strokeWidth: Float
) {
    val endX = startX + length
    drawLine(
        color = color,
        start = Offset(startX, y),
        end = Offset(endX, y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    val topLeft = if (curlUp) {
        Offset(endX - curlRadius, y - curlRadius * 2f)
    } else {
        Offset(endX - curlRadius, y)
    }
    drawArc(
        color = color,
        startAngle = if (curlUp) 90f else 270f,
        sweepAngle = if (curlUp) -245f else 235f,
        useCenter = false,
        topLeft = topLeft,
        size = Size(curlRadius * 2f, curlRadius * 2f),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
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
