package com.aprilarn.washflow.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.AppNavigation
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.aprilarn.washflow.ui.components.Header
import com.aprilarn.washflow.ui.components.KickedDialog
import com.aprilarn.washflow.ui.components.LeaveWorkspaceDialog
import com.aprilarn.washflow.ui.components.NotificationPanel
import com.aprilarn.washflow.ui.components.NotificationPreviewItem
import com.aprilarn.washflow.ui.contributors.ContributorsScreen
import com.aprilarn.washflow.ui.contributors.ContributorsViewModel
import com.aprilarn.washflow.ui.customers.CustomersScreen
import com.aprilarn.washflow.ui.customers.CustomersViewModel
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.home.LocationAwareHomePage
import com.aprilarn.washflow.ui.items.ItemsScreen
import com.aprilarn.washflow.ui.items.ItemsViewModel
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.manage_order.ManageOrderScreen
import com.aprilarn.washflow.ui.manage_order.ManageOrderViewModel
import com.aprilarn.washflow.ui.orders.OrdersScreen
import com.aprilarn.washflow.ui.orders.OrdersViewModel
import com.aprilarn.washflow.ui.services.ServicesScreen
import com.aprilarn.washflow.ui.services.ServicesViewModel
import com.aprilarn.washflow.ui.settings.LocationSelectionPanel
import com.aprilarn.washflow.ui.settings.SettingsScreen
import com.aprilarn.washflow.ui.settings.SettingsViewModel
import com.aprilarn.washflow.ui.tabledata.TableDataScreen
import com.aprilarn.washflow.ui.tabledata.TableDataViewModel
import com.aprilarn.washflow.ui.theme.MainBlue
import com.aprilarn.washflow.ui.theme.MornYellow
import com.aprilarn.washflow.ui.workspace.ActiveInviteDialog
import com.aprilarn.washflow.ui.workspace.CreateInviteDialog
import com.aprilarn.washflow.ui.workspace.DeleteWorkspaceDialog
import com.aprilarn.washflow.ui.workspace.OperationalHoursDialog
import com.aprilarn.washflow.ui.workspace.RenameWorkspaceDialog
import com.aprilarn.washflow.ui.workspace.WorkspaceOptionsDropdown

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
                unreadCount = mainUiState.unreadCount,
                isWorkspaceExpanded = mainUiState.showWorkspaceOptions,
                notificationPreviews = mainUiState.notificationPreviews,
                onWorkspaceClick = { mainViewModel.onWorkspaceNameClicked() },
                onNotifClick = { mainViewModel.onNotificationIconClicked() },
                onRemovePreview = { id, swiped -> mainViewModel.removeNotificationPreview(id, swiped) },
                workspaceDropdown = { wsOffset ->
                    WorkspaceOptionsDropdown(
                        expanded = mainUiState.showWorkspaceOptions,
                        isOwner = mainUiState.isCurrentUserOwner,
                        popupOffset = wsOffset,
                        onDismiss = { mainViewModel.onDismissWorkspaceOptions() },
                        onRenameClicked = { mainViewModel.showRenameDialog() },
                        onContributorsClicked = {
                            mainViewModel.onDismissWorkspaceOptions()
                            bottomNavController.navigate(AppNavigation.Contributors.route)
                        },
                        onOperationalHoursClicked = { mainViewModel.showOperationalHoursDialog() },
                        onAddContributorClicked = { mainViewModel.onAddNewContributorClicked() },
                        onLeaveWorkspaceClicked = { mainViewModel.onLeaveWorkspaceClicked() },
                        onDeleteWorkspaceClicked = { mainViewModel.onDeleteWorkspaceClicked() }
                    )
                }
            )
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
                        colors = listOf(MainBlue, MornYellow),
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

    // 2. PANEL NOTIFIKASI MELAYANG (Berada paling atas karena ditulis paling akhir)
    NotificationPanel(
        expanded = mainUiState.showNotificationOptions,
        notifications = mainUiState.notifications,
        currentUid = mainUiState.currentUserUid,
        onDismiss = { mainViewModel.onDismissNotificationOptions() },
        onNotificationClick = { notif ->
            mainViewModel.markNotificationAsRead(notif)
        }
    )

    // 3. OVERLAY PREVIEW NOTIFIKASI JATUH (TANPA POPUP)
    if (mainUiState.notificationPreviews.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize() // Memenuhi layar agar notif bisa jatuh sampai bawah
                // Box kosong di Compose TIDAK memblokir sentuhan (touch pass-through)
                .padding(top = 58.dp, end = 24.dp) // Jarak dari atas dan kanan, sesuaikan sedikit agar pas di bawah lonceng
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .wrapContentSize(), // Kotak kolom hanya sebesar notifikasinya
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                mainUiState.notificationPreviews.forEachIndexed { index, notif ->
                    key(notif.notificationId) {
                        NotificationPreviewItem(
                            modifier = Modifier.zIndex(mainUiState.notificationPreviews.size - index.toFloat()),
                            notification = notif,
                            onClick = {
                                if (notif.title == "Order Baru") {
                                    bottomNavController.navigate(AppNavigation.ManageOrder.route)
                                }
                                mainViewModel.removeNotificationPreview(notif.notificationId, true)
                            },
                            onRemove = { wasSwiped ->
                                mainViewModel.removeNotificationPreview(notif.notificationId, wasSwiped)
                            }
                        )
                    }
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

    if (mainUiState.showOperationalHoursDialog) {
        OperationalHoursDialog(
            openTime = mainUiState.openTime,
            closeTime = mainUiState.closeTime,
            onDismiss = { mainViewModel.onDismissOperationalHoursDialog() },
            onApply = { open, close -> mainViewModel.updateOperationalHours(open, close) }
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

    // --- DIALOG BARU UNTUK KICKED ---
    if (mainUiState.showKickedDialog) {
        KickedDialog(
            workspaceName = mainUiState.kickedFromWorkspaceName,
            onConfirm = { mainViewModel.onKickedDialogConfirm() }
        )
    }
}
