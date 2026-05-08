package com.aprilarn.washflow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    currentTemp: Int,
    prevTemp: Int?,
    nextTemp: Int?,
    minTemp: Int,
    maxTemp: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(40.dp)) {
        val range = (maxTemp - minTemp).coerceAtLeast(1)

        fun getY(temp: Int): Float {
            // Normalize: 0.2 to 0.8 range of height to avoid edges
            val normalized = (temp - minTemp).toFloat() / range
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
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draw line to right
        nextTemp?.let {
            val nextY = getY(it)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(centerX, currentY),
                end = Offset(size.width, (currentY + nextY) / 2),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draw dot
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(centerX, currentY)
        )
    }
}

@Composable
fun WeatherForecastPanel(forecasts: List<HourlyForecastUiState>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Weather Forecast",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (forecasts.isEmpty()) {
            Text(
                text = "loading weather forecast...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                forecasts.forEach { forecast ->
                    HourlyForecastItem(forecast = forecast)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(forecast: HourlyForecastUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = forecast.time,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        AsyncImage(
            model = forecast.iconUrl,
            contentDescription = "Weather Icon",
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = forecast.temperature,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
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
            val temps = forecasts.map { parseTemperature(it.temperature) }
            val minTemp = temps.minOrNull() ?: 0
            val maxTemp = temps.maxOrNull() ?: 0

            // LazyRow untuk scroll ke samping (Horizontal)
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                itemsIndexed(forecasts) { index, forecast ->
                    val currentTemp = temps[index]
                    val prevTemp = if (index > 0) temps[index - 1] else null
                    val nextTemp = if (index < forecasts.size - 1) temps[index + 1] else null

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
    currentTemp: Int,
    prevTemp: Int?,
    nextTemp: Int?,
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

        // 2. Ikon Cuaca
        AsyncImage(
            model = forecast.iconUrl,
            contentDescription = "Icon",
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Suhu
        Text(
            text = forecast.temperature,
            style = MaterialTheme.typography.bodyLarge.copy(
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
            maxTemp = maxTemp
        )
    }
}

@Preview(showBackground = true, name = "Horizontal Forecast")
@Composable
fun HorizontalWeatherForecastPreview() {
    val sampleForecasts = listOf(
        HourlyForecastUiState("18:00", "https://openweathermap.org/img/wn/10d@2x.png", "27°"),
        HourlyForecastUiState("19:00", "https://openweathermap.org/img/wn/10d@2x.png", "26°"),
        HourlyForecastUiState("20:00", "https://openweathermap.org/img/wn/10d@2x.png", "26°"),
        HourlyForecastUiState("21:00", "https://openweathermap.org/img/wn/10d@2x.png", "26°"),
        HourlyForecastUiState("22:00", "https://openweathermap.org/img/wn/04n@2x.png", "26°"),
        HourlyForecastUiState("23:00", "https://openweathermap.org/img/wn/04n@2x.png", "26°"),
        HourlyForecastUiState("00:00", "https://openweathermap.org/img/wn/04n@2x.png", "25°")
    )

    Box(
        modifier = Modifier
            .background(Color(0xFF34495E))
            .padding(20.dp)
    ) {
        HorizontalWeatherForecast(forecasts = sampleForecasts)
    }
}

@Preview(showBackground = true, name = "Panel with Data")
@Composable
fun WeatherForecastPanelPreview() {
    // Data dummy untuk preview
    val sampleForecasts = listOf(
        HourlyForecastUiState("19:00", "https://openweathermap.org/img/wn/10d@2x.png", "25°"),
        HourlyForecastUiState("22:00", "https://openweathermap.org/img/wn/04n@2x.png", "23°"),
        HourlyForecastUiState("01:00", "https://openweathermap.org/img/wn/02n@2x.png", "22°"),
        HourlyForecastUiState("04:00", "https://openweathermap.org/img/wn/01n@2x.png", "21°")
    )

    // Memberi background agar teks putih terlihat jelas di preview
    Box(
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF2C3E50), Color(0xFF4A6DA7))
            )
        )
    ) {
        WeatherForecastPanel(forecasts = sampleForecasts)
    }
}

@Preview(showBackground = true, name = "Panel Loading")
@Composable
fun WeatherForecastPanelEmptyPreview() {
    Box(
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF2C3E50), Color(0xFF4A6DA7))
            )
        )
    ) {
        // Mengirimkan list kosong untuk mensimulasikan kondisi loading
        WeatherForecastPanel(forecasts = emptyList())
    }
}