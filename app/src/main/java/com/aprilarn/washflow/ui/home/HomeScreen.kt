package com.aprilarn.washflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.ui.components.Button
import com.example.app.ui.components.StatusCard
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEnterDataClick: () -> Unit,
    onStatusCardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left panel: Weather Forecast
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                WeatherForecastPanel(forecasts = state.hourlyForecasts)
            }

            // Right panel: main content
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Greeting and weather
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Column untuk teks
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = state.greeting,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontSize = 42.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.weather} | ${state.temperature}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontSize = 22.sp,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Tampilkan ikon jika URL tidak kosong
                    if (state.weatherIconUrl.isNotBlank()) {
                        AsyncImage(
                            model = state.weatherIconUrl,
                            contentDescription = "Current Weather Icon",
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }

                Text(
                    text = state.recommendation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayBlue,
                        fontStyle = FontStyle.Italic
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
                Spacer(modifier = Modifier)

                Spacer(modifier = Modifier.weight(1f))

                // Status cards
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    // --- TERAPKAN onClick DI SINI ---
                    StatusCard(text = "In Queue", count = state.inQueue, onClick = onStatusCardClick)
                    StatusCard(text = "On Process", count = state.onProcess, onClick = onStatusCardClick)
                    StatusCard(text = "Done", count = state.done, onClick = onStatusCardClick)
                }

//                Spacer(modifier = Modifier.height(18.dp))
//
//                // Enter data button
//                Button(text = "Enter Data", onClick = onEnterDataClick)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 500)
@Composable
fun HomeScreenPreview() {
    Box(modifier = Modifier.background(
        Brush.linearGradient(
            colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
        )
    )) {
        HomeScreen(
            state = HomeUiState(
                greeting = "Good Morning!",
                weather = "Heavy Rain",
                temperature = "24°C",
                // Tambahkan URL ikon dummy untuk preview
                weatherIconUrl = "https://openweathermap.org/img/wn/10d@4x.png",
                recommendation = "pastikan semua cucian disimpan dalam ruang tertutup atau diberi pelindung untuk menghindari pakaian basah atau lembap.",
                inQueue = 14,
                onProcess = 3,
                done = 5,
                hourlyForecasts = listOf(
                    HourlyForecastUiState("13:00", "https://openweathermap.org/img/wn/10d@2x.png", "23°"),
                    HourlyForecastUiState("16:00", "https://openweathermap.org/img/wn/04d@2x.png", "22°"),
                    HourlyForecastUiState("19:00", "https://openweathermap.org/img/wn/03n@2x.png", "21°"),
                    HourlyForecastUiState("22:00", "https://openweathermap.org/img/wn/02n@2x.png", "20°")
                )
            ),
            onEnterDataClick = {},
            onStatusCardClick = {}
        )
    }
}