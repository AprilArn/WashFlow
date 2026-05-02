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
import androidx.compose.ui.geometry.Offset
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
import com.aprilarn.washflow.data.repository.NotificationsRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.aprilarn.washflow.ui.MainAppScreen
import com.aprilarn.washflow.ui.MainNavigationEvent
import com.aprilarn.washflow.ui.MainViewModel
import com.aprilarn.washflow.ui.login.GoogleAuthUiClient
import com.aprilarn.washflow.ui.login.LoginScreen
import com.aprilarn.washflow.ui.login.LoginViewModel
import com.aprilarn.washflow.ui.theme.MainBLue
import com.aprilarn.washflow.ui.theme.MornYellow
import com.aprilarn.washflow.ui.theme.WashFlowTheme
import com.aprilarn.washflow.ui.workspace.WorkspaceEvent
import com.aprilarn.washflow.ui.workspace.WorkspaceScreen
import com.aprilarn.washflow.ui.workspace.WorkspaceViewModel
import com.google.android.gms.auth.api.identity.Identity
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
                                    },
                                    onTimeoutDialogDismiss = {
                                        viewModel.resetLoginState()
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
                                WorkspaceScreen(
                                    state = uiState,
                                    onJoinClick = { code -> viewModel.joinWorkspace(code) },
                                    onCreateWorkspaceClick = { viewModel.createWorkspace() },
                                    onSwitchAccountClick = { viewModel.signOut() }
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
                                        InviteRepository(),
                                        NotificationsRepository()
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


//@Preview(showBackground = true, widthDp = 960, heightDp = 600)
//@Composable
//fun WashFlowAppPreview() {
//    WashFlowTheme {
//        MainAppScreen()
//    }
//}