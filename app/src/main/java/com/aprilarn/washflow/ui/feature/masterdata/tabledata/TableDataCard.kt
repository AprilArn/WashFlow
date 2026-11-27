package com.aprilarn.washflow.ui.feature.masterdata.tabledata

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg

enum class TableDataKind {
    CUSTOMERS,
    SERVICES,
    ITEMS
}

data class TableDataTheme(
    val kind: TableDataKind,
    val titleColor: Color,
    val subtitleColor: Color,
    val accentColor: Color,
    val countColor: Color,
    val softBgColor: Color,
    val badgeBgColor: Color,
    @param:StringRes val defaultSubtitleRes: Int
)

fun resolveTableDataTheme(text: String): TableDataTheme {
    val normalized = text.trim().lowercase()
    return when {
        normalized.contains("service") || normalized.contains("layanan") -> TableDataTheme(
            kind = TableDataKind.SERVICES,
            titleColor = GrayBlue,
            subtitleColor = Color(0xFF6B8575),
            accentColor = PastelGreen,
            countColor = PastelGreenDark,
            softBgColor = PastelGreenSoftBg,
            badgeBgColor = PastelGreenBg,
            defaultSubtitleRes = R.string.table_subtitle_services
        )
        normalized.contains("item") || normalized.contains("barang") -> TableDataTheme(
            kind = TableDataKind.ITEMS,
            titleColor = GrayBlue,
            subtitleColor = Color(0xFF947A59),
            accentColor = PastelAmber,
            countColor = PastelAmber,
            softBgColor = Color(0xFFFCF7F0),
            badgeBgColor = PastelAmberBg,
            defaultSubtitleRes = R.string.table_subtitle_items
        )
        else -> TableDataTheme(
            kind = TableDataKind.CUSTOMERS,
            titleColor = GrayBlue,
            subtitleColor = Color(0xFF6B7B9C),
            accentColor = PastelBlue,
            countColor = PastelBlue,
            softBgColor = PastelBlueSoftBg,
            badgeBgColor = PastelBlueBg,
            defaultSubtitleRes = R.string.table_subtitle_customers
        )
    }
}

@Composable
fun TableDataCard(
    text: String,
    subText: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val theme = remember(text) { resolveTableDataTheme(text) }

    Surface(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .height(104.dp),
        shape = borderRadius,
        color = Color.White,
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(borderRadius)
        ) {
            // Ilustrasi bergaya Google Calendar di atas latar putih solid
            TableDataBannerIllustration(
                theme = theme,
                modifier = Modifier.matchParentSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .background(theme.accentColor, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = theme.titleColor
                            )
                        )
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = theme.subtitleColor
                            )
                        )
                    }
                }

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = theme.countColor
                    )
                )
            }
        }
    }
}

@Composable
fun TableDataBannerIllustration(
    theme: TableDataTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clipToBounds()) {
        // Latar kartu putih solid
        drawRect(color = Color.White)

        when (theme.kind) {
            TableDataKind.CUSTOMERS -> drawCustomersNeighborhoodBanner()
            TableDataKind.SERVICES -> drawServicesWorkspaceBanner()
            TableDataKind.ITEMS -> drawItemsInventoryBanner()
        }

        // Scrim putih lembut di sisi kiri dan kanan agar teks judul serta angka tetap bersih & kontras
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to Color.White,
                    0.28f to Color.White.copy(alpha = 0.86f),
                    0.42f to Color.Transparent,
                    0.82f to Color.Transparent,
                    0.94f to Color.White.copy(alpha = 0.88f),
                    1.0f to Color.White
                )
            )
        )
    }
}

// ------------------------------------------------------------------
// 1. CUSTOMERS — Ilustrasi Komunitas & Kartu Profil di Atas Putih Solid
// ------------------------------------------------------------------
private fun DrawScope.drawCustomersNeighborhoodBanner() {
    val w = size.width
    val h = size.height

    // Halo pastel lembut di belakang objek utama & awan pastel tipis
    drawCircle(
        color = Color(0xFFEBF2FC),
        radius = h * 0.42f,
        center = Offset(w * 0.61f, h * 0.50f)
    )
    drawBannerCloud(Offset(w * 0.44f, h * 0.24f), scale = h * 0.012f, Color(0xFFEAF1FB))
    drawBannerCloud(Offset(w * 0.78f, h * 0.22f), scale = h * 0.011f, Color(0xFFEAF1FB))

    // Bukit biru pastel lembut di bawah ilustrasi
    drawBannerHill(w * 0.24f, w * 0.78f, h, w * 0.50f, h * 0.62f, Color(0xFFE4ECF8))
    drawBannerHill(w * 0.42f, w * 0.94f, h, w * 0.70f, h * 0.56f, Color(0xFFD5E2F5))
    drawBannerHill(w * 0.30f, w * 0.88f, h, w * 0.59f, h * 0.72f, Color(0xFFC3D5EF))

    // Pepohonan bulat komunitas di bukit
    drawBannerTree(w * 0.44f, h * 0.96f, h * 0.40f, Color(0xFF7B9CCB), Color(0xFF93B1DC), Color(0xFF7A6D63))
    drawBannerTree(w * 0.77f, h * 0.96f, h * 0.42f, Color(0xFF6B8EC2), Color(0xFF83A4D4), Color(0xFF7A6D63))

    // Kartu Buku Kontak / Direktori Pelanggan di tengah-kanan
    val cardW = h * 0.72f
    val cardH = h * 0.50f
    val cardLeft = w * 0.54f
    val cardTop = h * 0.27f

    drawRoundRect(
        color = Color(0xFF9FB7DB).copy(alpha = 0.35f),
        topLeft = Offset(cardLeft + 3f, cardTop + 4f),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color(0xFFFDFEFF),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color(0xFF6C92C7),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardW, cardH * 0.26f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    val avatarCenter = Offset(cardLeft + cardW * 0.26f, cardTop + cardH * 0.60f)
    val avatarR = cardH * 0.22f
    drawCircle(Color(0xFFE5EEF9), avatarR, avatarCenter)
    drawCircle(Color(0xFF5378AD), avatarR * 0.38f, Offset(avatarCenter.x, avatarCenter.y - avatarR * 0.20f))
    drawArc(
        color = Color(0xFF5378AD),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(avatarCenter.x - avatarR * 0.65f, avatarCenter.y + avatarR * 0.15f),
        size = Size(avatarR * 1.30f, avatarR * 0.90f)
    )

    drawLine(
        color = Color(0xFF5A7CAE),
        start = Offset(cardLeft + cardW * 0.52f, cardTop + cardH * 0.50f),
        end = Offset(cardLeft + cardW * 0.86f, cardTop + cardH * 0.50f),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFC2D3EC),
        start = Offset(cardLeft + cardW * 0.52f, cardTop + cardH * 0.70f),
        end = Offset(cardLeft + cardW * 0.78f, cardTop + cardH * 0.70f),
        strokeWidth = 3.0f,
        cap = StrokeCap.Round
    )

    val badgeCenter = Offset(cardLeft + cardW * 0.96f, cardTop + cardH * 0.78f)
    val badgeR = h * 0.20f
    drawCircle(Color(0xFF9FB7DB).copy(alpha = 0.35f), badgeR, Offset(badgeCenter.x + 2f, badgeCenter.y + 3f))
    drawCircle(Color.White, badgeR, badgeCenter)
    drawCircle(Color(0xFFEB9478), badgeR * 0.84f, badgeCenter)
    drawCircle(Color.White, badgeR * 0.28f, Offset(badgeCenter.x, badgeCenter.y - badgeR * 0.16f))
    drawArc(
        color = Color.White,
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(badgeCenter.x - badgeR * 0.50f, badgeCenter.y + badgeR * 0.14f),
        size = Size(badgeR * 1.0f, badgeR * 0.72f)
    )
}

// ------------------------------------------------------------------
// 2. SERVICES — Ilustrasi Katalog Layanan & Roda Gigi di Atas Putih Solid
// ------------------------------------------------------------------
private fun DrawScope.drawServicesWorkspaceBanner() {
    val w = size.width
    val h = size.height

    // Halo pastel hijau lembut & matahari hangat tipis
    drawCircle(
        color = Color(0xFFEAF5EF),
        radius = h * 0.42f,
        center = Offset(w * 0.61f, h * 0.50f)
    )
    drawCircle(
        color = Color(0xFFF9E5A7),
        radius = h * 0.14f,
        center = Offset(w * 0.46f, h * 0.26f)
    )
    drawBannerCloud(Offset(w * 0.76f, h * 0.22f), scale = h * 0.011f, Color(0xFFE4F2EA))

    // Bukit hijau-mint lembut
    drawBannerHill(w * 0.24f, w * 0.78f, h, w * 0.48f, h * 0.62f, Color(0xFFDFEFE6))
    drawBannerHill(w * 0.40f, w * 0.94f, h, w * 0.70f, h * 0.56f, Color(0xFFCDE7D9))
    drawBannerHill(w * 0.28f, w * 0.88f, h, w * 0.59f, h * 0.72f, Color(0xFFB8DCC9))

    drawBannerTree(w * 0.43f, h * 0.96f, h * 0.38f, Color(0xFF63A383), Color(0xFF7AB899), Color(0xFF7A685C))
    drawBannerTree(w * 0.78f, h * 0.96f, h * 0.42f, Color(0xFF559676), Color(0xFF6CAD8D), Color(0xFF7A685C))

    drawBannerGear(
        center = Offset(w * 0.53f, h * 0.42f),
        outerRadius = h * 0.21f,
        toothCount = 8,
        gearColor = Color(0xFF5A9E7E),
        innerRingColor = Color(0xFFD6EDE2),
        holeColor = Color(0xFFEAF5EF),
        rotationDeg = 15f
    )

    val cardW = h * 0.54f
    val cardH = h * 0.56f
    val cardLeft = w * 0.56f
    val cardTop = h * 0.24f

    drawRoundRect(
        color = Color(0xFF8ABFA3).copy(alpha = 0.34f),
        topLeft = Offset(cardLeft + 3f, cardTop + 4f),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        color = Color(0xFFFDFEFF),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        color = Color(0xFF529474),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardW, cardH * 0.22f),
        cornerRadius = CornerRadius(9f, 9f)
    )

    listOf(0.42f, 0.62f, 0.80f).forEachIndexed { idx, ry ->
        val rowY = cardTop + cardH * ry
        drawCircle(
            color = if (idx == 0) Color(0xFF529474) else Color(0xFF9AC8B1),
            radius = cardW * 0.06f,
            center = Offset(cardLeft + cardW * 0.20f, rowY)
        )
        drawLine(
            color = if (idx == 0) Color(0xFF529474) else Color(0xFFC3DFD1),
            start = Offset(cardLeft + cardW * 0.36f, rowY),
            end = Offset(cardLeft + cardW * (if (idx == 1) 0.74f else 0.84f), rowY),
            strokeWidth = 3.0f,
            cap = StrokeCap.Round
        )
    }

    val badgeCenter = Offset(cardLeft + cardW * 0.96f, cardTop + cardH * 0.80f)
    val badgeR = h * 0.19f
    drawCircle(Color(0xFF8ABFA3).copy(alpha = 0.35f), badgeR, Offset(badgeCenter.x + 2f, badgeCenter.y + 3f))
    drawCircle(Color.White, badgeR, badgeCenter)
    drawCircle(Color(0xFFF2B84B), badgeR * 0.84f, badgeCenter)
    drawBannerSparkle(badgeCenter, radius = badgeR * 0.52f, color = Color.White)

    drawBannerSparkle(Offset(w * 0.71f, h * 0.28f), radius = h * 0.060f, color = Color(0xFFF2B84B))
}

// ------------------------------------------------------------------
// 3. ITEMS — Ilustrasi Inventori & Label Item di Atas Putih Solid
// ------------------------------------------------------------------
private fun DrawScope.drawItemsInventoryBanner() {
    val w = size.width
    val h = size.height

    // Halo pastel amber lembut & awan tipis
    drawCircle(
        color = Color(0xFFFCF3E4),
        radius = h * 0.42f,
        center = Offset(w * 0.60f, h * 0.50f)
    )
    drawBannerCloud(Offset(w * 0.44f, h * 0.24f), scale = h * 0.011f, Color(0xFFF7EBD6))
    drawBannerCloud(Offset(w * 0.77f, h * 0.22f), scale = h * 0.011f, Color(0xFFF7EBD6))

    // Bukit amber lembut
    drawBannerHill(w * 0.24f, w * 0.78f, h, w * 0.48f, h * 0.62f, Color(0xFFF6E9D5))
    drawBannerHill(w * 0.40f, w * 0.94f, h, w * 0.70f, h * 0.56f, Color(0xFFEDD9BB))
    drawBannerHill(w * 0.28f, w * 0.88f, h, w * 0.59f, h * 0.72f, Color(0xFFE3C8A0))

    drawBannerTree(w * 0.43f, h * 0.96f, h * 0.38f, Color(0xFFCF9154), Color(0xFFE3A86D), Color(0xFF7D614D))
    drawBannerTree(w * 0.78f, h * 0.96f, h * 0.40f, Color(0xFFC28246), Color(0xFFD9985B), Color(0xFF7D614D))

    val box1W = h * 0.48f
    val box1H = h * 0.44f
    val box1Left = w * 0.52f
    val box1Top = h * 0.34f

    drawRoundRect(
        color = Color(0xFFD1AE7E).copy(alpha = 0.35f),
        topLeft = Offset(box1Left + 3f, box1Top + 4f),
        size = Size(box1W, box1H),
        cornerRadius = CornerRadius(7f, 7f)
    )
    drawRoundRect(
        color = Color(0xFFFFFCF7),
        topLeft = Offset(box1Left, box1Top),
        size = Size(box1W, box1H),
        cornerRadius = CornerRadius(7f, 7f)
    )
    drawRoundRect(
        color = Color(0xFFF3E2C6),
        topLeft = Offset(box1Left - 2f, box1Top),
        size = Size(box1W + 4f, box1H * 0.25f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRect(
        color = Color(0xFFD9924C),
        topLeft = Offset(box1Left + box1W * 0.40f, box1Top),
        size = Size(box1W * 0.20f, box1H * 0.55f)
    )

    val box2W = h * 0.38f
    val box2H = h * 0.34f
    val box2Left = box1Left + box1W * 0.72f
    val box2Top = h * 0.46f

    drawRoundRect(
        color = Color(0xFFD1AE7E).copy(alpha = 0.35f),
        topLeft = Offset(box2Left + 2.5f, box2Top + 3.5f),
        size = Size(box2W, box2H),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color(0xFFE68A6C),
        topLeft = Offset(box2Left, box2Top),
        size = Size(box2W, box2H),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color(0xFFF2A68D),
        topLeft = Offset(box2Left - 1.5f, box2Top),
        size = Size(box2W + 3f, box2H * 0.26f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRect(
        color = Color(0xFFFFF5E8),
        topLeft = Offset(box2Left + box2W * 0.39f, box2Top),
        size = Size(box2W * 0.22f, box2H * 0.52f)
    )

    val tagCenter = Offset(w * 0.67f, h * 0.33f)
    val tagW = h * 0.38f
    val tagH = h * 0.22f
    rotate(degrees = -18f, pivot = tagCenter) {
        val tagLeft = tagCenter.x - tagW * 0.5f
        val tagTop = tagCenter.y - tagH * 0.5f
        drawRoundRect(
            color = Color(0xFF5D87B8),
            topLeft = Offset(tagLeft, tagTop),
            size = Size(tagW, tagH),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawCircle(
            color = Color(0xFFFFFCF7),
            radius = tagH * 0.18f,
            center = Offset(tagLeft + tagW * 0.22f, tagCenter.y)
        )
        drawLine(
            color = Color.White,
            start = Offset(tagLeft + tagW * 0.42f, tagCenter.y),
            end = Offset(tagLeft + tagW * 0.82f, tagCenter.y),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )
    }

    drawBannerSparkle(Offset(w * 0.50f, h * 0.26f), radius = h * 0.058f, color = Color(0xFFD9924C))
}

// ------------------------------------------------------------------
// Shared Editorial Vector Helpers
// ------------------------------------------------------------------

private fun DrawScope.drawBannerHill(
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

private fun DrawScope.drawBannerCloud(
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

private fun DrawScope.drawBannerTree(
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
        strokeWidth = (treeHeight * 0.10f).coerceAtLeast(2.6f),
        cap = StrokeCap.Round
    )
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

private fun DrawScope.drawBannerGear(
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

private fun DrawScope.drawBannerSparkle(
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

@Preview(showBackground = true, widthDp = 660, heightDp = 380)
@Composable
fun PreviewTableDataCard() {
    Column(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TableDataCard(
            text = "Customers",
            subText = "Pelanggan",
            count = 16,
            onClick = {}
        )
        TableDataCard(
            text = "Services",
            subText = "Layanan",
            count = 4,
            onClick = {}
        )
        TableDataCard(
            text = "Items",
            subText = "Barang",
            count = 15,
            onClick = {}
        )
    }
}