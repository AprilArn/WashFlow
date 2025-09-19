package com.aprilarn.washflow.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.ServiceRepository  // <- IMPORT BARU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine // <- IMPORT BARU
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemsViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState = _uiState.asStateFlow()

    private val itemRepository = ItemRepository()
    private val serviceRepository = ServiceRepository() // <- REPOSITORY BARU

    init {
        // listenForItemChanges()
        listenForDataChanges()
    }

//    private fun listenForItemChanges() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            itemRepository.getItemsRealtime()
//                .catch { e ->
//                    // Tangani error jika flow gagal
//                    _uiState.update {
//                        it.copy(
//                            errorMessage = "Failed to listen for item data.",
//                            isLoading = false
//                        )
//                    }
//                }
//                .collect { customers ->
//                    // Setiap kali data baru datang dari flow, perbarui UI state
//                    _uiState.update {
//                        it.copy(
//                            items = customers,
//                            isLoading = false
//                        )
//                    }
//                }
//        }
//    }

    private fun listenForDataChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Ambil flow dari kedua repository
            val itemsFlow = itemRepository.getItemsRealtime()
            val servicesFlow = serviceRepository.getServicesRealtime()

            // Gabungkan flow items dan services
            combine(itemsFlow, servicesFlow) { items, services ->
                // Buat state baru dengan kedua data
                _uiState.update {
                    it.copy(
                        items = items,
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
            val success = itemRepository.addItem(service, name, price)
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
            val success = itemRepository.updateItem(
                item.itemId,
                item.serviceId,
                item.itemName,
                item.itemPrice
            )
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
            val success = itemRepository.deleteItems(item.itemId)
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