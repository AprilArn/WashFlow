package com.aprilarn.washflow.ui.feature.masterdata.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.domain.usecase.ManageItemUseCase
import com.aprilarn.washflow.domain.usecase.ManageServiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemsViewModel(
    private val itemUseCase: ManageItemUseCase,
    private val serviceUseCase: ManageServiceUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForDataChanges()
    }

    private fun listenForDataChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Ambil flow dari kedua use case
            val itemsFlow = itemUseCase.observeItems()
            val servicesFlow = serviceUseCase.observeServices()

            // Gabungkan flow items dan services
            combine(itemsFlow, servicesFlow) { items, services ->
                // --- URUTKAN BERDASARKAN SERVICE ID, LALU ITEM NAME ---
                val sortedItems = items.sortedWith(
                    compareBy<Items> { it.serviceId }
                        .thenBy { it.itemName.lowercase() }
                )

                // Buat state baru dengan kedua data
                _uiState.update {
                    it.copy(
                        items = sortedItems,
                        services = services,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update {
                    it.copy(errorMessage = "Failed to load data.", isLoading = false)
                }
            }.collect() // Mulai mengoleksi flow gabungan
        }
    }

    fun addItem(service: String, name: String, price: Double) {
        if (service.isBlank() || name.isBlank() || price <= 0.0) {
            _uiState.update {
                it.copy(
                    errorMessage = "All fields must be filled correctly."
                )
            }
            return
        }

        viewModelScope.launch {
            val success = itemUseCase.addItem(service, name, price)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Item '$name' added successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to add item. Please try again."
                    )
                }
            }
        }
    }

    fun updateItem(item: Items) {
        viewModelScope.launch {
            val success = itemUseCase.updateItem(item)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Item updated!",
                        selectedItem = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to update item."
                    )
                }
            }
        }
    }

    fun deleteItem(item: Items) {
        viewModelScope.launch {
            val success = itemUseCase.deleteItem(item.itemId)
            if (success) {
                _uiState.update {
                    it.copy(
                        successMessage = "Item deleted!",
                        selectedItem = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to delete item."
                    )
                }
            }
        }
    }

    fun onItemSelected(item: Items) {
        _uiState.update {
            it.copy(
                selectedItem = item
            )
        }
    }

    fun onDismissEditDialog() {
        _uiState.update {
            it.copy(
                selectedItem = null
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