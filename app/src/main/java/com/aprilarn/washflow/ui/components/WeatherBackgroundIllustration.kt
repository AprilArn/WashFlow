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
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kategori lanskap cuaca bergaya Google Calendar (Material Editorial)
 * yang mencakup seluruh enum `WeatherCondition.Type` dari Google Maps Weather API
 * beserta pemisahan kondisi siang (`isDaytime = true`) dan malam (`isDaytime = false`).
 */
enum class WeatherSceneKind {
    SUNNY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    CLOUDY_NIGHT,
    LIGHT_RAIN,
    RAINY,
    WIND_AND_RAIN,
    THUNDERSTORM,
    WINDY,
    WINDY_NIGHT,
    FOGGY,
    SNOWY,
    SNOWY_NIGHT,
    WINTRY_MIX
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
    WeatherSceneKind.CLEAR_NIGHT -> WeatherScenePalette(
        skyTop = Color(0xFF7286B8),
        skyMid = Color(0xFF94A5D4),
        skyBottom = Color(0xFFC8C0DE),
        horizonGlow = Color(0xFFDDD6EC)
    )
    WeatherSceneKind.PARTLY_CLOUDY -> WeatherScenePalette(
        skyTop = Color(0xFF88A9C5),
        skyMid = Color(0xFFABC3D8),
        skyBottom = Color(0xFFD8DDD9),
        horizonGlow = Color(0xFFE6E7E2)
    )
    WeatherSceneKind.PARTLY_CLOUDY_NIGHT -> WeatherScenePalette(
        skyTop = Color(0xFF7083B0),
        skyMid = Color(0xFF91A2CA),
        skyBottom = Color(0xFFC2C6DE),
        horizonGlow = Color(0xFFD5D8EB)
    )
    WeatherSceneKind.CLOUDY -> WeatherScenePalette(
        skyTop = Color(0xFF8AA8CB),
        skyMid = Color(0xFFACC3DE),
        skyBottom = Color(0xFFD0DEEC),
        horizonGlow = Color(0xFFE0E9F3)
    )
    WeatherSceneKind.CLOUDY_NIGHT -> WeatherScenePalette(
        skyTop = Color(0xFF6E7FA4),
        skyMid = Color(0xFF8B9BBE),
        skyBottom = Color(0xFFB7C2D9),
        horizonGlow = Color(0xFFCAD3E6)
    )
    WeatherSceneKind.LIGHT_RAIN -> WeatherScenePalette(
        skyTop = Color(0xFF84A2C4),
        skyMid = Color(0xFFA5BED9),
        skyBottom = Color(0xFFCCDCE8),
        horizonGlow = Color(0xFFDCE8F2)
    )
    WeatherSceneKind.RAINY -> WeatherScenePalette(
        skyTop = Color(0xFF7D9ABE),
        skyMid = Color(0xFF9DB6D4),
        skyBottom = Color(0xFFC3D3E7),
        horizonGlow = Color(0xFFD3E0F0)
    )
    WeatherSceneKind.WIND_AND_RAIN -> WeatherScenePalette(
        skyTop = Color(0xFF7896B4),
        skyMid = Color(0xFF97B1CA),
        skyBottom = Color(0xFFBFD0DE),
        horizonGlow = Color(0xFFD0DEE8)
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
    WeatherSceneKind.WINDY_NIGHT -> WeatherScenePalette(
        skyTop = Color(0xFF6F88AA),
        skyMid = Color(0xFF8FA6C4),
        skyBottom = Color(0xFFB8C9DC),
        horizonGlow = Color(0xFFCBD8E8)
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
    WeatherSceneKind.SNOWY_NIGHT -> WeatherScenePalette(
        skyTop = Color(0xFF738AB2),
        skyMid = Color(0xFF94AACD),
        skyBottom = Color(0xFFC1D0E6),
        horizonGlow = Color(0xFFD4E0F2)
    )
    WeatherSceneKind.WINTRY_MIX -> WeatherScenePalette(
        skyTop = Color(0xFF7D97B8),
        skyMid = Color(0xFF9DB4D0),
        skyBottom = Color(0xFFC6D5E8),
        horizonGlow = Color(0xFFD8E4F2)
    )
}

/**
 * Memetakan `weatherType` (enum `WeatherCondition.Type` dari Google Maps Weather API),
 * deskripsi teks cuaca (`weatherText`), status siang/malam lokasi (`isDaytime`),
 * serta sapaan waktu (`greetingText`) ke jenis ilustrasi latar belakang yang akurat.
 */
fun resolveWeatherSceneKind(
    weatherText: String,
    greetingText: String = "",
    weatherType: String = "",
    isDaytime: Boolean? = null
): WeatherSceneKind {
    val w = weatherText.trim().lowercase()
    val g = greetingText.trim().lowercase()
    val t = weatherType.trim().uppercase()

    // Prioritas 1: Gunakan `isDaytime` langsung dari Google Weather API (akurat sesuai sunrise/sunset lokasi & zona waktu).
    // Prioritas 2 (Fallback): Cek teks greeting / teks cuaca / jam lokal perangkat (18:00 - 05:59 dianggap malam).
    val isNightTime: Boolean = when {
        isDaytime != null -> !isDaytime
        g.contains("night") || g.contains("malam") || w.contains("night") || w.contains("malam") -> true
        g.contains("morning") || g.contains("pagi") || g.contains("afternoon") || g.contains("siang") -> false
        else -> {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            hour < 6 || hour >= 18
        }
    }

    // 1. Pemetaan langsung berdasarkan enum resmi Google Weather API `WeatherCondition.Type`
    if (t.isNotEmpty() && t != "TYPE_UNSPECIFIED") {
        when (t) {
            "CLEAR", "MOSTLY_CLEAR" -> {
                return if (isNightTime) WeatherSceneKind.CLEAR_NIGHT else WeatherSceneKind.SUNNY
            }
            "PARTLY_CLOUDY", "MOSTLY_CLOUDY" -> {
                return if (isNightTime) WeatherSceneKind.PARTLY_CLOUDY_NIGHT else WeatherSceneKind.PARTLY_CLOUDY
            }
            "CLOUDY" -> {
                return if (isNightTime) WeatherSceneKind.CLOUDY_NIGHT else WeatherSceneKind.CLOUDY
            }
            "WINDY" -> {
                return if (isNightTime) WeatherSceneKind.WINDY_NIGHT else WeatherSceneKind.WINDY
            }
            "WIND_AND_RAIN" -> {
                return WeatherSceneKind.WIND_AND_RAIN
            }
            "THUNDERSTORM",
            "THUNDERSHOWER",
            "LIGHT_THUNDERSTORM_RAIN",
            "SCATTERED_THUNDERSTORMS",
            "HEAVY_THUNDERSTORM" -> {
                return WeatherSceneKind.THUNDERSTORM
            }
            "LIGHT_RAIN_SHOWERS",
            "CHANCE_OF_SHOWERS",
            "SCATTERED_SHOWERS",
            "RAIN_SHOWERS",
            "LIGHT_RAIN",
            "LIGHT_TO_MODERATE_RAIN" -> {
                return WeatherSceneKind.LIGHT_RAIN
            }
            "HEAVY_RAIN_SHOWERS",
            "MODERATE_TO_HEAVY_RAIN",
            "RAIN",
            "HEAVY_RAIN",
            "RAIN_PERIODICALLY_HEAVY" -> {
                return WeatherSceneKind.RAINY
            }
            "RAIN_AND_SNOW",
            "HAIL",
            "HAIL_SHOWERS",
            "BLOWING_SNOW",
            "SNOWSTORM",
            "HEAVY_SNOW_STORM" -> {
                return WeatherSceneKind.WINTRY_MIX
            }
            "LIGHT_SNOW_SHOWERS",
            "CHANCE_OF_SNOW_SHOWERS",
            "SCATTERED_SNOW_SHOWERS",
            "SNOW_SHOWERS",
            "HEAVY_SNOW_SHOWERS",
            "LIGHT_TO_MODERATE_SNOW",
            "MODERATE_TO_HEAVY_SNOW",
            "SNOW",
            "LIGHT_SNOW",
            "HEAVY_SNOW",
            "SNOW_PERIODICALLY_HEAVY" -> {
                return if (isNightTime) WeatherSceneKind.SNOWY_NIGHT else WeatherSceneKind.SNOWY
            }
        }
    }

    // 2. Fallback berbasis teks deskripsi (`weatherCondition.description.text` atau kode regional)
    return when {
        // Badai petir / Thunderstorm
        w.contains("thunder") || w.contains("storm") && !w.contains("snow") ||
            w.contains("petir") || w.contains("badai") || w.contains("kilat") || w.contains("squall") -> {
            WeatherSceneKind.THUNDERSTORM
        }

        // Angin disertai hujan (Wind and rain)
        (w.contains("wind") || w.contains("angin") || w.contains("gale")) &&
            (w.contains("rain") || w.contains("shower") || w.contains("hujan")) -> {
            WeatherSceneKind.WIND_AND_RAIN
        }

        // Campuran hujan salju / hujan es / badai salju (Wintry Mix / Hail / Sleet / Snowstorm)
        w.contains("hail") || w.contains("sleet") || w.contains("freezing") ||
            w.contains("rain and snow") || w.contains("blowing snow") ||
            w.contains("snowstorm") || w.contains("snow storm") || w.contains("blizzard") ||
            w.contains("hujan es") -> {
            WeatherSceneKind.WINTRY_MIX
        }

        // Salju
        w.contains("snow") || w.contains("flurr") || w.contains("ice") ||
            w.contains("salju") || w.contains("frost") -> {
            if (isNightTime) WeatherSceneKind.SNOWY_NIGHT else WeatherSceneKind.SNOWY
        }

        // Hujan ringan / Gerimis / Showers
        w.contains("light rain") || w.contains("drizzle") || w.contains("shower") ||
            w.contains("gerimis") || w.contains("hujan ringan") || w.contains("periodic rain") ||
            w.contains("chance of rain") -> {
            WeatherSceneKind.LIGHT_RAIN
        }

        // Hujan sedang hingga lebat
        w.contains("rain") || w.contains("hujan") || w.contains("downpour") ||
            w.contains("precipitation") || w.contains("basah") -> {
            WeatherSceneKind.RAINY
        }

        // Kabut / Asap / Haze
        w.contains("fog") || w.contains("mist") || w.contains("haze") ||
            w.contains("smoke") || w.contains("kabut") || w.contains("asap") ||
            w.contains("dust") || w.contains("sand") -> {
            WeatherSceneKind.FOGGY
        }

        // Berangin
        w.contains("wind") || w.contains("breez") || w.contains("gale") ||
            w.contains("angin") || w.contains("gust") -> {
            if (isNightTime) WeatherSceneKind.WINDY_NIGHT else WeatherSceneKind.WINDY
        }

        // Sebagian berawan / Cerah berawan / Mostly cloudy
        w.contains("partly") || w.contains("mostly cloudy") || w.contains("mostly sunny") ||
            w.contains("cerah berawan") || w.contains("scattered cloud") ||
            w.contains("few cloud") || w.contains("broken cloud") || w.contains("periodic cloud") -> {
            if (isNightTime) WeatherSceneKind.PARTLY_CLOUDY_NIGHT else WeatherSceneKind.PARTLY_CLOUDY
        }

        // Mendung penuh / Overcast
        w.contains("cloud") || w.contains("overcast") || w.contains("berawan") ||
            w.contains("mendung") || w.contains("gloomy") -> {
            if (isNightTime) WeatherSceneKind.CLOUDY_NIGHT else WeatherSceneKind.CLOUDY
        }

        // Cerah / Sunny / Clear / Mostly clear
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
 * yang menyesuaikan diri secara dinamis dengan kondisi cuaca dan waktu siang/malam lokasi.
 */
@Composable
fun WeatherBackgroundIllustration(
    weather: String,
    modifier: Modifier = Modifier,
    greeting: String = "",
    weatherType: String = "",
    isDaytime: Boolean? = null
) {
    val sceneKind = remember(weather, greeting, weatherType, isDaytime) {
        resolveWeatherSceneKind(
            weatherText = weather,
            greetingText = greeting,
            weatherType = weatherType,
            isDaytime = isDaytime
        )
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
            WeatherSceneKind.CLEAR_NIGHT -> drawClearStarryTwilightScene()
            WeatherSceneKind.PARTLY_CLOUDY -> drawPartlyCloudyBreezeScene()
            WeatherSceneKind.PARTLY_CLOUDY_NIGHT -> drawPartlyCloudyNightScene()
            WeatherSceneKind.CLOUDY -> drawCloudyOvercastHillsScene()
            WeatherSceneKind.CLOUDY_NIGHT -> drawCloudyNightScene()
            WeatherSceneKind.LIGHT_RAIN -> drawLightRainShowersScene()
            WeatherSceneKind.RAINY -> drawRainyMistScene()
            WeatherSceneKind.WIND_AND_RAIN -> drawWindAndRainScene()
            WeatherSceneKind.THUNDERSTORM -> drawThunderstormScene()
            WeatherSceneKind.WINDY -> drawWindyKiteScene()
            WeatherSceneKind.WINDY_NIGHT -> drawWindyNightScene()
            WeatherSceneKind.FOGGY -> drawFoggyMountainValleyScene()
            WeatherSceneKind.SNOWY -> drawSnowyWinterPeakScene()
            WeatherSceneKind.SNOWY_NIGHT -> drawSnowyNightScene()
            WeatherSceneKind.WINTRY_MIX -> drawWintryMixScene()
        }
    }
}

// ------------------------------------------------------------------
// 1. SUNNY (Daytime CLEAR / MOSTLY_CLEAR)
// ------------------------------------------------------------------
private fun DrawScope.drawSunnyMeadowScene() {
    val w = size.width
    val h = size.height

    // Matahari krem-champagne lembut dengan halo kalem di kanan-atas
    val sunCenter = Offset(w * 0.86f, h * 0.20f)
    drawCircle(Color(0xFFFFF8E7).copy(alpha = 0.18f), radius = h * 0.17f, center = sunCenter)
    drawCircle(Color(0xFFF5E8C4).copy(alpha = 0.30f), radius = h * 0.115f, center = sunCenter)
    drawCircle(Color(0xFFEEDBA3), radius = h * 0.068f, center = sunCenter)

    // Awan datar besar bergaya Google Calendar
    drawFlatCloud(Offset(w * 0.42f, h * 0.16f), radiusPx = h * 0.085f, Color(0xFFF4F8FC).copy(alpha = 0.68f))
    drawFlatCloud(Offset(w * 0.68f, h * 0.22f), radiusPx = h * 0.065f, Color(0xFFF4F8FC).copy(alpha = 0.58f))

    // Burung terbang jauh
    drawBirdV(Offset(w * 0.52f, h * 0.18f), h * 0.018f, Color(0xFF5D7E9B).copy(alpha = 0.58f))
    drawBirdV(Offset(w * 0.55f, h * 0.155f), h * 0.013f, Color(0xFF5D7E9B).copy(alpha = 0.46f))

    // Bukit jauh & cakrawala dusty sage-slate kalem
    drawHillWave(-w * 0.08f, w * 0.64f, h, w * 0.32f, h * 0.80f, Color(0xFFC7D5CB))
    drawHillWave(w * 0.30f, w * 1.08f, h, w * 0.76f, h * 0.78f, Color(0xFFB4C6BA))
    drawHillWave(w * 0.04f, w * 0.98f, h, w * 0.46f, h * 0.86f, Color(0xFF9FB4A7))

    // Kincir angin mungil dengan warna kalem
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
// 2. CLEAR NIGHT (Nighttime CLEAR / MOSTLY_CLEAR)
// ------------------------------------------------------------------
private fun DrawScope.drawClearStarryTwilightScene() {
    val w = size.width
    val h = size.height

    // Bulan sabit pastel & halo lembut di sudut kanan-atas
    val moonCenter = Offset(w * 0.88f, h * 0.18f)
    drawCircle(Color(0xFFFCE9B8).copy(alpha = 0.22f), radius = h * 0.125f, center = moonCenter)
    drawCrescentMoon(
        center = moonCenter,
        radius = h * 0.056f,
        moonColor = Color(0xFFFCE6B2)
    )

    // Bintang-bintang kecil berkilau lembut
    val stars = listOf(
        0.37f to 0.16f, 0.45f to 0.22f, 0.56f to 0.13f,
        0.74f to 0.15f, 0.81f to 0.25f, 0.95f to 0.14f
    )
    stars.forEachIndexed { idx, (sx, sy) ->
        val starColor = if (idx % 2 == 0) Color(0xFFFCE8B2).copy(alpha = 0.84f) else Color.White.copy(alpha = 0.80f)
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
// 3. PARTLY CLOUDY (Daytime PARTLY_CLOUDY / MOSTLY_CLOUDY)
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
// 4. PARTLY CLOUDY NIGHT (Nighttime PARTLY_CLOUDY / MOSTLY_CLOUDY)
//    Bulan sabit berpadu awan malam besar berlapis & bintang samar (TANPA matahari)
// ------------------------------------------------------------------
private fun DrawScope.drawPartlyCloudyNightScene() {
    val w = size.width
    val h = size.height

    // Bulan sabit mengintip di balik awan malam di kanan-atas
    val moonCenter = Offset(w * 0.85f, h * 0.17f)
    drawCircle(Color(0xFFFCE9B8).copy(alpha = 0.20f), radius = h * 0.13f, center = moonCenter)
    drawCrescentMoon(
        center = moonCenter,
        radius = h * 0.056f,
        moonColor = Color(0xFFFBE5B0)
    )

    // Bintang-bintang halus di celah langit malam yang tidak tertutup awan
    val stars = listOf(
        0.36f to 0.12f, 0.55f to 0.13f, 0.72f to 0.12f, 0.94f to 0.13f
    )
    stars.forEachIndexed { idx, (sx, sy) ->
        val starColor = if (idx % 2 == 0) Color(0xFFFCE8B2).copy(alpha = 0.76f) else Color.White.copy(alpha = 0.72f)
        drawSparkleStar(Offset(w * sx, h * sy), radius = h * 0.013f, color = starColor)
    }

    // Awan malam besar berlapis (menutupi sebagian bawah bulan sabit)
    drawFlatCloud(Offset(w * 0.80f, h * 0.23f), radiusPx = h * 0.105f, Color(0xFFDCE4F4).copy(alpha = 0.72f))
    drawFlatCloud(Offset(w * 0.87f, h * 0.25f), radiusPx = h * 0.082f, Color(0xFFBDC9E3).copy(alpha = 0.76f))
    drawFlatCloud(Offset(w * 0.43f, h * 0.19f), radiusPx = h * 0.096f, Color(0xFFDCE4F4).copy(alpha = 0.68f))
    drawFlatCloud(Offset(w * 0.62f, h * 0.26f), radiusPx = h * 0.074f, Color(0xFFC8D3EA).copy(alpha = 0.62f))

    // Bukit malam indigo-slate berlapis solid
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.79f, Color(0xFFAAB4D6))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFF94A0C8))
    drawHillWave(w * 0.05f, w * 0.98f, h, w * 0.46f, h * 0.85f, Color(0xFF808DB8))

    // Kabin malam yang hangat di koridor terbuka tengah-kiri
    drawCozyCabin(
        cabinLeft = w * 0.37f,
        cabinTop = h * 0.84f,
        cabinW = h * 0.12f,
        cabinH = h * 0.078f,
        wallColor = Color(0xFF56618C),
        roofColor = Color(0xFF434D75),
        windowColor = Color(0xFFFCE09B),
        withSmoke = true
    )

    drawPineTree(w * 0.34f, h * 0.96f, h * 0.12f, Color(0xFF5A6592))
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.095f, Color(0xFF6B77A3))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF4F5A86))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF626D99))
}

// ------------------------------------------------------------------
// 5. CLOUDY / OVERCAST (Daytime CLOUDY — mendung penuh tanpa matahari)
// ------------------------------------------------------------------
private fun DrawScope.drawCloudyOvercastHillsScene() {
    val w = size.width
    val h = size.height

    // Gugusan awan mendung datar besar berlapis (tanpa lingkaran matahari)
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
// 6. CLOUDY NIGHT (Nighttime CLOUDY — mendung malam penuh tanpa matahari/bulan terbuka)
// ------------------------------------------------------------------
private fun DrawScope.drawCloudyNightScene() {
    val w = size.width
    val h = size.height

    // Gugusan awan malam mendung tebal berlapis
    drawFlatCloud(Offset(w * 0.42f, h * 0.18f), radiusPx = h * 0.115f, Color(0xFFD0DAEC).copy(alpha = 0.58f))
    drawFlatCloud(Offset(w * 0.48f, h * 0.21f), radiusPx = h * 0.088f, Color(0xFFB2C0DA).copy(alpha = 0.68f))

    drawFlatCloud(Offset(w * 0.80f, h * 0.19f), radiusPx = h * 0.125f, Color(0xFFD0DAEC).copy(alpha = 0.60f))
    drawFlatCloud(Offset(w * 0.86f, h * 0.22f), radiusPx = h * 0.095f, Color(0xFFAEBCD7).copy(alpha = 0.72f))

    drawFlatCloud(Offset(w * 0.62f, h * 0.25f), radiusPx = h * 0.080f, Color(0xFFC5D0E6).copy(alpha = 0.52f))

    // Bukit malam slate-indigo berlapis solid
    drawHillWave(-w * 0.05f, w * 0.64f, h, w * 0.33f, h * 0.79f, Color(0xFFA1B0CB))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFF8C9CBA))
    drawHillWave(w * 0.06f, w * 0.96f, h, w * 0.46f, h * 0.86f, Color(0xFF7889AA))

    // Kabin malam hangat di bawah langit mendung
    drawCozyCabin(
        cabinLeft = w * 0.37f,
        cabinTop = h * 0.84f,
        cabinW = h * 0.12f,
        cabinH = h * 0.078f,
        wallColor = Color(0xFF516080),
        roofColor = Color(0xFF3F4D6B),
        windowColor = Color(0xFFFAD98B),
        withSmoke = true
    )

    drawPineTree(w * 0.34f, h * 0.96f, h * 0.12f, Color(0xFF556686))
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.095f, Color(0xFF667898))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF4B5C7C))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF5D6E8E))
}

// ------------------------------------------------------------------
// 7. LIGHT RAIN / SHOWERS (LIGHT_RAIN_SHOWERS, SCATTERED_SHOWERS, CHANCE_OF_SHOWERS, LIGHT_RAIN)
//    Gerimis / hujan rintik-rintik lembut dengan bukit biru-sage tenang
// ------------------------------------------------------------------
private fun DrawScope.drawLightRainShowersScene() {
    val w = size.width
    val h = size.height

    // Awan gerimis lembut berlapis
    drawFlatCloud(Offset(w * 0.41f, h * 0.17f), radiusPx = h * 0.105f, Color(0xFFEBF2FA).copy(alpha = 0.72f))
    drawFlatCloud(Offset(w * 0.47f, h * 0.20f), radiusPx = h * 0.082f, Color(0xFFD0DFEF).copy(alpha = 0.75f))

    drawFlatCloud(Offset(w * 0.81f, h * 0.18f), radiusPx = h * 0.112f, Color(0xFFEBF2FA).copy(alpha = 0.74f))
    drawFlatCloud(Offset(w * 0.86f, h * 0.21f), radiusPx = h * 0.085f, Color(0xFFCEDDEE).copy(alpha = 0.76f))

    // Rintik gerimis pendek & jarang
    val drizzleLight = Color(0xFFF0F6FC).copy(alpha = 0.42f)
    val drizzleMuted = Color(0xFF6A86A8).copy(alpha = 0.24f)
    val drizzleDrops = listOf(
        0.38f to 0.28f, 0.45f to 0.34f, 0.53f to 0.29f,
        0.76f to 0.29f, 0.83f to 0.35f, 0.90f to 0.30f,
        0.41f to 0.50f, 0.85f to 0.52f
    )
    drizzleDrops.forEachIndexed { idx, (rx, ry) ->
        val start = Offset(w * rx, h * ry)
        val end = Offset((w * rx) - (h * 0.022f), (h * ry) + (h * 0.052f))
        drawLine(
            color = if (idx % 2 == 0) drizzleLight else drizzleMuted,
            start = start,
            end = end,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
    }

    // Bukit biru-sage lembut berlapis solid
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.79f, Color(0xFFB7C9DC))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFFA1B7CE))
    drawHillWave(w * 0.05f, w * 0.98f, h, w * 0.46f, h * 0.85f, Color(0xFF8AA3BE))

    // Pepohonan & riak rintik air halus
    drawPineTree(w * 0.37f, h * 0.96f, h * 0.12f, Color(0xFF637E9C))
    drawPineTree(w * 0.41f, h * 0.97f, h * 0.088f, Color(0xFF748FAD))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.13f, Color(0xFF5A7593))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.096f, Color(0xFF6C87A5))

    drawRainRipple(Offset(w * 0.40f, h * 0.96f), rx = h * 0.034f, ry = h * 0.008f, Color.White.copy(alpha = 0.32f))
    drawRainRipple(Offset(w * 0.87f, h * 0.96f), rx = h * 0.038f, ry = h * 0.008f, Color.White.copy(alpha = 0.28f))
}

// ------------------------------------------------------------------
// 8. RAINY (RAIN, MODERATE_TO_HEAVY_RAIN, HEAVY_RAIN, HEAVY_RAIN_SHOWERS, RAIN_PERIODICALLY_HEAVY)
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
// 9. WIND AND RAIN (Google Weather API `WIND_AND_RAIN`)
//    Kombinasi pusaran angin kencang, daun beterbangan & hujan miring tajam
// ------------------------------------------------------------------
private fun DrawScope.drawWindAndRainScene() {
    val w = size.width
    val h = size.height

    // Awan hujan besar terdorong angin
    drawFlatCloud(Offset(w * 0.41f, h * 0.16f), radiusPx = h * 0.110f, Color(0xFFE0EAF4).copy(alpha = 0.72f))
    drawFlatCloud(Offset(w * 0.48f, h * 0.19f), radiusPx = h * 0.088f, Color(0xFFC2D3E4).copy(alpha = 0.78f))
    drawFlatCloud(Offset(w * 0.81f, h * 0.17f), radiusPx = h * 0.118f, Color(0xFFE0EAF4).copy(alpha = 0.74f))
    drawFlatCloud(Offset(w * 0.87f, h * 0.20f), radiusPx = h * 0.090f, Color(0xFFBFD0E2).copy(alpha = 0.80f))

    // Pusaran angin kencang
    drawWindSwirl(
        startX = w * 0.36f,
        y = h * 0.28f,
        length = h * 0.32f,
        curlRadius = h * 0.028f,
        curlUp = true,
        color = Color.White.copy(alpha = 0.44f),
        strokeWidth = 2.0f
    )
    drawWindSwirl(
        startX = w * 0.76f,
        y = h * 0.34f,
        length = h * 0.28f,
        curlRadius = h * 0.024f,
        curlUp = false,
        color = Color(0xFF587990).copy(alpha = 0.32f),
        strokeWidth = 1.9f
    )

    // Garis hujan dengan kemiringan tajam akibat tiupan angin
    val rainLight = Color(0xFFEEF5FC).copy(alpha = 0.42f)
    val rainDark = Color(0xFF5D7A99).copy(alpha = 0.28f)
    val slantedDrops = listOf(
        0.38f to 0.30f, 0.46f to 0.37f, 0.55f to 0.29f,
        0.74f to 0.30f, 0.83f to 0.38f, 0.92f to 0.30f,
        0.41f to 0.54f, 0.86f to 0.56f
    )
    slantedDrops.forEachIndexed { idx, (rx, ry) ->
        drawLine(
            color = if (idx % 2 == 0) rainLight else rainDark,
            start = Offset(w * rx, h * ry),
            end = Offset((w * rx) - (h * 0.062f), (h * ry) + (h * 0.076f)),
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )
    }

    // Daun-daun terbawa angin hujan
    listOf(
        Triple(0.49f, 0.25f, -30f),
        Triple(0.88f, 0.31f, -24f)
    ).forEach { (lx, ly, angle) ->
        drawFloatingLeaf(
            center = Offset(w * lx, h * ly),
            length = h * 0.028f,
            width = h * 0.015f,
            angleDegrees = angle,
            color = Color(0xFF678B94)
        )
    }

    // Bukit slate-teal berlapis solid
    drawHillWave(-w * 0.06f, w * 0.65f, h, w * 0.33f, h * 0.79f, Color(0xFFADC2D2))
    drawHillWave(w * 0.28f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFF96AEC2))
    drawHillWave(w * 0.05f, w * 0.98f, h, w * 0.46f, h * 0.85f, Color(0xFF7F99B0))

    drawCozyCabin(
        cabinLeft = w * 0.37f,
        cabinTop = h * 0.84f,
        cabinW = h * 0.12f,
        cabinH = h * 0.078f,
        wallColor = Color(0xFF536980),
        roofColor = Color(0xFF42556B),
        windowColor = Color(0xFFFAD98B),
        withSmoke = false
    )

    drawPineTree(w * 0.34f, h * 0.96f, h * 0.12f, Color(0xFF587189))
    drawPineTree(w * 0.44f, h * 0.97f, h * 0.095f, Color(0xFF69829B))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF4F677F))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF617A92))
}

// ------------------------------------------------------------------
// 10. THUNDERSTORM (THUNDERSTORM, THUNDERSHOWER, LIGHT_THUNDERSTORM_RAIN, SCATTERED_THUNDERSTORMS, HEAVY_THUNDERSTORM)
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
// 11. WINDY (Daytime WINDY)
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
// 12. WINDY NIGHT (Nighttime WINDY — angin malam, bulan sabit & daun melayang)
// ------------------------------------------------------------------
private fun DrawScope.drawWindyNightScene() {
    val w = size.width
    val h = size.height

    // Bulan sabit malam berangin
    val moonCenter = Offset(w * 0.88f, h * 0.18f)
    drawCircle(Color(0xFFFCE9B8).copy(alpha = 0.20f), radius = h * 0.12f, center = moonCenter)
    drawCrescentMoon(
        center = moonCenter,
        radius = h * 0.054f,
        moonColor = Color(0xFFFBE5B0)
    )

    // Awan malam tertiup angin
    drawFlatCloud(Offset(w * 0.44f, h * 0.16f), radiusPx = h * 0.082f, Color(0xFFDCE6F4).copy(alpha = 0.64f))
    drawFlatCloud(Offset(w * 0.75f, h * 0.16f), radiusPx = h * 0.068f, Color(0xFFDCE6F4).copy(alpha = 0.56f))

    // Pusaran angin malam
    drawWindSwirl(
        startX = w * 0.35f,
        y = h * 0.25f,
        length = h * 0.30f,
        curlRadius = h * 0.028f,
        curlUp = true,
        color = Color.White.copy(alpha = 0.42f),
        strokeWidth = 2.0f
    )
    drawWindSwirl(
        startX = w * 0.78f,
        y = h * 0.32f,
        length = h * 0.26f,
        curlRadius = h * 0.024f,
        curlUp = false,
        color = Color(0xFFD0DFEF).copy(alpha = 0.34f),
        strokeWidth = 1.9f
    )

    // Daun melayang di udara malam
    listOf(
        Triple(0.45f, 0.23f, -22f),
        Triple(0.84f, 0.28f, 18f),
        Triple(0.88f, 0.36f, -28f)
    ).forEach { (lx, ly, angle) ->
        drawFloatingLeaf(
            center = Offset(w * lx, h * ly),
            length = h * 0.028f,
            width = h * 0.015f,
            angleDegrees = angle,
            color = Color(0xFF647F9B)
        )
    }

    // Bukit malam biru-slate berlapis solid
    drawHillWave(-w * 0.06f, w * 0.64f, h, w * 0.33f, h * 0.79f, Color(0xFFA7B8CE))
    drawHillWave(w * 0.30f, w * 1.06f, h, w * 0.74f, h * 0.77f, Color(0xFF91A4BE))
    drawHillWave(w * 0.06f, w * 0.96f, h, w * 0.48f, h * 0.86f, Color(0xFF7B90AD))

    drawPineTree(w * 0.37f, h * 0.96f, h * 0.12f, Color(0xFF566B88))
    drawPineTree(w * 0.41f, h * 0.97f, h * 0.088f, Color(0xFF677C99))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.13f, Color(0xFF4D627F))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.095f, Color(0xFF5F7491))
}

// ------------------------------------------------------------------
// 13. FOGGY / MIST — Lembah Pegunungan Berkabut Lembut (tanpa lingkaran matahari)
// ------------------------------------------------------------------
private fun DrawScope.drawFoggyMountainValleyScene() {
    val w = size.width
    val h = size.height

    // Pita kabut horizontal lembut di langit terbuka
    drawMistBand(Offset(w * 0.42f, h * 0.17f), widthPx = w * 0.22f, heightPx = h * 0.026f, Color.White.copy(alpha = 0.46f))
    drawMistBand(Offset(w * 0.84f, h * 0.20f), widthPx = w * 0.24f, heightPx = h * 0.028f, Color.White.copy(alpha = 0.48f))
    drawMistBand(Offset(w * 0.64f, h * 0.26f), widthPx = w * 0.18f, heightPx = h * 0.022f, Color.White.copy(alpha = 0.38f))

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
// 14. SNOWY (Daytime SNOW / SNOW_SHOWERS)
// ------------------------------------------------------------------
private fun DrawScope.drawSnowyWinterPeakScene() {
    val w = size.width
    val h = size.height

    // Awan salju lembut berlapis
    drawFlatCloud(Offset(w * 0.43f, h * 0.17f), radiusPx = h * 0.095f, Color.White.copy(alpha = 0.76f))
    drawFlatCloud(Offset(w * 0.82f, h * 0.18f), radiusPx = h * 0.105f, Color.White.copy(alpha = 0.74f))

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
// 15. SNOWY NIGHT (Nighttime SNOW / SNOW_SHOWERS — malam bersalju dengan bulan sabit)
// ------------------------------------------------------------------
private fun DrawScope.drawSnowyNightScene() {
    val w = size.width
    val h = size.height

    // Bulan sabit malam musim dingin
    val moonCenter = Offset(w * 0.88f, h * 0.17f)
    drawCircle(Color(0xFFFCE9B8).copy(alpha = 0.20f), radius = h * 0.12f, center = moonCenter)
    drawCrescentMoon(
        center = moonCenter,
        radius = h * 0.054f,
        moonColor = Color(0xFFFBE5B0)
    )

    // Awan salju malam
    drawFlatCloud(Offset(w * 0.43f, h * 0.18f), radiusPx = h * 0.096f, Color(0xFFDCE6F6).copy(alpha = 0.68f))
    drawFlatCloud(Offset(w * 0.79f, h * 0.21f), radiusPx = h * 0.090f, Color(0xFFDCE6F6).copy(alpha = 0.66f))

    // Pegunungan salju malam
    drawMountain(w * 0.22f, w * 0.38f, w * 0.55f, h * 0.71f, Color(0xFF8FA7C9), Color(0xFFE6EFFA))
    drawMountain(w * 0.72f, w * 0.89f, w * 1.04f, h * 0.70f, Color(0xFF8199BC), Color(0xFFEDF4FC))

    // Bukit salju malam berlapis solid
    drawHillWave(-w * 0.05f, w * 0.66f, h, w * 0.32f, h * 0.84f, Color(0xFFB7CAE4))
    drawHillWave(w * 0.28f, w * 1.05f, h, w * 0.72f, h * 0.82f, Color(0xFFA4B9D8))

    drawPineTree(w * 0.36f, h * 0.96f, h * 0.125f, Color(0xFF567096))
    drawPineTree(w * 0.40f, h * 0.97f, h * 0.092f, Color(0xFF6882A8))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF4D668B))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF5F799E))

    // Butiran salju malam
    val flakes = listOf(
        0.36f to 0.24f, 0.44f to 0.30f, 0.52f to 0.18f,
        0.76f to 0.20f, 0.85f to 0.30f, 0.93f to 0.26f,
        0.39f to 0.48f, 0.88f to 0.46f
    )
    flakes.forEachIndexed { idx, (fx, fy) ->
        val r = if (idx % 2 == 0) h * 0.0075f else h * 0.0055f
        drawCircle(Color.White.copy(alpha = 0.80f), radius = r, center = Offset(w * fx, h * fy))
    }
}

// ------------------------------------------------------------------
// 16. WINTRY MIX / HAIL / SNOWSTORM (RAIN_AND_SNOW, HAIL, HAIL_SHOWERS, BLOWING_SNOW, SNOWSTORM, HEAVY_SNOW_STORM)
//     Kombinasi awan badai salju, pegunungan es, pusaran angin, garis hujan es & butiran es/salju
// ------------------------------------------------------------------
private fun DrawScope.drawWintryMixScene() {
    val w = size.width
    val h = size.height

    // Awan badai es/salju berlapis besar
    drawFlatCloud(Offset(w * 0.41f, h * 0.16f), radiusPx = h * 0.112f, Color(0xFFE2EBF7).copy(alpha = 0.74f))
    drawFlatCloud(Offset(w * 0.48f, h * 0.19f), radiusPx = h * 0.088f, Color(0xFFC5D4E8).copy(alpha = 0.80f))
    drawFlatCloud(Offset(w * 0.80f, h * 0.17f), radiusPx = h * 0.120f, Color(0xFFE2EBF7).copy(alpha = 0.76f))
    drawFlatCloud(Offset(w * 0.86f, h * 0.20f), radiusPx = h * 0.092f, Color(0xFFC2D1E6).copy(alpha = 0.82f))

    // Pusaran angin dingin (blowing snow / sleet)
    drawWindSwirl(
        startX = w * 0.36f,
        y = h * 0.27f,
        length = h * 0.28f,
        curlRadius = h * 0.025f,
        curlUp = true,
        color = Color.White.copy(alpha = 0.45f),
        strokeWidth = 1.9f
    )

    // Campuran garis hujan-salju diagonal & butiran es (hail/sleet pellets)
    val sleetLineColor = Color(0xFFEEF5FC).copy(alpha = 0.42f)
    val sleetStreaks = listOf(
        0.38f to 0.30f, 0.47f to 0.36f, 0.76f to 0.29f, 0.85f to 0.37f, 0.92f to 0.31f
    )
    sleetStreaks.forEach { (rx, ry) ->
        drawLine(
            color = sleetLineColor,
            start = Offset(w * rx, h * ry),
            end = Offset((w * rx) - (h * 0.038f), (h * ry) + (h * 0.072f)),
            strokeWidth = 1.9f,
            cap = StrokeCap.Round
        )
    }

    val hailPellets = listOf(
        0.41f to 0.26f, 0.52f to 0.32f, 0.79f to 0.27f, 0.89f to 0.34f,
        0.37f to 0.48f, 0.45f to 0.54f, 0.83f to 0.49f, 0.91f to 0.53f
    )
    hailPellets.forEachIndexed { idx, (hx, hy) ->
        val r = if (idx % 2 == 0) h * 0.0080f else h * 0.0060f
        drawCircle(Color.White.copy(alpha = 0.82f), radius = r, center = Offset(w * hx, h * hy))
    }

    // Pegunungan es & bukit salju slate-blue
    drawMountain(w * 0.22f, w * 0.38f, w * 0.55f, h * 0.71f, Color(0xFF9EB5D4), Color(0xFFEDF4FC))
    drawMountain(w * 0.72f, w * 0.89f, w * 1.04f, h * 0.70f, Color(0xFF8FA8C9), Color(0xFFF5F9FE))

    drawHillWave(-w * 0.05f, w * 0.66f, h, w * 0.32f, h * 0.84f, Color(0xFFC5D8EE))
    drawHillWave(w * 0.28f, w * 1.05f, h, w * 0.72f, h * 0.82f, Color(0xFFB3CAE5))

    drawPineTree(w * 0.36f, h * 0.96f, h * 0.125f, Color(0xFF5D7B9E))
    drawPineTree(w * 0.40f, h * 0.97f, h * 0.092f, Color(0xFF6F8EB1))
    drawPineTree(w * 0.92f, h * 0.96f, h * 0.135f, Color(0xFF537194))
    drawPineTree(w * 0.96f, h * 0.97f, h * 0.10f, Color(0xFF6684A7))
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

@Preview(showBackground = true, widthDp = 1080, heightDp = 900)
@Composable
fun WeatherBackgroundGalleryPreview() {
    data class PreviewSample(
        val label: String,
        val greeting: String,
        val weatherType: String,
        val isDaytime: Boolean
    )
    val samples = listOf(
        PreviewSample("Sunny (Day)", "Good Morning!", "CLEAR", true),
        PreviewSample("Clear (Night)", "Good Night!", "CLEAR", false),
        PreviewSample("Partly Cloudy (Day)", "Good Afternoon!", "PARTLY_CLOUDY", true),
        PreviewSample("Partly Cloudy (Night)", "Good Evening!", "PARTLY_CLOUDY", false),
        PreviewSample("Cloudy (Day)", "Good Afternoon!", "CLOUDY", true),
        PreviewSample("Cloudy (Night)", "Good Evening!", "CLOUDY", false),
        PreviewSample("Light Rain Showers", "Good Morning!", "LIGHT_RAIN_SHOWERS", true),
        PreviewSample("Heavy Rain", "Good Afternoon!", "HEAVY_RAIN", true),
        PreviewSample("Wind & Rain", "Good Afternoon!", "WIND_AND_RAIN", true),
        PreviewSample("Thunderstorm", "Good Evening!", "THUNDERSTORM", false),
        PreviewSample("Windy (Day)", "Good Afternoon!", "WINDY", true),
        PreviewSample("Windy (Night)", "Good Evening!", "WINDY", false),
        PreviewSample("Misty Fog", "Good Morning!", "FOG", true),
        PreviewSample("Snow (Day)", "Good Morning!", "SNOW", true),
        PreviewSample("Snow (Night)", "Good Evening!", "SNOW", false),
        PreviewSample("Hail / Wintry Mix", "Good Afternoon!", "RAIN_AND_SNOW", true)
    )
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            samples.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { sample ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 3.dp
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                WeatherBackgroundIllustration(
                                    weather = sample.label,
                                    greeting = sample.greeting,
                                    weatherType = sample.weatherType,
                                    isDaytime = sample.isDaytime,
                                    modifier = Modifier.matchParentSize()
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = sample.greeting,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sample.label,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 11.sp,
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
