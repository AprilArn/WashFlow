package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.model.Orders
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    suspend fun getOrdersRealtime(activeOnly: Boolean = false): Flow<List<Orders>> {
        return callbackFlow {
            val workspaceId = getWorkspaceId()
            if (workspaceId == null) {
                close(IllegalStateException("Workspace ID not found"))
                return@callbackFlow
            }

            var query: Query = db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")

            if (activeOnly) {
                // Hanya ambil yang isArchived == false untuk hemat reads di halaman ManageOrder
                // Perhatikan: properti Kotlin 'isArchived' secara default dipetakan ke field 'archived' di Firestore
                query = query.whereEqualTo("archived", false)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                        return@addSnapshotListener
                    }
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    var orders = snapshot.toObjects(Orders::class.java)
                    
                    // Sort secara lokal (berdasarkan tanggal) untuk menghindari kebutuhan Index Composite Firestore
                    orders = orders.sortedBy { it.orderDate }

                    // Evaluasi lazy saat ada trigger data masuk (seperti metode awal yang hemat)
                    if (activeOnly) {
                        val currentTime = System.currentTimeMillis()
                        val oneDayInMillis = 24 * 60 * 60 * 1000L
                        
                        val ordersToHide = orders.filter { order ->
                            val isDone = order.status == "Done"
                            val isPaidAndPickedUp = order.alreadyPaid && order.alreadyPickedUp
                            
                            val pickupTime = order.orderPickupDate?.toDate()?.time ?: 0L
                            val isPickedUpMoreThanOneDayAgo = if (pickupTime > 0) {
                                (currentTime - pickupTime) > oneDayInMillis
                            } else true // Jika data lama tidak punya timestamp tapi alreadyPickedUp true, anggap sudah lewat 1 hari
                            
                            val paidTime = order.orderPaidDate?.toDate()?.time ?: 0L
                            val isPaidMoreThanOneDayAgo = if (paidTime > 0) {
                                (currentTime - paidTime) > oneDayInMillis
                            } else true // Jika data lama tidak punya timestamp tapi alreadyPaid true, anggap sudah lewat 1 hari
                            
                            // Sembunyikan HANYA JIKA kedua aktivitas (Pickup & Paid) sudah lewat 1 hari
                            isDone && isPaidAndPickedUp && isPickedUpMoreThanOneDayAgo && isPaidMoreThanOneDayAgo
                        }
                        
                        if (ordersToHide.isNotEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val batch = db.batch()
                                    ordersToHide.forEach { order ->
                                        val ref = db.collection("workspaces")
                                            .document(workspaceId)
                                            .collection("orders")
                                            .document(order.orderId)
                                        batch.update(ref, "archived", true)
                                    }
                                    
                                    val metadataRef = db.collection("workspaces")
                                        .document(workspaceId)
                                        .collection("metadata")
                                        .document("counts")
                                    
                                    val hiddenCount = ordersToHide.size.toLong()
                                    batch.update(metadataRef, "orderCount", FieldValue.increment(-hiddenCount))
                                    batch.update(metadataRef, "orderDoneCount", FieldValue.increment(-hiddenCount))

                                    batch.commit().await()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            orders = orders.filterNot { it in ordersToHide }
                        }
                    }

                    trySend(orders).isSuccess
                }
            }

            // Pastikan listener dihapus saat flow ditutup
            awaitClose { listener.remove() }
        }
    }

    suspend fun getActiveOrdersWithDeadlinesRealtime(): Flow<List<Orders>> {
        return callbackFlow {
            val workspaceId = getWorkspaceId()
            if (workspaceId == null) {
                close(IllegalStateException("Workspace ID not found"))
                return@callbackFlow
            }

            // Fetch only orders that are not Done and have a due date
            val listener = db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .whereIn("status", listOf("On Queue", "On Process"))
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
                            .filter { it.orderDueDate != null }
                        trySend(orders).isSuccess
                    }
                }
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

            val metadataDocRef = workspaceRef.collection("metadata").document("counts")

            // 3. GUNAKAN BATCH: Simpan keduanya secara bersamaan (Atomic)
            // Ini akan memastikan koleksi 'notifications' otomatis terbuat di Firestore
            db.runBatch { batch ->
                batch.set(newOrderDoc, finalOrder)
                batch.set(newNotifDoc, notification)
                batch.update(metadataDocRef, "orderCount", FieldValue.increment(1))
                if (finalOrder.status == "On Queue") {
                    batch.update(metadataDocRef, "orderOnQueueCount", FieldValue.increment(1))
                }
            }.await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Fungsi untuk update status order
    suspend fun updateOrderStatus(orderId: String, oldStatus: String, newStatus: String): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            val workspaceRef = db.collection("workspaces").document(workspaceId)
            val orderRef = workspaceRef.collection("orders").document(orderId)
            val metadataRef = workspaceRef.collection("metadata").document("counts")

            db.runBatch { batch ->
                batch.update(orderRef, "status", newStatus)
                
                if (newStatus == "Done") {
                    batch.update(orderRef, "orderFinishDate", Timestamp.now())
                } else {
                    batch.update(orderRef, "orderFinishDate", null)
                    // Reset field picked up jika status dikembalikan ke On Queue / On Process
                    batch.update(orderRef, "alreadyPickedUp", false)
                    batch.update(orderRef, "orderPickupDate", null)
                }

                // Update metadata counts
                val oldField = when (oldStatus) {
                    "On Queue" -> "orderOnQueueCount"
                    "On Process" -> "orderOnProcessCount"
                    "Done" -> "orderDoneCount"
                    else -> null
                }
                val newField = when (newStatus) {
                    "On Queue" -> "orderOnQueueCount"
                    "On Process" -> "orderOnProcessCount"
                    "Done" -> "orderDoneCount"
                    else -> null
                }

                if (oldField != null) batch.update(metadataRef, oldField, FieldValue.increment(-1))
                if (newField != null) batch.update(metadataRef, newField, FieldValue.increment(1))
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getOrderById(orderId: String): Orders? {
        val workspaceId = getWorkspaceId() ?: return null
        return try {
            val doc = db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .document(orderId)
                .get()
                .await()
            doc.toObject(Orders::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateOrder(order: Orders): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        val currentUser = Firebase.auth.currentUser ?: return false

        val userName = currentUser.displayName ?: "Anggota tim"

        return try {
            val workspaceRef = db.collection("workspaces").document(workspaceId)
            val orderRef = workspaceRef.collection("orders").document(order.orderId)
            val newNotifDoc = workspaceRef.collection("notifications").document()

            // Siapkan data Notifikasi (Order Diperbarui)
            val notification = Notifications(
                notificationId = newNotifDoc.id,
                title = "Order Diperbarui",
                message = "$userName memperbarui order milik ${order.customerName}",
                senderUid = currentUser.uid,
                timestamp = Timestamp.now(),
                readBy = listOf(currentUser.uid)
            )

            db.runBatch { batch ->
                batch.set(orderRef, order) // Timpa dokumen dengan data baru
                batch.set(newNotifDoc, notification)
            }.await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateOrderPaymentStatus(orderId: String, isPaid: Boolean): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            val paidDate = if (isPaid) Timestamp.now() else null
            db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "alreadyPaid" to isPaid,
                        "orderPaidDate" to paidDate
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateOrderPickupStatus(orderId: String, isPickedUp: Boolean): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            val pickupDate = if (isPickedUp) Timestamp.now() else null
            
            db.collection("workspaces")
                .document(workspaceId)
                .collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "alreadyPickedUp" to isPickedUp,
                        "orderPickupDate" to pickupDate
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteOrder(orderId: String, status: String?): Boolean {
        val workspaceId = getWorkspaceId() ?: return false
        return try {
            val workspaceRef = db.collection("workspaces").document(workspaceId)
            val orderDocRef = workspaceRef.collection("orders").document(orderId)
            val metadataDocRef = workspaceRef.collection("metadata").document("counts")

            db.runBatch { batch ->
                batch.delete(orderDocRef)
                batch.update(metadataDocRef, "orderCount", FieldValue.increment(-1))
                
                val statusField = when (status) {
                    "On Queue" -> "orderOnQueueCount"
                    "On Process" -> "orderOnProcessCount"
                    "Done" -> "orderDoneCount"
                    else -> null
                }
                if (statusField != null) {
                    batch.update(metadataDocRef, statusField, FieldValue.increment(-1))
                }
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}