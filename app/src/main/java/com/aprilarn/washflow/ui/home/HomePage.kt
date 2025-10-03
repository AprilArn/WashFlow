package com.aprilarn.washflow.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomePage(
    homeViewModel: HomeViewModel = viewModel(),
    onEnterDataClick: () -> Unit,
    onStatusCardClick: () -> Unit
) {
    // Menggunakan `collectAsState` untuk mengobservasi StateFlow
    // UI akan otomatis recompose setiap kali ada update dari ViewModel
    val state by homeViewModel.uiState.collectAsState()

    HomeScreen(
        state = state,
        onEnterDataClick = onEnterDataClick,
        onStatusCardClick = onStatusCardClick
    )
}