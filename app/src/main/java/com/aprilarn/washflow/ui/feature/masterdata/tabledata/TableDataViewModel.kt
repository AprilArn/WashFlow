package com.aprilarn.washflow.ui.feature.masterdata.tabledata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TableDataViewModel(
    private val workspaceUseCase: ManageWorkspaceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForMetadata()
    }

    private fun listenForMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            workspaceUseCase.observeMetadata()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    e.printStackTrace()
                }
                .collect { metadata ->
                    // Jika metadata kosong (misal baru pertama kali), jalankan sync
                    if (metadata.customerCount == 0 && metadata.serviceCount == 0 && metadata.itemCount == 0) {
                        val workspaceId = workspaceUseCase.getCurrentWorkspaceId()
                        if (workspaceId != null) {
                            workspaceUseCase.syncMetadata(workspaceId)
                        }
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
