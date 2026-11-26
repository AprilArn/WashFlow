package com.aprilarn.washflow.ui.feature.workspace

data class WorkspaceUiState(
    val isLoading: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null
)
