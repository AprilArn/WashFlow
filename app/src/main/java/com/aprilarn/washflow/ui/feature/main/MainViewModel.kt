package com.aprilarn.washflow.ui.feature.main

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.domain.usecase.ManageInviteUseCase
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import com.aprilarn.washflow.utils.WorkspacePresenceManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

sealed class MainNavigationEvent {
    object NavigateToWorkspace : MainNavigationEvent()
}

class MainViewModel(
    private val workspaceUseCase: ManageWorkspaceUseCase,
    private val inviteUseCase: ManageInviteUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainNavigationEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var isExpectingWorkspaceRemoval = false

    init {
        listenForWorkspaceChanges()
        listenForActiveInvite()
    }

    private fun listenForWorkspaceChanges() {
        viewModelScope.launch {
            workspaceUseCase.observeCurrentWorkspace().collect { workspace ->
                val currentUser = Firebase.auth.currentUser ?: return@collect

                val previousState = _uiState.value
                val previouslyHadWorkspace = previousState.workspaceName != "Loading..." && previousState.workspaceName.isNotEmpty()

                if (workspace == null) {
                    if (previouslyHadWorkspace && !isExpectingWorkspaceRemoval) {
                        // User was kicked!
                        _uiState.update {
                            it.copy(
                                showKickedDialog = true,
                                kickedFromWorkspaceName = previousState.workspaceName,
                                workspaceName = "" // Clear the loading state
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                workspaceName = "Loading...",
                                isCurrentUserOwner = false,
                                openTime = null,
                                closeTime = null
                            )
                        }
                    }
                } else {
                    val isOwner = workspace.contributors?.get(currentUser.uid) == "owner"

                    _uiState.update {
                        it.copy(
                            workspaceName = workspace.workspaceName ?: "Loading...",
                            isCurrentUserOwner = isOwner,
                            currentUserUid = currentUser.uid,
                            openTime = workspace.openTime,
                            closeTime = workspace.closeTime
                        )
                    }

                    // Save operational hours to shared preferences for HomeViewModel to access
                    sharedPreferences.edit().apply {
                        putString("WS_OPEN_TIME", workspace.openTime)
                        putString("WS_CLOSE_TIME", workspace.closeTime)
                        apply()
                    }
                }
            }
        }
    }

    private fun listenForActiveInvite() {
        viewModelScope.launch {
            inviteUseCase.observeActiveInvite().collect { invite ->
                _uiState.update { it.copy(activeInvite = invite, isInviteLoading = false) }
            }
        }
    }

    fun createInvitation(maxContributors: Int, expiresAt: Date) {
        viewModelScope.launch {
            // Convert Date to Firebase Timestamp before sending to repository
            inviteUseCase.createInvite(maxContributors, Timestamp(expiresAt))
            // Hide the dialog after creation, the listener will pick up the new active code
        }
    }

    fun deleteInvitation() {
        // Get the active invite ID from the state
        val inviteId = _uiState.value.activeInvite?.inviteId ?: return
        viewModelScope.launch {
            inviteUseCase.expireInvite(inviteId)
            // The realtime listener will automatically update the UI to show no active invite
        }
    }

    // --- UI INTERACTION HANDLERS ---

    fun onWorkspaceNameClicked() {
        _uiState.update { it.copy(showWorkspaceOptions = !it.showWorkspaceOptions) }
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
                workspaceUseCase.renameWorkspace(newName)
            }
            onDismissRenameDialog()
        }
    }

    fun onAddNewContributorClicked() {
        // Ubah ini untuk menjalankan pengecekan di background SEBELUM menampilkan dialog
        viewModelScope.launch {

            // 1. Panggil fungsi baru untuk mengecek dan mengubah status jika perlu
            inviteUseCase.checkAndExpireActiveInvite()

            // 2. (PENTING) Listener 'listenForActiveInvite' akan otomatis
            // mengambil perubahan status (jika ada) dan memperbarui _uiState.

            // 3. Setelah pengecekan selesai, tampilkan dialog.
            // Logika di MainActivity akan menampilkan dialog yang benar
            // (Gambar 1 atau 2) berdasarkan _uiState yang sudah ter-update.
            _uiState.update { it.copy(showCreateInviteDialog = true, showWorkspaceOptions = false) }

            // Jalankan pembersihan invite expired di background
            viewModelScope.launch(Dispatchers.IO) {
                cleanupExpiredInvites()
            }
        }
    }

    /**
     * Menghapus invite yang expiredAt-nya sudah lewat lebih dari 7 hari. (tidak peduli apakah dokumen invite milik workspace ini atau tidak / global delete)
     */
    private suspend fun cleanupExpiredInvites() {
        try {
            val db = Firebase.firestore
            val invitesRef = db.collection("invites")

            // Hapus yang expired > 7 hari lalu
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val thresholdDate = calendar.time

            Log.d("DEBUG_CLEANUP", "Mencari SEMUA invite di database yang expired sebelum: $thresholdDate")

            val snapshot = invitesRef.get().await()

            val batch = db.batch()
            var countDeleted = 0

            for (doc in snapshot.documents) {
                val expiresAtTimestamp = doc.getTimestamp("expiresAt")

                if (expiresAtTimestamp != null) {
                    val expiresDate = expiresAtTimestamp.toDate()

                    // Cek apakah tanggal expired document < thresholdDate
                    if (expiresDate.before(thresholdDate)) {
                        batch.delete(doc.reference)
                        countDeleted++
                        Log.d("DEBUG_CLEANUP", "Menandai hapus: ${doc.id} (Expired: $expiresDate)")
                    }
                }
            }

            if (countDeleted > 0) {
                batch.commit().await()
                Log.d("DEBUG_CLEANUP", "SUKSES GLOBAL CLEANUP: Menghapus $countDeleted invite.")
            } else {
                Log.d("DEBUG_CLEANUP", "INFO: Tidak ada invite expired yang ditemukan.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DEBUG_CLEANUP", "ERROR: ${e.message}")
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
            isExpectingWorkspaceRemoval = true
            _uiState.update { it.copy(showLeaveWorkspaceDialog = false) }
            Firebase.auth.currentUser?.uid?.let { WorkspacePresenceManager.setOffline(sharedPreferences, it) }
            val success = workspaceUseCase.leaveWorkspace()
            if (success) {
                // Kirim event untuk navigasi
                _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
            } else {
                isExpectingWorkspaceRemoval = false
            }
        }
    }


    // Fungsi untuk Delete Workspace
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
            isExpectingWorkspaceRemoval = true
            _uiState.update { it.copy(showDeleteWorkspaceDialog = false) }
            Firebase.auth.currentUser?.uid?.let { WorkspacePresenceManager.setOffline(sharedPreferences, it) }
            val success = workspaceUseCase.deleteCurrentWorkspace()
            if (success) {
                // Kirim event untuk navigasi kembali ke WorkspaceScreen
                _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
            } else {
                isExpectingWorkspaceRemoval = false
            }
        }
    }

    fun showOperationalHoursDialog() {
        _uiState.update { it.copy(showWorkspaceOptions = false, showOperationalHoursDialog = true) }
    }

    fun onDismissOperationalHoursDialog() {
        _uiState.update { it.copy(showOperationalHoursDialog = false) }
    }

    fun onKickedDialogConfirm() {
        _uiState.update { it.copy(showKickedDialog = false) }
        viewModelScope.launch {
            _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
        }
    }

    fun updateOperationalHours(openTime: String?, closeTime: String?) {
        viewModelScope.launch {
            workspaceUseCase.updateOperationalHours(openTime ?: "", closeTime ?: "")
            onDismissOperationalHoursDialog()
        }
    }
}
