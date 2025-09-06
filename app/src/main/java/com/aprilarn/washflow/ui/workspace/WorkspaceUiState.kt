package com.aprilarn.washflow.ui.workspace

data class WorkspaceUiState(
    val displayName: String = "--",       // Ganti dengan nama pengguna yang diambil dari autentikasi
    val inputWorkspaceCode: String? = null

)