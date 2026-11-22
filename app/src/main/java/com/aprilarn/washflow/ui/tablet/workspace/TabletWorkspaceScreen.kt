package com.aprilarn.washflow.ui.tablet.workspace

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.workspace.WorkspaceScreen
import com.aprilarn.washflow.ui.workspace.WorkspaceUiState

/**
 * Tampilan Pemilihan/Pembuatan Workspace khusus Tablet (Landscape / Expanded).
 */
@Composable
fun TabletWorkspaceScreen(
    state: WorkspaceUiState,
    onJoinClick: (String) -> Unit,
    onCreateWorkspaceClick: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    WorkspaceScreen(
        state = state,
        onJoinClick = onJoinClick,
        onCreateWorkspaceClick = onCreateWorkspaceClick,
        onSwitchAccountClick = onSwitchAccountClick
    )
}
