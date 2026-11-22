package com.aprilarn.washflow.ui.adaptive

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Representasi kategori form-factor perangkat.
 * - [PHONE]: Perangkat HP biasa (batang) dengan smallestScreenWidthDp < 600dp.
 * - [TABLET]: Perangkat Tablet / layar lebar dengan smallestScreenWidthDp >= 600dp (sw600dp).
 */
enum class DeviceFormFactor {
    PHONE,
    TABLET
}

@Immutable
data class WindowLayoutInfo(
    val formFactor: DeviceFormFactor,
    val isLandscape: Boolean,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val smallestScreenWidthDp: Int
) {
    val isTablet: Boolean
        get() = formFactor == DeviceFormFactor.TABLET

    val isPhone: Boolean
        get() = formFactor == DeviceFormFactor.PHONE
}

private const val TABLET_MIN_SMALLEST_WIDTH_DP = 600

fun Context.isTabletDevice(): Boolean {
    return resources.configuration.smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP
}

val LocalWindowLayoutInfo = compositionLocalOf {
    WindowLayoutInfo(
        formFactor = DeviceFormFactor.TABLET,
        isLandscape = true,
        screenWidthDp = 960,
        screenHeightDp = 600,
        smallestScreenWidthDp = 600
    )
}

@Composable
fun rememberWindowLayoutInfo(): WindowLayoutInfo {
    val configuration = LocalConfiguration.current
    return remember(
        configuration.smallestScreenWidthDp,
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        configuration.orientation
    ) {
        val formFactor = if (configuration.smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP) {
            DeviceFormFactor.TABLET
        } else {
            DeviceFormFactor.PHONE
        }
        WindowLayoutInfo(
            formFactor = formFactor,
            isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            smallestScreenWidthDp = configuration.smallestScreenWidthDp
        )
    }
}

@Composable
fun ProvideWindowLayoutInfo(
    content: @Composable () -> Unit
) {
    val windowLayoutInfo = rememberWindowLayoutInfo()
    CompositionLocalProvider(LocalWindowLayoutInfo provides windowLayoutInfo) {
        content()
    }
}
