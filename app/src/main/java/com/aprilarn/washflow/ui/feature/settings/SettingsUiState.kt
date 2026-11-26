package com.aprilarn.washflow.ui.feature.settings

import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.utils.UiText

data class SettingsUiState(
    val locationName: UiText = UiText.StringResource(R.string.settings_select_location),
    val hasFetchedLocation: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)