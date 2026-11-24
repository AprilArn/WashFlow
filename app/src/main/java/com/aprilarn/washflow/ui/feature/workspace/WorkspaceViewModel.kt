package com.aprilarn.washflow.ui.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.domain.usecase.ManageInviteUseCase
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Event untuk navigasi atau menampilkan pesan
sealed class WorkspaceEvent {
    object NavigateToDashboard : WorkspaceEvent()
    object NavigateToLogin : WorkspaceEvent() // New event
    data class ShowError(val message: String) : WorkspaceEvent()
}

class WorkspaceViewModel(
    private val workspaceUseCase: ManageWorkspaceUseCase,
    private val inviteUseCase: ManageInviteUseCase
) : ViewModel() {
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
            val success = workspaceUseCase.createWorkspace()
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

            // 2. Panggil dari inviteUseCase dan tangani return type baru (Pair)
            val result = inviteUseCase.joinWorkspace(inviteCode)

            if (result.first) { // 'first' berisi Boolean (sukses/gagal)
                _eventFlow.emit(WorkspaceEvent.NavigateToDashboard)
            } else {
                // 'second' berisi pesan error yang lebih spesifik
                _eventFlow.emit(WorkspaceEvent.ShowError(result.second))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                auth.signOut()
                _eventFlow.emit(WorkspaceEvent.NavigateToLogin)
            } catch (e: Exception) {
                _eventFlow.emit(WorkspaceEvent.ShowError(e.message ?: "Failed to sign out"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}