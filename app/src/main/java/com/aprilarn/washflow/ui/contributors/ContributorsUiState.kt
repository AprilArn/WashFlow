// com/aprilarn/washflow/ui/contributors/ContributorsUiState.kt
package com.aprilarn.washflow.ui.contributors

data class ContributorUiModel(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String
)

data class ContributorsUiState(
    val contributors: List<ContributorUiModel> = emptyList(),
    val filteredContributors: List<ContributorUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCurrentUserOwner: Boolean = false,
    val selectedContributor: ContributorUiModel? = null
)