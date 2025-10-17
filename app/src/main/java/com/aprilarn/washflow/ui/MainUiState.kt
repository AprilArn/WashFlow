package com.aprilarn.washflow.ui

data class MainUiState(
    val workspaceName: String = "Loading...",
    val showWorkspaceOptions: Boolean = false,
    val showRenameDialog: Boolean = false,
    val isCurrentUserOwner: Boolean = false
)