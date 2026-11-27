package com.aprilarn.washflow.ui.feature.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.adaptive.LocalWindowLayoutInfo
import com.aprilarn.washflow.ui.core.adaptive.ProvideWindowLayoutInfo
import com.aprilarn.washflow.ui.core.components.WashFlowDialogActionRow
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.components.performLightHaptic
import com.aprilarn.washflow.ui.core.theme.AlertRed
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.core.theme.SuccessGreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val DEFAULT_INDONESIA_LAT_LNG = LatLng(-2.357500, 118.203056)

// --- Pencarian Alamat (Geocoder) di Dispatchers.IO agar tidak memblokir Main Thread ---
suspend fun searchLocation(context: Context, query: String): List<Address> = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) return@withContext emptyList()
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
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
    } catch (_: Exception) {
        emptyList()
    }
}

// --- Reverse Geocoding di Dispatchers.IO ---
suspend fun getAddressFromLatLng(context: Context, latLng: LatLng): String? = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) return@withContext null
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
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
    } catch (_: Exception) {
        null
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationSelectionPanel(
    onLocationSelected: (lat: Double, lon: Double, isGps: Boolean) -> Unit,
    onBackClick: () -> Unit,
    initialLat: Double? = null,
    initialLon: Double? = null,
    initialIsAutoMode: Boolean? = null,
    isPreview: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isCompact = LocalWindowLayoutInfo.current.isPhone

    // Pre-load koordinat & mode tersimpan dari parameter atau SharedPreferences
    val resolvedInitial = remember(initialLat, initialLon, initialIsAutoMode, isPreview) {
        if (isPreview) {
            val previewLatLng = if (initialLat != null && initialLon != null) {
                LatLng(initialLat, initialLon)
            } else {
                null
            }
            Pair(previewLatLng, initialIsAutoMode ?: false)
        } else {
            val prefs = context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)
            val savedModeIsAuto = initialIsAutoMode ?: (prefs.getString("LOCATION_MODE", "AUTO") != "STATIC")
            val lat = initialLat ?: run {
                val staticLat = prefs.getFloat("STATIC_LAT", 0f).toDouble()
                val lastLat = prefs.getFloat("LAST_LAT", 0f).toDouble()
                when {
                    !savedModeIsAuto && staticLat != 0.0 -> staticLat
                    lastLat != 0.0 -> lastLat
                    staticLat != 0.0 -> staticLat
                    else -> null
                }
            }
            val lon = initialLon ?: run {
                val staticLon = prefs.getFloat("STATIC_LON", 0f).toDouble()
                val lastLon = prefs.getFloat("LAST_LON", 0f).toDouble()
                when {
                    !savedModeIsAuto && staticLon != 0.0 -> staticLon
                    lastLon != 0.0 -> lastLon
                    staticLon != 0.0 -> staticLon
                    else -> null
                }
            }
            val latLng = if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
                LatLng(lat, lon)
            } else {
                null
            }
            Pair(latLng, savedModeIsAuto && latLng != null)
        }
    }

    val fusedLocationClient: FusedLocationProviderClient? = if (!isPreview) {
        remember { LocationServices.getFusedLocationProviderClient(context) }
    } else {
        null
    }

    // State peta & lokasi terpilih
    var selectedLatLng by remember { mutableStateOf(resolvedInitial.first) }
    var selectedAddressName by remember {
        mutableStateOf<String?>(if (isPreview && resolvedInitial.first != null) "Jl. Pandanaran No. 45, Boyolali, Central Java" else null)
    }
    var isResolvingAddress by remember { mutableStateOf(false) }
    var isGpsPin by remember { mutableStateOf(resolvedInitial.second) }
    var gpsTriggerMode by remember { mutableStateOf("AUTO") }
    var isFetchingGps by remember { mutableStateOf(false) }
    var isSatelliteView by remember { mutableStateOf(false) }
    var showAutoDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = if (resolvedInitial.first != null) {
            CameraPosition.fromLatLngZoom(resolvedInitial.first!!, 16f)
        } else {
            CameraPosition.fromLatLngZoom(DEFAULT_INDONESIA_LAT_LNG, 5f)
        }
    }

    val markerState = remember {
        MarkerState(position = resolvedInitial.first ?: DEFAULT_INDONESIA_LAT_LNG)
    }

    // State Search Bar
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val searchingAddressText = stringResource(R.string.settings_map_searching_address)
    val addressNotFoundText = stringResource(R.string.settings_map_address_not_found)
    val gpsFailedText = stringResource(R.string.settings_map_gps_failed)
    val permissionDeniedText = stringResource(R.string.settings_map_permission_denied)

    // Reverse-geocode lokasi awal tersimpan saat halaman dibuka
    LaunchedEffect(resolvedInitial.first, isPreview) {
        val initialPos = resolvedInitial.first
        if (!isPreview && initialPos != null && selectedAddressName == null) {
            isResolvingAddress = true
            selectedAddressName = searchingAddressText
            val resolved = getAddressFromLatLng(context, initialPos)
            selectedAddressName = resolved ?: addressNotFoundText
            isResolvingAddress = false
        }
    }

    // Sinkronkan posisi MarkerState setiap kali selectedLatLng berubah secara programatik
    LaunchedEffect(selectedLatLng) {
        selectedLatLng?.let { target ->
            if (markerState.position != target) {
                markerState.position = target
            }
        }
    }

    // Tangani pin yang digeser (draggable) oleh user
    LaunchedEffect(markerState) {
        snapshotFlow { markerState.dragState }.collect { dragState ->
            if (dragState == DragState.END) {
                val draggedPos = markerState.position
                if (selectedLatLng != draggedPos) {
                    selectedLatLng = draggedPos
                    isGpsPin = false
                    performLightHaptic(view, context)
                    isResolvingAddress = true
                    selectedAddressName = searchingAddressText
                    val addressText = getAddressFromLatLng(context, draggedPos)
                    selectedAddressName = addressText ?: addressNotFoundText
                    isResolvingAddress = false
                }
            }
        }
    }

    // Auto-dismiss status / pesan error setelah 3.5 detik
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3500)
            statusMessage = null
        }
    }

    // Pencarian alamat dengan debounce
    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.length > 2) {
            isSearching = true
            delay(450)
            searchResults = searchLocation(context, trimmed)
            isSearching = false
            hasSearched = true
        } else {
            searchResults = emptyList()
            isSearching = false
            hasSearched = false
        }
    }

    val handleFetchedLocation: (Location) -> Unit = { location ->
        val currentLatLng = LatLng(location.latitude, location.longitude)
        selectedLatLng = currentLatLng
        isGpsPin = (gpsTriggerMode == "AUTO")
        isFetchingGps = false
        isResolvingAddress = true
        selectedAddressName = searchingAddressText
        performLightHaptic(view, context)

        coroutineScope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f),
                durationMs = 1100
            )
            val addressText = getAddressFromLatLng(context, currentLatLng)
            selectedAddressName = addressText ?: addressNotFoundText
            isResolvingAddress = false
        }
    }

    val requestFreshGpsLocation: () -> Unit = {
        val client = fusedLocationClient
        if (client == null) {
            isFetchingGps = false
        } else {
            isFetchingGps = true
            statusMessage = null
            client.lastLocation
                .addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        handleFetchedLocation(lastLoc)
                    } else {
                        val cts = CancellationTokenSource()
                        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                            .addOnSuccessListener { freshLoc ->
                                if (freshLoc != null) {
                                    handleFetchedLocation(freshLoc)
                                } else {
                                    isFetchingGps = false
                                    statusMessage = gpsFailedText
                                }
                            }
                            .addOnFailureListener {
                                isFetchingGps = false
                                statusMessage = gpsFailedText
                            }
                    }
                }
                .addOnFailureListener {
                    isFetchingGps = false
                    statusMessage = gpsFailedText
                }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestFreshGpsLocation()
        } else {
            isFetchingGps = false
            statusMessage = permissionDeniedText
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 1. EDITORIAL HEADER KHAS WASHFLOW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isCompact) 14.dp else 20.dp,
                        vertical = if (isCompact) 12.dp else 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelBlueSoftBg)
                            .clickable(role = Role.Button) {
                                performLightHaptic(view, context)
                                onBackClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = GrayBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(32.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_map_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isCompact) 16.sp else 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.settings_map_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = PastelNeutralText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Badge status mode (Auto GPS vs Manual Pin)
                if (selectedLatLng != null || isGpsPin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LocationModePill(isAutoMode = isGpsPin)
                }
            }

            HorizontalDivider(color = PastelNeutralBg)

            // --- 2. AREA PETA + FLOATING SEARCH & CONTROLS ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (!isPreview) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            mapType = if (isSatelliteView) MapType.HYBRID else MapType.NORMAL
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false,
                            compassEnabled = true
                        ),
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                            isGpsPin = false
                            performLightHaptic(view, context)
                            focusManager.clearFocus()
                            searchResults = emptyList()
                            hasSearched = false

                            isResolvingAddress = true
                            selectedAddressName = searchingAddressText
                            coroutineScope.launch {
                                val addressText = getAddressFromLatLng(context, latLng)
                                selectedAddressName = addressText ?: addressNotFoundText
                                isResolvingAddress = false
                            }
                        }
                    ) {
                        if (selectedLatLng != null) {
                            Marker(
                                state = markerState,
                                draggable = true
                            )
                        }
                    }
                } else {
                    // Tampilan placeholder rapi untuk Compose Preview
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PastelBlueSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(PastelBlueIconBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = PastelBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.settings_map_preview),
                                color = PastelNeutralText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Floating Search Bar (Responsif: penuh di HP, maksimal 380.dp di Tablet)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (isCompact) 12.dp else 16.dp)
                        .then(
                            if (isCompact) Modifier.fillMaxWidth()
                            else Modifier.widthIn(max = 380.dp).fillMaxWidth()
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, PastelBlueBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = stringResource(R.string.common_search),
                                tint = PastelBlue,
                                modifier = Modifier.size(20.dp)
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.settings_map_search_placeholder),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = PastelNeutralText,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MainFontBlack,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(GrayBlue),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = PastelBlue
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PastelNeutralBg)
                                        .clickable(role = Role.Button) {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                            hasSearched = false
                                            focusManager.clearFocus()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.common_clear_search),
                                        tint = GrayBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dropdown Hasil Pencarian
                    val showDropdown = searchResults.isNotEmpty() || (hasSearched && !isSearching && searchQuery.trim().length > 2)
                    if (showDropdown) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 6.dp,
                            color = Color.White,
                            border = BorderStroke(1.dp, PastelBlueBg)
                        ) {
                            if (searchResults.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = PastelNeutralText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_map_no_results),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = PastelNeutralText,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            } else {
                                LazyColumn {
                                    itemsIndexed(searchResults) { index, address ->
                                        val fullAddress = address.getAddressLine(0)
                                            ?: address.featureName
                                            ?: addressNotFoundText
                                        val primaryTitle = address.featureName
                                            ?.takeIf { it.isNotBlank() && it != fullAddress }
                                            ?: fullAddress.substringBefore(",")

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    performLightHaptic(view, context)
                                                    searchQuery = ""
                                                    searchResults = emptyList()
                                                    hasSearched = false
                                                    focusManager.clearFocus()
                                                    isGpsPin = false
                                                    selectedAddressName = fullAddress
                                                    isResolvingAddress = false

                                                    val latLng = LatLng(address.latitude, address.longitude)
                                                    selectedLatLng = latLng
                                                    coroutineScope.launch {
                                                        cameraPositionState.animate(
                                                            CameraUpdateFactory.newLatLngZoom(latLng, 17f),
                                                            durationMs = 1100
                                                        )
                                                    }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(PastelBlueIconBg, RoundedCornerShape(9.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LocationOn,
                                                    contentDescription = null,
                                                    tint = PastelBlue,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = primaryTitle,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MainFontBlack,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = fullAddress,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = PastelNeutralText,
                                                        fontSize = 11.sp
                                                    ),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (index < searchResults.lastIndex) {
                                            HorizontalDivider(
                                                color = PastelNeutralBg,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Banner status / peringatan GPS (jika izin ditolak atau GPS mati)
                    AnimatedVisibility(
                        visible = statusMessage != null,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200))
                    ) {
                        statusMessage?.let { message ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PastelRoseBg,
                                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.25f)),
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = AlertRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AlertRed,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Map Controls di Kanan Bawah (Toggle Satelit + GPS Statis)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Toggle Normal / Hybrid Satellite
                    Surface(
                        onClick = {
                            performLightHaptic(view, context)
                            isSatelliteView = !isSatelliteView
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSatelliteView) PastelBlueSoftBg else Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSatelliteView) PastelBlue else PastelBlueBg
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = stringResource(R.string.settings_map_toggle_satellite_cd),
                                tint = if (isSatelliteView) PastelBlue else GrayBlue,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    // Tombol Lokasi Saat Ini (Static Pin)
                    Surface(
                        onClick = {
                            if (!isFetchingGps) {
                                performLightHaptic(view, context)
                                gpsTriggerMode = "STATIC"
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, PastelBlueBg)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isFetchingGps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = GrayBlue
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.MyLocation,
                                    contentDescription = stringResource(R.string.settings_map_current_location_cd),
                                    tint = GrayBlue,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = PastelNeutralBg)

            // --- 3. PANEL BAWAH TERPADU (INFO ALAMAT + TOMBOL AKSI) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(
                        horizontal = if (isCompact) 14.dp else 20.dp,
                        vertical = if (isCompact) 12.dp else 14.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Baris ringkasan alamat & koordinat
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isGpsPin) PastelGreenSoftBg else PastelNeutralBg.copy(alpha = 0.7f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isGpsPin) PastelGreenBg else PastelBlueBg.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = when {
                                        isGpsPin -> PastelGreenIconBg
                                        selectedLatLng != null -> PastelBlueIconBg
                                        else -> Color.White
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGpsPin) Icons.Rounded.GpsFixed else Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = when {
                                    isGpsPin -> PastelGreen
                                    selectedLatLng != null -> PastelBlue
                                    else -> PastelNeutralText
                                },
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val displayTitle = when {
                                isFetchingGps -> stringResource(R.string.settings_map_fetching_gps)
                                !selectedAddressName.isNullOrBlank() -> selectedAddressName!!
                                else -> stringResource(R.string.settings_map_tap_hint)
                            }
                            Text(
                                text = displayTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedLatLng != null || isFetchingGps) MainFontBlack else PastelNeutralText,
                                    fontWeight = if (selectedLatLng != null) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            selectedLatLng?.let { latLng ->
                                Spacer(modifier = Modifier.height(2.dp))
                                val coords = String.format(
                                    Locale.US,
                                    "%.5f, %.5f",
                                    latLng.latitude,
                                    latLng.longitude
                                )
                                Text(
                                    text = "$coords · ${stringResource(R.string.settings_map_drag_tip)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PastelNeutralText,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (isResolvingAddress || isFetchingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (isGpsPin) PastelGreen else PastelBlue
                            )
                        }
                    }
                }

                // Baris Tombol Aksi: Mode Auto GPS + Konfirmasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            performLightHaptic(view, context)
                            showAutoDialog = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isGpsPin) PastelGreenSoftBg else PastelBlueSoftBg,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isGpsPin) PastelGreen.copy(alpha = 0.5f) else PastelBlueBg
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = if (isCompact) 14.dp else 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GpsFixed,
                                contentDescription = null,
                                tint = if (isGpsPin) PastelGreen else GrayBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_map_auto_button),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isGpsPin) PastelGreen else GrayBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            selectedLatLng?.let {
                                performLightHaptic(view, context)
                                onLocationSelected(it.latitude, it.longitude, isGpsPin)
                            }
                        },
                        enabled = selectedLatLng != null && !isFetchingGps,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayBlue,
                            contentColor = Color.White,
                            disabledContainerColor = PastelNeutralBg,
                            disabledContentColor = PastelNeutralText
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.common_confirm),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG KONFIRMASI MODE AUTO ---
    if (showAutoDialog) {
        Dialog(onDismissRequest = { showAutoDialog = false }) {
            WashFlowDialogSurface {
                WashFlowDialogHeader(
                    icon = Icons.Rounded.GpsFixed,
                    iconBgColor = PastelGreenIconBg,
                    iconTint = PastelGreen,
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
}

@Composable
private fun LocationModePill(isAutoMode: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isAutoMode) PastelGreenBg else PastelBlueSoftBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isAutoMode) {
                val infiniteTransition = rememberInfiniteTransition(label = "autoModePulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "autoDotAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = alpha))
                )
            }
            Text(
                text = stringResource(
                    if (isAutoMode) R.string.settings_map_auto_mode
                    else R.string.settings_map_manual_mode
                ),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isAutoMode) PastelGreen else PastelBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Preview(name = "Tablet Location Picker", showBackground = true, widthDp = 1200, heightDp = 760)
@Composable
fun LocationSelectionPanelPreview() {
    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
            .padding(24.dp)
    ) {
        LocationSelectionPanel(
            onLocationSelected = { _, _, _ -> },
            onBackClick = {},
            initialLat = -7.5312,
            initialLon = 110.5960,
            initialIsAutoMode = true,
            isPreview = true
        )
    }
}

@Preview(name = "Phone Location Picker", device = Devices.PHONE, showBackground = true)
@Composable
private fun PhoneLocationSelectionPanelPreview() {
    ProvideWindowLayoutInfo {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                    )
                )
                .padding(14.dp)
        ) {
            LocationSelectionPanel(
                onLocationSelected = { _, _, _ -> },
                onBackClick = {},
                initialLat = -7.5312,
                initialLon = 110.5960,
                initialIsAutoMode = false,
                isPreview = true
            )
        }
    }
}
