package com.aprilarn.washflow.ui.notifications

import com.aprilarn.washflow.data.model.Notifications

data class NotificationsUiState(
    val notifications: List<Notifications> = emptyList(),
    val unreadCount: Int = 0,
    val notificationPreviews: List<Notifications> = emptyList(),
    val showNotificationOptions: Boolean = false,
    val currentUserUid: String = ""
)
