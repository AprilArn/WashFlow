package com.aprilarn.washflow.ui.feature.contributors

data class ContributorUiModel(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val isOnline: Boolean = false,
    val activeDeviceCount: Int = 0,
    val lastActiveAtMillis: Long? = null
)

data class ContributorsUiState(
    val contributors: List<ContributorUiModel> = emptyList(),
    val filteredContributors: List<ContributorUiModel> = emptyList(),
    val onlineCount: Int = 0,
    val totalOnlineDevices: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCurrentUserOwner: Boolean = false,
    val selectedContributor: ContributorUiModel? = null,
    val currentUserUid: String = ""
)
