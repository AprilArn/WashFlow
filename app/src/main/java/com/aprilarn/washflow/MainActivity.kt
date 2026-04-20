package com.aprilarn.washflow

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.InviteRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.aprilarn.washflow.ui.MainNavigationEvent
import com.aprilarn.washflow.ui.MainViewModel
import com.aprilarn.washflow.ui.components.Header
import com.aprilarn.washflow.ui.components.LeaveWorkspaceDialog
import com.aprilarn.washflow.ui.components.NavigationBar
import com.aprilarn.washflow.ui.contributors.ContributorsScreen
import com.aprilarn.washflow.ui.contributors.ContributorsViewModel
import com.aprilarn.washflow.ui.customers.CustomersScreen
import com.aprilarn.washflow.ui.customers.CustomersViewModel
import com.aprilarn.washflow.ui.services.ServicesScreen
import com.aprilarn.washflow.ui.services.ServicesViewModel
import com.aprilarn.washflow.ui.home.HomePage
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.items.ItemsScreen
import com.aprilarn.washflow.ui.items.ItemsViewModel
import com.aprilarn.washflow.ui.login.GoogleAuthUiClient
import com.aprilarn.washflow.ui.login.LoginScreen
import com.aprilarn.washflow.ui.login.LoginViewModel
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.manage_order.ManageOrderScreen
import com.aprilarn.washflow.ui.manage_order.ManageOrderViewModel
import com.aprilarn.washflow.ui.orders.OrdersScreen
import com.aprilarn.washflow.ui.orders.OrdersViewModel
import com.aprilarn.washflow.ui.settings.LocationSelectionPanel
import com.aprilarn.washflow.ui.settings.SettingsScreen
import com.aprilarn.washflow.ui.settings.SettingsViewModel
import com.aprilarn.washflow.ui.tabledata.TableDataScreen
import com.aprilarn.washflow.ui.tabledata.TableDataViewModel
import com.aprilarn.washflow.ui.theme.MainBLue
import com.aprilarn.washflow.ui.theme.MornYellow
import com.aprilarn.washflow.ui.theme.NoonBlue
import com.aprilarn.washflow.ui.theme.EveOrange
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.ui.theme.NightDarkBlue
import com.aprilarn.washflow.ui.theme.WashFlowTheme
import com.aprilarn.washflow.ui.workspace.WorkspaceEvent
import com.aprilarn.washflow.ui.workspace.WorkspaceScreen
import com.aprilarn.washflow.ui.workspace.WorkspaceViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Transparent
                ) {
                    // 1. Navigasi utama aplikasi diatur di sini, dimulai dari "login"
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {

                        // 2. Rute untuk Login Screen
                        composable("login") {
                            val viewModel = viewModel<LoginViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(Unit) {
                                val currentUser = GoogleAuthUiClient.getSignedUser()
                                if (currentUser != null) {
                                    // Jika user sudah pernah login, langsung jalankan pengecekan Workspace
                                    // Ini otomatis akan memutar loading (isCheckingWorkspace = true)
                                    viewModel.checkAutoLogin(currentUser)
                                }
                            }

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
                            // --- MAINVIEWMODEL ---
                            val mainViewModelFactory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    @Suppress("UNCHECKED_CAST")
                                    return MainViewModel(
                                        WorkspaceRepository(),
                                        InviteRepository()
                                    ) as T
                                }
                            }
                            val mainViewModel: MainViewModel = viewModel(factory = mainViewModelFactory)
                            val userData = GoogleAuthUiClient.getSignedUser()


                            // --- EFEK UNTUK NAVIGASI KELUAR ---
                            LaunchedEffect(Unit) {
                                mainViewModel.eventFlow.collect { event ->
                                    when (event) {
                                        is MainNavigationEvent.NavigateToWorkspace -> {
                                            navController.navigate("workspace") {
                                                popUpTo("main") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- PASS VIEWMODEL KE MAINAPPSCREEN ---
                            // MainAppScreen(mainViewModel)
                            MainAppScreen(
                                mainViewModel = mainViewModel,
                                userData = userData,
                                onSignOut = {
                                    lifecycleScope.launch {
                                        // 1. TUNGGU proses Sign Out selesai sepenuhnya (Google & Firebase)
                                        try {
                                            GoogleAuthUiClient.signOut()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                        // 2. SETELAH semua beres, baru bersihkan memori dan pindah ke Login
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                        Toast.makeText(applicationContext, "Signed out", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
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
fun MainAppScreen(
    mainViewModel: MainViewModel,
    userData: UserData?,
    onSignOut: () -> Unit
) {
    // NavController khusus untuk navigasi di dalam Bottom Navigation Bar
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    // Inisialisasi SettingsViewModel di level MainAppScreen
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Inisialisasi HomeViewModel di level MainAppScreen agar bisa dibagikan
    val homeViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // 1. Buat Retrofit instance untuk Geocoding API
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

            val geocodingService = retrofit.create(com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService::class.java)

            // 2. Ambil SharedPreferences
            val sharedPrefs = context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)

            // 3. Masukkan ketiganya ke dalam HomeViewModel
            return HomeViewModel(OrderRepository(), geocodingService, sharedPrefs) as T
        }
    }
    val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header(
                modifier = Modifier.padding(bottom = 30.dp),
                navController = bottomNavController,
                workspaceName = mainUiState.workspaceName,
                onWorkspaceClick = { mainViewModel.onWorkspaceNameClicked() }
            ) { popupOffset ->
                // --- TERUSKAN INFORMASI OWNER KE DROPDOWN ---
                WorkspaceOptionsDropdown(
                    expanded = mainUiState.showWorkspaceOptions,
                    isOwner = mainUiState.isCurrentUserOwner,
                    popupOffset = popupOffset,
                    onDismiss = { mainViewModel.onDismissWorkspaceOptions() },
                    onRenameClicked = { mainViewModel.showRenameDialog() },
                    onContributorsClicked = {
                        mainViewModel.onDismissWorkspaceOptions()
                        bottomNavController.navigate(AppNavigation.Contributors.route)
                    },
                    onAddContributorClicked = { mainViewModel.onAddNewContributorClicked() },
                    onLeaveWorkspaceClicked = { mainViewModel.onLeaveWorkspaceClicked() },
                    onDeleteWorkspaceClicked = { mainViewModel.onDeleteWorkspaceClicked() }
                )
            }
        },
//        bottomBar = {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 26.dp)
//                    .padding(top = 36.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                NavigationBar(navController = bottomNavController)
//            }
//        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                .padding(horizontal = 32.dp)
                .navigationBarsPadding()
                .padding(bottom = 34.dp)
        ) {
            // NavHost internal untuk mengatur layar yang diakses dari Bottom Navigation Bar
            NavHost(navController = bottomNavController, startDestination = AppNavigation.Home.route) {

                composable(AppNavigation.Home.route) {
                    LocationAwareHomePage(
                        homeViewModel = homeViewModel,
                        onNavigateToManageOrder = {
                            bottomNavController.navigate(AppNavigation.ManageOrder.route)
                        },
                        onLocationFetched = { lat, lon ->
                            // Cek jika teks masih "Pilih Lokasi" agar API tidak dipanggil berulang kali
                            if (settingsState.locationName == "Pilih Lokasi") {
                                settingsViewModel.fetchAddressAndSave(lat, lon)
                            }
                        }
                    )
                }

                composable(AppNavigation.Contributors.route) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ContributorsViewModel(
                                WorkspaceRepository()
                            ) as T
                        }
                    }
                    val viewModel: ContributorsViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    ContributorsScreen(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                        // Aksi tombol "+ Add" di dalam layar Contributors
                        // Ini akan memicu dialog "Create Invite" yang sudah ada di MainViewModel
                        onAddClick = { mainViewModel.onAddNewContributorClicked() },
                        onContributorClick = { contributor -> viewModel.onContributorClicked(contributor) },
                        onDismissDialog = { viewModel.onDismissDetailDialog() },
                        onKickUser = { contributor -> viewModel.kickContributor(contributor) }
                    )
                }

                composable(AppNavigation.ManageOrder.route) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ManageOrderViewModel(
                                OrderRepository(),
                                CustomerRepository(),
                                ServiceRepository()
                            ) as T
                        }
                    }
                    val viewModel: ManageOrderViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    LaunchedEffect(uiState.errorMessage) {
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            viewModel.onErrorMessageShown()
                        }
                    }

                    ManageOrderScreen(
                        uiState = uiState,
                        onDrop = { orderId, newStatus ->
                            viewModel.changeOrderStatus(orderId, newStatus)
                        },
                        onOrderClick = { order -> viewModel.onOrderCardClicked(order) },
                        onDismissDialog = { viewModel.onDismissOrderDetailDialog() },
                        onDeleteOrder = { orderId -> viewModel.deleteOrder(orderId) }
                    )
                }

                composable(AppNavigation.TableData.route) {
                    val factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return TableDataViewModel(
                                CustomerRepository(),
                                ServiceRepository(),
                                ItemRepository()
                            ) as T
                        }
                    }
                    val viewModel: TableDataViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    TableDataScreen(
                        uiState = uiState,
                        onNavigate = { route ->
                            bottomNavController.navigate(route)
                        }
                    )
                }

                composable(AppNavigation.Customers.route) {
                    val viewModel: CustomersViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current

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

                    CustomersScreen(
                        uiState = uiState,
                        onAddCustomerClick = { name, contact -> viewModel.addCustomer(name, contact) },
                        onEditCustomerClick = { customer -> viewModel.updateCustomer(customer) },
                        onDeleteCustomerClick = { customer -> viewModel.deleteCustomer(customer) },
                        onCustomerSelected = { customer -> viewModel.onCustomerSelected(customer) },
                        onDismissDialog = { viewModel.onDismissEditDialog() }
                    )
                }

                composable(AppNavigation.Services.route) {
                    val viewModel: ServicesViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current

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

                    ServicesScreen(
                        uiState = uiState,
                        onAddServiceClick = { id, name -> viewModel.addService(id, name) },
                        onEditServiceClick = { service -> viewModel.updateService(service) },
                        onDeleteServiceClick = { service -> viewModel.deleteService(service) },
                        onServiceSelected = { service -> viewModel.onServiceSelected(service) },
                        onDismissDialog = { viewModel.onDismissEditDialog() }
                    )
                }

                composable(AppNavigation.Items.route) {
                    val viewModel: ItemsViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current

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

                    ItemsScreen(
                        uiState = uiState,
                        onAddItemClick = { serviceId, name, price -> viewModel.addItem(serviceId, name, price) },
                        onEditItemClick = { item -> viewModel.updateItem(item) },
                        onDeleteItemClick = { item -> viewModel.deleteItem(item) },
                        onItemSelected = { item -> viewModel.onItemSelected(item) },
                        onDismissDialog = { viewModel.onDismissEditDialog() }
                    )
                }

                composable(AppNavigation.Orders.route) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return OrdersViewModel(
                                CustomerRepository(),
                                ServiceRepository(),
                                ItemRepository(),
                                OrderRepository()
                            ) as T
                        }
                    }
                    val viewModel: OrdersViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current

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

                    OrdersScreen(uiState = uiState, viewModel = viewModel)
                }

                composable(AppNavigation.Settings.route) {
                    SettingsScreen(
                        userData = userData ?: UserData(
                            userId = "", displayName = "Unknown", email = "No Email", profilePictureUrl = null
                        ),
                        settingsUiState = settingsState,
                        onSetLocationClicked = { // <-- UBAH JADI onSetLocationClicked
                            bottomNavController.navigate("location_selection")
                        },
                        onSignOut = onSignOut
                    )
                }

                composable("location_selection") {
                    // Panggil context untuk SharedPreferences
                    val context = LocalContext.current
                    val sharedPreferences = remember { context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE) }

                    LocationSelectionPanel(
                        // Sekarang menerima 2 parameter (lat, lon)
                        onLocationSelected = { lat, lon, isGps ->
                            sharedPreferences.edit().apply {
                                // Jika isGps = true berarti "Auto", jika false berarti "Static/Manual"
                                putString("LOCATION_MODE", if (isGps) "AUTO" else "STATIC")
                                putFloat("STATIC_LAT", lat.toFloat())
                                putFloat("STATIC_LON", lon.toFloat())
                                apply()
                            }

                            // Panggil fungsi baru di SettingsViewModel untuk cari nama tempat & save
                            settingsViewModel.fetchAddressAndSave(lat, lon)

                            // Tetap update cuaca di Home
                            homeViewModel.fetchWeatherData(lat, lon, isGps)

                            // Kembali ke layar Settings
                            bottomNavController.popBackStack()
                        },
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }
        }
    }

    // --- DIALOG UNTUK RENAME WORKSPACE ---
    // Ditampilkan di luar Scaffold agar muncul di atas segalanya
    if (mainUiState.showRenameDialog) {
        RenameWorkspaceDialog(
            currentName = mainUiState.workspaceName,
            onDismiss = { mainViewModel.onDismissRenameDialog() },
            onApply = { newName -> mainViewModel.renameWorkspace(newName) }
        )
    }

    // Conditional logic for Invite Dialogs
    if (mainUiState.showCreateInviteDialog) {
        // Copy the value to a local variable
        val activeInvite = mainUiState.activeInvite

        if (activeInvite == null) {
            // If there's no active invite, show the creation dialog
            CreateInviteDialog(
                onDismiss = { mainViewModel.onDismissCreateInviteDialog() },
                onGenerate = { maxUsers, expiryDate ->
                    mainViewModel.createInvitation(maxUsers, expiryDate)
                }
            )
        } else {
            // If there IS an active invite, show its details
            // Now it's safe to use the local 'activeInvite' variable
            ActiveInviteDialog(
                invite = activeInvite,
                onDismiss = { mainViewModel.onDismissCreateInviteDialog() },
                onDelete = { mainViewModel.deleteInvitation() }
            )
        }
    }

    // --- DIALOG BARU UNTUK LEAVE WORKSPACE ---
    if (mainUiState.showLeaveWorkspaceDialog) {
        LeaveWorkspaceDialog(
            onDismiss = { mainViewModel.onDismissLeaveWorkspaceDialog() },
            onConfirm = { mainViewModel.confirmLeaveWorkspace() }
        )
    }

    // --- DIALOG BARU UNTUK DELETE WORKSPACE ---
    if (mainUiState.showDeleteWorkspaceDialog) {
        DeleteWorkspaceDialog(
            onDismiss = { mainViewModel.onDismissDeleteWorkspaceDialog() },
            onConfirm = { mainViewModel.confirmDeleteWorkspace() }
        )
    }
}

@Composable
fun WorkspaceOptionsDropdown(
    expanded: Boolean,
    isOwner: Boolean,
    popupOffset: IntOffset, // <--- Terima Parameter Baru
    onDismiss: () -> Unit,
    onRenameClicked: () -> Unit,
    onContributorsClicked: () -> Unit,
    onAddContributorClicked: () -> Unit,
    onLeaveWorkspaceClicked: () -> Unit,
    onDeleteWorkspaceClicked: () -> Unit
) {
    if (expanded) {
        Popup(
            alignment = Alignment.TopEnd, // Rata Kanan mengikuti ujung teks
            offset = popupOffset,         // Posisi persis di bawah teks
            properties = PopupProperties(focusable = true),
            onDismissRequest = onDismiss
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                ) {
                    if (isOwner) {
                        WorkspaceDropdownItem(
                            text = "Rename workspace",
                            onClick = {
                                onRenameClicked()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }

                    WorkspaceDropdownItem(
                        text = "Contributors",
                        onClick = {
                            onContributorsClicked()
                            onDismiss()
                        }
                    )

                    if (!isOwner) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        WorkspaceDropdownItem(
                            text = "Leave Workspace",
                            onClick = {
                                onLeaveWorkspaceClicked()
                                onDismiss()
                            }
                        )
                    }

                    if (isOwner) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        WorkspaceDropdownItem(
                            text = "Delete Workspace",
                            color = MaterialTheme.colorScheme.error,
                            onClick = {
                                onDeleteWorkspaceClicked()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Fungsi Bantuan untuk Item Menu (Agar rapi dan tidak mengulang kode)
@Composable
fun WorkspaceDropdownItem(
    text: String,
    color: Color = MainFontBlack,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

@Composable
fun RenameWorkspaceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)),
        onDismissRequest = onDismiss,
        title = { Text("Ubah Nama Workspace") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nama workspace baru") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onApply(newName) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ActiveInviteDialog(
    invite: Invites,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val formattedExpiry = remember(invite.expiresAt) {
        invite.expiresAt?.toDate()?.let {
            SimpleDateFormat("EEE, dd MMM yyyy 'at' HH:mm", Locale.getDefault()).format(it)
        } ?: "No expiry"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Active Invitation Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Code Display and Copy Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SelectionContainer {
                        Text(
                            text = invite.inviteId ?: "------",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(invite.inviteId ?: ""))
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                }
                // Info Section
                Column {
                    Text("Code is valid for ${invite.maxContributors} users.", style = MaterialTheme.typography.bodyMedium)
                    Text("Expires on: $formattedExpiry", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Dialog to create a new invitation code (Image 1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInviteDialog(
    onDismiss: () -> Unit,
    onGenerate: (Int, Date) -> Unit
) {
    var maxContributors by remember { mutableStateOf("1") }
    val calendar = Calendar.getInstance()
    // Default expiry: 1 day from now
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    var expiryDate by remember { mutableStateOf(calendar.time) }

    // --- Date & Time Picker States ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiryDate.time)
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // --- Dialogs ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = it
                        val currentCal = Calendar.getInstance()
                        currentCal.time = expiryDate
                        currentCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
                        expiryDate = currentCal.time
                    }
                    showTimePicker = true // Show time picker after date is selected
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog( // Wrap time picker in a dialog for better control
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val currentCal = Calendar.getInstance()
                    currentCal.time = expiryDate
                    currentCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    currentCal.set(Calendar.MINUTE, timePickerState.minute)
                    expiryDate = currentCal.time
                    showTimePicker = false
                }) { Text("OK") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Invitation Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = maxContributors,
                    onValueChange = { if (it.all { char -> char.isDigit() }) maxContributors = it },
                    label = { Text("Max Contributors") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                val formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
                Text("Expires at:")
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formatter.format(expiryDate))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val maxUsers = maxContributors.toIntOrNull() ?: 1
                onGenerate(maxUsers, expiryDate)
            }) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteWorkspaceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    // Tombol delete hanya aktif jika teks = "delete"
    val isDeleteButtonEnabled = confirmText == "delete"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure want to delete current workspace?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "This action will kick/delete all contributors in this workspace and than delete this workspace.",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Field untuk konfirmasi
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    label = { Text("Type 'delete' to confirm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                // Tombol hanya aktif jika teks diisi dengan benar
                enabled = isDeleteButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

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


//@Preview(showBackground = true, widthDp = 960, heightDp = 600)
//@Composable
//fun WashFlowAppPreview() {
//    WashFlowTheme {
//        MainAppScreen()
//    }
//}