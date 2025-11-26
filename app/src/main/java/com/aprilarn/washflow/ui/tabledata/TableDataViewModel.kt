package com.aprilarn.washflow.ui.tabledata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TableDataViewModel(
    private val customerRepository: CustomerRepository,
    private val serviceRepository: ServiceRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForDataCounts()
    }

    private fun listenForDataCounts() {
        viewModelScope.launch {
            // Gabungkan 3 flow data realtime
            combine(
                customerRepository.getCustomersRealtime(), //
                serviceRepository.getServicesRealtime(),  //
                itemRepository.getItemsRealtime()         //
            ) { customers, services, items ->
                // Saat data baru masuk, update state hanya dengan jumlahnya
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        customerCount = customers.size,
                        serviceCount = services.size,
                        itemCount = items.size
                    )
                }
            }.catch { e ->
                // Tangani error jika salah satu flow gagal
                _uiState.update { it.copy(isLoading = false) }
                e.printStackTrace()
            }.collect {} // 'collect' diperlukan untuk menjalankan flow
        }
    }
}