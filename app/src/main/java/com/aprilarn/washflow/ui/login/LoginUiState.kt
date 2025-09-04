package com.aprilarn.washflow.ui.login

data class LoginUiState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)