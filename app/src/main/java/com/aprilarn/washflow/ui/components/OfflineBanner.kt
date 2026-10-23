package com.aprilarn.washflow.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.aprilarn.washflow.ui.theme.AlertRed
import com.aprilarn.washflow.ui.theme.SuccessGreen
import com.aprilarn.washflow.ui.theme.WashFlowTheme

@Composable
fun rememberConnectivityState(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = true) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = true
            }
            override fun onLost(network: Network) {
                value = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
            
            // Check initial state
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            value = true
        }

        awaitDispose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {}
        }
    }
}

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(isOffline) }
    var isError by remember { mutableStateOf(isOffline) }

    LaunchedEffect(isOffline) {
        if (isOffline) {
            isError = true
            isVisible = true
        } else {
            if (isVisible && isError) {
                // Sebelumnya offline, lalu online kembali
                isError = false
                delay(3000) // Tampilkan pesan hijau selama 3 detik
                isVisible = false
            } else {
                isVisible = false
                isError = false
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isError) AlertRed else SuccessGreen,
        animationSpec = tween(500),
        label = "BannerBackgroundColor"
    )

    val displayText = if (isError) {
        "Tidak ada koneksi internet. Beberapa fitur mungkin dibatasi."
    } else {
        "Koneksi internet telah pulih."
    }

    OfflineBannerContent(
        isVisible = isVisible,
        backgroundColor = backgroundColor,
        displayText = displayText,
        modifier = modifier
    )
}

@Composable
fun OfflineBannerContent(
    isVisible: Boolean,
    backgroundColor: Color,
    displayText: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(animationSpec = tween(400)) { it }, // Muncul dari bawah
        exit = slideOutVertically(animationSpec = tween(400)) { it }, // Hilang ke bawah
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
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
            displayText = "Tidak ada koneksi internet. Beberapa fitur mungkin dibatasi."
        )
    }
}

@Preview(showBackground = true, name = "2. Reconnected State")
@Composable
fun OfflineBannerReconnectedPreview() {
    WashFlowTheme {
        OfflineBannerContent(
            isVisible = true,
            backgroundColor = SuccessGreen,
            displayText = "Koneksi internet telah pulih."
        )
    }
}
