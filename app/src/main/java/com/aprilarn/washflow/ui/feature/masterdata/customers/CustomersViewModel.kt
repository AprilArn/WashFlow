package com.aprilarn.washflow.ui.feature.masterdata.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.domain.usecase.ManageCustomerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomersViewModel(
    private val customerUseCase: ManageCustomerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForCustomerChanges()
    }

    private fun listenForCustomerChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            customerUseCase.observeCustomers()
                .catch { e ->
                    // Tangani error jika flow gagal
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to listen for customer data.",
                            isLoading = false
                        )
                    }
                }
                .collect { customers ->
                    // --- URUTKAN BERDASARKAN NAMA (A-Z) ---
                    val sortedCustomers = customers.sortedBy { it.name.lowercase() }

                    // Setiap kali data baru datang dari flow, perbarui UI state
                    _uiState.update {
                        it.copy(
                            customers = sortedCustomers,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun addCustomer(name: String, contact: String) {
        if (name.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Customer name cannot be empty."
                )
            }
            return
        }

        viewModelScope.launch {
            val success = customerUseCase.addCustomer(name, contact)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Customer '$name' added successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to add customer. Please try again."
                    )
                }
            }
        }
    }

    fun updateCustomer(customer: Customers) {
        viewModelScope.launch {
            val success = customerUseCase.updateCustomer(customer)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Customer updated!",
                        selectedCustomer = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to update customer."
                    )
                }
            }
        }
    }

    fun deleteCustomer(customer: Customers) {
        viewModelScope.launch {
            val success = customerUseCase.deleteCustomer(customer.customerId)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Customer deleted!",
                        selectedCustomer = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to delete customer."
                    )
                }
            }
        }
    }

    fun onCustomerSelected(customer: Customers) {
        _uiState.update {
            it.copy(
                selectedCustomer = customer
            )
        }
    }

    fun onDismissEditDialog() {
        _uiState.update {
            it.copy(
                selectedCustomer = null
            )
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
}