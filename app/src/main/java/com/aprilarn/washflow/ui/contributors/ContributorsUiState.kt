// com/aprilarn/washflow/ui/contributors/ContributorsUiState.kt
package com.aprilarn.washflow.ui.contributors

data class ContributorUiModel(
    val uid: String,
    val name: String,
    val photoUrl: String?,
    val role: String // "owner" atau "member"
)

data class ContributorsUiState(
    val contributors: List<ContributorUiModel> = emptyList(),
    val filteredContributors: List<ContributorUiModel> = emptyList(), // Untuk pencarian
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCurrentUserOwner: Boolean = false // Untuk menentukan apakah tombol "+ Add" muncul
)