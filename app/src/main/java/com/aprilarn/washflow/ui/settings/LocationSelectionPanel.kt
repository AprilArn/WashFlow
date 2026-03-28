// com/aprilarn/washflow/ui/settings/LocationSelectionScreen.kt
package com.aprilarn.washflow.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionPanel(
    onLocationSelected: (lat: Double, lon: Double) -> Unit,
    onBackClick: () -> Unit,
    isPreview: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // Untuk menjalankan animasi kamera peta

    val fusedLocationClient: FusedLocationProviderClient? = if (!isPreview) {
        remember { LocationServices.getFusedLocationProviderClient(context) }
    } else {
        null
    }

    // State untuk peta
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        // Melayang di tengah Indonesia saat pertama kali dibuka
        position = CameraPosition.fromLatLngZoom(LatLng(-2.357500, 118.203056), 5f)
    }

    // Launcher untuk meminta izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {

            // Jika diizinkan, ambil lokasi lalu terbangkan kamera ke sana
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    selectedLatLng = currentLatLng // Set pin di lokasi saat ini

                    // Terbangkan kamera (animasi)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f), // Zoom 15f agar lebih dekat
                            durationMs = 1500
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.clip(RoundedCornerShape(24.dp)),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Set Location on Map") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (!isPreview) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // PETA (Mengisi sebagian besar layar)
                    GoogleMap(
                        modifier = Modifier.weight(1f),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng -> selectedLatLng = latLng } // Klik manual untuk set pin
                    ) {
                        // Tampilkan Marker jika ada lokasi yang dipilih
                        selectedLatLng?.let { Marker(state = MarkerState(position = it)) }
                    }

                    // BOTTOM BAR AREA (Tombol)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // 1. Tombol Get Current Location (Icon Button atau Outlined)
                            OutlinedButton(
                                onClick = {
                                    // Minta izin dan ambil lokasi
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
                            }

                            // 2. Tombol Confirm Location (Primary Button)
                            Button(
                                onClick = {
                                    selectedLatLng?.let {
                                        onLocationSelected(it.latitude, it.longitude)
                                    }
                                },
                                enabled = selectedLatLng != null,
                                modifier = Modifier
                                    .weight(1f) // Agar tombol Confirm mengambil sisa ruang
                                    .height(50.dp)
                            ) {
                                Text("Confirm Location")
                            }
                        }
                    }
                }
            } else {
                // Mode Preview
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Text("Google Map (Preview Mode)", color = Color.DarkGray)
                }
            }
        }
    }
}

// --- FUNGSI PREVIEW ---
@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
fun LocationSelectionPanelPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        LocationSelectionPanel(
            onLocationSelected = { _, _ -> },
            onBackClick = {},
            isPreview = true
        )
    }
}