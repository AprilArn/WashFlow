package com.aprilarn.washflow.ui.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.StatusCard
import com.aprilarn.washflow.ui.core.components.isDarkWeatherScene
import com.aprilarn.washflow.ui.core.theme.GrayBlue

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEnterDataClick: () -> Unit,
    onStatusCardClick: () -> Unit
) {
    val leftPanelShape = RoundedCornerShape(24.dp)
    val isDarkBg = remember(state.weather, state.greeting, state.weatherType, state.isDaytime) {
        isDarkWeatherScene(
            weatherText = state.weather,
            greetingText = state.greeting,
            weatherType = state.weatherType,
            isDaytime = state.isDaytime
        )
    }
    val locationContentColor by animateColorAsState(
        targetValue = if (isDarkBg) Color(0xFFDCE8FF) else Color(0xFF3E4B73),
        animationSpec = tween(durationMillis = 700),
        label = "homeLocationContentColor"
    )
    val headlineShadow = remember {
        Shadow(
            color = Color(0xFF2B3F63).copy(alpha = 0.24f),
            offset = Offset(0f, 3f),
            blurRadius = 8f
        )
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left panel: Weather Details dengan kartu putih solid seperti StatusCard
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = leftPanelShape,
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            WeatherDetailsPanel(state = state)
        }

        // Right panel: main content
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1.2f))

            val density = LocalDensity.current
            var weatherHeaderWidth by remember { mutableIntStateOf(0) }

            val greetingText = state.greetingResId?.let { stringResource(it) } ?: state.greeting
            val weatherText = state.weatherFallbackResId?.let { stringResource(it) } ?: state.weather
            val locationText = state.locationNameResId?.let { stringResource(it) } ?: state.locationName
            val recommendationText = state.recommendationResId?.let { stringResource(it) } ?: state.recommendation

            // Greeting and weather
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.onSizeChanged { size ->
                    weatherHeaderWidth = size.width
                }
            ) {
                // Column untuk teks sapaan & cuaca
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontSize = 42.sp,
                            fontStyle = FontStyle.Italic,
                            shadow = headlineShadow
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$weatherText | ${state.temperature}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            shadow = headlineShadow
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (state.weatherIconUrl.isNotBlank()) {
                    AsyncImage(
                        model = state.weatherIconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .then(
                        if (weatherHeaderWidth > 0) {
                            Modifier.width(with(density) { weatherHeaderWidth.toDp() })
                        } else {
                            Modifier.wrapContentWidth()
                        }
                    )
            ) {
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = locationContentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    ),
                    textAlign = TextAlign.Right,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (state.isGpsLocation) Icons.Rounded.LocationOn else Icons.Rounded.LocationOff,
                    contentDescription = null,
                    tint = locationContentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // Weather tip pill
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TipsAndUpdates,
                        contentDescription = null,
                        tint = GrayBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = recommendationText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.9f))

            // Tampilkan Forecast Horizontal di sini
            HorizontalWeatherForecast(
                forecasts = state.hourlyForecasts,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.weight(1f))

            // Status cards
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                StatusCard(text = stringResource(R.string.home_status_on_queue), count = state.inQueue, onClick = onStatusCardClick)
                StatusCard(text = stringResource(R.string.home_status_on_process), count = state.onProcess, onClick = onStatusCardClick)
                StatusCard(text = stringResource(R.string.home_status_done), count = state.done, onClick = onStatusCardClick)
            }

            Spacer(modifier = Modifier.weight(1.2f))
        }
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 500)
@Composable
fun HomeScreenPreview() {
    val previewState = HomeUiState(
        isLoading = false,
        greeting = "Good Evening!",
        greetingResId = R.string.home_greeting_evening,
        weather = "Cloudy",
        weatherFallbackResId = null,
        temperature = "25°C",
        weatherIconUrl = "https://openweathermap.org/img/wn/04d@4x.png",
        locationName = "Boyolali, Central Java",
        locationNameResId = null,
        isGpsLocation = false,
        recommendationResId = R.string.home_recommendation_default,
        inQueue = 3,
        onProcess = 2,
        done = 4,
        feelsLike = "28°C",
        humidity = "89%",
        precipitationProb = "20%",
        uvIndex = "0",
        windSpeed = "8 km/h",
        windDirection = "SOUTH_SOUTHWEST (214°)",
        thunderstormProb = "40%",
        hourlyForecasts = listOf(
            HourlyForecastUiState("21:00", "https://openweathermap.org/img/wn/10d@2x.png", "25°"),
            HourlyForecastUiState("22:00", "https://openweathermap.org/img/wn/10d@2x.png", "25°"),
            HourlyForecastUiState("22:26", "WS_SUNSET", "--", isEvent = true, eventLabel = "Sunset"),
            HourlyForecastUiState("23:00", "https://openweathermap.org/img/wn/10d@2x.png", "25°"),
            HourlyForecastUiState("00:00", "https://openweathermap.org/img/wn/10d@2x.png", "25°"),
            HourlyForecastUiState("01:00", "https://openweathermap.org/img/wn/10d@2x.png", "25°"),
            HourlyForecastUiState("02:00", "https://openweathermap.org/img/wn/10d@2x.png", "24°"),
            HourlyForecastUiState("03:00", "https://openweathermap.org/img/wn/10d@2x.png", "24°")
        )
    )
    Box(modifier = Modifier.fillMaxSize()) {
        com.aprilarn.washflow.ui.core.components.WeatherBackgroundIllustration(
            weather = previewState.weather,
            greeting = previewState.greeting,
            weatherType = previewState.weatherType,
            isDaytime = previewState.isDaytime,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            HomeScreen(
                state = previewState,
                onEnterDataClick = {},
                onStatusCardClick = {}
            )
        }
    }
}
