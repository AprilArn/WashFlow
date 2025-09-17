// com/aprilarn/washflow/data/model/FirebaseCollection.kt
package com.aprilarn.washflow.data.model

import com.google.firebase.Timestamp

data class Users (
    val uid: String,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    var workspaceId: String?  // workspace aktif saat ini
)

data class Workspaces (
    // val workspaceId: String, // tidak perlu, karena tidak dipakai oleh koleksi Invites
    val workspaceName: String? = null,
    val ownerUid: String? = null,
    val createdAt: Timestamp,
    val contributors: Map<String, String>? = null // Daftar UID pengguna yang berkontribusi (uid, role)
)

    data class Customers (
        val customerId: String = "",
        val name: String = "",
        val contact: String? = null
    )

    data class Services (
        val serviceId: String = "",
        val serviceName: String = ""
    )

    data class Items (
        val itemId: String = "",
        val itemName: String = "",
        val itemPrice: Double = 0.0,
        val serviceId: String = ""
    )

    data class Orders (
        val orderId: String,
        val customerId: String,  // referensi ke Customers
        val customerName: String? = null,
        val orderDate: Timestamp,
        val orderDueDate: Timestamp? = null,
        val orderItems: List<OrderItem> = emptyList(),
        val totalPrice: Double? = 0.0,
        val status: String? = null,  // e.g., "in queue", "in progress", "ready for pickup", "completed"
    )

        data class OrderItem (
            val itemId: String,  // referensi ke Items
            val itemName: String? = null,
            val itemPrice: Double? = 0.0,
            val serviceId: String? = null,
            val itemQuantity: Int? = 0,
            val subtotal: Double? = 0.0
        )

data class Invites (
    val inviteId: String? = null,
    val workspaceId: String? = null, // workspace tujuan (diambil dari state viewmodel aplikasi)
    val persons: Int? = null,
    val expiresAt: Timestamp? = null,
    val status: String? = null,
)
