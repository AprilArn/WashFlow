package com.aprilarn.washflow.domain.model

import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Notifications
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.model.Users
import com.aprilarn.washflow.data.model.WorkspaceMetadata
import com.aprilarn.washflow.data.model.Workspaces

/**
 * Kontrak entitas domain pada layer Domain (Clean Architecture).
 */
typealias CustomerDomain = Customers
typealias ServiceDomain = Services
typealias ItemDomain = Items
typealias OrderDomain = Orders
typealias OrderItemDomain = OrderItem
typealias WorkspaceDomain = Workspaces
typealias WorkspaceMetadataDomain = WorkspaceMetadata
typealias NotificationDomain = Notifications
typealias InviteDomain = Invites
typealias UserDomain = Users

enum class OrderQueueStatus(val label: String, val subTitleId: String) {
    ON_QUEUE("On Queue", "Dalam antrian"),
    ON_PROCESS("On Process", "Sedang diproses"),
    DONE("Done", "Selesai");

    companion object {
        fun fromLabel(label: String?): OrderQueueStatus {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: ON_QUEUE
        }
    }
}
