package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Items
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class ItemRepository {
    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    private suspend fun getWorkspaceId(): String? {
        val user = currentUser ?: return null
        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            userDoc.getString("workspaceId")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getItemsRealtime(): Flow<List<Items>> = callbackFlow {
        val workspaceId = getWorkspaceId()
        if (workspaceId.isNullOrEmpty()) {
            // Kirim list kosong jika tidak ada workspace dan tutup flow
            send(emptyList())
            close()
            return@callbackFlow
        }

        val itemsCollection = db.collection("workspaces")
            .document(workspaceId)
            .collection("items")

        // 1. Pasang listener ke koleksi
        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    close()
                    return@addSnapshotListener
                }
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects<Items>()
                trySend(items)
            }
        }

        // 4. Saat flow dibatalkan (mis. ViewModel hancur), hapus listener-nya
        // Ini sangat penting untuk mencegah memory leak!
        awaitClose {
            listener.remove()
        }
    }

    suspend fun addItem(serviceId: String, itemName: String, itemPrice: Double): Boolean {
        val workspaceId = getWorkspaceId() ?: return false

        return try {
            val workspaceRef = db.collection("workspaces").document(workspaceId)
            val itemsCollection = workspaceRef.collection("items")
            val metadataDocRef = workspaceRef.collection("metadata").document("counts")

            val newItemDocRef = itemsCollection.document()

            val newItem = Items(
                itemId = newItemDocRef.id,
                itemName = itemName,
                itemPrice = itemPrice,
                serviceId = serviceId
            )

            db.runBatch { batch ->
                batch.set(newItemDocRef, newItem)
                batch.update(metadataDocRef, "itemCount", FieldValue.increment(1))
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateItem(itemId: String, newService: String, newName: String, newPrice: Double): Boolean {
        val workspaceId = getWorkspaceId() ?: return false

        return try {
            val itemDocRef = db.collection("workspaces")
                .document(workspaceId)
                .collection("items")
                .document(itemId)

            val updates = mapOf(
                "serviceId" to newService,
                "itemName" to newName,
                "itemPrice" to newPrice
            )

            itemDocRef.update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteItems(itemId: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            val workspaceRef = db.collection("workspaces").document(workspaceId)
            val itemDocRef = workspaceRef.collection("items").document(itemId)
            val metadataDocRef = workspaceRef.collection("metadata").document("counts")

            db.runBatch { batch ->
                batch.delete(itemDocRef)
                batch.update(metadataDocRef, "itemCount", FieldValue.increment(-1))
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}