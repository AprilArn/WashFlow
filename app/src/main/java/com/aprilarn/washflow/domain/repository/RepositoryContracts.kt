package com.aprilarn.washflow.domain.repository

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.model.WorkspaceMetadata
import com.aprilarn.washflow.data.model.Workspaces
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

interface CustomerRepositoryContract {
    suspend fun getCustomersRealtime(): Flow<List<Customers>>
    suspend fun addCustomer(customerName: String, customerContact: String): Boolean
    suspend fun updateCustomer(customerId: String, newName: String, newContact: String): Boolean
    suspend fun deleteCustomer(customerId: String): Boolean
}

interface ServiceRepositoryContract {
    suspend fun getServicesRealtime(): Flow<List<Services>>
    suspend fun addService(serviceId: String, serviceName: String): Boolean
    suspend fun updateService(serviceId: String, newName: String): Boolean
    suspend fun deleteService(serviceId: String): Boolean
}

interface ItemRepositoryContract {
    suspend fun getItemsRealtime(): Flow<List<Items>>
    suspend fun addItem(serviceId: String, itemName: String, itemPrice: Double): Boolean
    suspend fun updateItem(itemId: String, newService: String, newName: String, newPrice: Double): Boolean
    suspend fun deleteItems(itemId: String): Boolean
}

interface OrderRepositoryContract {
    suspend fun hasOrdersBefore(date: Timestamp): Boolean
    fun getOrdersRealtime(activeOnly: Boolean = false, startDate: Timestamp? = null): Flow<List<Orders>>
    fun getActiveOrdersWithDeadlinesRealtime(): Flow<List<Orders>>
    suspend fun createOrder(order: Orders): Boolean
    suspend fun updateOrderStatus(orderId: String, oldStatus: String, newStatus: String): Boolean
    suspend fun getOrderById(orderId: String): Orders?
    suspend fun updateOrder(order: Orders): Boolean
    suspend fun updateOrderPaymentStatus(orderId: String, isPaid: Boolean): Boolean
    suspend fun updateOrderPickupStatus(orderId: String, isPickedUp: Boolean): Boolean
    suspend fun deleteOrder(orderId: String, status: String?): Boolean
}

interface WorkspaceRepositoryContract {
    suspend fun getCurrentWorkspaceRealtime(): Flow<Workspaces?>
    suspend fun createWorkspace(): Boolean
    suspend fun getCurrentWorkspaceId(): String?
    suspend fun updateWorkspaceName(newName: String): Boolean
    suspend fun updateOperationalHours(openTime: String?, closeTime: String?): Boolean
    suspend fun leaveWorkspace(): Boolean
    suspend fun deleteCurrentWorkspace(): Boolean
    suspend fun removeContributor(contributorUid: String): Boolean
    fun getMetadataRealtime(): Flow<WorkspaceMetadata>
    suspend fun syncMetadata(workspaceId: String): Boolean
}

interface InviteRepositoryContract {
    suspend fun createInvite(maxContributors: Int, expiresAt: Timestamp): Invites?
    fun getActiveInviteForCurrentWorkspace(): Flow<Invites?>
    suspend fun checkAndExpireActiveInvite()
    suspend fun expireInvite(inviteId: String): Boolean
    suspend fun joinWorkspace(inviteCode: String): Pair<Boolean, String>
}

interface NotificationsRepositoryContract {
    suspend fun getNotificationsRealtime(): Flow<List<Notifications>>
    suspend fun markAsRead(notificationId: String): Boolean
    suspend fun markAllAsRead(notificationIds: List<String>): Boolean
    suspend fun cleanupOldNotifications()
}

interface UserRepositoryContract {
    suspend fun saveUserAndGetWorkspace(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): String?
}
