package com.aprilarn.washflow.ui.feature.masterdata.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.domain.usecase.ManageServiceUseCase
import com.aprilarn.washflow.ui.core.utils.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServicesViewModel(
    private val serviceUseCase: ManageServiceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForServiceChanges()
    }

    private fun listenForServiceChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            serviceUseCase.observeServices()
                .catch { e ->
                    // Tangani error jika flow gagal
                    _uiState.update {
                        it.copy(
                            errorMessage = UiText.StringResource(R.string.master_services_msg_listen_failed),
                            isLoading = false
                        )
                    }
                }
                .collect { services ->
                    // --- URUTKAN BERDASARKAN ID (A-Z) ---
                    val sortedServices = services.sortedBy { it.serviceId }

                    // Setiap kali data baru datang dari flow, perbarui UI state
                    _uiState.update {
                        it.copy(
                            services = sortedServices,
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
                    errorMessage = UiText.StringResource(R.string.master_services_msg_fields_required)
                )
            }
            return
        }

        viewModelScope.launch {
            val success = serviceUseCase.addService(id, name)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = UiText.StringResource(R.string.master_services_msg_added, name)
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = UiText.StringResource(R.string.master_services_msg_add_failed)
                    )
                }
            }
        }
    }

    fun updateService(service: Services) {
        viewModelScope.launch {
            val success = serviceUseCase.updateService(service)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = UiText.StringResource(R.string.master_services_msg_updated),
                        selectedService = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = UiText.StringResource(R.string.master_services_msg_update_failed)
                    )
                }
            }
        }
    }

    fun deleteService(service: Services) {
        viewModelScope.launch {
            val success = serviceUseCase.deleteService(service.serviceId)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = UiText.StringResource(R.string.master_services_msg_deleted),
                        selectedService = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = UiText.StringResource(R.string.master_services_msg_delete_failed)
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