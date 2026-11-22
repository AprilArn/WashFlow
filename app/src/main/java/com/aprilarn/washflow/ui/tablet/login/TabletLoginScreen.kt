package com.aprilarn.washflow.ui.tablet.login

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.login.LoginScreen
import com.aprilarn.washflow.ui.login.LoginUiState

/**
 * Tampilan Login khusus Tablet (Landscape / Expanded).
 */
@Composable
fun TabletLoginScreen(
    state: LoginUiState = LoginUiState(),
    onGoogleSignInClick: () -> Unit,
    onTimeoutDialogDismiss: () -> Unit = {}
) {
    LoginScreen(
        state = state,
        onGoogleSignInClick = onGoogleSignInClick,
        onTimeoutDialogDismiss = onTimeoutDialogDismiss
    )
}
