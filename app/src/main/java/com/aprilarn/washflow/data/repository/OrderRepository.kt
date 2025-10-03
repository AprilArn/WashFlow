package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Orders
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
        return try {
            val ordersCollection = db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")

            val newOrderDoc = ordersCollection.document()
            // Set orderId dari ID dokumen yang baru dibuat
            val finalOrder = order.copy(orderId = newOrderDoc.id)

            newOrderDoc.set(finalOrder).await()
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

}