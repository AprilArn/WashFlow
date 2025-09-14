// com/aprilarn/washflow/data/repository/CustomerRepository.kt
package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Customers
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class CustomerRepository {
    private val db = Firebase.firestore

    /**
     * Mengambil daftar pelanggan dari sub-koleksi 'customers' di dalam workspace aktif pengguna.
     * @return List<Customers> daftar pelanggan, atau list kosong jika gagal/tidak ada.
     */
    suspend fun getCustomers(): List<Customers> {
        val user = Firebase.auth.currentUser ?: return emptyList()
        val userDoc = db.collection("users").document(user.uid).get().await()
        val workspaceId = userDoc.getString("workspaceId")

        if (workspaceId.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val snapshot = db.collection("workspaces")
                .document(workspaceId)
                .collection("customers")
                .get()
                .await()
            snapshot.toObjects() // Konversi semua dokumen menjadi list objek Customers
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Menambahkan pelanggan baru ke sub-koleksi 'customers' di dalam workspace aktif pengguna.
     * @return Boolean true jika sukses, false jika gagal.
     */
    suspend fun addCustomer(customerName: String, customerContact: String): Boolean {
        // 1. Dapatkan pengguna dan workspaceId aktif saat ini
        val user = Firebase.auth.currentUser ?: return false
        val userDoc = db.collection("users").document(user.uid).get().await()
        val workspaceId = userDoc.getString("workspaceId")

        if (workspaceId.isNullOrEmpty()) {
            // Pengguna tidak memiliki workspace aktif, tidak bisa menambahkan customer
            return false
        }

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

    /**
     * Mengubah data pelanggan yang ada di Firestore.
     */
    suspend fun updateCustomer(customerId: String, newName: String, newContact: String): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val userDoc = db.collection("users").document(user.uid).get().await()
        val workspaceId = userDoc.getString("workspaceId") ?: return false

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

    /**
     * Menghapus data pelanggan dari Firestore.
     */
    suspend fun deleteCustomer(customerId: String): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val userDoc = db.collection("users").document(user.uid).get().await()
        val workspaceId = userDoc.getString("workspaceId") ?: return false

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