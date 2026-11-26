// com/aprilarn/washflow/data/repository/UserRepository.kt

package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Users
import com.aprilarn.washflow.domain.repository.UserRepositoryContract
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class UserRepository : UserRepositoryContract {

    private val usersCollection = Firebase.firestore.collection("users")
    private val workspacesCollection = Firebase.firestore.collection("workspaces")

    /**
     * 1. Menyimpan/mengupdate profil pengguna dalam transaksi server dan membaca `workspaceId`.
     * 2. Memverifikasi keberadaan `workspaceId` langsung ke server (`Source.SERVER`).
     * 3. Membersihkan `workspaceId = null` HANYA jika server mengonfirmasi workspace sudah dihapus
     *    atau akses ditolak (`PERMISSION_DENIED`).
     *
     * Error jaringan (misal `UNAVAILABLE`, `DEADLINE_EXCEEDED`) akan dilempar ulang agar
     * pemanggil tidak salah menganggap user belum punya workspace dan tidak menghapus `workspaceId`.
     *
     * @return workspaceId (String) yang valid, atau `null` jika user memang tidak/belum punya workspace.
     */
    override suspend fun saveUserAndGetWorkspace(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): String? {
        val userDocRef = usersCollection.document(uid)

        // --- Langkah 1: Transaksi untuk simpan profil & baca workspaceId ---
        val existingWorkspaceId: String? = try {
            Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                if (snapshot.exists()) {
                    // --- PENGGUNA LAMA ---
                    val userUpdates = mapOf(
                        "displayName" to displayName,
                        "email" to email,
                        "photoUrl" to photoUrl
                    )
                    transaction.update(userDocRef, userUpdates)
                    snapshot.getString("workspaceId")
                } else {
                    // --- PENGGUNA BARU ---
                    val newUser = Users(
                        uid = uid,
                        displayName = displayName,
                        email = email,
                        photoUrl = photoUrl,
                        workspaceId = null
                    )
                    transaction.set(userDocRef, newUser)
                    null
                }
            }.await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            // Jangan return null saat gagal jaringan/server — lempar ulang agar tidak salah masuk WorkspaceScreen
            throw e
        }

        // --- Langkah 2: Verifikasi & Cleanup (DI LUAR TRANSAKSI, LANGSUNG KE SERVER) ---
        if (existingWorkspaceId.isNullOrEmpty()) {
            return null // Pengguna baru atau memang tidak punya workspace
        }

        return try {
            val workspaceDocRef = workspacesCollection.document(existingWorkspaceId)
            val workspaceSnapshot = workspaceDocRef.get(Source.SERVER).await()

            if (!workspaceSnapshot.exists()) {
                // Server mengonfirmasi workspace sudah dihapus oleh owner
                clearUserWorkspaceIdSafely(userDocRef)
                null
            } else {
                existingWorkspaceId
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()

            // Cleanup HANYA jika error secara spesifik adalah PERMISSION_DENIED (dikeluarkan dari workspace)
            if (e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                clearUserWorkspaceIdSafely(userDocRef)
                null
            } else {
                // Error jaringan (UNAVAILABLE, DEADLINE_EXCEEDED, dll.):
                // JANGAN hapus workspaceId dan JANGAN return null!
                throw e
            }
        }
    }

    private suspend fun clearUserWorkspaceIdSafely(
        userDocRef: com.google.firebase.firestore.DocumentReference
    ) {
        try {
            userDocRef.update("workspaceId", null).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }
}
