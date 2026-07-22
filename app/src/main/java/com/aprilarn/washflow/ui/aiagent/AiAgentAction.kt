package com.aprilarn.washflow.ui.aiagent

import com.aprilarn.washflow.AppNavigation

sealed class AiAgentAction {
    data class Navigate(val destination: AppNavigation, val isImmediate: Boolean = false) : AiAgentAction()
    data class AddCustomer(val name: String, val phoneNumber: String) : AiAgentAction()
    data class DeleteCustomer(val name: String, val contact: String, val customerId: String = "") : AiAgentAction()
    data class AddItem(val itemName: String, val itemPrice: Double, val serviceName: String, val serviceId: String = "") : AiAgentAction()
    data class DeleteItem(val itemName: String, val serviceName: String = "", val itemId: String = "") : AiAgentAction()
    data class Unknown(val message: String) : AiAgentAction()
    object None : AiAgentAction() // Jika AI hanya ngobrol biasa
}
