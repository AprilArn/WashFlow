package com.aprilarn.washflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

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