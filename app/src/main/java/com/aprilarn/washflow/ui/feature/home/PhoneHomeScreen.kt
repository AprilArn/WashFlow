package com.aprilarn.washflow.ui.feature.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.WeatherBackgroundIllustration
import com.aprilarn.washflow.ui.core.components.isDarkWeatherScene
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.google.android.gms.location.LocationServices

/**
 * Wrapper Location-Aware untuk Home Screen khusus HP biasa (Batang).
 */
@Composable
fun PhoneLocationAwareHomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToManageOrder: () -> Unit,
    onLocationFetched: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE) }
    val locationMode = sharedPreferences.getString("LOCATION_MODE", "AUTO") ?: "AUTO"
    val savedLat = sharedPreferences.getFloat("STATIC_LAT", 0f).toDouble()
    val savedLon = sharedPreferences.getFloat("STATIC_LON", 0f).toDouble()

    val isStaticMode = locationMode == "STATIC" && savedLat != 0.0 && savedLon != 0.0
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            hasLocationPermission = true
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(key1 = hasLocationPermission, key2 = isStaticMode) {
        if (isStaticMode) {
            if (!homeViewModel.hasInitializedWeather) {
                homeViewModel.fetchWeatherData(savedLat, savedLon, isGps = false)
                onLocationFetched(savedLat, savedLon)
            }
        } else {
            val fineLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarseLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineLocationGranted || coarseLocationGranted) {
                hasLocationPermission = true
                if (!homeViewModel.hasInitializedWeather) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            homeViewModel.fetchWeatherData(location.latitude, location.longitude, isGps = true)
                            onLocationFetched(location.latitude, location.longitude)
                        } else {
                            val fallbackLat = sharedPreferences.getFloat("LAST_LAT", 0f).toDouble()
                            val fallbackLon = sharedPreferences.getFloat("LAST_LON", 0f).toDouble()
                            if (fallbackLat != 0.0 || fallbackLon != 0.0) {
                                homeViewModel.fetchWeatherData(fallbackLat, fallbackLon, isGps = true)
                                onLocationFetched(fallbackLat, fallbackLon)
                            }
                        }
                    }
                }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    if (isStaticMode || hasLocationPermission) {
        val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
        PhoneHomeScreen(
            state = uiState,
            onStatusCardClick = onNavigateToManageOrder
        )
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Text(
                    text = stringResource(R.string.home_location_permission_message),
                    color = GrayBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

/**
 * Tampilan Home 1-Kolom Vertikal khusus HP biasa (Batang / Portrait).
 * Menyusun Sapaan & Cuaca Utama, Pill Rekomendasi, 3 StatusCard Ringkas,
 * Forecast Per Jam, dan Kartu Detail Cuaca secara vertikal agar mudah di-scroll satu tangan.
 */
@Composable
fun PhoneHomeScreen(
    state: HomeUiState,
    onStatusCardClick: () -> Unit
) {
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
        label = "phoneHomeLocationColor"
    )
    val headlineShadow = remember {
        Shadow(
            color = Color(0xFF2B3F63).copy(alpha = 0.24f),
            offset = Offset(0f, 3f),
            blurRadius = 8f
        )
    }

    val greetingText = state.greetingResId?.let { stringResource(it) } ?: state.greeting
    val weatherText = state.weatherFallbackResId?.let { stringResource(it) } ?: state.weather
    val locationText = state.locationNameResId?.let { stringResource(it) } ?: state.locationName
    val recommendationText = state.recommendationResId?.let { stringResource(it) } ?: state.recommendation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Sapaan & Cuaca Saat Ini (ruang vertikal lega agar ilustrasi cuaca & sapaan tampil luas)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontSize = 28.sp,
                            fontStyle = FontStyle.Italic,
                            shadow = headlineShadow
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$weatherText | ${state.temperature}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            shadow = headlineShadow
                        )
                    )
                }

                if (state.weatherIconUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = state.weatherIconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = if (state.isGpsLocation) Icons.Rounded.LocationOn else Icons.Rounded.LocationOff,
                    contentDescription = null,
                    tint = locationContentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = locationContentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. Pill Rekomendasi
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates,
                    contentDescription = null,
                    tint = GrayBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = recommendationText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                )
            }
        }

        // 3. Baris 3 StatusCard Ringkas untuk HP (On Queue, On Process, Done)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PhoneCompactStatusCard(
                title = stringResource(R.string.home_status_on_queue),
                subTitle = stringResource(R.string.home_status_on_queue_sub),
                count = state.inQueue,
                onClick = onStatusCardClick,
                modifier = Modifier.weight(1f)
            )
            PhoneCompactStatusCard(
                title = stringResource(R.string.home_status_on_process),
                subTitle = stringResource(R.string.home_status_on_process_sub),
                count = state.onProcess,
                onClick = onStatusCardClick,
                modifier = Modifier.weight(1f)
            )
            PhoneCompactStatusCard(
                title = stringResource(R.string.home_status_done),
                subTitle = stringResource(R.string.home_status_done_sub),
                count = state.done,
                onClick = onStatusCardClick,
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Forecast Per Jam & Deadline Hari Ini
        HorizontalWeatherForecast(
            forecasts = state.hourlyForecasts,
            isLoading = state.isLoading
        )

        // 5. Panel Detail Cuaca di Bagian Bawah
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            WeatherDetailsPanel(state = state)
        }
    }
}

@Composable
private fun PhoneCompactStatusCard(
    title: String,
    subTitle: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GrayBlue.copy(alpha = 0.65f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(name = "Phone Home", device = Devices.PHONE, showBackground = true)
@Composable
private fun PhoneHomeScreenPreview() {
    val previewState = HomeUiState(
        isLoading = false,
        greeting = "Good Evening!",
        greetingResId = R.string.home_greeting_evening,
        weather = "Cloudy",
        weatherFallbackResId = null,
        temperature = "25°C",
        locationName = "Boyolali, Central Java",
        locationNameResId = null,
        recommendationResId = R.string.home_recommendation_default,
        inQueue = 3,
        onProcess = 2,
        done = 4,
        feelsLike = "28°C",
        humidity = "89%",
        precipitationProb = "20%",
        uvIndex = "0",
        windSpeed = "8 km/h",
        windDirection = "SW",
        thunderstormProb = "40%",
        hourlyForecasts = listOf(
            HourlyForecastUiState("21:00", "", "25°"),
            HourlyForecastUiState("22:00", "", "25°"),
            HourlyForecastUiState("23:00", "", "24°")
        )
    )
    Box(modifier = Modifier.fillMaxSize()) {
        WeatherBackgroundIllustration(
            weather = previewState.weather,
            greeting = previewState.greeting,
            modifier = Modifier.matchParentSize()
        )
        Box(modifier = Modifier.padding(16.dp)) {
            PhoneHomeScreen(
                state = previewState,
                onStatusCardClick = {}
            )
        }
    }
}
