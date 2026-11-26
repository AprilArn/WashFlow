package com.aprilarn.washflow.ui.feature.contributors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import com.aprilarn.washflow.utils.ContributorPresenceRules
import com.aprilarn.washflow.utils.WorkspacePresenceManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributorsViewModel(
    private val workspaceUseCase: ManageWorkspaceUseCase
) : ViewModel() {

    private data class RawContributorData(
        val uid: String,
        val name: String,
        val email: String,
        val photoUrl: String?,
        val role: String,
        val activeSessionsMillis: Map<String, Long>?,
        val legacyIsOnline: Boolean,
        val lastActiveAtMillis: Long?
    )

    private val _uiState = MutableStateFlow(ContributorsUiState())
    val uiState = _uiState.asStateFlow()
    private val db = Firebase.firestore

    private val userListeners = mutableMapOf<String, ListenerRegistration>()
    private val rawContributors = mutableMapOf<String, RawContributorData>()
    private val resolvedUids = mutableSetOf<String>()
    private var currentContributorRoles: Map<String, String> = emptyMap()

    init {
        loadContributors()
        startFreshnessTicker()
    }

    private fun loadContributors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Dengarkan perubahan workspace secara realtime
            workspaceUseCase.observeCurrentWorkspace().collect { workspace ->
                if (workspace != null) {
                    val contributorMap = workspace.contributors ?: emptyMap()
                    currentContributorRoles = contributorMap

                    val currentUserUid = Firebase.auth.currentUser?.uid ?: ""
                    val isOwner = contributorMap[currentUserUid] == "owner"

                    _uiState.update {
                        it.copy(
                            isCurrentUserOwner = isOwner,
                            currentUserUid = currentUserUid,
                            errorMessage = null
                        )
                    }

                    syncUserListeners(contributorMap)
                } else {
                    clearUserListeners()
                    rawContributors.clear()
                    resolvedUids.clear()
                    currentContributorRoles = emptyMap()
                    _uiState.update {
                        it.copy(
                            contributors = emptyList(),
                            filteredContributors = emptyList(),
                            onlineCount = 0,
                            totalOnlineDevices = 0,
                            isLoading = false,
                            errorMessage = "Workspace not found"
                        )
                    }
                }
            }
        }
    }

    private fun syncUserListeners(contributorMap: Map<String, String>) {
        // 1. Hapus listener & cache untuk UID yang sudah keluar/dikeluarkan dari workspace
        val removedUids = userListeners.keys - contributorMap.keys
        removedUids.forEach { uid ->
            userListeners.remove(uid)?.remove()
            rawContributors.remove(uid)
            resolvedUids.remove(uid)
        }

        // 2. Perbarui role untuk UID yang sudah punya listener aktif
        contributorMap.forEach { (uid, role) ->
            rawContributors[uid]?.let { existing ->
                if (existing.role != role) {
                    rawContributors[uid] = existing.copy(role = role)
                }
            }
        }

        if (contributorMap.isEmpty()) {
            publishContributors(markLoaded = true)
            return
        }

        // 3. Pasang realtime listener untuk setiap UID baru agar status online/offline per-device langsung terbarui
        contributorMap.forEach { (uid, _) ->
            if (!userListeners.containsKey(uid)) {
                val registration = db.collection("users").document(uid)
                    .addSnapshotListener { snapshot, error ->
                        resolvedUids.add(uid)
                        val allInitialResolved = resolvedUids.containsAll(currentContributorRoles.keys)

                        if (error != null) {
                            if (allInitialResolved) {
                                publishContributors(markLoaded = true)
                            }
                            return@addSnapshotListener
                        }

                        val latestRole = currentContributorRoles[uid] ?: return@addSnapshotListener
                        if (snapshot != null && snapshot.exists()) {
                            val displayName = snapshot.getString("displayName") ?: "Unknown"
                            val email = snapshot.getString("email") ?: "-"
                            val photoUrl = snapshot.getString("photoUrl")
                            val legacyIsOnline = snapshot.getBoolean("isOnline") ?: false
                            val lastActiveAt = snapshot.getTimestamp(
                                "lastActiveAt",
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                            )?.toDate()?.time
                            val activeSessionsMillis = extractActiveSessionsMillis(snapshot)

                            // Bersihkan sesi basi (> 24 jam) khusus untuk dokumen user sendiri
                            if (uid == Firebase.auth.currentUser?.uid && activeSessionsMillis != null) {
                                val staleIds = ContributorPresenceRules.findStaleSessionIds(activeSessionsMillis)
                                if (staleIds.isNotEmpty()) {
                                    WorkspacePresenceManager.cleanupStaleSessions(uid, staleIds)
                                }
                            }

                            rawContributors[uid] = RawContributorData(
                                uid = uid,
                                name = displayName,
                                email = email,
                                photoUrl = photoUrl,
                                role = latestRole,
                                activeSessionsMillis = activeSessionsMillis,
                                legacyIsOnline = legacyIsOnline,
                                lastActiveAtMillis = lastActiveAt
                            )
                        }
                        if (allInitialResolved || !_uiState.value.isLoading) {
                            publishContributors(markLoaded = true)
                        }
                    }
                userListeners[uid] = registration
            }
        }

        if (resolvedUids.containsAll(contributorMap.keys)) {
            publishContributors(markLoaded = true)
        }
    }

    /**
     * Mengekstrak map `activeSessions` dari snapshot Firestore menggunakan `ServerTimestampBehavior.ESTIMATE`
     * agar serverTimestamp yang masih pending di lokal langsung terbaca sebagai timestamp aktif.
     */
    private fun extractActiveSessionsMillis(snapshot: DocumentSnapshot): Map<String, Long>? {
        val rawField = snapshot.get("activeSessions", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: return null
        val rawMap = rawField as? Map<*, *> ?: return null
        val parsed = mutableMapOf<String, Long>()
        rawMap.forEach { (key, value) ->
            val deviceId = key as? String ?: return@forEach
            val millis = when (value) {
                is Timestamp -> value.toDate().time
                is Number -> value.toLong()
                else -> null
            }
            if (millis != null) {
                parsed[deviceId] = millis
            }
        }
        return parsed
    }

    /**
     * Evaluasi ulang status timeout kehadiran setiap 15 detik agar sesi device yang mendadak
     * terputus/force-close otomatis kedaluwarsa setelah 2 menit tanpa perlu refresh manual.
     */
    private fun startFreshnessTicker() {
        viewModelScope.launch {
            while (true) {
                delay(15_000L)
                if (rawContributors.isNotEmpty()) {
                    publishContributors(markLoaded = false)
                }
            }
        }
    }

    private fun publishContributors(markLoaded: Boolean) {
        val nowMillis = System.currentTimeMillis()
        val mappedList = rawContributors.values.map { raw ->
            val activeDevices = ContributorPresenceRules.countActiveDevices(
                activeSessionsMillis = raw.activeSessionsMillis,
                legacyIsOnline = raw.legacyIsOnline,
                legacyLastActiveAtMillis = raw.lastActiveAtMillis,
                nowMillis = nowMillis
            )
            ContributorUiModel(
                uid = raw.uid,
                name = raw.name,
                email = raw.email,
                photoUrl = raw.photoUrl,
                role = raw.role,
                isOnline = activeDevices > 0,
                activeDeviceCount = activeDevices,
                lastActiveAtMillis = raw.lastActiveAtMillis
            )
        }

        val sortedList = ContributorPresenceRules.sortContributors(mappedList)
        val onlineCount = sortedList.count { it.isOnline }
        val totalOnlineDevices = sortedList.sumOf { it.activeDeviceCount }

        _uiState.update { current ->
            val refreshedSelected = current.selectedContributor?.let { selected ->
                sortedList.firstOrNull { it.uid == selected.uid }
            }
            current.copy(
                contributors = sortedList,
                filteredContributors = filterList(sortedList, current.searchQuery),
                onlineCount = onlineCount,
                totalOnlineDevices = totalOnlineDevices,
                isLoading = if (markLoaded) false else current.isLoading,
                selectedContributor = refreshedSelected
            )
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
            val success = workspaceUseCase.removeContributor(contributor.uid)
            if (success) {
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
        val trimmed = query.trim()
        return list.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
                it.email.contains(trimmed, ignoreCase = true)
        }
    }

    private fun clearUserListeners() {
        userListeners.values.forEach { it.remove() }
        userListeners.clear()
    }

    override fun onCleared() {
        super.onCleared()
        clearUserListeners()
    }
}
