package com.aprilarn.washflow.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kategori lanskap cuaca bergaya Google Calendar (Material Editorial)
 * untuk latar belakang utama aplikasi WashFlow.
 */
enum class WeatherSceneKind {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    RAINY,
    THUNDERSTORM,
    WINDY,
    FOGGY,
    SNOWY,
    CLEAR_NIGHT
}

internal data class WeatherScenePalette(
    val skyTop: Color,
    val skyMid: Color,
    val skyBottom: Color,
    val horizonGlow: Color
)

internal fun weatherScenePalette(kind: WeatherSceneKind): WeatherScenePalette = when (kind) {
    WeatherSceneKind.SUNNY -> WeatherScenePalette(
        skyTop = Color(0xFF88ACC8),
        skyMid = Color(0xFFADC7DC),
        skyBottom = Color(0xFFE2D8CE),
        horizonGlow = Color(0xFFEDE5DC)
    )
    WeatherSceneKind.PARTLY_CLOUDY -> WeatherScenePalette(
        skyTop = Color(0xFF88A9C5),
        skyMid = Color(0xFFABC3D8),
        skyBottom = Color(0xFFD8DDD9),
        horizonGlow = Color(0xFFE6E7E2)
    )
    WeatherSceneKind.CLOUDY -> WeatherScenePalette(
        skyTop = Color(0xFF8AA8CB),
        skyMid = Color(0xFFACC3DE),
        skyBottom = Color(0xFFD0DEEC),
        horizonGlow = Color(0xFFE0E9F3)
    )
    WeatherSceneKind.RAINY -> WeatherScenePalette(
        skyTop = Color(0xFF7D9ABE),
        skyMid = Color(0xFF9DB6D4),
        skyBottom = Color(0xFFC3D3E7),
        horizonGlow = Color(0xFFD3E0F0)
    )
    WeatherSceneKind.THUNDERSTORM -> WeatherScenePalette(
        skyTop = Color(0xFF7488B3),
        skyMid = Color(0xFF93A4CA),
        skyBottom = Color(0xFFBEC8E2),
        horizonGlow = Color(0xFFD0D8EC)
    )
    WeatherSceneKind.WINDY -> WeatherScenePalette(
        skyTop = Color(0xFF86A8C0),
        skyMid = Color(0xFFA7C2D4),
        skyBottom = Color(0xFFCEDCE0),
        horizonGlow = Color(0xFFDDE7EA)
    )
    WeatherSceneKind.FOGGY -> WeatherScenePalette(
        skyTop = Color(0xFF92A9C5),
        skyMid = Color(0xFFB1C4DB),
        skyBottom = Color(0xFFD4DFEC),
        horizonGlow = Color(0xFFE4ECF4)
    )
    WeatherSceneKind.SNOWY -> WeatherScenePalette(
        skyTop = Color(0xFF88ACC9),
        skyMid = Color(0xFFACC6DD),
        skyBottom = Color(0xFFD4E2F0),
        horizonGlow = Color(0xFFE5EEF7)
    )
    WeatherSceneKind.CLEAR_NIGHT -> WeatherScenePalette(
        skyTop = Color(0xFF7A8FC0),
        skyMid = Color(0xFF9CADDA),
        skyBottom = Color(0xFFD0C6E4),
        horizonGlow = Color(0xFFE2DAF0)
    )
}

/**
 * Memetakan deskripsi cuaca (Bahasa Inggris maupun Indonesia dari API/Cache)
 * serta sapaan waktu (`greeting`) ke jenis ilustrasi latar belakang yang sesuai.
 */
fun resolveWeatherSceneKind(
    weatherText: String,
    greetingText: String = ""
): WeatherSceneKind {
    val w = weatherText.trim().lowercase()
    val g = greetingText.trim().lowercase()
    val isNightTime = g.contains("night") || g.contains("malam") || w.contains("night") || w.contains("malam")

    return when {
        // 1. Badai petir / Thunderstorm
        w.contains("thunder") || w.contains("storm") || w.contains("petir") ||
            w.contains("badai") || w.contains("kilat") || w.contains("squall") -> {
            WeatherSceneKind.THUNDERSTORM
        }

        // 2. Hujan / Gerimis / Showers
        w.contains("rain") || w.contains("shower") || w.contains("drizzle") ||
            w.contains("hujan") || w.contains("gerimis") || w.contains("downpour") ||
            w.contains("precipitation") || w.contains("basah") -> {
            WeatherSceneKind.RAINY
        }

        // 3. Salju / Es
        w.contains("snow") || w.contains("sleet") || w.contains("flurr") ||
            w.contains("hail") || w.contains("ice") || w.contains("salju") ||
            w.contains("blizzard") || w.contains("frost") || w.contains("freezing") -> {
            WeatherSceneKind.SNOWY
        }

        // 4. Kabut / Asap / Haze
        w.contains("fog") || w.contains("mist") || w.contains("haze") ||
            w.contains("smoke") || w.contains("kabut") || w.contains("asap") ||
            w.contains("dust") || w.contains("sand") -> {
            WeatherSceneKind.FOGGY
        }

        // 5. Berangin
        w.contains("wind") || w.contains("breez") || w.contains("gale") ||
            w.contains("angin") || w.contains("gust") -> {
            WeatherSceneKind.WINDY
        }

        // 6. Sebagian berawan / Cerah berawan
        w.contains("partly") || w.contains("mostly sunny") || w.contains("mostly clear") ||
            w.contains("cerah berawan") || w.contains("scattered cloud") ||
            w.contains("few cloud") || w.contains("broken cloud") -> {
            if (isNightTime) WeatherSceneKind.CLEAR_NIGHT else WeatherSceneKind.PARTLY_CLOUDY
        }

        // 7. Mendung / Berawan penuh
        w.contains("cloud") || w.contains("overcast") || w.contains("berawan") ||
            w.contains("mendung") || w.contains("gloomy") -> {
            WeatherSceneKind.CLOUDY
        }

        // 8. Cerah / Sunny / Clear
        w.contains("clear") || w.contains("sunny") || w.contains("cerah") ||
            w.contains("fair") || w.contains("hot") || w.contains("panas") -> {
            if (isNightTime) WeatherSceneKind.CLEAR_NIGHT else WeatherSceneKind.SUNNY
        }

        // Default fallback saat masih loading atau tidak diketahui
        else -> {
            if (isNightTime) WeatherSceneKind.CLEAR_NIGHT else WeatherSceneKind.SUNNY
        }
    }
}

/**
 * Ilustrasi latar belakang layar penuh bergaya Google Calendar (Material Editorial)
 * yang menyesuaikan diri secara dinamis dengan kondisi cuaca saat ini.
 *
 * Komposisi dirancang minimalis dengan langit pastel bersih di area tengah-kanan
 * (agar teks sapaan, cuaca, dan panel forecast selalu tajam & mudah dibaca)
 * serta siluet lanskap datar berlapis yang tenang di sepanjang cakrawala bawah.
 */
@Composable
fun WeatherBackgroundIllustration(
    weather: String,
    modifier: Modifier = Modifier,
    greeting: String = ""
) {
    val sceneKind = remember(weather, greeting) {
        resolveWeatherSceneKind(weather, greeting)
    }

    Crossfade(
        targetState = sceneKind,
        animationSpec = tween(durationMillis = 700),
        label = "weatherBackgroundCrossfade",
        modifier = modifier
    ) { targetKind ->
        WeatherBackgroundSceneCanvas(
            sceneKind = targetKind,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun WeatherBackgroundSceneCanvas(
    sceneKind: WeatherSceneKind,
    modifier: Modifier = Modifier
) {
    val palette = remember(sceneKind) { weatherScenePalette(sceneKind) }

    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height

        // Latar langit gradien lembut diagonal/vertikal
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.skyTop, palette.skyMid, palette.skyBottom),
                start = Offset(0f, 0f),
                end = Offset(w * 0.85f, h)
            )
        )

        // Pendar cakrawala lembut di sisi kanan-bawah
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.horizonGlow.copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = Offset(w * 0.78f, h * 0.80f),
                radius = h * 0.70f
            ),
            radius = h * 0.70f,
            center = Offset(w * 0.78f, h * 0.80f)
        )

        when (sceneKind) {
            WeatherSceneKind.SUNNY -> drawSunnyMeadowScene()
            WeatherSceneKind.PARTLY_CLOUDY -> drawPartlyCloudyBreezeScene()
            WeatherSceneKind.CLOUDY -> drawCloudyOvercastHillsScene()
            WeatherSceneKind.RAINY -> drawRainyMistScene()
            WeatherSceneKind.THUNDERSTORM -> drawThunderstormScene()
            WeatherSceneKind.WINDY -> drawWindyKiteScene()
            WeatherSceneKind.FOGGY -> drawFoggyMountainValleyScene()
            WeatherSceneKind.SNOWY -> drawSnowyWinterPeakScene()
            WeatherSceneKind.CLEAR_NIGHT -> drawClearStarryTwilightScene()
        }
    }
}

// ------------------------------------------------------------------
// 1. SUNNY — Langit Cerah Kalem, Bukit Sage Lembut & Kincir Angin
// ------------------------------------------------------------------
private fun DrawScope.drawSunnyMeadowScene() {
    val w = size.width
    val h = size.height

    // Matahari krem-champagne lembut dengan halo kalem di kanan-atas
    val sunCenter = Offset(w * 0.86f, h * 0.20f)
    drawCircle(Color(0xFFFFF8E7).copy(alpha = 0.18f), radius = h * 0.17f, center = sunCenter)
    drawCircle(Color(0xFFF5E8C4).copy(alpha = 0.30f), radius = h * 0.115f, center = sunCenter)
    drawCircle(Color(0xFFEEDBA3), radius = h * 0.068f, center = sunCenter)

    // Awan datar besar bergaya Google Calendar (path menyatu utuh)
    drawFlatCloud(Offset(w * 0.42f, h * 0.16f), radiusPx = h * 0.085f, Color(0xFFF4F8FC).copy(alpha = 0.68f))
    drawFlatCloud(Offset(w * 0.68f, h * 0.22f), radiusPx = h * 0.065f, Color(0xFFF4F8FC).copy(alpha = 0.58f))

    // Burung terbang jauh
    drawBirdV(Offset(w * 0.52f, h * 0.18f), h * 0.018f, Color(0xFF5D7E9B).copy(alpha = 0.58f))
    drawBirdV(Offset(w * 0.55f, h * 0.155f), h * 0.013f, Color(0xFF5D7E9B).copy(alpha = 0.46f))

    // Bukit jauh & cakrawala dusty sage-slate kalem di bagian bawah layar
    drawHillWave(-w * 0.08f, w * 0.64f, h, w * 0.32f, h * 0.80f, Color(0xFFC7D5CB))
    drawHillWave(w * 0.30f, w * 1.08f, h, w * 0.76f, h * 0.78f, Color(0xFFB4C6BA))
    drawHillWave(w * 0.04f, w * 0.98f, h, w * 0.46f, h * 0.86f, Color(0xFF9FB4A7))

    // Kincir angin mungil dengan warna kalem di koridor terbuka tengah-kiri
    drawWindmill(
        baseCenter = Offset(w * 0.39f, h * 0.88f),
        towerHeight = h * 0.125f,
        bodyColor = Color(0xFFF3F1ED),
        roofColor = Color(0xFFC48B7E),
        bladeColor = Color(0xFF6B8377)
    )

    // Pepohonan bulat minimalis bernuansa muted sage
    drawRoundTree(w * 0.35f, h * 0.96f, h * 0.105f, Color(0xFF789485), Color(0xFF8BA698), Color(0xFF7A7067))
    drawRoundTree(w * 0.92f, h * 0.96f, h * 0.120f, Color(0xFF6E8A7B), Color(0xFF829E8F), Color(0xFF7A7067))
    drawRoundTree(w * 0.96f, h * 0.97f, h * 0.088f, Color(0xFF819D8E), Color(0xFF94AF9F), Color(0xFF7A7067))
}

// ------------------------------------------------------------------
// 2. PARTLY CLOUDY — Matahari Lembut Berpadu Awan Berlapis & Bukit Kalem
// ------------------------------------------------------------------
private fun DrawScope.drawPartlyCloudyBreezeScene() {
    val w = size.width
    val h = size.height

    // Matahari krem lembut sebagian tertutup awan di kanan-atas
    val sunCenter = Offset(w * 0.84f, h * 0.20f)
    drawCircle(Color(0xFFFAF0D2).copy(alpha = 0.24f), radius = h * 0.145f, center = sunCenter)
    drawCircle(Color(0xFFEEDBA6), radius = h * 0.064f, center = sunCenter)

    // Awan datar besar berlapis dengan kontras lembut
    drawFlatCloud(Offset(w * 0.80f, h * 0.24f), radiusPx = h * 0.105f, Color(0xFFF3F7FC).copy(alpha = 0.78f))
    drawFlatCloud(Offset(w * 0.44f, h * 0.18f), radiusPx = h * 0.095f, Color(0xFFF3F7FC).copy(alpha = 0.72f))
    drawFlatCloud(Offset(w * 0.62f, h * 0.26f), radiusPx = h * 0.072f, Color(0xFFDCE7F2).copy(alpha = 0.68f))

    // Bukit pastel dusty blue-sage kalem di cakrawala bawah
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.80f, Color(0xFFBFD0D1))
    drawHillWave(w * 0.30f, w * 1.06f, h, w * 0.74f, h * 0.78f, Color(0xFFABC0C4))
    drawHillWave(w * 0.06f, w * 0.96f, h, w * 0.48f, h * 0.86f, Color(0xFF96AEB4))

    // Pepohonan bulat di koridor tengah-kiri & tepi kanan
    drawRoundTree(w * 0.37f, h * 0.96f, h * 0.11f, Color(0xFF6E8B93), Color(0xFF819EA6), Color(0xFF766D65))
    drawRoundTree(w * 0.41f, h * 0.97f, h * 0.082f, Color(0xFF7C99A1), Color(0xFF8FAAB2), Color(0xFF766D65))
    drawRoundTree(w * 0.92f, h * 0.96f, h * 0.12f, Color(0xFF66838B), Color(0xFF79969E), Color(0xFF766D65))
    drawRoundTree(w * 0.96f, h * 0.97f, h * 0.088f, Color(0xFF76939B), Color(0xFF88A5AD), Color(0xFF766D65))
}

// ------------------------------------------------------------------
// 3. CLOUDY / OVERCAST — Langit Mendung Tenang & Pegunungan Biru-Slate
// ------------------------------------------------------------------
private fun DrawScope.drawCloudyOvercastHillsScene() {
    val w = size.width
    val h = size.height

    // Pendar cahaya lembut di balik awan
    drawCircle(Color.White.copy(alpha = 0.22f), radius = h * 0.15f, center = Offset(w * 0.78f, h * 0.18f))

    // Gugusan awan mendung datar besar (path menyatu utuh dengan 2 nada warna lembut)
    drawFlatCloud(Offset(w * 0.42f, h * 0.18f), radiusPx = h * 0.115f, Color.White.copy(alpha = 0.65f))
    drawFlatCloud(Offset(w * 0.48f, h * 0.21f), radiusPx = h * 0.088f, Color(0xFFD6E3F3).copy(alpha = 0.72f))

    drawFlatCloud(Offset(w * 0.80f, h * 0.19f), radiusPx = h * 0.125f, Color.White.copy(alpha = 0.68f))
    drawFlatCloud(Offset(w * 0.86f, h * 0.22f), radiusPx = h * 0.095f, Color(0xFFD2E0F0).copy(alpha = 0.75f))

    drawFlatCloud(Offset(w * 0.62f, h * 0.25f), radiusPx = h * 0.080f, Color.White.copy(alpha = 0.55f))

    // Siluet pegunungan jauh & bukit slate-blue solid
    drawHillWave(-w * 0.05f, w * 0.64f, h, w * 0.33f, h * 0.79f, Color(0xFFB6C9E0))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFFA3B9D5))
    drawHillWave(w * 0.06f, w * 0.96f, h, w * 0.46f, h * 0.86f, Color(0xFF90A9C7))

    // Pohon cemara tenang di lereng bukit
    drawPineTree(w * 0.37f, h * 0.96f, h * 0.12f, Color(0xFF6983A4))
    drawPineTree(w * 0.41f, h * 0.97f, h * 0.088f, Color(0xFF7993B3))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.13f, Color(0xFF617B9C))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.096f, Color(0xFF728CAC))
}

// ------------------------------------------------------------------
// 4. RAINY — Hujan Teduh, Awan Hujan, Bukit Berkaca & Kabin Hangat
// ------------------------------------------------------------------
private fun DrawScope.drawRainyMistScene() {
    val w = size.width
    val h = size.height

    // Awan hujan datar besar di langit atas
    drawFlatCloud(Offset(w * 0.40f, h * 0.16f), radiusPx = h * 0.115f, Color(0xFFE4ECF7).copy(alpha = 0.72f))
    drawFlatCloud(Offset(w * 0.47f, h * 0.19f), radiusPx = h * 0.090f, Color(0xFFC5D5E8).copy(alpha = 0.78f))

    drawFlatCloud(Offset(w * 0.80f, h * 0.17f), radiusPx = h * 0.125f, Color(0xFFE4ECF7).copy(alpha = 0.75f))
    drawFlatCloud(Offset(w * 0.86f, h * 0.20f), radiusPx = h * 0.095f, Color(0xFFC2D2E6).copy(alpha = 0.80f))

    drawFlatCloud(Offset(w * 0.62f, h * 0.22f), radiusPx = h * 0.080f, Color(0xFFD6E2F0).copy(alpha = 0.65f))

    // Garis-garis hujan diagonal lembut
    val rainPrimary = Color(0xFFEEF5FC).copy(alpha = 0.42f)
    val rainSecondary = Color(0xFF6480A6).copy(alpha = 0.26f)
    val rainDrops = listOf(
        0.36f to 0.28f, 0.42f to 0.36f, 0.48f to 0.27f, 0.54f to 0.40f,
        0.60f to 0.30f, 0.67f to 0.38f, 0.74f to 0.28f, 0.80f to 0.37f,
        0.87f to 0.29f, 0.93f to 0.39f,
        0.38f to 0.54f, 0.46f to 0.60f, 0.78f to 0.55f, 0.88f to 0.62f
    )
    rainDrops.forEachIndexed { idx, (rx, ry) ->
        val start = Offset(w * rx, h * ry)
        val end = Offset((w * rx) - (h * 0.036f), (h * ry) + (h * 0.082f))
        drawLine(
            color = if (idx % 2 == 0) rainPrimary else rainSecondary,
            start = start,
            end = end,
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )
    }

    // Pegunungan kabut hujan berlapis solid di cakrawala bawah
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.79f, Color(0xFFB0C2DA))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFF98AECB))
    drawHillWave(w * 0.05f, w * 0.98f, h, w * 0.46f, h * 0.85f, Color(0xFF819ABA))

    // Kabin kecil hangat dengan jendela kuning menyala di koridor terbuka tengah-kiri
    drawCozyCabin(
        cabinLeft = w * 0.37f,
        cabinTop = h * 0.84f,
        cabinW = h * 0.12f,
        cabinH = h * 0.078f,
        wallColor = Color(0xFF566A8C),
        roofColor = Color(0xFF445675),
        windowColor = Color(0xFFFAD98B),
        withSmoke = true
    )

    // Pohon cemara hujan di sekitar kabin & tepi kanan
    drawPineTree(w * 0.34f, h * 0.96f, h * 0.12f, Color(0xFF5B7297))
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.095f, Color(0xFF6C84A9))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF52688C))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF667EA3))

    // Riak genangan air minimalis
    drawRainRipple(Offset(w * 0.39f, h * 0.96f), rx = h * 0.040f, ry = h * 0.009f, Color.White.copy(alpha = 0.35f))
    drawRainRipple(Offset(w * 0.88f, h * 0.96f), rx = h * 0.045f, ry = h * 0.009f, Color.White.copy(alpha = 0.30f))
}

// ------------------------------------------------------------------
// 5. THUNDERSTORM — Hujan Badai, Kilat Geometris & Kabin Hangat
// ------------------------------------------------------------------
private fun DrawScope.drawThunderstormScene() {
    val w = size.width
    val h = size.height

    // Pendar kilat lembut di balik awan kanan-atas
    val flashCenter = Offset(w * 0.83f, h * 0.22f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFCE49C).copy(alpha = 0.38f), Color.Transparent),
            center = flashCenter,
            radius = h * 0.24f
        ),
        radius = h * 0.24f,
        center = flashCenter
    )

    // Awan badai berlapis besar
    drawFlatCloud(Offset(w * 0.40f, h * 0.16f), radiusPx = h * 0.115f, Color(0xFFD5DEF0).copy(alpha = 0.72f))
    drawFlatCloud(Offset(w * 0.47f, h * 0.19f), radiusPx = h * 0.092f, Color(0xFFB5C2DC).copy(alpha = 0.80f))

    drawFlatCloud(Offset(w * 0.80f, h * 0.17f), radiusPx = h * 0.128f, Color(0xFFD8E0F2).copy(alpha = 0.75f))
    drawFlatCloud(Offset(w * 0.86f, h * 0.20f), radiusPx = h * 0.098f, Color(0xFFB3C0DA).copy(alpha = 0.82f))

    // Kilat geometris minimalis bergaya datar
    drawFlatLightningBolt(
        topPoint = Offset(w * 0.84f, h * 0.24f),
        heightPx = h * 0.135f,
        color = Color(0xFFFAD87A)
    )
    drawFlatLightningBolt(
        topPoint = Offset(w * 0.45f, h * 0.24f),
        heightPx = h * 0.095f,
        color = Color(0xFFFAD87A).copy(alpha = 0.78f)
    )

    // Garis hujan badai diagonal
    val rainLight = Color(0xFFEDF2FA).copy(alpha = 0.40f)
    val rainDark = Color(0xFF5A6D94).copy(alpha = 0.26f)
    val drops = listOf(
        0.36f to 0.28f, 0.43f to 0.36f, 0.51f to 0.30f, 0.59f to 0.38f,
        0.74f to 0.30f, 0.81f to 0.39f, 0.89f to 0.31f, 0.94f to 0.40f,
        0.39f to 0.54f, 0.85f to 0.56f
    )
    drops.forEachIndexed { idx, (rx, ry) ->
        drawLine(
            color = if (idx % 2 == 0) rainLight else rainDark,
            start = Offset(w * rx, h * ry),
            end = Offset((w * rx) - (h * 0.040f), (h * ry) + (h * 0.088f)),
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )
    }

    // Bukit indigo-slate berlapis solid di cakrawala bawah
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.79f, Color(0xFFA5B3D1))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFF8D9DC1))
    drawHillWave(w * 0.05f, w * 0.98f, h, w * 0.46f, h * 0.85f, Color(0xFF798AB1))

    // Kabin hangat berlindung dari badai di koridor tengah-kiri
    drawCozyCabin(
        cabinLeft = w * 0.37f,
        cabinTop = h * 0.84f,
        cabinW = h * 0.12f,
        cabinH = h * 0.078f,
        wallColor = Color(0xFF4F5E82),
        roofColor = Color(0xFF3E4C6E),
        windowColor = Color(0xFFFCE092),
        withSmoke = false
    )

    drawPineTree(w * 0.34f, h * 0.96f, h * 0.12f, Color(0xFF526287))
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.095f, Color(0xFF63749A))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF4A5A7F))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF5D6E93))
}

// ------------------------------------------------------------------
// 6. WINDY — Pusaran Angin Sejuk Kalem, Daun Melayang & Layang-layang
// ------------------------------------------------------------------
private fun DrawScope.drawWindyKiteScene() {
    val w = size.width
    val h = size.height

    // Awan besar tertiup angin dengan warna lembut
    drawFlatCloud(Offset(w * 0.45f, h * 0.16f), radiusPx = h * 0.082f, Color(0xFFF2F6FA).copy(alpha = 0.68f))
    drawFlatCloud(Offset(w * 0.74f, h * 0.15f), radiusPx = h * 0.068f, Color(0xFFF2F6FA).copy(alpha = 0.60f))

    // Garis pusaran angin bergaya editorial
    val windColor = Color.White.copy(alpha = 0.46f)
    val windAccent = Color(0xFF5D7F8E).copy(alpha = 0.30f)
    drawWindSwirl(
        startX = w * 0.35f,
        y = h * 0.25f,
        length = h * 0.30f,
        curlRadius = h * 0.028f,
        curlUp = true,
        color = windColor,
        strokeWidth = 2.0f
    )
    drawWindSwirl(
        startX = w * 0.80f,
        y = h * 0.32f,
        length = h * 0.26f,
        curlRadius = h * 0.024f,
        curlUp = false,
        color = windAccent,
        strokeWidth = 1.9f
    )

    // Layang-layang minimalis bernuansa dusty terracotta kalem di langit kanan-atas
    drawKite(
        center = Offset(w * 0.90f, h * 0.21f),
        sizePx = h * 0.088f,
        primaryColor = Color(0xFFC68A80),
        secondaryColor = Color(0xFFD9ABA2),
        tailColor = Color(0xFF5D7F87)
    )

    // Daun-daun melayang tertiup angin
    listOf(
        Triple(0.45f, 0.23f, -22f),
        Triple(0.84f, 0.28f, 18f),
        Triple(0.88f, 0.36f, -28f)
    ).forEach { (lx, ly, angle) ->
        drawFloatingLeaf(
            center = Offset(w * lx, h * ly),
            length = h * 0.030f,
            width = h * 0.016f,
            angleDegrees = angle,
            color = Color(0xFF6E9298)
        )
    }

    // Bukit dusty slate-sage berlapis solid yang kalem
    drawHillWave(-w * 0.06f, w * 0.64f, h, w * 0.33f, h * 0.79f, Color(0xFFBACCD0))
    drawHillWave(w * 0.30f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFFA4BAC0))
    drawHillWave(w * 0.06f, w * 0.96f, h, w * 0.48f, h * 0.86f, Color(0xFF8FA7AF))

    drawRoundTree(w * 0.38f, h * 0.96f, h * 0.11f, Color(0xFF68868F), Color(0xFF7B98A1), Color(0xFF756B63))
    drawRoundTree(w * 0.92f, h * 0.96f, h * 0.12f, Color(0xFF5F7D86), Color(0xFF729099), Color(0xFF756B63))
    drawRoundTree(w * 0.96f, h * 0.97f, h * 0.088f, Color(0xFF73919A), Color(0xFF86A3AC), Color(0xFF756B63))
}

// ------------------------------------------------------------------
// 7. FOGGY / MIST — Lembah Pegunungan Berkabut Lembut
// ------------------------------------------------------------------
private fun DrawScope.drawFoggyMountainValleyScene() {
    val w = size.width
    val h = size.height

    // Matahari pucat samar menembus kabut di sudut kanan-atas
    drawCircle(Color.White.copy(alpha = 0.38f), radius = h * 0.11f, center = Offset(w * 0.89f, h * 0.18f))

    // Pita kabut horizontal lembut di langit terbuka
    drawMistBand(Offset(w * 0.41f, h * 0.17f), widthPx = w * 0.16f, heightPx = h * 0.022f, Color.White.copy(alpha = 0.46f))
    drawMistBand(Offset(w * 0.86f, h * 0.21f), widthPx = w * 0.18f, heightPx = h * 0.024f, Color.White.copy(alpha = 0.48f))

    // Bukit & pegunungan berlapis solid dengan selimut kabut tipis di antaranya
    drawHillWave(-w * 0.06f, w * 0.66f, h, w * 0.33f, h * 0.78f, Color(0xFFB9C9DE))
    drawMistBand(Offset(w * 0.38f, h * 0.81f), widthPx = w * 0.36f, heightPx = h * 0.028f, Color.White.copy(alpha = 0.48f))

    drawHillWave(w * 0.26f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFFA2B6D0))
    drawMistBand(Offset(w * 0.76f, h * 0.85f), widthPx = w * 0.36f, heightPx = h * 0.026f, Color.White.copy(alpha = 0.50f))

    drawHillWave(w * 0.04f, w * 0.96f, h, w * 0.48f, h * 0.86f, Color(0xFF8CA3C1))

    // Pohon cemara muncul dari balik kabut
    drawPineTree(w * 0.37f, h * 0.96f, h * 0.12f, Color(0xFF667D9D))
    drawPineTree(w * 0.41f, h * 0.97f, h * 0.088f, Color(0xFF778EAD))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.13f, Color(0xFF5E7594))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.095f, Color(0xFF7188A7))
}

// ------------------------------------------------------------------
// 8. SNOWY — Pegunungan Salju Musim Dingin & Butiran Salju Halus
// ------------------------------------------------------------------
private fun DrawScope.drawSnowyWinterPeakScene() {
    val w = size.width
    val h = size.height

    // Matahari musim dingin pucat & awan besar
    drawCircle(Color.White.copy(alpha = 0.52f), radius = h * 0.095f, center = Offset(w * 0.89f, h * 0.17f))
    drawFlatCloud(Offset(w * 0.43f, h * 0.17f), radiusPx = h * 0.088f, Color.White.copy(alpha = 0.78f))

    // Pegunungan bersalju di koridor tengah-kiri & kanan
    drawMountain(w * 0.22f, w * 0.38f, w * 0.55f, h * 0.71f, Color(0xFFACC4E0), Color(0xFFF4F9FE))
    drawMountain(w * 0.72f, w * 0.89f, w * 1.04f, h * 0.70f, Color(0xFF9CB7D8), Color(0xFFFFFFFF))

    // Bukit salju depan berlapis solid
    drawHillWave(-w * 0.05f, w * 0.66f, h, w * 0.32f, h * 0.84f, Color(0xFFD9E9F9))
    drawHillWave(w * 0.28f, w * 1.05f, h, w * 0.72f, h * 0.82f, Color(0xFFC8DEF4))

    // Pohon cemara musim dingin
    drawPineTree(w * 0.36f, h * 0.96f, h * 0.125f, Color(0xFF678AB1))
    drawPineTree(w * 0.40f, h * 0.97f, h * 0.092f, Color(0xFF7A9EC2))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF5C7FA6))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF7294BA))

    // Butiran salju halus jatuh perlahan
    val flakes = listOf(
        0.36f to 0.22f, 0.44f to 0.28f, 0.52f to 0.16f,
        0.78f to 0.18f, 0.85f to 0.28f, 0.93f to 0.24f,
        0.39f to 0.48f, 0.88f to 0.46f
    )
    flakes.forEachIndexed { idx, (fx, fy) ->
        val r = if (idx % 2 == 0) h * 0.0075f else h * 0.0055f
        drawCircle(Color.White.copy(alpha = 0.78f), radius = r, center = Offset(w * fx, h * fy))
    }
}

// ------------------------------------------------------------------
// 9. CLEAR NIGHT / TWILIGHT — Bulan Sabit, Bintang & Kabin Senja
// ------------------------------------------------------------------
private fun DrawScope.drawClearStarryTwilightScene() {
    val w = size.width
    val h = size.height

    // Bulan sabit pastel & halo lembut di sudut kanan-atas
    val moonCenter = Offset(w * 0.90f, h * 0.18f)
    drawCircle(Color(0xFFFCE9B8).copy(alpha = 0.24f), radius = h * 0.12f, center = moonCenter)
    drawCrescentMoon(
        center = moonCenter,
        radius = h * 0.056f,
        moonColor = Color(0xFFFCE6B2)
    )

    // Bintang-bintang kecil berkilau lembut
    val stars = listOf(
        0.37f to 0.16f, 0.45f to 0.22f, 0.56f to 0.13f,
        0.78f to 0.15f, 0.83f to 0.24f, 0.95f to 0.14f
    )
    stars.forEachIndexed { idx, (sx, sy) ->
        val starColor = if (idx % 2 == 0) Color(0xFFFCE8B2).copy(alpha = 0.86f) else Color.White.copy(alpha = 0.82f)
        drawSparkleStar(Offset(w * sx, h * sy), radius = h * 0.015f, color = starColor)
    }

    // Bukit senja lavender-biru berlapis solid
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.79f, Color(0xFFB5B5DC))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFFA0A2CF))
    drawHillWave(w * 0.05f, w * 0.98f, h, w * 0.46f, h * 0.85f, Color(0xFF8C8FC2))

    // Kabin malam yang hangat di koridor terbuka tengah-kiri
    drawCozyCabin(
        cabinLeft = w * 0.37f,
        cabinTop = h * 0.84f,
        cabinW = h * 0.12f,
        cabinH = h * 0.078f,
        wallColor = Color(0xFF5D5F8E),
        roofColor = Color(0xFF4A4C78),
        windowColor = Color(0xFFFCE09B),
        withSmoke = true
    )

    drawPineTree(w * 0.34f, h * 0.96f, h * 0.12f, Color(0xFF5F6394))
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.095f, Color(0xFF7175A6))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF545888))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF676B9C))
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
        lineTo(peakX + (snowRightX - peakX) * 0.35f, snowY - 4f)
        lineTo(peakX, snowY + 3f)
        lineTo(peakX - (peakX - snowLeftX) * 0.35f, snowY - 4f)
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
    val lowerCanopy = Path().apply {
        moveTo(centerX, topY + (canopyBottom - topY) * 0.22f)
        lineTo(centerX + halfW, canopyBottom)
        lineTo(centerX - halfW, canopyBottom)
        close()
    }
    val upperCanopy = Path().apply {
        moveTo(centerX, topY)
        lineTo(centerX + halfW * 0.78f, midY)
        lineTo(centerX - halfW * 0.78f, midY)
        close()
    }
    val combinedCanopy = Path().apply {
        op(lowerCanopy, upperCanopy, PathOperation.Union)
    }
    drawPath(combinedCanopy, color)
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
        strokeWidth = (treeHeight * 0.10f).coerceAtLeast(2.4f),
        cap = StrokeCap.Round
    )
    val leftLobe = Path().apply {
        val c = Offset(centerX - radius * 0.42f, canopyCenterY + radius * 0.18f)
        val r = radius * 0.85f
        addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r))
    }
    val rightLobe = Path().apply {
        val c = Offset(centerX + radius * 0.42f, canopyCenterY + radius * 0.20f)
        val r = radius * 0.82f
        addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r))
    }
    val baseCanopy = Path().apply {
        op(leftLobe, rightLobe, PathOperation.Union)
    }
    drawPath(baseCanopy, canopyColor)
    drawCircle(
        color = highlightColor,
        radius = radius * 0.95f,
        center = Offset(centerX, canopyCenterY - radius * 0.15f)
    )
}

/**
 * Menggambar awan vektor datar bergaya Google Calendar menggunakan `PathOperation.Union`
 * sehingga siluet awan menyatu utuh tanpa garis irisan lingkaran di dalamnya.
 */
private fun DrawScope.drawFlatCloud(
    center: Offset,
    radiusPx: Float,
    color: Color
) {
    val basePill = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = Offset(center.x - radiusPx * 1.6f, center.y + radiusPx * 0.1f),
                    size = Size(radiusPx * 3.2f, radiusPx * 0.95f)
                ),
                cornerRadius = CornerRadius(radiusPx * 0.5f, radiusPx * 0.5f)
            )
        )
    }
    val leftPuff = Path().apply {
        val c = Offset(center.x - radiusPx * 0.9f, center.y + radiusPx * 0.15f)
        val pr = radiusPx * 0.85f
        addOval(Rect(c.x - pr, c.y - pr, c.x + pr, c.y + pr))
    }
    val centerPuff = Path().apply {
        val c = Offset(center.x, center.y - radiusPx * 0.15f)
        val pr = radiusPx * 1.1f
        addOval(Rect(c.x - pr, c.y - pr, c.x + pr, c.y + pr))
    }
    val rightPuff = Path().apply {
        val c = Offset(center.x + radiusPx * 0.95f, center.y + radiusPx * 0.2f)
        val pr = radiusPx * 0.8f
        addOval(Rect(c.x - pr, c.y - pr, c.x + pr, c.y + pr))
    }

    val u1 = Path().apply { op(basePill, leftPuff, PathOperation.Union) }
    val u2 = Path().apply { op(u1, centerPuff, PathOperation.Union) }
    val cloudPath = Path().apply { op(u2, rightPuff, PathOperation.Union) }
    drawPath(cloudPath, color)
}

private fun DrawScope.drawCozyCabin(
    cabinLeft: Float,
    cabinTop: Float,
    cabinW: Float,
    cabinH: Float,
    wallColor: Color,
    roofColor: Color,
    windowColor: Color,
    withSmoke: Boolean
) {
    if (withSmoke) {
        drawCircle(
            Color.White.copy(alpha = 0.45f),
            radius = cabinH * 0.18f,
            center = Offset(cabinLeft + cabinW * 0.82f, cabinTop - cabinH * 0.48f)
        )
        drawCircle(
            Color.White.copy(alpha = 0.30f),
            radius = cabinH * 0.24f,
            center = Offset(cabinLeft + cabinW * 0.94f, cabinTop - cabinH * 0.82f)
        )
    }
    drawRoundRect(
        color = wallColor,
        topLeft = Offset(cabinLeft, cabinTop),
        size = Size(cabinW, cabinH),
        cornerRadius = CornerRadius(3f, 3f)
    )
    val roof = Path().apply {
        moveTo(cabinLeft - cabinW * 0.12f, cabinTop + 2f)
        lineTo(cabinLeft + cabinW * 0.5f, cabinTop - cabinH * 0.58f)
        lineTo(cabinLeft + cabinW * 1.12f, cabinTop + 2f)
        close()
    }
    drawPath(roof, roofColor)
    drawRoundRect(
        color = windowColor,
        topLeft = Offset(cabinLeft + cabinW * 0.24f, cabinTop + cabinH * 0.26f),
        size = Size(cabinW * 0.22f, cabinH * 0.38f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = windowColor,
        topLeft = Offset(cabinLeft + cabinW * 0.56f, cabinTop + cabinH * 0.26f),
        size = Size(cabinW * 0.22f, cabinH * 0.38f),
        cornerRadius = CornerRadius(2f, 2f)
    )
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

    val tower = Path().apply {
        moveTo(baseCenter.x - baseHalfW, baseCenter.y)
        lineTo(baseCenter.x - topHalfW, topY)
        lineTo(baseCenter.x + topHalfW, topY)
        lineTo(baseCenter.x + baseHalfW, baseCenter.y)
        close()
    }
    drawPath(tower, bodyColor)

    val roof = Path().apply {
        moveTo(baseCenter.x - topHalfW * 1.15f, topY)
        lineTo(baseCenter.x, topY - towerHeight * 0.24f)
        lineTo(baseCenter.x + topHalfW * 1.15f, topY)
        close()
    }
    drawPath(roof, roofColor)

    val hub = Offset(baseCenter.x, topY + towerHeight * 0.08f)
    val bladeLen = towerHeight * 0.56f
    listOf(35f, 125f, 215f, 305f).forEach { deg ->
        val rad = Math.toRadians(deg.toDouble())
        val end = Offset(
            x = hub.x + (cos(rad) * bladeLen).toFloat(),
            y = hub.y + (sin(rad) * bladeLen).toFloat()
        )
        drawLine(bladeColor, hub, end, strokeWidth = 2.2f, cap = StrokeCap.Round)
    }
    drawCircle(roofColor, radius = towerHeight * 0.06f, center = hub)
}

private fun DrawScope.drawFlatLightningBolt(
    topPoint: Offset,
    heightPx: Float,
    color: Color
) {
    val w = heightPx * 0.42f
    val bolt = Path().apply {
        moveTo(topPoint.x, topPoint.y)
        lineTo(topPoint.x - w * 0.65f, topPoint.y + heightPx * 0.52f)
        lineTo(topPoint.x - w * 0.08f, topPoint.y + heightPx * 0.52f)
        lineTo(topPoint.x - w * 0.45f, topPoint.y + heightPx)
        lineTo(topPoint.x + w * 0.55f, topPoint.y + heightPx * 0.40f)
        lineTo(topPoint.x - w * 0.02f, topPoint.y + heightPx * 0.40f)
        close()
    }
    drawPath(bolt, color)
}

private fun DrawScope.drawRainRipple(
    center: Offset,
    rx: Float,
    ry: Float,
    color: Color
) {
    drawOval(
        color = color,
        topLeft = Offset(center.x - rx, center.y - ry),
        size = Size(rx * 2f, ry * 2f),
        style = Stroke(width = 1.6f)
    )
}

private fun DrawScope.drawMistBand(
    center: Offset,
    widthPx: Float,
    heightPx: Float,
    color: Color
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - widthPx * 0.5f, center.y - heightPx * 0.5f),
        size = Size(widthPx, heightPx),
        cornerRadius = CornerRadius(heightPx * 0.5f, heightPx * 0.5f)
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
    drawPath(tail, color = tailColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    drawCircle(primaryColor, radius = sizePx * 0.09f, center = Offset(center.x - sizePx * 0.14f, center.y + sizePx * 1.12f))
    drawCircle(secondaryColor, radius = sizePx * 0.08f, center = Offset(center.x - sizePx * 0.05f, center.y + sizePx * 1.56f))

    val leftHalf = Path().apply {
        moveTo(center.x, center.y - sizePx * 0.65f)
        lineTo(center.x, center.y + sizePx * 0.65f)
        lineTo(center.x - sizePx * 0.48f, center.y)
        close()
    }
    drawPath(leftHalf, primaryColor)

    val rightHalf = Path().apply {
        moveTo(center.x, center.y - sizePx * 0.65f)
        lineTo(center.x + sizePx * 0.48f, center.y)
        lineTo(center.x, center.y + sizePx * 0.65f)
        close()
    }
    drawPath(rightHalf, secondaryColor)
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

private fun DrawScope.drawCrescentMoon(
    center: Offset,
    radius: Float,
    moonColor: Color
) {
    val fullCircle = Path().apply {
        addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
    }
    val cutCenter = Offset(center.x + radius * 0.42f, center.y - radius * 0.15f)
    val cutR = radius * 0.88f
    val cutCircle = Path().apply {
        addOval(Rect(cutCenter.x - cutR, cutCenter.y - cutR, cutCenter.x + cutR, cutCenter.y + cutR))
    }
    val crescent = Path().apply {
        op(fullCircle, cutCircle, PathOperation.Difference)
    }
    drawPath(crescent, moonColor)
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
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
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

@Preview(showBackground = true, widthDp = 960, heightDp = 720)
@Composable
fun WeatherBackgroundGalleryPreview() {
    val samples = listOf(
        Triple("Sunny", "Good Morning!", WeatherSceneKind.SUNNY),
        Triple("Partly Cloudy", "Good Afternoon!", WeatherSceneKind.PARTLY_CLOUDY),
        Triple("Cloudy / Overcast", "Good Afternoon!", WeatherSceneKind.CLOUDY),
        Triple("Heavy Rain", "Good Morning!", WeatherSceneKind.RAINY),
        Triple("Thunderstorm", "Good Evening!", WeatherSceneKind.THUNDERSTORM),
        Triple("Windy Breeze", "Good Afternoon!", WeatherSceneKind.WINDY),
        Triple("Misty Fog", "Good Morning!", WeatherSceneKind.FOGGY),
        Triple("Snow Showers", "Good Morning!", WeatherSceneKind.SNOWY),
        Triple("Clear Night", "Good Night!", WeatherSceneKind.CLEAR_NIGHT)
    )
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            samples.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (label, greeting, _) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 3.dp
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                WeatherBackgroundIllustration(
                                    weather = label,
                                    greeting = greeting,
                                    modifier = Modifier.matchParentSize()
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = greeting,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = label,
                                        color = Color(0xFF535F8C),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
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
