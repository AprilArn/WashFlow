package com.aprilarn.washflow.ui.feature.workspace

data class WorkspaceUiState(
    val isLoading: Boolean = false, // <-- Tambahkan properti ini
    val displayName: String = ""
)