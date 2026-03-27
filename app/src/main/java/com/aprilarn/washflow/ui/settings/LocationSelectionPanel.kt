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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionPanel(
    onLocationSelected: (lat: Double, lon: Double) -> Unit,
    onBackClick: () -> Unit,
    isPreview: Boolean = false // Parameter tambahan untuk keperluan preview
) {
    val context = LocalContext.current

    // Cegah inisialisasi FusedLocationProviderClient jika sedang di mode Preview
    val fusedLocationClient: FusedLocationProviderClient? = if (!isPreview) {
        remember { LocationServices.getFusedLocationProviderClient(context) }
    } else {
        null
    }

    var showMap by remember { mutableStateOf(false) }

    // Launcher untuk meminta izin lokasi saat tombol "Current Location" ditekan
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationSelected(location.latitude, location.longitude)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.clip(RoundedCornerShape(24.dp)),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Set Location") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (!showMap) {
                // TAMPILAN OPSI
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (!isPreview) {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use Current Location")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("OR", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showMap = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose on Map")
                    }
                }
            } else {
                // TAMPILAN PETA (Google Maps) - Jangan render peta jika isPreview true
                if (!isPreview) {
                    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(-2.357500, 118.203056), 5f) // Titik awal (Jawa Tengah)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.weight(1f),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng -> selectedLatLng = latLng } // Titik pin manual
                        ) {
                            selectedLatLng?.let { Marker(state = MarkerState(position = it)) }
                        }

                        // Tombol konfirmasi lokasi peta
                        Button(
                            onClick = {
                                selectedLatLng?.let {
                                    onLocationSelected(it.latitude, it.longitude)
                                }
                            },
                            enabled = selectedLatLng != null,
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
                        ) {
                            Text("Confirm Location")
                        }
                    }
                } else {
                    // Tampilan dummy pengganti peta untuk mode Preview jika dibutuhkan
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Text("Google Map tidak dapat ditampilkan di Preview", color = Color.DarkGray)
                    }
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
            onLocationSelected = { _, _, -> },
            onBackClick = {},
            isPreview = true // Wajib true agar tidak crash di IDE
        )
    }
}