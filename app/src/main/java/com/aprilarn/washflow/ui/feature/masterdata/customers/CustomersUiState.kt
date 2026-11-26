package com.aprilarn.washflow.ui.feature.masterdata.customers

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.ui.core.utils.UiText

data class CustomersUiState(
    val customers: List<Customers> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: UiText? = null,
    val errorMessage: UiText? = null,
    val selectedCustomer: Customers? = null
)