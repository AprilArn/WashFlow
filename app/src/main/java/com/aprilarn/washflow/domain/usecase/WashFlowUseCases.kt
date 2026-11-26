package com.aprilarn.washflow.domain.usecase

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.model.WorkspaceMetadata
import com.aprilarn.washflow.data.model.Workspaces
import com.aprilarn.washflow.domain.repository.CustomerRepositoryContract
import com.aprilarn.washflow.domain.repository.InviteRepositoryContract
import com.aprilarn.washflow.domain.repository.ItemRepositoryContract
import com.aprilarn.washflow.domain.repository.NotificationsRepositoryContract
import com.aprilarn.washflow.domain.repository.OrderRepositoryContract
import com.aprilarn.washflow.domain.repository.ServiceRepositoryContract
import com.aprilarn.washflow.domain.repository.UserRepositoryContract
import com.aprilarn.washflow.domain.repository.WorkspaceRepositoryContract
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

/**
 * Kumpulan UseCase pada Domain Layer untuk mengelola aturan bisnis utama WashFlow.
 */
class ManageCustomerUseCase(
    private val repository: CustomerRepositoryContract
) {
    suspend fun observeCustomers(): Flow<List<Customers>> = repository.getCustomersRealtime()

    suspend fun getCustomersOnce(): List<Customers> = repository.getCustomersOnce()

    suspend fun addCustomer(name: String, contact: String): Boolean {
        if (name.isBlank()) return false
        return repository.addCustomer(name.trim(), contact.trim())
    }

    suspend fun updateCustomer(customer: Customers): Boolean {
        if (customer.customerId.isBlank() || customer.name.isBlank()) return false
        return repository.updateCustomer(
            customerId = customer.customerId,
            newName = customer.name.trim(),
            newContact = customer.contact?.trim().orEmpty()
        )
    }

    suspend fun deleteCustomer(customerId: String): Boolean {
        if (customerId.isBlank()) return false
        return repository.deleteCustomer(customerId)
    }
}

class ManageServiceUseCase(
    private val repository: ServiceRepositoryContract
) {
    suspend fun observeServices(): Flow<List<Services>> = repository.getServicesRealtime()

    suspend fun addService(serviceId: String, serviceName: String): Boolean {
        if (serviceId.isBlank() || serviceName.isBlank()) return false
        return repository.addService(serviceId.trim(), serviceName.trim())
    }

    suspend fun updateService(service: Services): Boolean {
        if (service.serviceId.isBlank() || service.serviceName.isBlank()) return false
        return repository.updateService(service.serviceId, service.serviceName.trim())
    }

    suspend fun deleteService(serviceId: String): Boolean {
        if (serviceId.isBlank()) return false
        return repository.deleteService(serviceId)
    }
}

class ManageItemUseCase(
    private val repository: ItemRepositoryContract
) {
    suspend fun observeItems(): Flow<List<Items>> = repository.getItemsRealtime()

    suspend fun addItem(serviceId: String, itemName: String, itemPrice: Double): Boolean {
        if (serviceId.isBlank() || itemName.isBlank() || itemPrice <= 0.0) return false
        return repository.addItem(serviceId.trim(), itemName.trim(), itemPrice)
    }

    suspend fun updateItem(item: Items): Boolean {
        if (item.itemId.isBlank() || item.serviceId.isBlank() || item.itemName.isBlank()) return false
        return repository.updateItem(
            itemId = item.itemId,
            newService = item.serviceId.trim(),
            newName = item.itemName.trim(),
            newPrice = item.itemPrice
        )
    }

    suspend fun deleteItem(itemId: String): Boolean {
        if (itemId.isBlank()) return false
        return repository.deleteItems(itemId)
    }
}

class ManageOrderUseCase(
    private val repository: OrderRepositoryContract
) {
    suspend fun hasOrdersBefore(date: Timestamp): Boolean = repository.hasOrdersBefore(date)

    fun observeOrders(activeOnly: Boolean = false, startDate: Timestamp? = null): Flow<List<Orders>> {
        return repository.getOrdersRealtime(activeOnly = activeOnly, startDate = startDate)
    }

    fun observeActiveOrdersWithDeadlines(): Flow<List<Orders>> {
        return repository.getActiveOrdersWithDeadlinesRealtime()
    }

    suspend fun getOrderById(orderId: String): Orders? = repository.getOrderById(orderId)

    suspend fun createOrder(order: Orders): Boolean = repository.createOrder(order)

    suspend fun updateOrder(order: Orders): Boolean = repository.updateOrder(order)

    suspend fun changeStatus(orderId: String, oldStatus: String, newStatus: String): Boolean {
        if (oldStatus == newStatus) return true
        return repository.updateOrderStatus(orderId, oldStatus, newStatus)
    }

    suspend fun togglePayment(orderId: String, isPaid: Boolean): Boolean {
        return repository.updateOrderPaymentStatus(orderId, isPaid)
    }

    suspend fun togglePickup(orderId: String, isPickedUp: Boolean): Boolean {
        return repository.updateOrderPickupStatus(orderId, isPickedUp)
    }

    suspend fun deleteOrder(orderId: String, status: String?): Boolean {
        return repository.deleteOrder(orderId, status)
    }
}

class ManageWorkspaceUseCase(
    private val repository: WorkspaceRepositoryContract
) {
    suspend fun observeCurrentWorkspace(): Flow<Workspaces?> = repository.getCurrentWorkspaceRealtime()

    suspend fun createWorkspace(): Boolean = repository.createWorkspace()

    suspend fun getCurrentWorkspaceId(): String? = repository.getCurrentWorkspaceId()

    suspend fun renameWorkspace(newName: String): Boolean {
        if (newName.isBlank()) return false
        return repository.updateWorkspaceName(newName.trim())
    }

    suspend fun updateOperationalHours(openTime: String?, closeTime: String?): Boolean {
        return repository.updateOperationalHours(openTime, closeTime)
    }

    suspend fun leaveWorkspace(): Boolean = repository.leaveWorkspace()

    suspend fun deleteCurrentWorkspace(): Boolean = repository.deleteCurrentWorkspace()

    suspend fun removeContributor(contributorUid: String): Boolean {
        if (contributorUid.isBlank()) return false
        return repository.removeContributor(contributorUid)
    }

    fun observeMetadata(): Flow<WorkspaceMetadata> = repository.getMetadataRealtime()

    suspend fun syncMetadata(workspaceId: String): Boolean = repository.syncMetadata(workspaceId)
}

class ManageInviteUseCase(
    private val repository: InviteRepositoryContract
) {
    suspend fun createInvite(maxContributors: Int, expiresAt: Timestamp): Invites? {
        return repository.createInvite(maxContributors, expiresAt)
    }

    fun observeActiveInvite(): Flow<Invites?> = repository.getActiveInviteForCurrentWorkspace()

    suspend fun checkAndExpireActiveInvite() = repository.checkAndExpireActiveInvite()

    suspend fun expireInvite(inviteId: String): Boolean = repository.expireInvite(inviteId)

    suspend fun joinWorkspace(inviteCode: String): Pair<Boolean, String> {
        return repository.joinWorkspace(inviteCode)
    }
}

class ManageNotificationsUseCase(
    private val repository: NotificationsRepositoryContract
) {
    suspend fun observeNotifications(): Flow<List<Notifications>> = repository.getNotificationsRealtime()

    suspend fun markAsRead(notificationId: String): Boolean = repository.markAsRead(notificationId)

    suspend fun markAllAsRead(notificationIds: List<String>): Boolean = repository.markAllAsRead(notificationIds)

    suspend fun cleanupOldNotifications() = repository.cleanupOldNotifications()
}

class ManageUserUseCase(
    private val repository: UserRepositoryContract
) {
    suspend fun saveUserAndGetWorkspace(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): String? {
        return repository.saveUserAndGetWorkspace(
            uid = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl
        )
    }
}
