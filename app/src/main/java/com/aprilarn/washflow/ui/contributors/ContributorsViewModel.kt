// com/aprilarn/washflow/ui/contributors/ContributorsViewModel.kt
package com.aprilarn.washflow.ui.contributors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.WorkspaceRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.aprilarn.washflow.data.model.Users
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContributorsViewModel(
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContributorsUiState())
    val uiState = _uiState.asStateFlow()
    private val db = Firebase.firestore

    init {
        loadContributors()
    }

    private fun loadContributors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Dengarkan perubahan workspace secara realtime
            workspaceRepository.getCurrentWorkspaceRealtime().collect { workspace ->
                if (workspace != null) {
                    val contributorMap = workspace.contributors ?: emptyMap()
                    val contributorList = mutableListOf<ContributorUiModel>()

                    // Ambil detail user untuk setiap UID
                    // Catatan: Ini bisa dioptimalkan, tapi untuk jumlah kecil ini oke.
                    for ((uid, role) in contributorMap) {
                        try {
                            val userSnapshot = db.collection("users").document(uid).get().await()
                            val user = userSnapshot.toObject<Users>()
                            if (user != null) {
                                contributorList.add(
                                    ContributorUiModel(
                                        uid = uid,
                                        name = user.displayName ?: "Unknown",
                                        email = user.email ?: "-",
                                        photoUrl = user.photoUrl,
                                        role = role
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Urutkan: Owner di atas, lalu Member berdasarkan nama
                    val sortedList = contributorList.sortedWith(
                        compareBy<ContributorUiModel> { it.role != "owner" } // Owner (false) first
                            .thenBy { it.name }
                    )

                    val currentUserUid = Firebase.auth.currentUser?.uid ?: ""
                    val isOwner = contributorMap[currentUserUid] == "owner"

                    _uiState.update {
                        it.copy(
                            contributors = sortedList,
                            filteredContributors = filterList(sortedList, it.searchQuery),
                            isLoading = false,
                            isCurrentUserOwner = isOwner,
                            currentUserUid = currentUserUid
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Workspace not found") }
                }
            }
        }
    }

    fun onContributorClicked(contributor: ContributorUiModel) {
        _uiState.update { it.copy(selectedContributor = contributor) }
    }

    fun onDismissDetailDialog() {
        _uiState.update { it.copy(selectedContributor = null) }
    }

    fun kickContributor(contributor: ContributorUiModel) {
        viewModelScope.launch {
            val success = workspaceRepository.removeContributor(contributor.uid)
            if (success) {
                // Tutup dialog. List akan otomatis update karena realtime listener di loadContributors
                _uiState.update { it.copy(selectedContributor = null) }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to kick user.") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredContributors = filterList(it.contributors, query)
            )
        }
    }

    private fun filterList(list: List<ContributorUiModel>, query: String): List<ContributorUiModel> {
        if (query.isBlank()) return list
        return list.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}