package com.aprilarn.washflow.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.repository.NotificationsRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val appInitTime = Timestamp.now()
    private val displayedNotifIds = mutableSetOf<String>()

    init {
        listenForNotifications()
    }

    private fun listenForNotifications() {
        viewModelScope.launch {
            // Jalankan pembersihan notifikasi lama (2 hari+) di background
            viewModelScope.launch(Dispatchers.IO) {
                notificationsRepository.cleanupOldNotifications()
            }

            notificationsRepository.getNotificationsRealtime().collect { list ->
                val currentUid = Firebase.auth.currentUser?.uid ?: ""

                // Cari notifikasi yang benar-benar baru masuk SAAT aplikasi sedang aktif
                val newNotifs = list.filter { notif ->
                    notif.notificationId !in displayedNotifIds
                            && notif.timestamp > appInitTime
                }

                // Tampilkan animasi melayang hanya untuk notifikasi yang lolos filter di atas
                newNotifs.forEach { notif ->
                    showNotificationPreview(notif)
                }

                // Masukkan semua ID notifikasi ke dalam set agar tidak diproses ulang
                list.forEach { displayedNotifIds.add(it.notificationId) }

                // Update UI State
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
            it.copy(notificationPreviews = listOf(notif) + it.notificationPreviews)
        }
    }

    fun removeNotificationPreview(notifId: String, wasActioned: Boolean) {
        val notif = _uiState.value.notificationPreviews.find { it.notificationId == notifId }
        val currentUid = _uiState.value.currentUserUid

        _uiState.update { state ->
            state.copy(notificationPreviews = state.notificationPreviews.filter { it.notificationId != notifId })
        }

        if (wasActioned && notif != null) {
            if (currentUid != notif.senderUid && currentUid !in notif.readBy) {
                markNotificationAsRead(notif)
            }
        }
    }

    fun onNotificationIconClicked() {
        _uiState.update { it.copy(showNotificationOptions = !it.showNotificationOptions) }
    }

    fun onDismissNotificationOptions() {
        _uiState.update { it.copy(showNotificationOptions = false) }
    }

    fun onFilterChanged(newFilter: NotificationFilter) {
        _uiState.update { it.copy(filter = newFilter) }
    }

    fun markAllAsRead() {
        val unreadIds = _uiState.value.notifications
            .filter { _uiState.value.currentUserUid !in it.readBy }
            .map { it.notificationId }

        if (unreadIds.isNotEmpty()) {
            viewModelScope.launch {
                notificationsRepository.markAllAsRead(unreadIds)
            }
        }
    }

    fun markNotificationAsRead(notif: Notifications) {
        viewModelScope.launch {
            notificationsRepository.markAsRead(notif.notificationId)
        }
    }
}
