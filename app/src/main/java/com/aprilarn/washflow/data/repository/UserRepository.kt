//// com/aprilarn/washflow/data/repository/UserRepository.kt
//
//package com.aprilarn.washflow.data.repository
//
//import com.aprilarn.washflow.data.model.Users
//import com.google.firebase.Firebase
//import com.google.firebase.firestore.firestore
//import kotlinx.coroutines.tasks.await
//
//class UserRepository {
//
//    private val usersCollection = Firebase.firestore.collection("users")
//
//    /**
//     * Menyimpan data pengguna (membuat jika belum ada, atau update jika sudah ada)
//     * secara atomik menggunakan transaksi dan mengembalikan workspaceId yang ada.
//     * @return workspaceId (String) jika ada, atau null jika tidak ada.
//     */
//    suspend fun saveUserAndGetWorkspace(
//        uid: String,
//        displayName: String?,
//        email: String?,
//        photoUrl: String?
//    ): String? {
//        val userDocRef = usersCollection.document(uid)
//        return try {
//            Firebase.firestore.runTransaction { transaction ->
//                val snapshot = transaction.get(userDocRef)
//
//                if (snapshot.exists()) {
//                    // --- PENGGUNA LAMA ---
//                    // Hanya update data profil, jangan sentuh workspaceId
//                    val userUpdates = mapOf(
//                        "displayName" to displayName,
//                        "email" to email,
//                        "photoUrl" to photoUrl
//                    )
//                    transaction.update(userDocRef, userUpdates)
//                    // Kembalikan workspaceId yang sudah ada dari snapshot
//                    snapshot.getString("workspaceId")
//                } else {
//                    // --- PENGGUNA BARU ---
//                    // Buat dokumen baru dengan workspaceId null
//                    val newUser = Users(
//                        uid = uid,
//                        displayName = displayName,
//                        email = email,
//                        photoUrl = photoUrl,
//                        workspaceId = null // Secara eksplisit null untuk pengguna baru
//                    )
//                    transaction.set(userDocRef, newUser)
//                    // Pengguna baru belum punya workspaceId
//                    null
//                }
//            }.await()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Kembalikan null jika terjadi error
//            null
//        }
//    }
//}

// com/aprilarn/washflow/data/repository/UserRepository.kt

package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Users
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException // <-- Tambahkan import ini

class UserRepository {

    private val usersCollection = Firebase.firestore.collection("users")
    private val workspacesCollection = Firebase.firestore.collection("workspaces") // <-- Tambahkan ini

    /**
     * --- FUNGSI YANG DIPERBARUI (DENGAN LAZY CLEANUP) ---
     * 1. Menyimpan/Mengupdate profil pengguna dalam transaksi.
     * 2. Memverifikasi workspaceId di luar transaksi.
     * 3. Membersihkan workspaceId jika tidak valid.
     * @return workspaceId (String) yang valid, atau null jika tidak ada/sudah dihapus.
     */
    suspend fun saveUserAndGetWorkspace(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): String? {
        val userDocRef = usersCollection.document(uid)
        var existingWorkspaceId: String? = null

        // --- Langkah 1: Transaksi untuk simpan profil & BACA workspaceId ---
        try {
            existingWorkspaceId = Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                if (snapshot.exists()) {
                    // --- PENGGUNA LAMA ---
                    val userUpdates = mapOf(
                        "displayName" to displayName,
                        "email" to email,
                        "photoUrl" to photoUrl
                    )
                    transaction.update(userDocRef, userUpdates)
                    snapshot.getString("workspaceId") // Baca ID yang ada
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
            return null // Gagal saat simpan profil
        }

        // --- Langkah 2: Verifikasi & Cleanup (DI LUAR TRANSAKSI) ---
        if (existingWorkspaceId.isNullOrEmpty()) {
            return null // Pengguna baru atau memang tidak punya workspace
        }

        // Coba verifikasi apakah workspaceId tsb masih ada dan valid
        try {
            val workspaceDocRef = workspacesCollection.document(existingWorkspaceId)
            val workspaceSnapshot = workspaceDocRef.get().await()

            if (!workspaceSnapshot.exists()) {
                // Workspace-nya tidak ada (dihapus owner)
                // Lakukan cleanup
                userDocRef.update("workspaceId", null).await()
                return null // Kembalikan null (akan ke WorkspaceScreen)
            } else {
                // Workspace ada dan valid
                return existingWorkspaceId // Kembalikan ID (akan ke MainScreen)
            }
        } catch (e: Exception) {
            // Ini kemungkinan besar PERMISSION_DENIED (dikeluarkan dari contributors)
            if (e is CancellationException) throw e
            e.printStackTrace()
            // Lakukan cleanup
            userDocRef.update("workspaceId", null).await()
            return null // Kembalikan null (akan ke WorkspaceScreen)
        }
    }
}