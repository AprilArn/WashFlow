package com.aprilarn.washflow.ui.feature.login

import com.aprilarn.washflow.ui.core.utils.UiText

data class LoginUiState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isCheckingWorkspace: Boolean = false,
    val userHasWorkspace: Boolean? = null,
    val showTimeoutDialog: Boolean = false,
    val isWaitingForNetwork: Boolean = false,
    val networkErrorMessage: UiText? = null
)
