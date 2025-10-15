// com/aprilarn/washflow/data/repository/WorkspaceRepository.kt

package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Workspaces
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkspaceRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val workspacesCollection = db.collection("workspaces")
    private val invitesCollection = db.collection("invites")

    suspend fun getCurrentWorkspaceRealtime(): Flow<Workspaces?> {
        return callbackFlow {
            val user = Firebase.auth.currentUser
            if (user == null) {
                trySend(null)
                close()
                return@callbackFlow
            }

            // Dapatkan workspaceId dari dokumen pengguna
            val userDocRef = usersCollection.document(user.uid)
            val userSnapshot = userDocRef.get().await()
            val workspaceId = userSnapshot.getString("workspaceId")

            if (workspaceId.isNullOrEmpty()) {
                trySend(null)
                close()
                return@callbackFlow
            }

            // Pasang listener ke dokumen workspace yang sesuai
            val workspaceDocRef = workspacesCollection.document(workspaceId)
            val listener = workspaceDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Workspaces::class.java))
                } else {
                    trySend(null)
                }
            }

            // Hapus listener saat flow ditutup
            awaitClose { listener.remove() }
        }
    }

    /**
     * Membuat workspace baru dan mengaitkannya dengan pengguna.
     * @return Boolean true jika sukses.
     */
    suspend fun createWorkspace(): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        return try {
            // 1. Buat dokumen workspace baru
            val newWorkspace = Workspaces(
                workspaceName = "${user.displayName}'s Workspace",
                ownerUid = user.uid,
                createdAt = Timestamp.now(),
                contributors = mapOf(user.uid to "owner")
            )
            val workspaceDocRef = workspacesCollection.add(newWorkspace).await()

            // 2. Update dokumen pengguna dengan workspaceId yang baru
            usersCollection.document(user.uid).update("workspaceId", workspaceDocRef.id).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Bergabung ke workspace menggunakan kode invite.
     * @return Boolean true jika sukses.
     */
    suspend fun joinWorkspace(inviteCode: String): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val inviteDocRef = invitesCollection.document(inviteCode)

        try {
            db.runTransaction { transaction ->
                val inviteSnapshot = transaction.get(inviteDocRef)

                // Validasi 1: Cek apakah kode ada
                if (!inviteSnapshot.exists()) {
                    throw Exception("Invalid invite code.")
                }

                // --- TAMBAHAN VALIDASI ---
                // Validasi 2: Cek status (jika ada)
                if (inviteSnapshot.getString("status") != "active") {
                    throw Exception("This invitation code has already been used.")
                }
                // Validasi 3: Cek waktu kedaluwarsa
                val expiresAt = inviteSnapshot.getTimestamp("expiresAt")
                if (expiresAt != null && expiresAt.toDate().before(Timestamp.now().toDate())) {
                    throw Exception("This invitation code has expired.")
                }
                // --- AKHIR TAMBAHAN ---

                val workspaceId = inviteSnapshot.getString("workspaceId")
                    ?: throw Exception("Workspace ID not found in invite.")

                val workspaceDocRef = workspacesCollection.document(workspaceId)
                val userDocRef = usersCollection.document(user.uid)

                // Update workspace: tambahkan user sebagai 'member'
                transaction.update(workspaceDocRef, "contributors.${user.uid}", "member")
                // Update user: set workspaceId
                transaction.update(userDocRef, "workspaceId", workspaceId)

                // Ubah status invite menjadi 'used' daripada menghapusnya langsung
                // Ini baik untuk pencatatan (opsional, tapi lebih baik)
                transaction.update(inviteDocRef, "status", "used")

            }.await()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            // Di sini Anda bisa meneruskan e.message ke ViewModel untuk ditampilkan ke user
            return false
        }
    }
}