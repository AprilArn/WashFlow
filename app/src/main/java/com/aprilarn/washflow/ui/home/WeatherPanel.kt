package com.aprilarn.washflow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private fun parseTemperature(tempStr: String): Int {
    return tempStr.filter { it.isDigit() || it == '-' }.toIntOrNull() ?: 0
}

@Composable
fun TemperatureChartSegment(
    modifier: Modifier = Modifier,
    currentTemp: Float,
    prevTemp: Float?,
    nextTemp: Float?,
    minTemp: Int,
    maxTemp: Int,
    isEvent: Boolean = false
) {
    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(40.dp)) {
        val range = (maxTemp - minTemp).toFloat().coerceAtLeast(1f)

        fun getY(temp: Float): Float {
            // Normalize: 0.2 to 0.8 range of height to avoid edges
            val normalized = (temp - minTemp.toFloat()) / range
            return size.height * (1f - (normalized * 0.6f + 0.2f))
        }

        val currentY = getY(currentTemp)
        val centerX = size.width / 2

        // Draw line from left
        prevTemp?.let {
            val prevY = getY(it)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, (prevY + currentY) / 2),
                end = Offset(centerX, currentY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw line to right
        nextTemp?.let {
            val nextY = getY(it)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(centerX, currentY),
                end = Offset(size.width, (currentY + nextY) / 2),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw dot
        drawCircle(
            color = Color.White,
            radius = (if (isEvent) 2.dp else 3.5.dp).toPx(),
            center = Offset(centerX, currentY)
        )
    }
}

@Composable
fun WeatherDetailsPanel(state: HomeUiState) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Current Details",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid-like layout for details
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            WeatherDetailRow(
                icon = Icons.Default.Thermostat,
                label = "Feels Like",
                value = state.feelsLike
            )
            WeatherDetailRow(
                icon = Icons.Default.WaterDrop,
                label = "Humidity",
                value = state.humidity
            )
            WeatherDetailRow(
                icon = Icons.Default.Cloud,
                label = "Precipitation",
                value = state.precipitationProb
            )
            WeatherDetailRow(
                icon = Icons.Default.WbSunny,
                label = "UV Index",
                value = state.uvIndex
            )
            WeatherDetailRow(
                icon = Icons.Default.Air,
                label = "Wind Speed",
                value = state.windSpeed
            )
            WeatherDetailRow(
                icon = Icons.Default.Explore,
                label = "Wind Direction",
                value = state.windDirection
            )
            WeatherDetailRow(
                icon = Icons.Default.Thunderstorm,
                label = "Thunderstorm",
                value = state.thunderstormProb
            )
        }
    }
}

@Composable
fun WeatherDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HorizontalWeatherForecast(
    forecasts: List<HourlyForecastUiState>,
    modifier: Modifier = Modifier
) {
    // Container transparan agar rapi
    Box(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight() // Tinggi area forecast
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f)) // Background tipis
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (forecasts.isEmpty()) {
            Text(
                text = "Loading weather forecast...",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                modifier = Modifier.padding(12.dp)
            )
        } else {
            // Find valid temperatures for the chart line (skipping event items)
            val weatherOnlyTemps = forecasts.filter { !it.isEvent }.map { parseTemperature(it.temperature) }
            val minTemp = weatherOnlyTemps.minOrNull() ?: 0
            val maxTemp = weatherOnlyTemps.maxOrNull() ?: 0

            // Pre-calculate effective temperatures using INDEX-based linear interpolation for visual straightness
            val effectiveTemps = FloatArray(forecasts.size)

            // Step 1: Set anchor points (Weather)
            for (i in forecasts.indices) {
                if (!forecasts[i].isEvent) {
                    effectiveTemps[i] = parseTemperature(forecasts[i].temperature).toFloat()
                }
            }

            // Step 2: Interpolate event points based on index position (visual median)
            for (i in forecasts.indices) {
                if (forecasts[i].isEvent) {
                    var prevIdx = -1
                    for (j in i - 1 downTo 0) {
                        if (!forecasts[j].isEvent) { prevIdx = j; break }
                    }

                    var nextIdx = -1
                    for (j in i + 1 until forecasts.size) {
                        if (!forecasts[j].isEvent) { nextIdx = j; break }
                    }

                    if (prevIdx != -1 && nextIdx != -1) {
                        // Calculate height based on index position to ensure a straight visual diagonal
                        val dist = (nextIdx - prevIdx).toFloat()
                        val pos = (i - prevIdx).toFloat()
                        val y1 = effectiveTemps[prevIdx]
                        val y2 = effectiveTemps[nextIdx]
                        effectiveTemps[i] = y1 + (pos / dist) * (y2 - y1)
                    } else if (prevIdx != -1) {
                        effectiveTemps[i] = effectiveTemps[prevIdx]
                    } else if (nextIdx != -1) {
                        effectiveTemps[i] = effectiveTemps[nextIdx]
                    } else {
                        effectiveTemps[i] = 0f
                    }
                }
            }

            // LazyRow untuk scroll ke samping (Horizontal)
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                itemsIndexed(forecasts) { index, forecast ->
                    val currentTemp = effectiveTemps[index]
                    val prevTemp = if (index > 0) effectiveTemps[index - 1] else null
                    val nextTemp = if (index < forecasts.size - 1) effectiveTemps[index + 1] else null

                    HorizontalForecastItem(
                        forecast = forecast,
                        currentTemp = currentTemp,
                        prevTemp = prevTemp,
                        nextTemp = nextTemp,
                        minTemp = minTemp,
                        maxTemp = maxTemp
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalForecastItem(
    forecast: HourlyForecastUiState,
    currentTemp: Float,
    prevTemp: Float?,
    nextTemp: Float?,
    minTemp: Int,
    maxTemp: Int
) {
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Jam (Time)
        Text(
            text = forecast.time,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Ikon (Cuaca atau Event)
        if (forecast.isEvent) {
            val icon = if (forecast.iconUrl == "WS_OPEN") Icons.Default.Storefront else Icons.Default.DoorBack
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        } else {
            AsyncImage(
                model = forecast.iconUrl,
                contentDescription = "Icon",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Suhu atau Label Event
        Text(
            text = forecast.eventLabel ?: forecast.temperature,
            style = if (forecast.isEvent) MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            else MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Diagram Suhu
        TemperatureChartSegment(
            currentTemp = currentTemp,
            prevTemp = prevTemp,
            nextTemp = nextTemp,
            minTemp = minTemp,
            maxTemp = maxTemp,
            isEvent = forecast.isEvent
        )
    }
}

@Preview(showBackground = true, name = "Weather Details")
@Composable
fun WeatherDetailsPanelPreview() {
    Box(
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF2C3E50), Color(0xFF4A6DA7))
            )
        )
    ) {
        WeatherDetailsPanel(
            state = HomeUiState(
                feelsLike = "26°C",
                humidity = "80%",
                uvIndex = "5",
                precipitationProb = "20%",
                windSpeed = "12 km/h",
                windDirection = "North",
                thunderstormProb = "5%"
            )
        )
    }
}

@Preview(showBackground = true, name = "Horizontal Forecast with Events")
@Composable
fun HorizontalWeatherForecastEventsPreview() {
    val sampleForecasts = listOf(
        HourlyForecastUiState("09:00", "https://openweathermap.org/img/wn/01d@2x.png", "26°"),
        HourlyForecastUiState("09:00", "WS_OPEN", "--", isEvent = true, eventLabel = "Open"),
        HourlyForecastUiState("10:00", "https://openweathermap.org/img/wn/02d@2x.png", "32°"),
        HourlyForecastUiState("17:00", "https://openweathermap.org/img/wn/04d@2x.png", "24°"),
        HourlyForecastUiState("17:00", "WS_CLOSE", "--", isEvent = true, eventLabel = "Closed"),
        //HourlyForecastUiState("18:00", "https://openweathermap.org/img/wn/04n@2x.png", "25°")
    )

    Box(
        modifier = Modifier
            .background(Color(0xFF34495E))
            .wrapContentWidth()
            .padding(20.dp)
    ) {
        HorizontalWeatherForecast(forecasts = sampleForecasts)
    }
}
