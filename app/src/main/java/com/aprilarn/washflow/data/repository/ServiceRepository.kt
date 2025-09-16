package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Services
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

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

    suspend fun getServicesRealtime(): Flow<List<Services>> = callbackFlow {
        val workspaceId = getWorkspaceId()
        if (workspaceId.isNullOrEmpty()) {
            // Kirim list kosong jika tidak ada workspace dan tutup flow
            send(emptyList())
            close()
            return@callbackFlow
        }

        val servicesCollection = db.collection("workspaces")
            .document(workspaceId)
            .collection("services")

        // 1. Pasang listener ke koleksi
        val listener = servicesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Jika ada error, tutup flow dengan exception
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // 2. Konversi snapshot ke list objek
                val services = snapshot.toObjects<Services>()
                // 3. Kirim data terbaru ke flow
                trySend(services)
            }
        }
        // 4. Saat flow dibatalkan (mis. ViewModel hancur), hapus listener-nya
        // Ini sangat penting untuk mencegah memory leak!
        awaitClose {
            listener.remove()
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