package com.aprilarn.washflow.ui.feature.masterdata.services

import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.utils.UiText

data class ServicesUiState(
    val services: List<Services> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: UiText? = null,
    val errorMessage: UiText? = null,
    val selectedService: Services? = null
)