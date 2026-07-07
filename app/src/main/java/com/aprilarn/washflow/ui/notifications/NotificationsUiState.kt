package com.aprilarn.washflow.ui.notifications

import com.aprilarn.washflow.data.model.Notifications

enum class NotificationFilter { ALL, UNREAD }

data class NotificationsUiState(
    val notifications: List<Notifications> = emptyList(),
    val unreadCount: Int = 0,
    val notificationPreviews: List<Notifications> = emptyList(),
    val showNotificationOptions: Boolean = false,
    val currentUserUid: String = "",
    val filter: NotificationFilter = NotificationFilter.UNREAD
) {
    val filteredNotifications: List<Notifications>
        get() = when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { currentUserUid !in it.readBy }
        }
}
