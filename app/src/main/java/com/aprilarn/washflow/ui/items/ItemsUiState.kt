package com.aprilarn.washflow.ui.items

import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services

data class ItemsUiState(
    val items: List<Items> = emptyList(),
    val services: List<Services> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedItem: Items? = null
)