// com/aprilarn/washflow/ui/workspace/WorkspaceUiState.kt
package com.aprilarn.washflow.ui.workspace

data class WorkspaceUiState(
    val isLoading: Boolean = false, // <-- Tambahkan properti ini
    val displayName: String = ""
)