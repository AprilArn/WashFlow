package com.aprilarn.washflow

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
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.data.repository.InviteRepository
import com.aprilarn.washflow.data.repository.UserRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.aprilarn.washflow.domain.usecase.ManageInviteUseCase
import com.aprilarn.washflow.domain.usecase.ManageUserUseCase
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import com.aprilarn.washflow.ui.core.adaptive.LocalWindowLayoutInfo
import com.aprilarn.washflow.ui.core.adaptive.ProvideWindowLayoutInfo
import com.aprilarn.washflow.ui.core.adaptive.isTabletDevice
import com.aprilarn.washflow.ui.core.components.OfflineBanner
import com.aprilarn.washflow.ui.core.components.WeatherBackgroundIllustration
import com.aprilarn.washflow.ui.core.theme.WashFlowTheme
import com.aprilarn.washflow.ui.feature.login.GoogleAuthUiClient
import com.aprilarn.washflow.ui.feature.login.LoginScreen
import com.aprilarn.washflow.ui.feature.login.LoginViewModel
import com.aprilarn.washflow.ui.feature.login.PhoneLoginScreen
import com.aprilarn.washflow.ui.feature.main.MainAppScreen
import com.aprilarn.washflow.ui.feature.main.MainNavigationEvent
import com.aprilarn.washflow.ui.feature.main.MainViewModel
import com.aprilarn.washflow.ui.feature.main.PhoneMainScreen
import com.aprilarn.washflow.ui.feature.workspace.PhoneWorkspaceScreen
import com.aprilarn.washflow.ui.feature.workspace.WorkspaceEvent
import com.aprilarn.washflow.ui.feature.workspace.WorkspaceScreen
import com.aprilarn.washflow.ui.feature.workspace.WorkspaceViewModel
import com.aprilarn.washflow.utils.NetworkMonitor
import com.aprilarn.washflow.utils.NetworkStatus
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private val networkMonitor by lazy {
        NetworkMonitor(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (isTabletDevice()) {
            hideSystemBars()
        }
        setContent {
            ProvideWindowLayoutInfo {
                val windowLayoutInfo = LocalWindowLayoutInfo.current
                WashFlowTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        val networkStatus by networkMonitor.networkStatus.collectAsStateWithLifecycle(
                            initialValue = NetworkStatus.Connected
                        )
                        var isBannerOccupyingSpace by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .then(
                                        if (isBannerOccupyingSpace) {
                                            Modifier.consumeWindowInsets(
                                                androidx.compose.foundation.layout.WindowInsets.navigationBars
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                // 1. Navigasi utama aplikasi diatur di sini, dimulai dari "login"
                                val navController = rememberNavController()

                                NavHost(navController = navController, startDestination = "login") {

                            // 2. Rute untuk Login Screen (Adaptif: Tablet vs Phone)
                            composable("login") {
                                val loginViewModelFactory = remember {
                                    object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return LoginViewModel(
                                                ManageUserUseCase(UserRepository())
                                            ) as T
                                        }
                                    }
                                }
                                val viewModel = viewModel<LoginViewModel>(factory = loginViewModelFactory)
                                val state by viewModel.state.collectAsStateWithLifecycle()

                                LaunchedEffect(Unit) {
                                    val currentUser = googleAuthUiClient.getSignedUser()
                                    if (currentUser != null) {
                                        viewModel.checkAutoLogin(currentUser)
                                    }
                                }

                                LaunchedEffect(networkStatus) {
                                    viewModel.onNetworkStatusChanged(networkStatus)
                                }

                                LaunchedEffect(key1 = state.userHasWorkspace, key2 = state.showTimeoutDialog) {
                                    if (state.showTimeoutDialog) {
                                        return@LaunchedEffect
                                    }

                                    if (state.userHasWorkspace == true) {
                                        Toast.makeText(applicationContext, "Welcome back!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                        viewModel.onNavigationComplete()
                                    } else if (state.userHasWorkspace == false) {
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
                                                val signInResult = googleAuthUiClient.signInWithIntent(
                                                    intent = result.data ?: return@launch
                                                )
                                                viewModel.onSignInResult(signInResult)
                                            }
                                        }
                                    }
                                )

                                val loginPrefs = remember {
                                    applicationContext.getSharedPreferences("WashFlowPrefs", MODE_PRIVATE)
                                }
                                val cachedLoginWeather = remember {
                                    loginPrefs.getString("WEATHER_DESC", "Sunny") ?: "Sunny"
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    WeatherBackgroundIllustration(
                                        weather = cachedLoginWeather,
                                        modifier = Modifier.matchParentSize()
                                    )
                                    val onGoogleSignIn: () -> Unit = {
                                        lifecycleScope.launch {
                                            val signInIntentSender = googleAuthUiClient.signIn()
                                            launcher.launch(
                                                IntentSenderRequest.Builder(
                                                    signInIntentSender ?: return@launch
                                                ).build()
                                            )
                                        }
                                    }
                                    if (windowLayoutInfo.isTablet) {
                                        LoginScreen(
                                            state = state,
                                            onGoogleSignInClick = onGoogleSignIn,
                                            onRetryClick = { viewModel.retryWorkspaceCheck() },
                                            onTimeoutDialogDismiss = { viewModel.resetLoginState() }
                                        )
                                    } else {
                                        PhoneLoginScreen(
                                            state = state,
                                            onGoogleSignInClick = onGoogleSignIn,
                                            onRetryClick = { viewModel.retryWorkspaceCheck() },
                                            onTimeoutDialogDismiss = { viewModel.resetLoginState() }
                                        )
                                    }
                                }
                            }

                            // Rute untuk Workspace Screen (Adaptif: Tablet vs Phone)
                            composable("workspace") {
                                val workspaceViewModelFactory = remember {
                                    object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return WorkspaceViewModel(
                                                ManageWorkspaceUseCase(WorkspaceRepository()),
                                                ManageInviteUseCase(InviteRepository())
                                            ) as T
                                        }
                                    }
                                }
                                val viewModel = viewModel<WorkspaceViewModel>(factory = workspaceViewModelFactory)
                                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                                val context = LocalContext.current

                                LaunchedEffect(Unit) {
                                    viewModel.eventFlow.collect { event ->
                                        when (event) {
                                            is WorkspaceEvent.NavigateToDashboard -> {
                                                navController.navigate("main") {
                                                    popUpTo("workspace") { inclusive = true }
                                                }
                                            }
                                            is WorkspaceEvent.NavigateToLogin -> {
                                                navController.navigate("login") {
                                                    popUpTo("workspace") { inclusive = true }
                                                }
                                                Toast.makeText(applicationContext, "Signed out", Toast.LENGTH_SHORT).show()
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
                                    if (windowLayoutInfo.isTablet) {
                                        WorkspaceScreen(
                                            state = uiState,
                                            onJoinClick = { code -> viewModel.joinWorkspace(code) },
                                            onCreateWorkspaceClick = { viewModel.createWorkspace() },
                                            onSwitchAccountClick = { viewModel.signOut() }
                                        )
                                    } else {
                                        PhoneWorkspaceScreen(
                                            state = uiState,
                                            onJoinClick = { code -> viewModel.joinWorkspace(code) },
                                            onCreateWorkspaceClick = { viewModel.createWorkspace() },
                                            onSwitchAccountClick = { viewModel.signOut() }
                                        )
                                    }
                                }
                            }

                            composable("main") {
                                val mainViewModelFactory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return MainViewModel(
                                            ManageWorkspaceUseCase(WorkspaceRepository()),
                                            ManageInviteUseCase(InviteRepository()),
                                            applicationContext.getSharedPreferences("WashFlowPrefs", MODE_PRIVATE)
                                        ) as T
                                    }
                                }
                                val mainViewModel: MainViewModel = viewModel(factory = mainViewModelFactory)
                                val userData = googleAuthUiClient.getSignedUser()

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

                                val handleSignOut: () -> Unit = {
                                    lifecycleScope.launch {
                                        try {
                                            googleAuthUiClient.signOut()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                        Toast.makeText(applicationContext, "Signed out", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                if (windowLayoutInfo.isTablet) {
                                    MainAppScreen(
                                        mainViewModel = mainViewModel,
                                        userData = userData,
                                        onSignOut = handleSignOut
                                    )
                                } else {
                                    PhoneMainScreen(
                                        mainViewModel = mainViewModel,
                                        userData = userData,
                                        onSignOut = handleSignOut
                                    )
                                }
                            }
                        }
                            }

                            // Banner status koneksi global di bawah layout (mendorong konten ke atas tanpa menutupi)
                            OfflineBanner(
                                networkStatus = networkStatus,
                                onOccupyingSpaceChanged = { isBannerOccupyingSpace = it }
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


//@Preview(showBackground = true, widthDp = 960, heightDp = 600)
//@Composable
//fun WashFlowAppPreview() {
//    WashFlowTheme {
//        MainAppScreen()
//    }
//}