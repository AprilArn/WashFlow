package com.aprilarn.washflow.ui.feature.masterdata.items

import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.utils.UiText

data class ItemsUiState(
    val items: List<Items> = emptyList(),
    val services: List<Services> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: UiText? = null,
    val errorMessage: UiText? = null,
    val selectedItem: Items? = null
)