// com/aprilarn/washflow/data/repository/WorkspaceRepository.kt

package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Workspaces
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkspaceRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val workspacesCollection = db.collection("workspaces")

    /**
     * --- FUNGSI YANG DIPERBARUI (DISEDERHANAKAN) ---
     * Hanya membaca dan melaporkan data workspace.
     * Tidak lagi melakukan cleanup (update workspaceId).
     */
    suspend fun getCurrentWorkspaceRealtime(): Flow<Workspaces?> {
        return callbackFlow {
            val user = Firebase.auth.currentUser
            if (user == null) {
                trySend(null)
                close()
                return@callbackFlow
            }

            var workspaceListener: ListenerRegistration? = null
            val userDocRef = usersCollection.document(user.uid)

            // 1. Dengarkan dokumen pengguna
            val userListener = userDocRef.addSnapshotListener { userSnapshot, userError ->
                if (userError != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                workspaceListener?.remove() // Hapus listener lama
                val workspaceId = userSnapshot?.getString("workspaceId")

                if (workspaceId.isNullOrEmpty()) {
                    trySend(null) // Tidak punya workspace
                } else {
                    // 2. Dengarkan dokumen workspace
                    workspaceListener = workspacesCollection.document(workspaceId)
                        .addSnapshotListener { workspaceSnapshot, workspaceError ->
                            if (workspaceError != null || workspaceSnapshot == null || !workspaceSnapshot.exists()) {
                                trySend(null) // Workspace tidak valid atau error
                            } else {
                                // Sukses, kirim data workspace
                                trySend(workspaceSnapshot.toObject(Workspaces::class.java))
                            }
                        }
                }
            }

            // Saat flow ditutup, hapus kedua listener
            awaitClose {
                userListener.remove()
                workspaceListener?.remove()
            }
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
     * Mengambil HANYA ID workspace saat ini dari dokumen User.
     */
    suspend fun getCurrentWorkspaceId(): String? {
        val user = Firebase.auth.currentUser ?: return null
        return try {
            // Kita ambil ID dari dokumen User saja, tidak perlu ke koleksi Workspaces
            val userDoc = usersCollection.document(user.uid).get().await()
            userDoc.getString("workspaceId")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateWorkspaceName(newName: String): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val workspaceId = usersCollection.document(user.uid).get().await().getString("workspaceId")
        if (workspaceId.isNullOrEmpty()) return false

        return try {
            workspacesCollection.document(workspaceId).update("workspaceName", newName).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Menghapus pengguna dari workspace saat ini.
     * @return Boolean true jika sukses.
     */
    suspend fun leaveWorkspace(): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val userDocRef = usersCollection.document(user.uid)

        return try {
            val workspaceId = userDocRef.get().await().getString("workspaceId")
            if (workspaceId.isNullOrEmpty()) {
                // Pengguna sudah tidak di workspace, anggap sukses
                return true
            }

            val workspaceDocRef = workspacesCollection.document(workspaceId)

            // Jalankan transaksi untuk memastikan kedua operasi berhasil
            db.runTransaction { transaction ->
                // 1. Hapus pengguna dari map contributors di workspace
                // Kita menggunakan FieldValue.delete() untuk menghapus key dari map
                val workspaceUpdate = mapOf(
                    "contributors.${user.uid}" to FieldValue.delete()
                )
                transaction.update(workspaceDocRef, workspaceUpdate)

                // 2. Set workspaceId pengguna menjadi null
                transaction.update(userDocRef, "workspaceId", null)
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Menghapus workspace saat ini dan membersihkan workspaceId
     * HANYA UNTUK OWNER.
     * Kontributor lain akan dibersihkan saat mereka login berikutnya.
     * @return Boolean true jika sukses.
     */
    suspend fun deleteCurrentWorkspace(): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val userDocRef = usersCollection.document(user.uid)

        try {
            // 1. Dapatkan workspaceId dari dokumen owner
            val workspaceId = userDocRef.get().await().getString("workspaceId")
            if (workspaceId.isNullOrEmpty()) return false // Tidak ada workspace untuk dihapus

            val workspaceDocRef = workspacesCollection.document(workspaceId)

            // 2. (Opsional tapi aman) Verifikasi owner
            val workspaceSnapshot = workspaceDocRef.get().await()
            if (!workspaceSnapshot.exists()) return true // Workspace sudah tidak ada

            val workspace = workspaceSnapshot.toObject(Workspaces::class.java)
            if (workspace?.ownerUid != user.uid) {
                throw Exception("Hanya owner yang bisa menghapus workspace.")
            }

            // 3. Jalankan transaksi HANYA untuk owner
            db.runTransaction { transaction ->
                // A. Hapus dokumen workspace
                transaction.delete(workspaceDocRef)

                // B. Set workspaceId owner ke null
                transaction.update(userDocRef, "workspaceId", null)
            }.await()

            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Mengeluarkan kontributor dari workspace.
     * 1. Hapus entri UID dari map 'contributors' di dokumen Workspace.
     * 2. Set 'workspaceId' menjadi null di dokumen User yang bersangkutan.
     */
    suspend fun removeContributor(contributorUid: String): Boolean {
        // Ambil ID workspace dari user yang sedang login (Owner)
        val currentUser = Firebase.auth.currentUser ?: return false
        val userDoc = usersCollection.document(currentUser.uid).get().await()
        val workspaceId = userDoc.getString("workspaceId") ?: return false

        val workspaceRef = workspacesCollection.document(workspaceId)
        val targetUserRef = usersCollection.document(contributorUid)

        return try {
            db.runBatch { batch ->
                // 1. Hapus dari map contributors di Workspace (Syntax: contributors.UID)
                batch.update(workspaceRef, "contributors.$contributorUid", FieldValue.delete())

                // 2. Hapus workspaceId dari User yang di-kick
                batch.update(targetUserRef, "workspaceId", null)
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}