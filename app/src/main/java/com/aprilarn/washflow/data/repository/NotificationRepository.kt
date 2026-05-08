package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Notifications
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class NotificationsRepository {
    private val db = Firebase.firestore

    private suspend fun getWorkspaceId(): String? {
        val user = Firebase.auth.currentUser ?: return null
        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            userDoc.getString("workspaceId")
        } catch (e: Exception) {
            null
        }
    }

    // Mengambil notifikasi secara realtime, diurutkan dari yang paling baru
    // Dibatasi hanya untuk 2 hari terakhir agar tidak memberatkan UI
    suspend fun getNotificationsRealtime(): Flow<List<Notifications>> = callbackFlow {
        val workspaceId = getWorkspaceId()
        if (workspaceId.isNullOrEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Hitung batas waktu 2 hari yang lalu
        val twoDaysAgo = Timestamp(Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000))

        val listener = db.collection("workspaces")
            .document(workspaceId)
            .collection("notifications")
            .whereGreaterThanOrEqualTo("timestamp", twoDaysAgo) // Filter 2 hari terakhir
            .orderBy("timestamp", Query.Direction.DESCENDING) // Yang terbaru di atas
            .limit(50) // Batasi 50 agar tidak berat
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { it.toObject(Notifications::class.java) }
                    trySend(notifications)
                }
            }

        awaitClose { listener.remove() }
    }

    // Menandai notifikasi sebagai sudah dibaca untuk user saat ini
    suspend fun markAsRead(notificationId: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        val currentUserUid = Firebase.auth.currentUser?.uid ?: return false

        return try {
            db.collection("workspaces")
                .document(workspaceId)
                .collection("notifications")
                .document(notificationId)
                // arrayUnion memastikan UID kita masuk ke daftar yang sudah membaca
                .update("readBy", FieldValue.arrayUnion(currentUserUid))
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Menghapus notifikasi yang lebih tua dari 2 hari dari database workspace ini.
     */
    suspend fun cleanupOldNotifications() {
        val workspaceId = getWorkspaceId() ?: return
        try {
            // Hitung batas waktu 2 hari yang lalu
            val twoDaysAgo = Timestamp(Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000))

            val snapshot = db.collection("workspaces")
                .document(workspaceId)
                .collection("notifications")
                .whereLessThan("timestamp", twoDaysAgo)
                .get()
                .await()

            if (snapshot.isEmpty) return

            val batch = db.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}