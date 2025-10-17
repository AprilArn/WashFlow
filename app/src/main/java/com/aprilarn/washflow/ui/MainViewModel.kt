package com.aprilarn.washflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
                // --- LOGIKA PENGECEKAN ROLE DITAMBAHKAN DI SINI ---
                val currentUser = Firebase.auth.currentUser
                val isOwner = if (workspace != null && currentUser != null) {
                    // Cek apakah UID pengguna saat ini ada di map contributors dengan role "owner"
                    workspace.contributors?.get(currentUser.uid) == "owner"
                } else {
                    false // Default ke false jika data tidak tersedia
                }

                _uiState.update {
                    it.copy(
                        workspaceName = workspace?.workspaceName ?: "My Workspace",
                        isCurrentUserOwner = isOwner // Perbarui state dengan hasil pengecekan
                    )
                }
            }
        }
    }

    // --- FUNGSI BARU UNTUK MENGELOLA INTERAKSI UI ---

    fun onWorkspaceNameClicked() {
        _uiState.update { it.copy(showWorkspaceOptions = true) }
    }

    fun onDismissWorkspaceOptions() {
        _uiState.update { it.copy(showWorkspaceOptions = false) }
    }

    fun showRenameDialog() {
        // Tutup dropdown, buka dialog rename
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
            // Nama akan otomatis ter-update di header melalui listener real-time
        }
    }
}