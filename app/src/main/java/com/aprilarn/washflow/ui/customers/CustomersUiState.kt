package com.aprilarn.washflow.ui.customers

import com.aprilarn.washflow.data.model.Customers

data class CustomersUiState(
    val customers: List<Customers> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedCustomer: Customers? = null
)