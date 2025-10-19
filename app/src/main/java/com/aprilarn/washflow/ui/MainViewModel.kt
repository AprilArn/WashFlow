// com/aprilarn/washflow/ui/MainViewModel.kt
package com.aprilarn.washflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.data.repository.InviteRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val inviteRepository: InviteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForWorkspaceChanges()
        listenForActiveInvite()
    }

    private fun listenForWorkspaceChanges() {
        viewModelScope.launch {
            workspaceRepository.getCurrentWorkspaceRealtime().collect { workspace ->
                val currentUser = Firebase.auth.currentUser
                val isOwner = if (workspace != null && currentUser != null) {
                    workspace.contributors?.get(currentUser.uid) == "owner"
                } else {
                    false
                }

                _uiState.update {
                    it.copy(
                        workspaceName = workspace?.workspaceName ?: "My Workspace",
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
        // This will trigger the appropriate dialog in the UI based on whether an active invite exists.
        // We set a flag to show "a" dialog, and the UI decides which one.
        _uiState.update { it.copy(showCreateInviteDialog = true, showWorkspaceOptions = false) }
    }

    fun onDismissCreateInviteDialog() {
        _uiState.update { it.copy(showCreateInviteDialog = false) }
    }
}