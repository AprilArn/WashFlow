package com.aprilarn.washflow.ui.services

import com.aprilarn.washflow.data.model.Services

data class ServicesUiState(
    val services: List<Services> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedService: Services? = null
)