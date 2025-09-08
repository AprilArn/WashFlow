// com/aprilarn/washflow/ui/login/LoginUiState.kt
package com.aprilarn.washflow.ui.login

data class LoginUiState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isCheckingWorkspace: Boolean = false,   // Untuk menampilkan loading
    val userHasWorkspace: Boolean? = null      // null: belum dicek, true: ada, false: tidak ada
)