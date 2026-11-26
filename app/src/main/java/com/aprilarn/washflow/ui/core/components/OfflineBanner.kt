package com.aprilarn.washflow.ui.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.core.theme.AlertRed
import com.aprilarn.washflow.ui.core.theme.SuccessGreen
import com.aprilarn.washflow.ui.core.theme.WarningAmber
import com.aprilarn.washflow.ui.core.theme.WashFlowTheme
import com.aprilarn.washflow.utils.NetworkStatus
import kotlinx.coroutines.delay

@Composable
fun OfflineBanner(
    networkStatus: NetworkStatus,
    modifier: Modifier = Modifier,
    onOccupyingSpaceChanged: (Boolean) -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(networkStatus != NetworkStatus.Connected) }
    var displayedStatus by remember { mutableStateOf(networkStatus) }
    var hadConnectivityIssue by remember { mutableStateOf(networkStatus != NetworkStatus.Connected) }

    LaunchedEffect(networkStatus) {
        if (networkStatus != NetworkStatus.Connected) {
            hadConnectivityIssue = true
            displayedStatus = networkStatus
            isVisible = true
        } else {
            if (isVisible && hadConnectivityIssue) {
                // Sebelumnya bermasalah, lalu koneksi kembali normal
                displayedStatus = NetworkStatus.Connected
                hadConnectivityIssue = false
                delay(3000)
                isVisible = false
            } else {
                isVisible = false
                hadConnectivityIssue = false
            }
        }
    }

    val targetColor = when (displayedStatus) {
        NetworkStatus.Offline,
        NetworkStatus.NoInternet -> AlertRed
        NetworkStatus.PoorConnection -> WarningAmber
        NetworkStatus.Connected -> SuccessGreen
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(400),
        label = "BannerBackgroundColor"
    )

    val (icon, displayText) = when (displayedStatus) {
        NetworkStatus.Offline -> Icons.Rounded.WifiOff to
            "Tidak ada koneksi internet. Beberapa fitur terbatas."
        NetworkStatus.NoInternet -> Icons.Rounded.CloudOff to
            "Terhubung ke jaringan, namun tidak ada akses internet."
        NetworkStatus.PoorConnection -> Icons.Rounded.NetworkCheck to
            "Koneksi internet lambat. Sinkronisasi mungkin tertunda."
        NetworkStatus.Connected -> Icons.Rounded.Wifi to
            "Koneksi internet kembali normal."
    }

    OfflineBannerContent(
        isVisible = isVisible,
        backgroundColor = backgroundColor,
        icon = icon,
        displayText = displayText,
        modifier = modifier,
        onOccupyingSpaceChanged = onOccupyingSpaceChanged
    )
}

@Composable
fun OfflineBannerContent(
    isVisible: Boolean,
    backgroundColor: Color,
    icon: ImageVector,
    displayText: String,
    modifier: Modifier = Modifier,
    onOccupyingSpaceChanged: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    val navBarBottomPx = WindowInsets.navigationBars.getBottom(density)
    val visibilityState = remember { MutableTransitionState(isVisible) }
    visibilityState.targetState = isVisible

    val isOccupyingSpace = visibilityState.currentState || visibilityState.targetState
    LaunchedEffect(isOccupyingSpace) {
        onOccupyingSpaceChanged(isOccupyingSpace)
    }

    AnimatedVisibility(
        visibleState = visibilityState,
        enter = expandVertically(
            animationSpec = tween(300),
            expandFrom = Alignment.Top,
            initialHeight = { fullHeight -> navBarBottomPx.coerceAtMost(fullHeight) }
        ) + fadeIn(animationSpec = tween(220)),
        exit = shrinkVertically(
            animationSpec = tween(300),
            shrinkTowards = Alignment.Top,
            targetHeight = { fullHeight -> navBarBottomPx.coerceAtMost(fullHeight) }
        ) + fadeOut(animationSpec = tween(220)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = displayText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "1. Offline State")
@Composable
fun OfflineBannerPreview() {
    WashFlowTheme {
        OfflineBannerContent(
            isVisible = true,
            backgroundColor = AlertRed,
            icon = Icons.Rounded.WifiOff,
            displayText = "Tidak ada koneksi internet. Beberapa fitur terbatas."
        )
    }
}

@Preview(showBackground = true, name = "2. No Internet State")
@Composable
fun OfflineBannerNoInternetPreview() {
    WashFlowTheme {
        OfflineBannerContent(
            isVisible = true,
            backgroundColor = AlertRed,
            icon = Icons.Rounded.CloudOff,
            displayText = "Terhubung ke jaringan, namun tidak ada akses internet."
        )
    }
}

@Preview(showBackground = true, name = "3. Poor Connection State")
@Composable
fun OfflineBannerPoorConnectionPreview() {
    WashFlowTheme {
        OfflineBannerContent(
            isVisible = true,
            backgroundColor = WarningAmber,
            icon = Icons.Rounded.NetworkCheck,
            displayText = "Koneksi internet lambat. Sinkronisasi mungkin tertunda."
        )
    }
}

@Preview(showBackground = true, name = "4. Reconnected State")
@Composable
fun OfflineBannerReconnectedPreview() {
    WashFlowTheme {
        OfflineBannerContent(
            isVisible = true,
            backgroundColor = SuccessGreen,
            icon = Icons.Rounded.Wifi,
            displayText = "Koneksi internet kembali normal."
        )
    }
}
