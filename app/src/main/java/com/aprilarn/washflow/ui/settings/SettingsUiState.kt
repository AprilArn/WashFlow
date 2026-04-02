package com.aprilarn.washflow.ui.settings

data class SettingsUiState(
    val locationName: String = "Pilih Lokasi",
    val latitude: Double? = null,
    val longitude: Double? = null
)