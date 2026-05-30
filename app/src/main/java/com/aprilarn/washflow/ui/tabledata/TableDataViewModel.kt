package com.aprilarn.washflow.ui.tabledata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TableDataViewModel(
    private val customerRepository: CustomerRepository,
    private val serviceRepository: ServiceRepository,
    private val itemRepository: ItemRepository,
    private val workspaceRepository: WorkspaceRepository = WorkspaceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForMetadata()
    }

    private fun listenForMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val workspaceId = workspaceRepository.getCurrentWorkspaceId() ?: run {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            workspaceRepository.getMetadataRealtime(workspaceId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    e.printStackTrace()
                }
                .collect { metadata ->
                    // Jika metadata kosong (misal baru pertama kali), jalankan sync
                    // Catatan: Jika memang datanya 0 semua, sync tetap dijalankan sekali.
                    // Idealnya ada flag khusus atau pengecekan eksistensi dokumen.
                    if (metadata.customerCount == 0 && metadata.serviceCount == 0 && metadata.itemCount == 0) {
                        workspaceRepository.syncMetadata(workspaceId)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            customerCount = metadata.customerCount,
                            serviceCount = metadata.serviceCount,
                            itemCount = metadata.itemCount
                        )
                    }
                }
        }
    }
}