// com/aprilarn/washflow/ui/MainViewModel.kt
package com.aprilarn.washflow.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.repository.InviteRepository
import com.aprilarn.washflow.data.repository.NotificationsRepository
import com.aprilarn.washflow.data.repository.WorkspaceRepository
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
    private val workspaceRepository: WorkspaceRepository,
    private val inviteRepository: InviteRepository,
    private val notificationsRepository: NotificationsRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainNavigationEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val appInitTime = Timestamp.now()
    private val displayedNotifIds = mutableSetOf<String>()

    init {
        listenForWorkspaceChanges()
        listenForActiveInvite()
        listenForNotifications()
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
                        // Tampilkan "Loading..." jika workspace null (misal: saat user baru 'leave')
                        workspaceName = workspace?.workspaceName ?: "Loading...",
                        isCurrentUserOwner = isOwner,
                        currentUserUid = currentUser?.uid ?: "",
                        openTime = workspace?.openTime,
                        closeTime = workspace?.closeTime
                    )
                }

                // Save operational hours to shared preferences for HomeViewModel to access
                sharedPreferences.edit().apply {
                    putString("WS_OPEN_TIME", workspace?.openTime)
                    putString("WS_CLOSE_TIME", workspace?.closeTime)
                    apply()
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

    private fun listenForNotifications() {
        viewModelScope.launch {
            // Jalankan pembersihan notifikasi lama (2 hari+) di background
            viewModelScope.launch(Dispatchers.IO) {
                notificationsRepository.cleanupOldNotifications()
            }

            notificationsRepository.getNotificationsRealtime().collect { list ->
                val currentUid = Firebase.auth.currentUser?.uid ?: ""

                // 2. ROMBAK LOGIKA FILTER NOTIFIKASINYA DI SINI
                // Kita cari notifikasi yang benar-benar baru masuk SAAT aplikasi sedang aktif
                val newNotifs = list.filter { notif ->
                    notif.notificationId !in displayedNotifIds
                            && // Belum pernah ditampilkan
                            notif.timestamp > appInitTime
                            // && // Terjadi SETELAH aplikasi dibuka
                            // notif.senderUid != currentUid // (Opsional UX) Jangan munculkan popup untuk aksi yang dilakukan user itu sendiri
                }

                // Tampilkan animasi melayang hanya untuk notifikasi yang lolos filter di atas
                newNotifs.forEach { notif ->
                    showNotificationPreview(notif)
                }

                // 3. Masukkan semua ID notifikasi (baik yang lama maupun yang baru)
                // ke dalam set agar tidak diproses ulang di masa depan.
                list.forEach { displayedNotifIds.add(it.notificationId) }

                // Update UI State seperti biasa (ini tetap mempengaruhi angka badge merah di lonceng)
                val unread = list.count { currentUid !in it.readBy }
                _uiState.update { it.copy(
                    notifications = list,
                    unreadCount = unread,
                    currentUserUid = currentUid
                )}
            }
        }
    }

    private fun showNotificationPreview(notif: Notifications) {
        _uiState.update {
            // Menaruh yang terbaru di index 0 (paling atas di Column)
            it.copy(notificationPreviews = listOf(notif) + it.notificationPreviews)
        }
    }

    fun removeNotificationPreview(notifId: String, wasActioned: Boolean) {
        val notif = _uiState.value.notificationPreviews.find { it.notificationId == notifId }
        val currentUid = _uiState.value.currentUserUid

        _uiState.update { state ->
            state.copy(notificationPreviews = state.notificationPreviews.filter { it.notificationId != notifId })
        }

        // Logika Status Read:
        // 1. Jika pembuat (User A / Sender), di database sudah 'read' (karena UID ada di readBy secara default).
        //    Maka jika senderUid == currentUid, tidak perlu kirim update markAsRead lagi.
        // 2. Jika user lain (User B), tandai read di DB.
        if (wasActioned && notif != null) {
            // Hanya kirim ke DB jika user bukan pembuat DAN user belum ada di daftar readBy
            if (currentUid != notif.senderUid && currentUid !in notif.readBy) {
                markNotificationAsRead(notif)
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
            _uiState.update { it.copy(showLeaveWorkspaceDialog = false) }
            val success = workspaceRepository.leaveWorkspace()
            if (success) {
                // Kirim event untuk navigasi
                _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
            }
            // Jika gagal, bisa tambahkan event untuk menampilkan error
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
            _uiState.update { it.copy(showDeleteWorkspaceDialog = false) }
            val success = workspaceRepository.deleteCurrentWorkspace()
            if (success) {
                // Kirim event untuk navigasi kembali ke WorkspaceScreen
                _eventFlow.emit(MainNavigationEvent.NavigateToWorkspace)
            }
            // TODO: Tambahkan penanganan error jika 'success' adalah false
        }
    }

    // Aksi klik ikon lonceng
    fun onNotificationIconClicked() {
        _uiState.update { it.copy(showNotificationOptions = !it.showNotificationOptions, showWorkspaceOptions = false) }
    }

    fun onDismissNotificationOptions() {
        _uiState.update { it.copy(showNotificationOptions = false) }
    }

    fun showOperationalHoursDialog() {
        _uiState.update { it.copy(showWorkspaceOptions = false, showOperationalHoursDialog = true) }
    }

    fun onDismissOperationalHoursDialog() {
        _uiState.update { it.copy(showOperationalHoursDialog = false) }
    }

    fun updateOperationalHours(openTime: String?, closeTime: String?) {
        viewModelScope.launch {
            workspaceRepository.updateOperationalHours(openTime ?: "", closeTime ?: "")
            onDismissOperationalHoursDialog()
        }
    }

    fun markNotificationAsRead(notif: Notifications) {
        viewModelScope.launch {
            notificationsRepository.markAsRead(notif.notificationId)
        }
    }
}