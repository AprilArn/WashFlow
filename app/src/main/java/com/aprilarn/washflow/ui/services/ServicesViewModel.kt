package com.aprilarn.washflow.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState = _uiState.asStateFlow()

    private val serviceRepository = ServiceRepository()

    init {
        listenForServiceChanges()
    }

    private fun listenForServiceChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            serviceRepository.getServicesRealtime()
                .catch { e ->
                    // Tangani error jika flow gagal
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to listen for service data.",
                            isLoading = false
                        )
                    }
                }
                .collect { services ->
                    // Setiap kali data baru datang dari flow, perbarui UI state
                    _uiState.update {
                        it.copy(
                            services = services,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun addService(id: String, name: String) {
        if (id.isBlank() || name.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "All fields must be filled correctly."
                )
            }
            return
        }

        viewModelScope.launch {
            val success = serviceRepository.addService(id, name)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Service '$name' added successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to add service. The ID might already exist."
                    )
                }
            }
        }
    }

    fun updateService(service: Services) {
        viewModelScope.launch {
            val success = serviceRepository.updateService(
                service.serviceId,
                service.serviceName
            )
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Service updated!",
                        selectedService = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to update service."
                    )
                }
            }
        }
    }

    fun deleteService(service: Services) {
        viewModelScope.launch {
            val success = serviceRepository.deleteService(service.serviceId)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Service deleted!",
                        selectedService = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to delete service."
                    )
                }
            }
        }
    }

    fun onServiceSelected(service: Services) {
        _uiState.update {
            it.copy(
                selectedService = service
            )
        }
    }

    fun onDismissEditDialog() {
        _uiState.update {
            it.copy(
                selectedService = null
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