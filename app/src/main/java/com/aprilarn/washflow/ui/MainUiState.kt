// com/aprilarn/washflow/ui/MainUiState.kt
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
    val showDeleteWorkspaceDialog: Boolean = false
)