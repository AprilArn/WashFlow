package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Customers
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class CustomerRepository {
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

    suspend fun getCustomersRealtime(): Flow<List<Customers>> = callbackFlow {
        val workspaceId = getWorkspaceId()
        if (workspaceId.isNullOrEmpty()) {
            // Kirim list kosong jika tidak ada workspace dan tutup flow
            send(emptyList())
            close()
            return@callbackFlow
        }

        val customersCollection = db.collection("workspaces")
            .document(workspaceId)
            .collection("customers")

        // Pasang listener ke koleksi
        val listener = customersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    close() // Tutup flow dengan aman tanpa crash
                    return@addSnapshotListener
                }
                close(error) // Lempar error jika masalah lain
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val customers = snapshot.toObjects<Customers>()
                trySend(customers)
            }
        }
        // Saat flow dibatalkan (mis. ViewModel hancur), hapus listener-nya
        // Ini sangat penting untuk mencegah memory leak!
        awaitClose {
            listener.remove()
        }
    }

    suspend fun addCustomer(customerName: String, customerContact: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false

        return try {
            // 2. Buat referensi ke sub-koleksi 'customers'
            val customersCollection = db.collection("workspaces")
                .document(workspaceId)
                .collection("customers")

            // 3. Buat dokumen baru untuk mendapatkan ID unik
            val newCustomerDocRef = customersCollection.document()

            // 4. Siapkan objek Customer dengan ID yang baru dibuat
            val newCustomer = Customers(
                customerId = newCustomerDocRef.id,
                name = customerName,
                contact = customerContact
            )

            // 5. Simpan objek ke Firestore
            newCustomerDocRef.set(newCustomer).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateCustomer(customerId: String, newName: String, newContact: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false

        return try {
            val customerDocRef = db.collection("workspaces")
                .document(workspaceId)
                .collection("customers")
                .document(customerId)

            val updates = mapOf(
                "name" to newName,
                "contact" to newContact
            )

            customerDocRef.update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteCustomer(customerId: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            db.collection("workspaces")
                .document(workspaceId)
                .collection("customers")
                .document(customerId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}