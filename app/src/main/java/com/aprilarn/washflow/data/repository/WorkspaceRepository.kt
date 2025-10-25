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
    // private val invitesCollection = db.collection("invites")

    /**
     * FUNGSI YANG DIPERBARUI:
     * Mendengarkan dokumen pengguna, lalu mendengarkan dokumen workspace
     * berdasarkan workspaceId yang didapat.
     */
    suspend fun getCurrentWorkspaceRealtime(): Flow<Workspaces?> {
        return callbackFlow {
            val user = Firebase.auth.currentUser
            if (user == null) {
                trySend(null)
                close()
                return@callbackFlow
            }

            // Simpan referensi ke listener workspace agar bisa dihapus
            var workspaceListener: ListenerRegistration? = null
            val userDocRef = usersCollection.document(user.uid)

            // 1. UTAMA: Dengarkan dokumen pengguna
            val userListener = userDocRef.addSnapshotListener { userSnapshot, userError ->
                if (userError != null) {
                    close(userError) // Error serius, tutup flow
                    return@addSnapshotListener
                }

                // Hapus listener workspace yang lama setiap kali data pengguna berubah
                workspaceListener?.remove()

                val workspaceId = userSnapshot?.getString("workspaceId")

                if (workspaceId.isNullOrEmpty()) {
                    // Pengguna tidak punya workspace (atau baru saja keluar)
                    trySend(null)
                } else {
                    // Pengguna punya workspace, buat listener baru ke sana
                    val workspaceDocRef = workspacesCollection.document(workspaceId)
                    workspaceListener = workspaceDocRef.addSnapshotListener { workspaceSnapshot, workspaceError ->

                        if (workspaceError != null) {
                            // INI ADALAH ERROR PERMISSION_DENIED YANG DIHARAPKAN
                            // Jangan 'close(error)', cukup kirim null
                            // karena listener pengguna masih valid.
                            trySend(null)
                            return@addSnapshotListener
                        }

                        if (workspaceSnapshot != null && workspaceSnapshot.exists()) {
                            trySend(workspaceSnapshot.toObject(Workspaces::class.java))
                        } else {
                            trySend(null)
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
}