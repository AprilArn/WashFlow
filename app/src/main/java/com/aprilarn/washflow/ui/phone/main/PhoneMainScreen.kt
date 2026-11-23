package com.aprilarn.washflow.ui.phone.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.aprilarn.washflow.AppNavigation
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.NotificationsRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.aprilarn.washflow.ui.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.aiagent.AiAgentPanel
import com.aprilarn.washflow.ui.aiagent.AiAgentViewModel
import com.aprilarn.washflow.ui.aiagent.AiModelStatus
import com.aprilarn.washflow.ui.aiagent.VoiceAgentStatus
import com.aprilarn.washflow.ui.aiagent.component.VoiceAgentOverlay
import com.aprilarn.washflow.ui.analytics.AnalyticsViewModel
import com.aprilarn.washflow.ui.components.ExitConfirmationDialog
import com.aprilarn.washflow.ui.components.KickedDialog
import com.aprilarn.washflow.ui.components.LeaveWorkspaceDialog
import com.aprilarn.washflow.ui.components.OfflineBanner
import com.aprilarn.washflow.ui.components.WeatherBackgroundIllustration
import com.aprilarn.washflow.ui.components.isDarkWeatherScene
import com.aprilarn.washflow.ui.components.rememberConnectivityState
import com.aprilarn.washflow.ui.contributors.ContributorsViewModel
import com.aprilarn.washflow.ui.customers.CustomersViewModel
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.items.ItemsViewModel
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.main.MainViewModel
import com.aprilarn.washflow.ui.manage_order.ManageOrderViewModel
import com.aprilarn.washflow.ui.notifications.NotificationPanel
import com.aprilarn.washflow.ui.notifications.NotificationPreviewItem
import com.aprilarn.washflow.ui.notifications.NotificationsViewModel
import com.aprilarn.washflow.ui.orders.OrdersViewModel
import com.aprilarn.washflow.ui.phone.analytics.PhoneAnalyticsScreen
import com.aprilarn.washflow.ui.phone.contributors.PhoneContributorsScreen
import com.aprilarn.washflow.ui.phone.home.PhoneLocationAwareHomeScreen
import com.aprilarn.washflow.ui.phone.manageorder.PhoneManageOrderScreen
import com.aprilarn.washflow.ui.phone.masterdata.PhoneCustomersScreen
import com.aprilarn.washflow.ui.phone.masterdata.PhoneItemsScreen
import com.aprilarn.washflow.ui.phone.masterdata.PhoneServicesScreen
import com.aprilarn.washflow.ui.phone.masterdata.PhoneTableDataScreen
import com.aprilarn.washflow.ui.phone.navigation.PhoneBottomNavigationBar
import com.aprilarn.washflow.ui.phone.navigation.PhoneTopHeader
import com.aprilarn.washflow.ui.phone.orders.PhoneOrdersScreen
import com.aprilarn.washflow.ui.phone.settings.PhoneSettingsScreen
import com.aprilarn.washflow.ui.services.ServicesViewModel
import com.aprilarn.washflow.ui.settings.LocationSelectionPanel
import com.aprilarn.washflow.ui.settings.SettingsViewModel
import com.aprilarn.washflow.ui.tabledata.TableDataViewModel
import com.aprilarn.washflow.ui.workspace.ActiveInviteDialog
import com.aprilarn.washflow.ui.workspace.CreateInviteDialog
import com.aprilarn.washflow.ui.workspace.DeleteWorkspaceDialog
import com.aprilarn.washflow.ui.workspace.OperationalHoursDialog
import com.aprilarn.washflow.ui.workspace.RenameWorkspaceDialog
import com.aprilarn.washflow.ui.workspace.WorkspaceOptionsDropdown
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Shell Utama khusus HP biasa (Batang / Portrait).
 * Menggunakan PhoneTopHeader ringkas di atas dan PhoneBottomNavigationBar ergonomis di bawah,
 * serta mengarahkan setiap rute ke layar khusus di dalam folder ui/phone/.
 */
@Composable
fun PhoneMainScreen(
    mainViewModel: MainViewModel,
    userData: UserData?,
    onSignOut: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    val notificationsViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationsViewModel(NotificationsRepository()) as T
            }
        }
    }
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = notificationsViewModelFactory)
    val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

    val aiAgentViewModel: AiAgentViewModel = viewModel()
    val aiAgentUiState by aiAgentViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                aiAgentViewModel.onStartVoiceAgent(context)
            } else {
                Toast.makeText(context, "Microphone permission is required for voice agent", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val orderRepository = remember { OrderRepository() }
    val customerRepository = remember { CustomerRepository() }
    val serviceRepository = remember { ServiceRepository() }
    val itemRepository = remember { ItemRepository() }
    val workspaceRepository = remember { WorkspaceRepository() }

    val customersViewModel: CustomersViewModel = viewModel()
    val itemsViewModel: ItemsViewModel = viewModel()

    LaunchedEffect(Unit) {
        aiAgentViewModel.actionEvents.collect { action ->
            when (action) {
                is AiAgentAction.Navigate -> {
                    bottomNavController.navigate(action.destination.route)
                }
                is AiAgentAction.AddCustomer -> {
                    customersViewModel.addCustomer(action.name, action.phoneNumber)
                    Toast.makeText(context, "Customer '${action.name}' added successfully!", Toast.LENGTH_SHORT).show()
                }
                is AiAgentAction.DeleteCustomer -> {
                    if (action.customerId.isNotEmpty()) {
                        val customerToDelete = Customers(
                            customerId = action.customerId,
                            name = action.name
                        )
                        customersViewModel.deleteCustomer(customerToDelete)
                        Toast.makeText(context, "Customer '${action.name}' deleted successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error: Customer ID not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                is AiAgentAction.AddItem -> {
                    itemsViewModel.addItem(action.serviceId, action.itemName, action.itemPrice)
                    Toast.makeText(context, "Item '${action.itemName}' added successfully!", Toast.LENGTH_SHORT).show()
                }
                is AiAgentAction.DeleteItem -> {
                    if (action.itemId.isNotEmpty()) {
                        val itemToDelete = Items(
                            itemId = action.itemId,
                            itemName = action.itemName
                        )
                        itemsViewModel.deleteItem(itemToDelete)
                        Toast.makeText(context, "Item '${action.itemName}' deleted successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error: Item ID not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                is AiAgentAction.Unknown -> Unit
                AiAgentAction.None -> Unit
            }
        }
    }

    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val appContext = remember(context) { context.applicationContext }
    val homeViewModelFactory = remember(appContext) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val geocodingService = retrofit.create(GeocodingApiService::class.java)
                val sharedPrefs = appContext.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)
                return HomeViewModel(orderRepository, workspaceRepository, geocodingService, sharedPrefs) as T
            }
        }
    }
    val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val isDarkWeatherBg = remember(
        homeUiState.weather,
        homeUiState.greeting,
        homeUiState.weatherType,
        homeUiState.isDaytime
    ) {
        isDarkWeatherScene(
            weatherText = homeUiState.weather,
            greetingText = homeUiState.greeting,
            weatherType = homeUiState.weatherType,
            isDaytime = homeUiState.isDaytime
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PhoneTopHeader(
                workspaceName = mainUiState.workspaceName,
                unreadCount = notificationsUiState.unreadCount,
                isDarkBackground = isDarkWeatherBg,
                isAiVoiceActive = aiAgentUiState.voiceAgentStatus != VoiceAgentStatus.IDLE ||
                        aiAgentUiState.isAiThinking ||
                        aiAgentUiState.isTypewriterActive ||
                        aiAgentUiState.modelStatus == AiModelStatus.COOLDOWN,
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
        bottomBar = {
            PhoneBottomNavigationBar(
                navController = bottomNavController,
                isDarkBackground = isDarkWeatherBg
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            WeatherBackgroundIllustration(
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
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                NavHost(
                    navController = bottomNavController,
                    startDestination = AppNavigation.Home.route,
                    enterTransition = {
                        val fromIndex = phoneNavRouteOrderIndex(initialState.destination.route)
                        val toIndex = phoneNavRouteOrderIndex(targetState.destination.route)
                        val slideDirection = if (toIndex >= fromIndex) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }
                        fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                slideIntoContainer(
                                    towards = slideDirection,
                                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                                    initialOffset = { fullSize -> fullSize / 36 }
                                )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(100, easing = FastOutSlowInEasing))
                    },
                    popEnterTransition = {
                        val fromIndex = phoneNavRouteOrderIndex(initialState.destination.route)
                        val toIndex = phoneNavRouteOrderIndex(targetState.destination.route)
                        val slideDirection = if (toIndex <= fromIndex) {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        }
                        fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                slideIntoContainer(
                                    towards = slideDirection,
                                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                                    initialOffset = { fullSize -> fullSize / 36 }
                                )
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(100, easing = FastOutSlowInEasing))
                    }
                ) {
                    composable(AppNavigation.Home.route) {
                        BackHandler(enabled = true) {
                            showExitDialog = true
                        }
                        PhoneLocationAwareHomeScreen(
                            homeViewModel = homeViewModel,
                            onNavigateToManageOrder = {
                                bottomNavController.navigate(AppNavigation.ManageOrder.route)
                            },
                            onLocationFetched = { lat, lon ->
                                if (settingsState.locationName == "Pilih Lokasi") {
                                    settingsViewModel.fetchAddressAndSave(lat, lon)
                                }
                            }
                        )
                    }

                    composable(AppNavigation.Contributors.route) {
                        val factory = remember {
                            object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return ContributorsViewModel(workspaceRepository) as T
                                }
                            }
                        }
                        val viewModel: ContributorsViewModel = viewModel(factory = factory)
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        PhoneContributorsScreen(
                            uiState = uiState,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onAddClick = { mainViewModel.onAddNewContributorClicked() },
                            onContributorClick = { viewModel.onContributorClicked(it) },
                            onDismissDialog = { viewModel.onDismissDetailDialog() },
                            onKickUser = { viewModel.kickContributor(it) }
                        )
                    }

                    composable(AppNavigation.ManageOrder.route) {
                        val factory = remember {
                            object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return ManageOrderViewModel(
                                        orderRepository,
                                        customerRepository,
                                        serviceRepository
                                    ) as T
                                }
                            }
                        }
                        val viewModel: ManageOrderViewModel = viewModel(factory = factory)
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(uiState.errorMessage) {
                            uiState.errorMessage?.let {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                viewModel.onErrorMessageShown()
                            }
                        }

                        PhoneManageOrderScreen(
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
                                        customerRepository,
                                        serviceRepository,
                                        itemRepository,
                                        workspaceRepository
                                    ) as T
                                }
                            }
                        }
                        val viewModel: TableDataViewModel = viewModel(factory = factory)
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        PhoneTableDataScreen(
                            uiState = uiState,
                            onNavigate = { route -> bottomNavController.navigate(route) }
                        )
                    }

                    composable(AppNavigation.Analytics.route) {
                        val factory = remember {
                            object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return AnalyticsViewModel(
                                        orderRepository,
                                        serviceRepository,
                                        customerRepository
                                    ) as T
                                }
                            }
                        }
                        val viewModel: AnalyticsViewModel = viewModel(factory = factory)
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        PhoneAnalyticsScreen(
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
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                customersViewModel.onMessageShown()
                            }
                            uiState.errorMessage?.let {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                customersViewModel.onMessageShown()
                            }
                        }

                        PhoneCustomersScreen(
                            uiState = uiState,
                            onAddCustomerClick = { name, contact -> customersViewModel.addCustomer(name, contact) },
                            onEditCustomerClick = { customer -> customersViewModel.updateCustomer(customer) },
                            onDeleteCustomerClick = { customer -> customersViewModel.deleteCustomer(customer) },
                            onCustomerSelected = { customer -> customersViewModel.onCustomerSelected(customer) },
                            onDismissDialog = { customersViewModel.onDismissEditDialog() }
                        )
                    }

                    composable(AppNavigation.Services.route) {
                        val viewModel: ServicesViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

                        PhoneServicesScreen(
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
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                itemsViewModel.onMessageShown()
                            }
                            uiState.errorMessage?.let {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                itemsViewModel.onMessageShown()
                            }
                        }

                        PhoneItemsScreen(
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
                                        customerRepository,
                                        serviceRepository,
                                        itemRepository,
                                        orderRepository
                                    ) as T
                                }
                            }
                        }
                        val viewModel: OrdersViewModel = viewModel(factory = factory)

                        LaunchedEffect(orderId) {
                            viewModel.setEditingOrder(orderId)
                        }

                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

                        PhoneOrdersScreen(uiState = uiState, viewModel = viewModel)
                    }

                    composable(AppNavigation.Settings.route) {
                        PhoneSettingsScreen(
                            userData = userData ?: UserData(
                                userId = "",
                                displayName = "Unknown",
                                email = "No Email",
                                profilePictureUrl = null
                            ),
                            settingsUiState = settingsState,
                            onSetLocationClicked = {
                                bottomNavController.navigate("location_selection")
                            },
                            onSignOut = onSignOut
                        )
                    }

                    composable("location_selection") {
                        val sharedPreferences = remember {
                            context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)
                        }

                        LocationSelectionPanel(
                            onLocationSelected = { lat, lon, isGps ->
                                sharedPreferences.edit().apply {
                                    putString("LOCATION_MODE", if (isGps) "AUTO" else "STATIC")
                                    putFloat("STATIC_LAT", lat.toFloat())
                                    putFloat("STATIC_LON", lon.toFloat())
                                    apply()
                                }
                                settingsViewModel.fetchAddressAndSave(lat, lon)
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

    // Floating Overlays (Notifications, Voice Agent, AI Agent Panel, Dialogs, Offline Banner)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(99f),
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

    if (notificationsUiState.notificationPreviews.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, end = 16.dp)
                .zIndex(98f)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                notificationsUiState.notificationPreviews.forEachIndexed { index, notif ->
                    key(notif.notificationId) {
                        NotificationPreviewItem(
                            modifier = Modifier.zIndex(notificationsUiState.notificationPreviews.size - index.toFloat()),
                            notification = notif,
                            onClick = {
                                if (notif.title == "Order Baru" || notif.title == "Order Diperbarui") {
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

    Box(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        AiAgentPanel(
            expanded = aiAgentUiState.expanded,
            userName = userData?.displayName ?: "Unknown",
            profilePictureUrl = userData?.profilePictureUrl,
            inputMessage = aiAgentUiState.inputMessage,
            messages = aiAgentUiState.messages,
            isAiThinking = aiAgentUiState.isAiThinking,
            isTypewriterActive = aiAgentUiState.isTypewriterActive,
            currentModelName = aiAgentUiState.currentModelName,
            modelStatus = aiAgentUiState.modelStatus,
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

    if (mainUiState.showCreateInviteDialog) {
        val activeInvite = mainUiState.activeInvite
        if (activeInvite == null) {
            CreateInviteDialog(
                onDismiss = { mainViewModel.onDismissCreateInviteDialog() },
                onGenerate = { maxUsers, expiryDate ->
                    mainViewModel.createInvitation(maxUsers, expiryDate)
                }
            )
        } else {
            ActiveInviteDialog(
                invite = activeInvite,
                onDismiss = { mainViewModel.onDismissCreateInviteDialog() },
                onDelete = { mainViewModel.deleteInvitation() }
            )
        }
    }

    if (mainUiState.showLeaveWorkspaceDialog) {
        LeaveWorkspaceDialog(
            onDismiss = { mainViewModel.onDismissLeaveWorkspaceDialog() },
            onConfirm = { mainViewModel.confirmLeaveWorkspace() }
        )
    }

    if (mainUiState.showDeleteWorkspaceDialog) {
        DeleteWorkspaceDialog(
            onDismiss = { mainViewModel.onDismissDeleteWorkspaceDialog() },
            onConfirm = { mainViewModel.confirmDeleteWorkspace() }
        )
    }

    if (mainUiState.showKickedDialog) {
        KickedDialog(
            workspaceName = mainUiState.kickedFromWorkspaceName,
            onConfirm = { mainViewModel.onKickedDialogConfirm() }
        )
    }

    val isOnline by rememberConnectivityState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(200f),
        contentAlignment = Alignment.BottomCenter
    ) {
        OfflineBanner(isOffline = !isOnline)
    }
}

private fun phoneNavRouteOrderIndex(route: String?): Int {
    val cleanRoute = route?.substringBefore("?")
    return when (cleanRoute) {
        AppNavigation.TableData.route -> 0
        AppNavigation.Customers.route,
        AppNavigation.Services.route,
        AppNavigation.Items.route -> 1
        AppNavigation.Orders.route -> 2
        AppNavigation.Home.route -> 3
        AppNavigation.ManageOrder.route -> 4
        AppNavigation.Analytics.route -> 5
        AppNavigation.Settings.route -> 6
        AppNavigation.Contributors.route -> 7
        else -> 3
    }
}
