// com/aprilarn/washflow/ui/customers/CustomersViewModel.kt
package com.aprilarn.washflow.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState = _uiState.asStateFlow()

    private val customerRepository = CustomerRepository()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val customers = customerRepository.getCustomers()
                _uiState.update { it.copy(customers = customers, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load customers.", isLoading = false) }
            }
        }
    }

    fun addCustomer(name: String, contact: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Customer name cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = customerRepository.addCustomer(name, contact)
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Customer '$name' added successfully!"
                    )
                }
                loadCustomers()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to add customer. Please try again."
                    )
                }
            }
        }
    }

    fun updateCustomer(customer: Customers) {
        viewModelScope.launch {
            val success = customerRepository.updateCustomer(customer.customerId, customer.name, customer.contact ?: "")
            if (success) {
                // Tutup dialog setelah berhasil dan refresh data
                _uiState.update { it.copy(successMessage = "Customer updated!", selectedCustomer = null) }
                loadCustomers()
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to update customer.") }
            }
        }
    }

    fun deleteCustomer(customer: Customers) {
        viewModelScope.launch {
            val success = customerRepository.deleteCustomer(customer.customerId)
            if (success) {
                // Tutup dialog setelah berhasil dan refresh data
                _uiState.update { it.copy(successMessage = "Customer deleted!", selectedCustomer = null) }
                loadCustomers()
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to delete customer.") }
            }
        }
    }

    /**
     * Fungsi baru untuk memilih pelanggan dan menampilkan dialog edit.
     */
    fun onCustomerSelected(customer: Customers) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    /**
     * Fungsi baru untuk menutup/membatalkan dialog edit.
     */
    fun onDismissEditDialog() {
        _uiState.update { it.copy(selectedCustomer = null) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}