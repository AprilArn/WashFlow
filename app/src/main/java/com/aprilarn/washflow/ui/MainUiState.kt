package com.aprilarn.washflow.ui

import com.aprilarn.washflow.data.model.Invites

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
    val currentUserUid: String = "",

    // Status Kick
    val showKickedDialog: Boolean = false,
    val kickedFromWorkspaceName: String = "",

    // Jam Operasional
    val showOperationalHoursDialog: Boolean = false,
    val openTime: String? = null,
    val closeTime: String? = null
)
