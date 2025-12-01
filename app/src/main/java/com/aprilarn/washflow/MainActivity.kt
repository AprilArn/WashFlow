package com.aprilarn.washflow

import android.Manifest
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.aprilarn.washflow.ui.components.NavigationBar
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
import com.aprilarn.washflow.ui.manage_order.ManageOrderScreen
import com.aprilarn.washflow.ui.manage_order.ManageOrderViewModel
import com.aprilarn.washflow.ui.orders.OrdersScreen
import com.aprilarn.washflow.ui.orders.OrdersViewModel
import com.aprilarn.washflow.ui.tabledata.TableDataScreen
import com.aprilarn.washflow.ui.tabledata.TableDataViewModel
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
                            // --- MAINVIEWMODEL DIBUAT DI SINI ---
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
                            MainAppScreen(mainViewModel)
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
    mainViewModel: MainViewModel
) {
    // NavController khusus untuk navigasi di dalam Bottom Navigation Bar
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header(
                modifier = Modifier.padding(bottom = 36.dp),
                workspaceName = mainUiState.workspaceName,
                onWorkspaceClick = { mainViewModel.onWorkspaceNameClicked() }
            ) {
                // --- TERUSKAN INFORMASI OWNER KE DROPDOWN ---
                WorkspaceOptionsDropdown(
                    expanded = mainUiState.showWorkspaceOptions,
                    isOwner = mainUiState.isCurrentUserOwner,
                    onDismiss = { mainViewModel.onDismissWorkspaceOptions() },
                    onRenameClicked = { mainViewModel.showRenameDialog() },
                    onAddContributorClicked = { mainViewModel.onAddNewContributorClicked() },
                    onLeaveWorkspaceClicked = { mainViewModel.onLeaveWorkspaceClicked() },
                    onDeleteWorkspaceClicked = { mainViewModel.onDeleteWorkspaceClicked() }
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 26.dp)
                    .padding(top = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(navController = bottomNavController)
            }
        },
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
        ) {
            // NavHost internal untuk mengatur layar yang diakses dari Bottom Navigation Bar
            NavHost(navController = bottomNavController, startDestination = AppNavigation.Home.route) {

                composable(AppNavigation.Home.route) {
                    LocationAwareHomePage(
                        onNavigateToManageOrder = {
                            bottomNavController.navigate(AppNavigation.ManageOrder.route)
                        }
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Settings Screen", color = Color.White)
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
    onDismiss: () -> Unit,
    onRenameClicked: () -> Unit,
    onAddContributorClicked: () -> Unit,
    onLeaveWorkspaceClicked: () -> Unit,
    onDeleteWorkspaceClicked: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Ubah Nama Workspace") },
            onClick = onRenameClicked,
            enabled = isOwner // Hanya aktif jika owner
        )
        DropdownMenuItem(
            text = { Text("Add New Contributor") },
            onClick = onAddContributorClicked,
            enabled = isOwner // Only owner can add contributors
        )
        DropdownMenuItem(
            text = { Text("Leave Workspace") },
            onClick = onLeaveWorkspaceClicked,
            enabled = !isOwner // Hanya aktif jika BUKAN owner
        )
        DropdownMenuItem(
            text = {
                // Tentukan warna teks berdasarkan apakah 'owner' atau bukan
                val textColor = if (isOwner) {
                    // Jika 'owner' (enabled), warnanya merah terang
                    MaterialTheme.colorScheme.error
                } else {
                    // Jika 'member' (disabled), warnanya merah pudar (merah keabu-abuan)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                }

                Text(
                    text = "Delete Workspace",
                    color = textColor
                )
            },
            onClick = onDeleteWorkspaceClicked,
            enabled = isOwner
        )
    }
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
fun LeaveWorkspaceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave Workspace") },
        text = { Text("Are you sure you want to leave from this workspace?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Leave")
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
    onNavigateToManageOrder: () -> Unit
) {
    val context = LocalContext.current
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(OrderRepository()) as T
        }
    }
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
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
            onEnterDataClick = {},
            onStatusCardClick = onNavigateToManageOrder
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


//@Preview(showBackground = true, widthDp = 960, heightDp = 600)
//@Composable
//fun WashFlowAppPreview() {
//    WashFlowTheme {
//        MainAppScreen()
//    }
//}