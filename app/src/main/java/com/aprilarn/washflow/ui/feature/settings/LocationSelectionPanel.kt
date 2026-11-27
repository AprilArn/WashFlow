package com.aprilarn.washflow.ui.feature.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.WashFlowDialogActionRow
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
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

// --- Fungsi Bantuan untuk Mendapatkan Teks Alamat dari Koordinat (Reverse Geocoding) ---
suspend fun getAddressFromLatLng(context: Context, latLng: LatLng): String? {
    return try {
        val geocoder = Geocoder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCoroutine { cont ->
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                    cont.resume(addresses.firstOrNull()?.getAddressLine(0))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.firstOrNull()?.getAddressLine(0)
        }
    } catch (e: Exception) {
        null
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionPanel(
    onLocationSelected: (lat: Double, lon: Double, isGps: Boolean) -> Unit,
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
    var selectedAddressName by remember { mutableStateOf<String?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-2.357500, 118.203056), 5f)
    }

    var isGpsPin by remember { mutableStateOf(false) }
    var gpsTriggerMode by remember { mutableStateOf("AUTO") }

    var showAutoDialog by remember { mutableStateOf(false) }

    // State untuk Search Bar
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }

    // Efek untuk memantau ketikan user dan mencari alamat
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(500) // Debounce
            searchResults = searchLocation(context, searchQuery)
        } else {
            searchResults = emptyList()
        }
    }

    val searchingAddressText = stringResource(R.string.settings_map_searching_address)
    val addressNotFoundText = stringResource(R.string.settings_map_address_not_found)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    selectedLatLng = currentLatLng
                    isGpsPin = (gpsTriggerMode == "AUTO")
                    selectedAddressName = searchingAddressText

                    coroutineScope.launch {
                        // Terbangkan kamera
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f),
                            durationMs = 1500
                        )
                        // Ambil nama jalan dari GPS
                        val addressText = getAddressFromLatLng(context, currentLatLng)
                        selectedAddressName = addressText ?: addressNotFoundText
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
                title = { Text(stringResource(R.string.settings_map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back))
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
                        // Google Map
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                selectedLatLng = latLng
                                isGpsPin = false
                                focusManager.clearFocus()
                                searchResults = emptyList()
                                isSearchExpanded = false

                                // Mulai proses pencarian nama alamat untuk pin yang baru dijatuhkan
                                selectedAddressName = searchingAddressText
                                coroutineScope.launch {
                                    val addressText = getAddressFromLatLng(context, latLng)
                                    selectedAddressName = addressText ?: addressNotFoundText
                                }
                            }
                        ) {
                            selectedLatLng?.let { Marker(state = MarkerState(position = it)) }
                        }

                        // Auto Mode Indicator (Kanan Atas)
                        if (isGpsPin) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 8.dp,
                                //border = BorderStroke(1.dp, GrayBlue.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Pulse Animation for the Green Dot
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "alpha"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50).copy(alpha = alpha))
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_map_auto_mode),
                                        color = GrayBlue,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Floating Search Bar / Icon (Kiri Atas)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .then(if (isSearchExpanded) Modifier.fillMaxWidth(0.4f) else Modifier.wrapContentSize())
                        ) {
                            if (!isSearchExpanded) {
                                // HANYA IKON BULAT (MINIMIZED)
                                Surface(
                                    shape = CircleShape,
                                    // shadowElevation = 6.dp,
                                    color = Color.White,
                                    modifier = Modifier.size(56.dp),
                                    onClick = { isSearchExpanded = true } // Buka bar pencarian saat diklik
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.common_search), tint = Color.DarkGray)
                                    }
                                }
                            } else {
                                // SEARCH BAR UTUH (EXPANDED)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(12.dp)),
                                        placeholder = { Text(stringResource(R.string.settings_map_search_placeholder)) },
                                        leadingIcon = {
                                            // Tombol back untuk menutup search bar
                                            IconButton(onClick = {
                                                isSearchExpanded = false
                                                searchQuery = ""
                                                searchResults = emptyList()
                                                focusManager.clearFocus()
                                            }) {
                                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_close))
                                            }
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = {
                                                    searchQuery = ""
                                                    searchResults = emptyList()
                                                }) {
                                                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.common_clear_search))
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
                                                .heightIn(max = 250.dp),
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
                                                                // Jika alamat dipilih:
                                                                searchQuery = addressName
                                                                searchResults = emptyList()
                                                                focusManager.clearFocus()
                                                                isSearchExpanded = false // Tutup otomatis agar peta terlihat
                                                                isGpsPin = false

                                                                // Set text pin address dengan nama yang dicari
                                                                selectedAddressName = addressName

                                                                // Set pin & terbangkan kamera
                                                                val latLng = LatLng(address.latitude, address.longitude)
                                                                selectedLatLng = latLng
                                                                coroutineScope.launch {
                                                                    cameraPositionState.animate(
                                                                        CameraUpdateFactory.newLatLngZoom(latLng, 18f),
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
                        }

                        // Floating Address Text (Kiri Bawah) - MUNCUL KETIKA PIN ADA
                        selectedAddressName?.let { address ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                                    .fillMaxWidth(0.4f) // Maksimal 40% layar agar tidak mentok
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = address,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3, // Batasi 3 baris jika terlalu panjang
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // --- BAGIAN BAWAH: 3 TOMBOL (AUTO, STATIC, CONFIRM) ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            // Menggunakan spasi yang lebih kecil agar 3 tombol muat
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // 1. Tombol Auto (Dinamis)
                            OutlinedButton(
                                onClick = {
                                    // Tampilkan dialog konfirmasi
                                    showAutoDialog = true
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(stringResource(R.string.settings_map_auto_button), color = GrayBlue)
                            }

                            // 2. Tombol Sniper (Statis / Lock Coordinate)
                            OutlinedButton(
                                onClick = {
                                    gpsTriggerMode = "STATIC"
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Rounded.MyLocation, contentDescription = stringResource(R.string.settings_map_current_location_cd), tint = GrayBlue)
                            }

                            // 3. Tombol Confirm
                            Button(
                                onClick = {
                                    selectedLatLng?.let {
                                        onLocationSelected(it.latitude, it.longitude, isGpsPin)
                                    }
                                },
                                enabled = selectedLatLng != null,
                                modifier = Modifier
                                    .weight(1f) // Ambil sisa lebar
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrayBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(stringResource(R.string.common_confirm))
                            }
                        }
                    }
                }

                // --- DIALOG KONFIRMASI MODE AUTO ---
                if (showAutoDialog) {
                    Dialog(onDismissRequest = { showAutoDialog = false }) {
                        WashFlowDialogSurface {
                            WashFlowDialogHeader(
                                icon = Icons.Rounded.MyLocation,
                                iconBgColor = PastelBlueIconBg,
                                iconTint = PastelBlue,
                                title = stringResource(R.string.settings_map_auto_dialog_title),
                                subtitle = stringResource(R.string.settings_map_auto_dialog_message)
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            WashFlowDialogActionRow(
                                dismissText = stringResource(R.string.common_cancel),
                                onDismiss = { showAutoDialog = false },
                                confirmText = stringResource(R.string.settings_map_enable),
                                onConfirm = {
                                    showAutoDialog = false
                                    gpsTriggerMode = "AUTO"
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.settings_map_preview), color = Color.DarkGray)
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
            onLocationSelected = { _, _, _-> },
            onBackClick = {},
            isPreview = false
        )
    }
}
