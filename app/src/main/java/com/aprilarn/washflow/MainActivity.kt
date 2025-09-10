package com.aprilarn.washflow

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.ui.components.Header
import com.aprilarn.washflow.ui.components.NavigationBar
import com.aprilarn.washflow.ui.customers.CustomersScreen
import com.aprilarn.washflow.ui.customers.CustomersViewModel
import com.aprilarn.washflow.ui.home.HomePage
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.login.GoogleAuthUiClient
import com.aprilarn.washflow.ui.login.LoginScreen
import com.aprilarn.washflow.ui.login.LoginViewModel
import com.aprilarn.washflow.ui.theme.MainBLue
import com.aprilarn.washflow.ui.theme.MornYellow
import com.aprilarn.washflow.ui.theme.NoonBlue
import com.aprilarn.washflow.ui.theme.EveOrange
import com.aprilarn.washflow.ui.theme.NightDarkBlue
import com.aprilarn.washflow.ui.theme.WashFlowTheme
import com.aprilarn.washflow.ui.workspace.WorkspaceEvent
import com.aprilarn.washflow.ui.workspace.WorkspaceScreen
import com.aprilarn.washflow.ui.workspace.WorkspaceViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val GoogleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
        setContent {
            WashFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    // 1. Navigasi utama aplikasi diatur di sini, dimulai dari "login"
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {

                        // 2. Rute untuk Login Screen
                        composable("login") {
                            val viewModel = viewModel<LoginViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            // Efek ini akan memantau status workspace dan menavigasi
                            LaunchedEffect(key1 = state.userHasWorkspace) {
                                if (state.userHasWorkspace == true) { // Punya workspace
                                    Toast.makeText(applicationContext, "Welcome back!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    viewModel.onNavigationComplete()
                                } else if (state.userHasWorkspace == false) { // Tidak punya
                                    navController.navigate("workspace") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    viewModel.onNavigationComplete()
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = GoogleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            // Tampilkan background gradient untuk Login Screen
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
                            ) {
                                LoginScreen(
                                    state = state,
                                    onGoogleSignInClick = {
                                        lifecycleScope.launch {
                                            val signInIntentSender = GoogleAuthUiClient.signIn()
                                            launcher.launch(
                                                IntentSenderRequest.Builder(
                                                    signInIntentSender ?: return@launch
                                                ).build()
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        // Rute baru untuk Workspace Screen
                        composable("workspace") {
                            val viewModel = viewModel<WorkspaceViewModel>()
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            val context = LocalContext.current

                            // Efek untuk menangani event dari WorkspaceViewModel
                            LaunchedEffect(Unit) {
                                viewModel.eventFlow.collect { event ->
                                    when (event) {
                                        is WorkspaceEvent.NavigateToDashboard -> {
                                            navController.navigate("main") {
                                                popUpTo("workspace") { inclusive = true }
                                            }
                                        }
                                        is WorkspaceEvent.ShowError -> {
                                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
                                        )
                                    )
                            ) {
                                WorkspaceScreen(
                                    state = uiState,
                                    onJoinClick = { code -> viewModel.joinWorkspace(code) },
                                    onCreateWorkspaceClick = { viewModel.createWorkspace() }
                                )
                            }
                        }

                        composable("main") {
                            MainAppScreen()
                        }
                    }
                }
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
fun MainAppScreen() {
    // NavController khusus untuk navigasi di dalam Bottom Navigation Bar
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Hanya tampilkan Header jika tidak di halaman tertentu (opsional)
        topBar = { Header() },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(navController = bottomNavController)
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
            // NavHost internal untuk mengatur layar yang diakses dari Bottom Navigation Bar
            NavHost(navController = bottomNavController, startDestination = AppNavigation.Home.route) {
                composable(AppNavigation.Home.route) {
                    LocationAwareHomePage()
                }
//                composable(AppNavigation.Customers.route) {
//                    CustomersScreen(
//                        onAddCustomerClick = { _, _ -> /* TODO */ },
//                        onEditCustomerClick = { /* TODO */ },
//                        onDeleteCustomerClick = { /* TODO */ }
//                    )
//                }
                composable(AppNavigation.Customers.route) {
                    // 1. Buat instance ViewModel
                    val viewModel: CustomersViewModel = viewModel()
                    // 2. Ambil state dari ViewModel
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    // Menampilkan pesan Toast dari ViewModel
                    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
                        uiState.successMessage?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            viewModel.onMessageShown()
                        }
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            viewModel.onMessageShown()
                        }
                    }

                    // 3. Berikan 'uiState' ke CustomersScreen
                    CustomersScreen(
                        uiState = uiState,
                        onAddCustomerClick = { name, contact ->
                            viewModel.addCustomer(name, contact)
                        },
                        onEditCustomerClick = { /* TODO */ },
                        onDeleteCustomerClick = { /* TODO */ }
                    )
                }
                composable(AppNavigation.Orders.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Orders Screen", color = Color.White)
                    }
                }
                composable(AppNavigation.Settings.route) {
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
        MainAppScreen()
    }
}