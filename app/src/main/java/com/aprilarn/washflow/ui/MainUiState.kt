package com.aprilarn.washflow.ui

import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.data.model.Notifications

data class MainUiState(
    val workspaceName: String = "Loading...",
    val showWorkspaceOptions: Boolean = false,
    val showRenameDialog: Boolean = false,
    val isCurrentUserOwner: Boolean = false,
    val showCreateInviteDialog: Boolean = false,
    val activeInvite: Invites? = null,
    val isInviteLoading: Boolean = true,
    val showLeaveWorkspaceDialog: Boolean = false,
    val showDeleteWorkspaceDialog: Boolean = false,
    val notifications: List<Notifications> = emptyList(),
    val unreadCount: Int = 0,
    val notificationPreviews: List<Notifications> = emptyList(),
    val showNotificationOptions: Boolean = false,
    val currentUserUid: String = ""
)