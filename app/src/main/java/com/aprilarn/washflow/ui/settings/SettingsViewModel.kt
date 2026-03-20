package com.aprilarn.washflow.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun updateLocation(name: String, lat: Double, lon: Double) {
        _uiState.update { currentState ->
            currentState.copy(
                locationName = name,
                latitude = lat,
                longitude = lon
            )
        }
    }
}