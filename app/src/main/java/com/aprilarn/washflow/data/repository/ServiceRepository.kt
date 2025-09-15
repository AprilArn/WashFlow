package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Services
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class ServiceRepository {
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

    suspend fun getServices(): List<Services> {
        val workspaceId = getWorkspaceId() ?: return emptyList()
        return try {
            val snapshot = db.collection("workspaces")
                .document(workspaceId)
                .collection("services")
                .get()
                .await()
            snapshot.toObjects()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Menambahkan service baru dengan ID kustom yang ditentukan oleh pengguna.
     * @return Boolean true jika sukses.
     */
    suspend fun addService(serviceId: String, serviceName: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            // Referensi ke koleksi 'services'
            val servicesCollection = db.collection("workspaces")
                .document(workspaceId)
                .collection("services")

            // **KUNCI UTAMA**: Gunakan .document(serviceId) untuk menetapkan ID kustom
            val newServiceDocRef = servicesCollection.document(serviceId)

            val newService = Services(
                serviceId = serviceId, // ID dari input pengguna
                serviceName = serviceName
            )

            newServiceDocRef.set(newService).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateService(serviceId: String, newName: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            val serviceDocRef = db.collection("workspaces")
                .document(workspaceId)
                .collection("services")
                .document(serviceId)

            serviceDocRef.update("serviceName", newName).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteService(serviceId: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            db.collection("workspaces")
                .document(workspaceId)
                .collection("services")
                .document(serviceId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}