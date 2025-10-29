// com/aprilarn/washflow/ui/MainViewModel.kt
package com.aprilarn.washflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.InviteRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

sealed class MainNavigationEvent {
    object NavigateToWorkspace : MainNavigationEvent()
}

class MainViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val inviteRepository: InviteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainNavigationEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        listenForWorkspaceChanges()
        listenForActiveInvite()
    }

//    private fun listenForWorkspaceChanges() {
//        viewModelScope.launch {
//            workspaceRepository.getCurrentWorkspaceRealtime().collect { workspace ->
//                val currentUser = Firebase.auth.currentUser
//                val isOwner = if (workspace != null && currentUser != null) {
//                    workspace.contributors?.get(currentUser.uid) == "owner"
//                } else {
//                    false
//                }
//
//                _uiState.update {
//                    it.copy(
//                        workspaceName = workspace?.workspaceName ?: "My Workspace",
//                        isCurrentUserOwner = isOwner
//                    )
//                }
//            }
//        }
//    }

    private fun listenForWorkspaceChanges() {
        viewModelScope.launch {
            workspaceRepository.getCurrentWorkspaceRealtime().collect { workspace ->

                // --- LOGIKA KICK-OUT DIHAPUS ---
                // 'UserRepository' sudah memvalidasi workspace saat login.
                // if (workspace == null) {
                //     _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
                //     return@collect
                // }
                // --- AKHIR PENGHAPUSAN ---

                val currentUser = Firebase.auth.currentUser
                val isOwner = if (workspace != null && currentUser != null) {
                    workspace.contributors?.get(currentUser.uid) == "owner"
                } else {
                    false
                }

                _uiState.update {
                    it.copy(
                        // Tampilkan "Loading..." jika workspace null (misal: saat user baru 'leave')
                        workspaceName = workspace?.workspaceName ?: "Loading...",
                        isCurrentUserOwner = isOwner
                    )
                }
            }
        }
    }

    private fun listenForActiveInvite() {
        viewModelScope.launch {
            inviteRepository.getActiveInviteForCurrentWorkspace().collect { invite ->
                _uiState.update { it.copy(activeInvite = invite, isInviteLoading = false) }
            }
        }
    }

    fun createInvitation(maxContributors: Int, expiresAt: Date) {
        viewModelScope.launch {
            // Convert Date to Firebase Timestamp before sending to repository
            inviteRepository.createInvite(maxContributors, Timestamp(expiresAt))
            // Hide the dialog after creation, the listener will pick up the new active code
        }
    }

    fun deleteInvitation() {
        // Get the active invite ID from the state
        val inviteId = _uiState.value.activeInvite?.inviteId ?: return
        viewModelScope.launch {
            inviteRepository.expireInvite(inviteId)
            // The realtime listener will automatically update the UI to show no active invite
        }
    }

    // --- UI INTERACTION HANDLERS ---

    fun onWorkspaceNameClicked() {
        _uiState.update { it.copy(showWorkspaceOptions = true) }
    }

    fun onDismissWorkspaceOptions() {
        _uiState.update { it.copy(showWorkspaceOptions = false) }
    }

    fun showRenameDialog() {
        _uiState.update { it.copy(showWorkspaceOptions = false, showRenameDialog = true) }
    }

    fun onDismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false) }
    }

    fun renameWorkspace(newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                workspaceRepository.updateWorkspaceName(newName)
            }
            onDismissRenameDialog()
        }
    }

    fun onAddNewContributorClicked() {
        // Ubah ini untuk menjalankan pengecekan di background SEBELUM menampilkan dialog
        viewModelScope.launch {

            // 1. Panggil fungsi baru untuk mengecek dan mengubah status jika perlu
            inviteRepository.checkAndExpireActiveInvite()

            // 2. (PENTING) Listener 'listenForActiveInvite' akan otomatis
            // mengambil perubahan status (jika ada) dan memperbarui _uiState.

            // 3. Setelah pengecekan selesai, tampilkan dialog.
            // Logika di MainActivity akan menampilkan dialog yang benar
            // (Gambar 1 atau 2) berdasarkan _uiState yang sudah ter-update.
            _uiState.update { it.copy(showCreateInviteDialog = true, showWorkspaceOptions = false) }
        }
    }

    fun onDismissCreateInviteDialog() {
        _uiState.update { it.copy(showCreateInviteDialog = false) }
    }

    fun onLeaveWorkspaceClicked() {
        _uiState.update {
            it.copy(showWorkspaceOptions = false, showLeaveWorkspaceDialog = true)
        }
    }

    fun onDismissLeaveWorkspaceDialog() {
        _uiState.update { it.copy(showLeaveWorkspaceDialog = false) }
    }

    fun confirmLeaveWorkspace() {
        viewModelScope.launch {
            _uiState.update { it.copy(showLeaveWorkspaceDialog = false) }
            val success = workspaceRepository.leaveWorkspace()
            if (success) {
                // Kirim event untuk navigasi
                _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
            }
            // Jika gagal, bisa tambahkan event untuk menampilkan error
        }
    }

    // --- FUNGSI BARU UNTUK DELETE WORKSPACE ---

    fun onDeleteWorkspaceClicked() {
        _uiState.update {
            it.copy(showWorkspaceOptions = false, showDeleteWorkspaceDialog = true)
        }
    }

    fun onDismissDeleteWorkspaceDialog() {
        _uiState.update { it.copy(showDeleteWorkspaceDialog = false) }
    }

    fun confirmDeleteWorkspace() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteWorkspaceDialog = false) }
            val success = workspaceRepository.deleteCurrentWorkspace()
            if (success) {
                // Kirim event untuk navigasi kembali ke WorkspaceScreen
                _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
            }
            // TODO: Tambahkan penanganan error jika 'success' adalah false
        }
    }
}