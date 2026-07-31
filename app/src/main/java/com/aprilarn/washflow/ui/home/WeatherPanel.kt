package com.aprilarn.washflow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.components.shimmerModifier
import androidx.compose.foundation.shape.CircleShape
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
    isEvent: Boolean = false,
    isVisible: Boolean = true
) {
    if (!isVisible) {
        Spacer(modifier = modifier
            .fillMaxWidth()
            .height(40.dp))
        return
    }

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
            radius = (if (isEvent) 0.dp else 3.5.dp).toPx(),
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
                icon = Icons.Rounded.Thermostat,
                label = "Feels Like",
                value = state.feelsLike,
                isLoading = state.isLoading
            )
            WeatherDetailRow(
                icon = Icons.Rounded.WaterDrop,
                label = "Humidity",
                value = state.humidity,
                isLoading = state.isLoading
            )
            WeatherDetailRow(
                icon = Icons.Rounded.Cloud,
                label = "Precipitation",
                value = state.precipitationProb,
                isLoading = state.isLoading
            )
            WeatherDetailRow(
                icon = Icons.Rounded.WbSunny,
                label = "UV Index",
                value = state.uvIndex,
                isLoading = state.isLoading
            )
            WeatherDetailRow(
                icon = Icons.Rounded.Air,
                label = "Wind Speed",
                value = state.windSpeed,
                isLoading = state.isLoading
            )
            WeatherDetailRow(
                icon = Icons.Rounded.Explore,
                label = "Wind Direction",
                value = state.windDirection,
                isLoading = state.isLoading
            )
            WeatherDetailRow(
                icon = Icons.Rounded.Thunderstorm,
                label = "Thunderstorm",
                value = state.thunderstormProb,
                isLoading = state.isLoading
            )
        }
    }
}

@Composable
fun WeatherDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLoading: Boolean = false
) {
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
            Box(
                modifier = Modifier.height(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerModifier()
                    )
                } else {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalWeatherForecast(
    modifier: Modifier = Modifier,
    forecasts: List<HourlyForecastUiState>,
    isLoading: Boolean = false
) {
    // Container transparan agar rapi
    Box(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight() // Tinggi area forecast
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f)) // Background tipis
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            HorizontalWeatherForecastShimmer()
        } else if (forecasts.isEmpty()) {
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

                    val firstWeatherIdx = forecasts.indexOfFirst { !it.isEvent }
                    val lastWeatherIdx = forecasts.indexOfLast { !it.isEvent }
                    val showChart = firstWeatherIdx != -1 && index >= firstWeatherIdx && index <= lastWeatherIdx

                    HorizontalForecastItem(
                        forecast = forecast,
                        currentTemp = currentTemp,
                        prevTemp = if (index > firstWeatherIdx) prevTemp else null,
                        nextTemp = if (index < lastWeatherIdx) nextTemp else null,
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                        showChart = showChart
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalWeatherForecastShimmer() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        repeat(7) {
            HorizontalForecastItemShimmer()
        }
    }
}

@Composable
fun HorizontalForecastItemShimmer() {
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Jam (Time) Shimmer
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerModifier()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Ikon Shimmer
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .shimmerModifier()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Suhu Shimmer
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerModifier()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Diagram Suhu Shimmer (Long horizontal shimmer)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(vertical = 18.dp) // Adjust to match line height visually
                .clip(RoundedCornerShape(2.dp))
                .shimmerModifier()
        )
    }
}

@Composable
fun HorizontalForecastItem(
    forecast: HourlyForecastUiState,
    currentTemp: Float,
    prevTemp: Float?,
    nextTemp: Float?,
    minTemp: Int,
    maxTemp: Int,
    showChart: Boolean = true
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
            val icon = when (forecast.iconUrl) {
                "WS_OPEN" -> Icons.Rounded.Storefront
                "WS_CLOSE" -> Icons.Rounded.DoorBack
                "WS_DEADLINE" -> Icons.Rounded.Timer
                "WS_SUNRISE" -> Icons.Rounded.WbSunny
                "WS_SUNSET" -> Icons.Rounded.WbTwilight
                else -> Icons.Rounded.Event
            }
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
            ),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.basicMarquee()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Diagram Suhu
        TemperatureChartSegment(
            currentTemp = currentTemp,
            prevTemp = prevTemp,
            nextTemp = nextTemp,
            minTemp = minTemp,
            maxTemp = maxTemp,
            isEvent = forecast.isEvent,
            isVisible = showChart
        )
    }
}

@Preview(showBackground = true, name = "Weather Panel Loading")
@Composable
fun WeatherPanelLoadingPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Horizontal Weather Forecast Loading", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalWeatherForecast(forecasts = emptyList(), isLoading = true)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Weather Details Panel Loading", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        WeatherDetailsPanel(state = HomeUiState(isLoading = true))
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

@Preview(showBackground = true, name = "Horizontal Forecast Scenarios")
@Composable
fun HorizontalWeatherForecastScenariosPreview() {
    val scenarios = listOf(
        // Scenario 1: event -> weather -> weather -> event -> event -> weather
        listOf(
            HourlyForecastUiState("09:00", "WS_OPEN", "--", isEvent = true, eventLabel = "E1"),
            HourlyForecastUiState("10:00", "https://openweathermap.org/img/wn/01d@2x.png", "26°"),
            HourlyForecastUiState("11:00", "https://openweathermap.org/img/wn/01d@2x.png", "28°"),
            HourlyForecastUiState("12:00", "WS_DEADLINE", "--", isEvent = true, eventLabel = "E2"),
            HourlyForecastUiState("13:00", "WS_CLOSE", "--", isEvent = true, eventLabel = "E3"),
            HourlyForecastUiState("14:00", "https://openweathermap.org/img/wn/01d@2x.png", "30°"),
        ),
        // Scenario 2: Sunrise and Sunset
        listOf(
            HourlyForecastUiState("05:00", "https://openweathermap.org/img/wn/01d@2x.png", "22°"),
            HourlyForecastUiState("05:45", "WS_SUNRISE", "--", isEvent = true, eventLabel = "Sunrise"),
            HourlyForecastUiState("06:00", "https://openweathermap.org/img/wn/01d@2x.png", "23°"),
            HourlyForecastUiState("17:30", "https://openweathermap.org/img/wn/01d@2x.png", "28°"),
            HourlyForecastUiState("18:15", "WS_SUNSET", "--", isEvent = true, eventLabel = "Sunset"),
            HourlyForecastUiState("19:00", "https://openweathermap.org/img/wn/01d@2x.png", "26°"),
        )
    )

    Column(
        modifier = Modifier
            .background(Color(0xFF34495E))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        scenarios.forEachIndexed { index, forecast ->
            Text("Scenario ${index + 1}", color = Color.White)
            HorizontalWeatherForecast(forecasts = forecast, isLoading = false)
        }
    }
}

