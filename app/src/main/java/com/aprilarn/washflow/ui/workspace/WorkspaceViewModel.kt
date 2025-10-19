// com/aprilarn/washflow/ui/workspace/WorkspaceViewModel.kt
package com.aprilarn.washflow.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.InviteRepository // <-- Import yang baru
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Event untuk navigasi atau menampilkan pesan
sealed class WorkspaceEvent {
    object NavigateToDashboard : WorkspaceEvent()
    data class ShowError(val message: String) : WorkspaceEvent()
}

class WorkspaceViewModel : ViewModel() {
    private val workspaceRepository = WorkspaceRepository()
    private val inviteRepository = InviteRepository() // <-- 1. Tambahkan InviteRepository
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<WorkspaceEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        // Ambil nama pengguna saat ViewModel dibuat
        val userName = auth.currentUser?.displayName ?: "User"
        _uiState.update { it.copy(displayName = userName) }
    }

    fun createWorkspace() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = workspaceRepository.createWorkspace()
            if (success) {
                _eventFlow.emit(WorkspaceEvent.NavigateToDashboard)
            } else {
                _eventFlow.emit(WorkspaceEvent.ShowError("Failed to create workspace."))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun joinWorkspace(inviteCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 2. Panggil dari inviteRepository dan tangani return type baru (Pair)
            val result = inviteRepository.joinWorkspace(inviteCode)

            if (result.first) { // 'first' berisi Boolean (sukses/gagal)
                _eventFlow.emit(WorkspaceEvent.NavigateToDashboard)
            } else {
                // 'second' berisi pesan error yang lebih spesifik
                _eventFlow.emit(WorkspaceEvent.ShowError(result.second))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}