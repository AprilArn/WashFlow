package com.aprilarn.washflow.ui.feature.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.res.stringResource
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.navigation.AppNavigation
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.NotificationsRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.aprilarn.washflow.domain.usecase.ManageCustomerUseCase
import com.aprilarn.washflow.domain.usecase.ManageItemUseCase
import com.aprilarn.washflow.domain.usecase.ManageNotificationsUseCase
import com.aprilarn.washflow.domain.usecase.ManageOrderUseCase
import com.aprilarn.washflow.domain.usecase.ManageServiceUseCase
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import com.aprilarn.washflow.ui.feature.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.core.components.Header
import com.aprilarn.washflow.ui.core.components.KickedDialog
import com.aprilarn.washflow.ui.core.components.LeaveWorkspaceDialog
import com.aprilarn.washflow.ui.core.components.ExitConfirmationDialog
import com.aprilarn.washflow.ui.feature.aiagent.AiAgentPanel
import com.aprilarn.washflow.ui.feature.aiagent.component.VoiceAgentOverlay
import com.aprilarn.washflow.ui.feature.aiagent.AiAgentViewModel
import com.aprilarn.washflow.ui.feature.aiagent.AiModelStatus
import com.aprilarn.washflow.ui.feature.aiagent.VoiceAgentStatus
import com.aprilarn.washflow.ui.feature.notifications.NotificationPanel
import com.aprilarn.washflow.ui.feature.notifications.NotificationPreviewItem
import com.aprilarn.washflow.ui.feature.notifications.NotificationsViewModel
import com.aprilarn.washflow.ui.feature.analytics.AnalyticsScreen
import com.aprilarn.washflow.ui.feature.analytics.AnalyticsViewModel
import com.aprilarn.washflow.ui.feature.contributors.ContributorsScreen
import com.aprilarn.washflow.ui.feature.contributors.ContributorsViewModel
import com.aprilarn.washflow.ui.feature.masterdata.customers.CustomersScreen
import com.aprilarn.washflow.ui.feature.masterdata.customers.CustomersViewModel
import com.aprilarn.washflow.ui.feature.home.HomeViewModel
import com.aprilarn.washflow.ui.feature.home.LocationAwareHomePage
import com.aprilarn.washflow.ui.feature.masterdata.items.ItemsScreen
import com.aprilarn.washflow.ui.feature.masterdata.items.ItemsViewModel
import com.aprilarn.washflow.ui.feature.login.UserData
import com.aprilarn.washflow.ui.feature.manageorder.ManageOrderScreen
import com.aprilarn.washflow.ui.feature.manageorder.ManageOrderViewModel
import com.aprilarn.washflow.ui.feature.orders.OrdersScreen
import com.aprilarn.washflow.ui.feature.orders.OrdersViewModel
import com.aprilarn.washflow.ui.feature.masterdata.services.ServicesScreen
import com.aprilarn.washflow.ui.feature.masterdata.services.ServicesViewModel
import com.aprilarn.washflow.ui.feature.settings.LocationSelectionPanel
import com.aprilarn.washflow.ui.feature.settings.SettingsScreen
import com.aprilarn.washflow.ui.feature.settings.SettingsViewModel
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.TableDataScreen
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.TableDataViewModel
import com.aprilarn.washflow.ui.feature.workspace.ActiveInviteDialog
import com.aprilarn.washflow.ui.feature.workspace.CreateInviteDialog
import com.aprilarn.washflow.ui.feature.workspace.DeleteWorkspaceDialog
import com.aprilarn.washflow.ui.feature.workspace.OperationalHoursDialog
import com.aprilarn.washflow.ui.feature.workspace.RenameWorkspaceDialog
import com.aprilarn.washflow.ui.feature.workspace.WorkspaceOptionsDropdown
import com.aprilarn.washflow.utils.WorkspacePresenceEffect
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun MainAppScreen(
    mainViewModel: MainViewModel,
    userData: UserData?,
    onSignOut: () -> Unit
) {
    // NavController khusus untuk navigasi di dalam Bottom Navigation Bar
    val bottomNavController = rememberNavController()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    // Lacak kehadiran online/offline user saat aktif di workspace
    WorkspacePresenceEffect(userUid = userData?.userId ?: mainUiState.currentUserUid)

    // Shared Domain UseCases (ringan, tidak menyalakan listener Firestore sebelum dipanggil oleh ViewModel)
    val orderUseCase = remember { ManageOrderUseCase(OrderRepository()) }
    val customerUseCase = remember { ManageCustomerUseCase(CustomerRepository()) }
    val serviceUseCase = remember { ManageServiceUseCase(ServiceRepository()) }
    val itemUseCase = remember { ManageItemUseCase(ItemRepository()) }
    val workspaceUseCase = remember { ManageWorkspaceUseCase(WorkspaceRepository()) }
    val notificationsUseCase = remember { ManageNotificationsUseCase(NotificationsRepository()) }

    // Inisialisasi NotificationsViewModel
    val notificationsViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationsViewModel(notificationsUseCase) as T
            }
        }
    }
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = notificationsViewModelFactory)
    val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

    // Inisialisasi AiAgentViewModel
    val aiAgentViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiAgentViewModel(customerUseCase, itemUseCase, serviceUseCase) as T
            }
        }
    }
    val aiAgentViewModel: AiAgentViewModel = viewModel(factory = aiAgentViewModelFactory)
    val aiAgentUiState by aiAgentViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                aiAgentViewModel.onStartVoiceAgent(context)
            } else {
                Toast.makeText(context, context.getString(R.string.main_mic_permission_required), Toast.LENGTH_SHORT).show()
            }
        }
    )

    val customersViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CustomersViewModel(customerUseCase) as T
            }
        }
    }
    val itemsViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ItemsViewModel(itemUseCase, serviceUseCase) as T
            }
        }
    }
    val customersViewModel: CustomersViewModel = viewModel(factory = customersViewModelFactory)
    val itemsViewModel: ItemsViewModel = viewModel(factory = itemsViewModelFactory)

    // Handle AI Agent actions (navigation)
    LaunchedEffect(Unit) {
        aiAgentViewModel.actionEvents.collect { action ->
            when (action) {
                is AiAgentAction.Navigate -> {
                    bottomNavController.navigate(action.destination.route)
                }
                is AiAgentAction.AddCustomer -> {
                    customersViewModel.addCustomer(action.name, action.phoneNumber)
                    Toast.makeText(context, context.getString(R.string.main_customer_added, action.name), Toast.LENGTH_SHORT).show()
                }
                is AiAgentAction.DeleteCustomer -> {
                    if (action.customerId.isNotEmpty()) {
                        val customerToDelete = Customers(
                            customerId = action.customerId,
                            name = action.name
                        )
                        customersViewModel.deleteCustomer(customerToDelete)
                        Toast.makeText(context, context.getString(R.string.main_customer_deleted, action.name), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.main_customer_id_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
                is AiAgentAction.AddItem -> {
                    itemsViewModel.addItem(action.serviceId, action.itemName, action.itemPrice)
                    Toast.makeText(context, context.getString(R.string.main_item_added, action.itemName), Toast.LENGTH_SHORT).show()
                }
                is AiAgentAction.DeleteItem -> {
                    if (action.itemId.isNotEmpty()) {
                        val itemToDelete = Items(
                            itemId = action.itemId,
                            itemName = action.itemName
                        )
                        itemsViewModel.deleteItem(itemToDelete)
                        Toast.makeText(context, context.getString(R.string.main_item_deleted, action.itemName), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.main_item_id_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
                is AiAgentAction.Unknown -> {
                    // Log or handle unknown action
                }
                AiAgentAction.None -> {
                    // Do nothing
                }
            }
        }
    }

    // Inisialisasi SettingsViewModel di level MainAppScreen
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    // Inisialisasi HomeViewModel di level MainAppScreen agar bisa dibagikan
    val appContext = remember(context) { context.applicationContext }
    val homeViewModelFactory = remember(appContext) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // 1. Buat Retrofit instance untuk Geocoding API
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val geocodingService = retrofit.create(GeocodingApiService::class.java)

                // 2. Ambil SharedPreferences via applicationContext
                val sharedPrefs = appContext.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)

                // 3. Masukkan ketiganya ke dalam HomeViewModel
                return HomeViewModel(orderUseCase, workspaceUseCase, geocodingService, sharedPrefs) as T
            }
        }
    }
    val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    var showExitDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val isDarkWeatherBg = remember(
        homeUiState.weather,
        homeUiState.greeting,
        homeUiState.weatherType,
        homeUiState.isDaytime
    ) {
        com.aprilarn.washflow.ui.core.components.isDarkWeatherScene(
            weatherText = homeUiState.weather,
            greetingText = homeUiState.greeting,
            weatherType = homeUiState.weatherType,
            isDaytime = homeUiState.isDaytime
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header(
                modifier = Modifier.padding(bottom = 30.dp),
                navController = bottomNavController,
                workspaceName = mainUiState.workspaceName,
                unreadCount = notificationsUiState.unreadCount,
                isWorkspaceExpanded = mainUiState.showWorkspaceOptions,
                isDarkBackground = isDarkWeatherBg,
                notificationPreviews = notificationsUiState.notificationPreviews,
                onWorkspaceClick = { mainViewModel.onWorkspaceNameClicked() },
                onNotifClick = { notificationsViewModel.onNotificationIconClicked() },
                onAiAgentClick = { aiAgentViewModel.onToggleAiAgent() },
                onAiAgentLongClick = {
                    val permission = Manifest.permission.RECORD_AUDIO
                    val isGranted = ContextCompat.checkSelfPermission(
                        context, permission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        aiAgentViewModel.onStartVoiceAgent(context)
                    } else {
                        permissionLauncher.launch(permission)
                    }
                },
                isAiVoiceActive = aiAgentUiState.voiceAgentStatus != VoiceAgentStatus.IDLE ||
                        aiAgentUiState.isAiThinking ||
                        aiAgentUiState.isTypewriterActive ||
                        aiAgentUiState.modelStatus == AiModelStatus.COOLDOWN,
                onRemovePreview = { id, swiped -> notificationsViewModel.removeNotificationPreview(id, swiped) },
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
            modifier = Modifier.fillMaxSize()
        ) {
            com.aprilarn.washflow.ui.core.components.WeatherBackgroundIllustration(
                weather = homeUiState.weather,
                greeting = homeUiState.greeting,
                weatherType = homeUiState.weatherType,
                isDaytime = homeUiState.isDaytime,
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 34.dp)
            ) {
            // NavHost internal untuk mengatur layar yang diakses dari Top Navigation Bar
            // Exit hanya menggunakan fade cepat (100ms) agar GPU tidak merender 2 slide penuh sekaligus
            // saat layar tujuan pertama kali di-compose.
            NavHost(
                navController = bottomNavController,
                startDestination = AppNavigation.Home.route,
                enterTransition = {
                    val fromIndex = navRouteOrderIndex(initialState.destination.route)
                    val toIndex = navRouteOrderIndex(targetState.destination.route)
                    val slideDirection = if (toIndex >= fromIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                    fadeIn(
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    ) + slideIntoContainer(
                        towards = slideDirection,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        initialOffset = { fullSize -> fullSize / 36 }
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                    )
                },
                popEnterTransition = {
                    val fromIndex = navRouteOrderIndex(initialState.destination.route)
                    val toIndex = navRouteOrderIndex(targetState.destination.route)
                    val slideDirection = if (toIndex <= fromIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    }
                    fadeIn(
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    ) + slideIntoContainer(
                        towards = slideDirection,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        initialOffset = { fullSize -> fullSize / 36 }
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                    )
                }
            ) {

                composable(AppNavigation.Home.route) {
                    val isPanelOpen = notificationsUiState.showNotificationOptions ||
                        aiAgentUiState.expanded ||
                        aiAgentUiState.voiceAgentStatus != VoiceAgentStatus.IDLE
                    androidx.activity.compose.BackHandler(enabled = !isPanelOpen) {
                        showExitDialog = true
                    }
                    
                    LocationAwareHomePage(
                        homeViewModel = homeViewModel,
                        onNavigateToManageOrder = {
                            bottomNavController.navigate(AppNavigation.ManageOrder.route)
                        },
                        onLocationFetched = { lat, lon ->
                            if (!settingsState.hasFetchedLocation) {
                                val prefs = context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)
                                val isAuto = prefs.getString("LOCATION_MODE", "AUTO") != "STATIC"
                                settingsViewModel.fetchAddressAndSave(lat, lon, isAutoMode = isAuto)
                            }
                        }
                    )
                }

                composable(AppNavigation.Contributors.route) {
                    val factory = remember {
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ContributorsViewModel(
                                    workspaceUseCase
                                ) as T
                            }
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
                    val factory = remember {
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ManageOrderViewModel(
                                    orderUseCase,
                                    customerUseCase,
                                    serviceUseCase
                                ) as T
                            }
                        }
                    }
                    val viewModel: ManageOrderViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState.errorMessage) {
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
                            viewModel.onErrorMessageShown()
                        }
                    }

                    ManageOrderScreen(
                        uiState = uiState,
                        onDrop = { orderId, newStatus ->
                            viewModel.changeOrderStatus(orderId, newStatus)
                        },
                        onOrderClick = { order -> viewModel.onOrderCardClicked(order) },
                        onTogglePayment = { orderId, isPaid ->
                            viewModel.toggleOrderPaymentStatus(orderId, isPaid)
                        },
                        onTogglePickup = { orderId, isPickedUp ->
                            viewModel.toggleOrderPickupStatus(orderId, isPickedUp)
                        },
                        onDismissDialog = { viewModel.onDismissOrderDetailDialog() },
                        onDeleteOrder = { orderId -> viewModel.deleteOrder(orderId) },
                        onEditOrder = { orderId ->
                            bottomNavController.navigate("${AppNavigation.Orders.route}?orderId=$orderId")
                        }
                    )
                }

                composable(AppNavigation.TableData.route) {
                    val factory = remember {
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return TableDataViewModel(
                                    workspaceUseCase
                                ) as T
                            }
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

                composable(AppNavigation.Analytics.route) {
                    val factory = remember {
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return AnalyticsViewModel(
                                    orderUseCase,
                                    serviceUseCase,
                                    customerUseCase
                                ) as T
                            }
                        }
                    }
                    val viewModel: AnalyticsViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    AnalyticsScreen(
                        uiState = uiState,
                        onTabClick = { viewModel.onTabSelected(it) },
                        onMonthSelected = { viewModel.onMonthSelected(it) },
                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onStatusFilterChanged = { viewModel.onStatusFilterChanged(it) },
                        onSortColumnClicked = { viewModel.onSortColumnClicked(it) },
                        onLoadMoreMonths = { viewModel.loadMoreMonths() },
                        onTogglePayment = { orderId, isPaid -> viewModel.toggleOrderPaymentStatus(orderId, isPaid) },
                        onTogglePickup = { orderId, isPickedUp -> viewModel.toggleOrderPickupStatus(orderId, isPickedUp) },
                        onToastShown = { viewModel.onToastShown() }
                    )
                }

                composable(AppNavigation.Customers.route) {
                    val uiState by customersViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
                        uiState.successMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
                            customersViewModel.onMessageShown()
                        }
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
                            customersViewModel.onMessageShown()
                        }
                    }

                    CustomersScreen(
                        uiState = uiState,
                        onAddCustomerClick = { name, contact -> customersViewModel.addCustomer(name, contact) },
                        onEditCustomerClick = { customer -> customersViewModel.updateCustomer(customer) },
                        onDeleteCustomerClick = { customer -> customersViewModel.deleteCustomer(customer) },
                        onCustomerSelected = { customer -> customersViewModel.onCustomerSelected(customer) },
                        onDismissDialog = { customersViewModel.onDismissEditDialog() }
                    )
                }

                composable(AppNavigation.Services.route) {
                    val factory = remember {
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ServicesViewModel(serviceUseCase) as T
                            }
                        }
                    }
                    val viewModel: ServicesViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
                        uiState.successMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
                            viewModel.onMessageShown()
                        }
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
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
                    val uiState by itemsViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
                        uiState.successMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
                            itemsViewModel.onMessageShown()
                        }
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
                            itemsViewModel.onMessageShown()
                        }
                    }

                    ItemsScreen(
                        uiState = uiState,
                        onAddItemClick = { serviceId, name, price -> itemsViewModel.addItem(serviceId, name, price) },
                        onEditItemClick = { item -> itemsViewModel.updateItem(item) },
                        onDeleteItemClick = { item -> itemsViewModel.deleteItem(item) },
                        onItemSelected = { item -> itemsViewModel.onItemSelected(item) },
                        onDismissDialog = { itemsViewModel.onDismissEditDialog() }
                    )
                }

                composable(
                    route = "${AppNavigation.Orders.route}?orderId={orderId}",
                    arguments = listOf(navArgument("orderId") {
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")

                    val factory = remember {
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return OrdersViewModel(
                                    customerUseCase,
                                    serviceUseCase,
                                    itemUseCase,
                                    orderUseCase
                                ) as T
                            }
                        }
                    }
                    val viewModel: OrdersViewModel = viewModel(factory = factory)
                    
                    // Trigger setEditingOrder when screen is loaded with orderId
                    LaunchedEffect(orderId) {
                        viewModel.setEditingOrder(orderId)
                    }

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
                        uiState.successMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
                            viewModel.onMessageShown()
                        }
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
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
                        workspaceName = mainUiState.workspaceName,
                        isOwner = mainUiState.isCurrentUserOwner,
                        onSetLocationClicked = {
                            bottomNavController.navigate("location_selection")
                        },
                        onSignOut = onSignOut
                    )
                }

                composable("location_selection") {
                    val sharedPreferences = remember { context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE) }

                    LocationSelectionPanel(
                        initialLat = settingsState.latitude,
                        initialLon = settingsState.longitude,
                        initialIsAutoMode = if (settingsState.hasFetchedLocation) settingsState.isAutoLocationMode else null,
                        onLocationSelected = { lat, lon, isGps ->
                            sharedPreferences.edit().apply {
                                putString("LOCATION_MODE", if (isGps) "AUTO" else "STATIC")
                                putFloat("STATIC_LAT", lat.toFloat())
                                putFloat("STATIC_LON", lon.toFloat())
                                apply()
                            }

                            settingsViewModel.fetchAddressAndSave(lat, lon, isAutoMode = isGps)
                            homeViewModel.fetchWeatherData(lat, lon, isGps)
                            bottomNavController.popBackStack()
                        },
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }

            if (showExitDialog) {
                ExitConfirmationDialog(
                    onDismiss = { showExitDialog = false }
                )
            }
            }
        }
    }

    // 2. PANEL NOTIFIKASI MELAYANG (Berada paling atas karena memiliki zIndex tinggi)
    Box(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        NotificationPanel(
            expanded = notificationsUiState.showNotificationOptions,
            notifications = notificationsUiState.filteredNotifications,
            currentUid = notificationsUiState.currentUserUid,
            filter = notificationsUiState.filter,
            onFilterChange = { notificationsViewModel.onFilterChanged(it) },
            onMarkAllAsRead = { notificationsViewModel.markAllAsRead() },
            onDismiss = { notificationsViewModel.onDismissNotificationOptions() },
            onNotificationClick = { notif ->
                notificationsViewModel.markNotificationAsRead(notif)
            }
        )
    }

    // 2.5 OVERLAY VOICE AGENT (Berada di belakang AI Agent Panel jika terbuka)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .zIndex(99f), // Tetap di depan elemen UI dasar, tapi di bawah AI Panel (100f)
        contentAlignment = Alignment.TopCenter
    ) {
        VoiceAgentOverlay(
            status = aiAgentUiState.voiceAgentStatus,
            text = aiAgentUiState.voiceAgentText,
            lastMessage = aiAgentUiState.messages.lastOrNull(),
            isAlreadyAnimated = aiAgentViewModel.wasMessageAnimated(aiAgentUiState.messages.lastOrNull()?.id ?: ""),
            customers = aiAgentUiState.customers,
            items = aiAgentUiState.items,
            services = aiAgentUiState.services,
            onConfirmAction = { updatedAction ->
                val lastMessage = aiAgentUiState.messages.lastOrNull()
                if (lastMessage != null) {
                    aiAgentViewModel.onConfirmAction(lastMessage.id, updatedAction)
                }
            },
            onCancelAction = {
                val lastMessage = aiAgentUiState.messages.lastOrNull()
                if (lastMessage != null) {
                    aiAgentViewModel.onCancelAction(lastMessage.id)
                }
            },
            onInteractionChange = { aiAgentViewModel.onVoiceInteractionChanged(it) },
            onDismissListening = { aiAgentViewModel.onStopVoiceAgent() },
            onDismissThinking = { aiAgentViewModel.onStopProcessing() },
            onDismissAnswering = { aiAgentViewModel.onDismissVoiceOverlay() }
        )
    }

    // 2.6 OVERLAY PREVIEW NOTIFIKASI JATUH (TANPA POPUP)
    if (notificationsUiState.notificationPreviews.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize() // Memenuhi layar agar notif bisa jatuh sampai bawah
                // Box kosong di Compose TIDAK memblokir sentuhan (touch pass-through)
                .padding(top = 58.dp, end = 24.dp) // Jarak dari atas dan kanan, sesuaikan sedikit agar pas di bawah lonceng
                .zIndex(98f) // Berada satu tingkat di bawah Voice Agent Overlay (99f)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .wrapContentSize(), // Kotak kolom hanya sebesar notifikasinya
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                notificationsUiState.notificationPreviews.forEachIndexed { index, notif ->
                    key(notif.notificationId) {
                        NotificationPreviewItem(
                            modifier = Modifier.zIndex(notificationsUiState.notificationPreviews.size - index.toFloat()),
                            notification = notif,
                            onClick = {
                                if (notif.title == "Order Baru" ||
                                    notif.title == "Order Diperbarui" ||
                                    notif.title == "New Order" ||
                                    notif.title == "Order Updated"
                                ) {
                                    bottomNavController.navigate(AppNavigation.ManageOrder.route)
                                }
                                notificationsViewModel.removeNotificationPreview(notif.notificationId, true)
                            },
                            onRemove = { wasSwiped ->
                                notificationsViewModel.removeNotificationPreview(notif.notificationId, wasSwiped)
                            }
                        )
                    }
                }
            }
        }
    }

    // 2.7 PANEL AI AGENT (Berada paling atas karena memiliki zIndex tertinggi)
    Box(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        AiAgentPanel(
            expanded = aiAgentUiState.expanded,
            userName = userData?.displayName ?: stringResource(R.string.common_unknown),
            profilePictureUrl = userData?.profilePictureUrl,
            inputMessage = aiAgentUiState.inputMessage,
            messages = aiAgentUiState.messages,
            isAiThinking = aiAgentUiState.isAiThinking,
            isTypewriterActive = aiAgentUiState.isTypewriterActive,
            currentModelName = aiAgentUiState.currentModelName,
            modelStatus = aiAgentUiState.modelStatus,
            cooldownSeconds = aiAgentUiState.cooldownSeconds,
            customers = aiAgentUiState.customers,
            items = aiAgentUiState.items,
            services = aiAgentUiState.services,
            animatedMessageIds = aiAgentUiState.animatedMessageIds,
            wasMessageAnimated = { aiAgentViewModel.wasMessageAnimated(it) },
            onMessageAnimated = { aiAgentViewModel.markMessageAsAnimated(it) },
            getAnimationProgress = { aiAgentViewModel.getAnimationProgress(it) },
            onInputChange = { aiAgentViewModel.onInputChange(it) },
            onSendMessage = { aiAgentViewModel.onSendMessage() },
            onStopProcessing = { aiAgentViewModel.onStopProcessing() },
            onClearHistory = { aiAgentViewModel.onClearHistory() },
            onConfirmAction = { id, action -> aiAgentViewModel.onConfirmAction(id, action) },
            onCancelAction = { aiAgentViewModel.onCancelAction(it) },
            onDismiss = { aiAgentViewModel.onDismissAiAgent() }
        )
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
            workspaceName = mainUiState.workspaceName,
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

private fun navRouteOrderIndex(route: String?): Int {
    val cleanRoute = route?.substringBefore("?")
    return when (cleanRoute) {
        AppNavigation.Home.route -> 0
        AppNavigation.ManageOrder.route -> 1
        AppNavigation.Orders.route -> 2
        AppNavigation.TableData.route -> 3
        AppNavigation.Customers.route,
        AppNavigation.Services.route,
        AppNavigation.Items.route -> 4
        AppNavigation.Analytics.route -> 5
        AppNavigation.Settings.route -> 6
        "location_selection" -> 7
        AppNavigation.Contributors.route -> 8
        else -> 0
    }
}
