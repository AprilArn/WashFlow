package com.aprilarn.washflow.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices

@Composable
fun LocationAwareHomePage(
    homeViewModel: HomeViewModel,
    onNavigateToManageOrder: () -> Unit,
    onLocationFetched: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE) }
    val locationMode = sharedPreferences.getString("LOCATION_MODE", "AUTO") ?: "AUTO"
    val savedLat = sharedPreferences.getFloat("STATIC_LAT", 0f). toDouble()
    val savedLon = sharedPreferences.getFloat("STATIC_LON", 0f). toDouble()

    // cek apakah user sedang menggunakan mode statis/tembak manual
    val isStaticMode = locationMode == "STATIC" && savedLat != 0.0 && savedLon != 0.0

    var hasLocationPermission by remember { mutableStateOf(false) }

    // Launcher untuk meminta izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            hasLocationPermission = true
        }
    }

    // FusedLocationProviderClient untuk mendapatkan lokasi
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Cek mode dan jalankan logika pengambilan cuaca
    LaunchedEffect(key1 = hasLocationPermission, key2 = isStaticMode) {
        if (isStaticMode) {
            // JIKA MODE STATIC: Langsung gunakan koordinat memori, tidak perlu menyalakan GPS
            if (homeViewModel.uiState.value.weather == "loading weather...") {
                homeViewModel.fetchWeatherData(savedLat, savedLon, isGps = false)
                onLocationFetched(savedLat, savedLon)
            }
        } else {
            // JIKA MODE AUTO: Cek izin dan ambil lokasi GPS saat ini
            val fineLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarseLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineLocationGranted || coarseLocationGranted) {
                hasLocationPermission = true
                if (homeViewModel.uiState.value.weather == "loading weather...") {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            homeViewModel.fetchWeatherData(location.latitude, location.longitude, isGps = true)
                            onLocationFetched(location.latitude, location.longitude)
                        }
                    }
                }
            } else {
                // Jika belum ada izin, minta ke pengguna
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // Tentukan UI yang akan ditampilkan (Jika Static, abaikan pengecekan izin lokasi)
    if (isStaticMode || hasLocationPermission) {
        val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

        HomePage(
            homeViewModel = homeViewModel,
            onEnterDataClick = {},
            onStatusCardClick = onNavigateToManageOrder
        )
    } else {
        // Tampilan jika pengguna menolak izin GPS (dan mereka belum pernah mengeset manual pin)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Aplikasi memerlukan izin lokasi untuk menampilkan data cuaca. Silakan berikan izin atau set lokasi manual di Settings.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}