package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Orders
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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
}