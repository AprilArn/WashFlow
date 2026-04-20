package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.model.Orders
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {
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

    // Fungsi untuk mendapatkan semua order secara realtime
    suspend fun getOrdersRealtime(): Flow<List<Orders>> {
        return callbackFlow {
            val workspaceId = getWorkspaceId()
            if (workspaceId == null) {
                close(IllegalStateException("Workspace ID not found"))
                return@callbackFlow
            }

            val listener = db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .orderBy("orderDate", Query.Direction.ASCENDING) // Urutkan berdasarkan tanggal order
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            close()
                            return@addSnapshotListener
                        }
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val orders = snapshot.toObjects(Orders::class.java)
                        trySend(orders).isSuccess
                    }
                }
            // Pastikan listener dihapus saat flow ditutup
            awaitClose { listener.remove() }
        }
    }

    suspend fun createOrder(order: Orders): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        val currentUser = Firebase.auth.currentUser ?: return false

        // Ambil nama user yang sedang login untuk isi pesan notifikasi
        val userName = currentUser.displayName ?: "Anggota tim"

        return try {
            val workspaceRef = db.collection("workspaces").document(workspaceId)

            // Siapkan referensi dokumen baru (ID akan digenerate otomatis oleh Firebase)
            val newOrderDoc = workspaceRef.collection("orders").document()
            val newNotifDoc = workspaceRef.collection("notifications").document()

            // 1. Siapkan data Order dengan ID dokumen yang baru
            val finalOrder = order.copy(orderId = newOrderDoc.id)

            // 2. Siapkan data Notifikasi
            val notification = Notifications(
                notificationId = newNotifDoc.id,
                title = "Order Baru",
                message = "$userName membuat order baru untuk pelanggan ${order.customerName}",
                senderUid = currentUser.uid,
                timestamp = Timestamp.now(),
                // Masukkan UID pembuat ke readBy agar dia tidak mendapat notif buatannya sendiri
                readBy = listOf(currentUser.uid)
            )

            // 3. GUNAKAN BATCH: Simpan keduanya secara bersamaan (Atomic)
            // Ini akan memastikan koleksi 'notifications' otomatis terbuat di Firestore
            db.runBatch { batch ->
                batch.set(newOrderDoc, finalOrder)
                batch.set(newNotifDoc, notification)
            }.await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Fungsi untuk update status order
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .document(orderId)
                .update("status", newStatus)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteOrder(orderId: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .document(orderId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}