package com.aprilarn.washflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForWorkspaceChanges()
    }

    private fun listenForWorkspaceChanges() {
        viewModelScope.launch {
            workspaceRepository.getCurrentWorkspaceRealtime().collect { workspace ->
                _uiState.update {
                    it.copy(workspaceName = workspace?.workspaceName ?: "My Workspace")
                }
            }
        }
    }
}