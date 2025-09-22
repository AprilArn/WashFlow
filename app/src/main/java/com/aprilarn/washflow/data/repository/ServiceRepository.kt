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
            // 1. Buat referensi ke koleksi-koleksi yang relevan
            val workspaceRef = db.collection("workspaces").document(workspaceId)
            val serviceToDeleteRef = workspaceRef.collection("services").document(serviceId)
            val itemsCollectionRef = workspaceRef.collection("items")

            // 2. Buat query untuk menemukan semua item yang memiliki serviceId yang sama
            val itemsToDeleteQuery = itemsCollectionRef.whereEqualTo("serviceId", serviceId)

            // 3. Eksekusi query untuk mendapatkan daftar item yang akan dihapus
            val itemsSnapshot = itemsToDeleteQuery.get().await()

            // 4. Mulai Batched Write
            db.runBatch { batch ->
                // 5. Tambahkan penghapusan dokumen service ke dalam batch
                batch.delete(serviceToDeleteRef)

                // 6. Loop melalui hasil query dan tambahkan setiap item untuk dihapus
                for (document in itemsSnapshot.documents) {
                    batch.delete(document.reference)
                }

            }.await() // 7. Jalankan semua operasi dalam batch

            true // Operasi berhasil
        } catch (e: Exception) {
            e.printStackTrace()
            false // Operasi gagal
        }
    }
}