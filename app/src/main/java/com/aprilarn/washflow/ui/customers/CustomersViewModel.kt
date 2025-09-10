// com/aprilarn/washflow/ui/customers/CustomersViewModel.kt
package com.aprilarn.washflow.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        // Langsung panggil fungsi untuk memuat daftar customer saat ViewModel dibuat
        loadCustomers()
    }

    /**
     * Memuat daftar customer dari Firestore dan memperbarui UI state.
     */
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
                // Panggil fungsi untuk refresh daftar customer setelah berhasil menambahkan
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

    /**
     * Dipanggil dari UI setelah pesan ditampilkan untuk membersihkan state.
     */
    fun onMessageShown() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}