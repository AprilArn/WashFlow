package com.aprilarn.washflow.ui.tablet.contributors

import androidx.compose.runtime.Composable
import com.aprilarn.washflow.ui.contributors.ContributorUiModel
import com.aprilarn.washflow.ui.contributors.ContributorsScreen
import com.aprilarn.washflow.ui.contributors.ContributorsUiState

/**
 * Tampilan Daftar Kontributor khusus Tablet (kontainer 600.dp di tengah).
 */
@Composable
fun TabletContributorsScreen(
    uiState: ContributorsUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onContributorClick: (ContributorUiModel) -> Unit,
    onDismissDialog: () -> Unit,
    onKickUser: (ContributorUiModel) -> Unit
) {
    ContributorsScreen(
        uiState = uiState,
        onSearchQueryChange = onSearchQueryChange,
        onAddClick = onAddClick,
        onContributorClick = onContributorClick,
        onDismissDialog = onDismissDialog,
        onKickUser = onKickUser
    )
}
