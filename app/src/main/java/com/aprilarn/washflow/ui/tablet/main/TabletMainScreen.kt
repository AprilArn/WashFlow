package com.aprilarn.washflow.ui.tablet.main

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.main.MainAppScreen
import com.aprilarn.washflow.ui.main.MainViewModel

/**
 * Shell Utama khusus Tablet (Landscape Multi-Panel dengan Floating Top Header & Center Navigation Pill).
 */
@Composable
fun TabletMainScreen(
    mainViewModel: MainViewModel,
    userData: UserData?,
    onSignOut: () -> Unit
) {
    MainAppScreen(
        mainViewModel = mainViewModel,
        userData = userData,
        onSignOut = onSignOut
    )
}
