package com.aprilarn.washflow

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.ui.components.Header
import com.aprilarn.washflow.ui.components.NavigationBar
import com.aprilarn.washflow.ui.customers.CustomersScreen
import com.aprilarn.washflow.ui.home.HomePage
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.theme.MainBLue
import com.aprilarn.washflow.ui.theme.MornYellow
import com.aprilarn.washflow.ui.theme.NoonBlue
import com.aprilarn.washflow.ui.theme.EveOrange
import com.aprilarn.washflow.ui.theme.NightDarkBlue
import com.aprilarn.washflow.ui.theme.WashFlowTheme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
        setContent {
            WashFlowTheme {
                WashFlowApp()
            }
        }
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}

@Composable
fun WashFlowApp() {
    // 1. Buat NavController
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { Header() },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // 2. Berikan NavController ke NavigationBar
                NavigationBar(navController = navController)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(MainBLue, MornYellow),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
                .padding(innerPadding)
        ) {
            // 3. Gunakan NavHost untuk mengatur layar
            NavHost(navController = navController, startDestination = AppNavigation.Home.route) {
                composable(AppNavigation.Home.route) {
                    LocationAwareHomePage()
                }
                composable(AppNavigation.Customers.route) {
                    CustomersScreen(
                        onAddCustomerClick = { _, _ -> /* TODO */ },
                        onEditCustomerClick = { /* TODO */ },
                        onDeleteCustomerClick = { /* TODO */ }
                    )
                }
                // Tambahkan layar lain di sini jika perlu
                composable(AppNavigation.Orders.route) {
                    // Placeholder untuk layar Orders
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Orders Screen", color = Color.White)
                    }
                }
                composable(AppNavigation.Settings.route) {
                    // Placeholder untuk layar Settings
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Settings Screen", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationAwareHomePage() {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel()
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Launcher untuk meminta izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            hasLocationPermission = true
        }
    }

    // FusedLocationProviderClient untuk mendapatkan lokasi
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Cek izin dan ambil lokasi saat komponen pertama kali dibuat
    LaunchedEffect(key1 = hasLocationPermission) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            hasLocationPermission = true
            // Jika izin sudah ada, ambil lokasi terakhir yang diketahui
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    homeViewModel.fetchWeatherData(location.latitude, location.longitude)
                }
            }
        } else {
            // Jika belum ada, minta izin
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Tentukan UI yang akan ditampilkan berdasarkan state izin
    if (hasLocationPermission) {
        HomePage(
            homeViewModel = homeViewModel,
            onEnterDataClick = {}
        )
    } else {
        // Tampilan jika pengguna menolak izin
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Aplikasi memerlukan izin lokasi untuk menampilkan data cuaca.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 960, heightDp = 600)
@Composable
fun WashFlowAppPreview() {
    WashFlowTheme {
        WashFlowApp()
    }
}