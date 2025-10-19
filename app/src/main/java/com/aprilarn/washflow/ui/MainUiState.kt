// com/aprilarn/washflow/ui/MainUiState.kt
package com.aprilarn.washflow.ui

import com.aprilarn.washflow.data.model.Invites

data class MainUiState(
    val workspaceName: String = "Loading...",
    val showWorkspaceOptions: Boolean = false,
    val showRenameDialog: Boolean = false,
    val isCurrentUserOwner: Boolean = false,

    // This flag signals the intent to show an invite-related dialog.
    // If activeInvite is null, we show the "Create" dialog.
    // If activeInvite is not null, we show the "Active Code" dialog.
    val showCreateInviteDialog: Boolean = false,
    val activeInvite: Invites? = null,
    val isInviteLoading: Boolean = true
)