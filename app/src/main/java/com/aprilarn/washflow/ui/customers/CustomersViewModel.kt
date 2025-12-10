package com.aprilarn.washflow.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState = _uiState.asStateFlow()

    private val customerRepository = CustomerRepository()

    init {
        listenForCustomerChanges()
    }

    private fun listenForCustomerChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            customerRepository.getCustomersRealtime()
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
            val success = customerRepository.addCustomer(name, contact)
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
            val success = customerRepository.updateCustomer(
                customer.customerId,
                customer.name,
                customer.contact ?: ""
            )
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
            val success = customerRepository.deleteCustomer(customer.customerId)
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