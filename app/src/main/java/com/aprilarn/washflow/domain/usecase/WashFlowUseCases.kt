package com.aprilarn.washflow.domain.usecase

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.model.WorkspaceMetadata
import com.aprilarn.washflow.data.model.Workspaces
import com.aprilarn.washflow.domain.repository.CustomerRepositoryContract
import com.aprilarn.washflow.domain.repository.ItemRepositoryContract
import com.aprilarn.washflow.domain.repository.OrderRepositoryContract
import com.aprilarn.washflow.domain.repository.ServiceRepositoryContract
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
        if (serviceId.isBlank() || itemName.isBlank() || itemPrice < 0.0) return false
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
    fun observeOrders(activeOnly: Boolean = false, startDate: Timestamp? = null): Flow<List<Orders>> {
        return repository.getOrdersRealtime(activeOnly = activeOnly, startDate = startDate)
    }

    fun observeActiveOrdersWithDeadlines(): Flow<List<Orders>> {
        return repository.getActiveOrdersWithDeadlinesRealtime()
    }

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
    fun observeMetadata(): Flow<WorkspaceMetadata> = repository.getMetadataRealtime()
    suspend fun renameWorkspace(newName: String): Boolean = repository.updateWorkspaceName(newName.trim())
    suspend fun updateOperationalHours(openTime: String?, closeTime: String?): Boolean {
        return repository.updateOperationalHours(openTime, closeTime)
    }
}
