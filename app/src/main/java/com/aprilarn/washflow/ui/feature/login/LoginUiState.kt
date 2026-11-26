package com.aprilarn.washflow.ui.feature.login

data class LoginUiState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isCheckingWorkspace: Boolean = false,   // Untuk menampilkan loading
    val userHasWorkspace: Boolean? = null,      // null: belum dicek/error jaringan, true: ada, false: tidak ada
    val showTimeoutDialog: Boolean = false,
    val isWaitingForNetwork: Boolean = false,   // True jika cek workspace gagal karena sinyal hilang/buruk
    val networkErrorMessage: String? = null
)
