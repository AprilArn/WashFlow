// com/aprilarn/washflow/data/repository/UserRepository.kt

package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Users
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val usersCollection = Firebase.firestore.collection("users")

    /**
     * Menyimpan data pengguna (membuat jika belum ada, atau update jika sudah ada)
     * secara atomik menggunakan transaksi dan mengembalikan workspaceId yang ada.
     * @return workspaceId (String) jika ada, atau null jika tidak ada.
     */
    suspend fun saveUserAndGetWorkspace(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): String? {
        val userDocRef = usersCollection.document(uid)
        return try {
            Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                if (snapshot.exists()) {
                    // --- PENGGUNA LAMA ---
                    // Hanya update data profil, jangan sentuh workspaceId
                    val userUpdates = mapOf(
                        "displayName" to displayName,
                        "email" to email,
                        "photoUrl" to photoUrl
                    )
                    transaction.update(userDocRef, userUpdates)
                    // Kembalikan workspaceId yang sudah ada dari snapshot
                    snapshot.getString("workspaceId")
                } else {
                    // --- PENGGUNA BARU ---
                    // Buat dokumen baru dengan workspaceId null
                    val newUser = Users(
                        uid = uid,
                        displayName = displayName,
                        email = email,
                        photoUrl = photoUrl,
                        workspaceId = null // Secara eksplisit null untuk pengguna baru
                    )
                    transaction.set(userDocRef, newUser)
                    // Pengguna baru belum punya workspaceId
                    null
                }
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            // Kembalikan null jika terjadi error
            null
        }
    }
}