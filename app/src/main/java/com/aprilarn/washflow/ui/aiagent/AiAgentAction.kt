package com.aprilarn.washflow.ui.aiagent

import com.aprilarn.washflow.AppNavigation

sealed class AiAgentAction {
    data class Navigate(val destination: AppNavigation) : AiAgentAction()
    data class AddCustomer(val name: String, val phoneNumber: String) : AiAgentAction()
    data class Unknown(val message: String) : AiAgentAction()
    object None : AiAgentAction() // Jika AI hanya ngobrol biasa
}
