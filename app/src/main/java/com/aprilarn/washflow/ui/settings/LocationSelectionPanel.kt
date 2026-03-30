// com/aprilarn/washflow/ui/settings/LocationSelectionScreen.kt
package com.aprilarn.washflow.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// --- Fungsi Bantuan untuk Pencarian Alamat (Geocoder) ---
suspend fun searchLocation(context: Context, query: String): List<Address> {
    return try {
        val geocoder = Geocoder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCoroutine { cont ->
                geocoder.getFromLocationName(query, 5) { addresses ->
                    cont.resume(addresses)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, 5) ?: emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionPanel(
    onLocationSelected: (lat: Double, lon: Double) -> Unit,
    onBackClick: () -> Unit,
    isPreview: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val fusedLocationClient: FusedLocationProviderClient? = if (!isPreview) {
        remember { LocationServices.getFusedLocationProviderClient(context) }
    } else {
        null
    }

    // State untuk peta
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-2.357500, 118.203056), 5f)
    }

    // State untuk Search Bar
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }

    // Efek untuk memantau ketikan user dan mencari alamat
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(500) // Debounce: Tunggu user berhenti mengetik selama 500ms
            searchResults = searchLocation(context, searchQuery)
        } else {
            searchResults = emptyList() // Kosongkan hasil jika ketikan terlalu pendek
        }
    }

    // Launcher untuk meminta izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    selectedLatLng = currentLatLng
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
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

                    // --- BAGIAN ATAS: PETA & FLOATING SEARCH BAR ---
                    Box(modifier = Modifier.weight(1f)) {
                        // 1. Google Map (Berada di layar paling dasar)
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                selectedLatLng = latLng
                                focusManager.clearFocus() // Sembunyikan keyboard jika peta disentuh
                                searchResults = emptyList() // Sembunyikan dropdown
                            }
                        ) {
                            selectedLatLng?.let { Marker(state = MarkerState(position = it)) }
                        }

                        // 2. Floating Search Bar (Melayang di atas peta, kiri atas)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .fillMaxWidth(0.9f) // Mengambil 90% lebar layar agar terlihat mengambang
                        ) {
                            // Input Text
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                placeholder = { Text("Search here...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // Dropdown Hasil Pencarian
                            if (searchResults.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .heightIn(max = 250.dp), // Batasi tinggi maksimal dropdown
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 8.dp,
                                    color = Color.White
                                ) {
                                    LazyColumn {
                                        items(searchResults) { address ->
                                            val addressName = address.getAddressLine(0) ?: address.featureName
                                            Text(
                                                text = addressName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        // Jika alamat diklik:
                                                        searchQuery = addressName // Set teks di search bar
                                                        searchResults = emptyList() // Tutup dropdown
                                                        focusManager.clearFocus() // Tutup keyboard

                                                        // Set pin & terbangkan kamera
                                                        val latLng = LatLng(address.latitude, address.longitude)
                                                        selectedLatLng = latLng
                                                        coroutineScope.launch {
                                                            cameraPositionState.animate(
                                                                CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                                                durationMs = 1500
                                                            )
                                                        }
                                                    }
                                                    .padding(16.dp),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- BAGIAN BAWAH: TOMBOL KONFIRMASI ---
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
                            OutlinedButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
                            }

                            Button(
                                onClick = {
                                    selectedLatLng?.let {
                                        onLocationSelected(it.latitude, it.longitude)
                                    }
                                },
                                enabled = selectedLatLng != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Confirm Location")
                            }
                        }
                    }
                }
            } else {
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