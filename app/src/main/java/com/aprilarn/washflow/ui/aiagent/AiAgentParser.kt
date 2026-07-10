package com.aprilarn.washflow.ui.aiagent

import com.aprilarn.washflow.AppNavigation

object AiAgentParser {
    private const val ACTION_TAG_START = "[ACTION:"
    private const val ACTION_TAG_END = "]"

    /**
     * Parses the AI response text to find any action tags.
     * Format: [ACTION:NAVIGATE:routeName]
     */
    fun parseAction(text: String): AiAgentAction? {
        val startIndex = text.indexOf(ACTION_TAG_START)
        if (startIndex == -1) return null

        val endIndex = text.indexOf(ACTION_TAG_END, startIndex)
        if (endIndex == -1) return null

        val tag = text.substring(startIndex + ACTION_TAG_START.length, endIndex)
        val parts = tag.split(":")

        if (parts.isEmpty()) return null

        return when (parts[0]) {
            "NAVIGATE" -> {
                if (parts.size >= 2) {
                    val route = parts[1]
                    mapRouteToAction(route)
                } else null
            }
            "ADD_CUSTOMER" -> {
                if (parts.size >= 3) {
                    val name = parts[1]
                    val phone = parts[2]
                    AiAgentAction.AddCustomer(name, phone)
                } else null
            }
            "ADD_ITEM" -> {
                if (parts.size >= 4) {
                    val itemName = parts[1]
                    val itemPrice = parts[2].toDoubleOrNull() ?: 0.0
                    val serviceName = parts[3]
                    AiAgentAction.AddItem(itemName, itemPrice, serviceName)
                } else null
            }
            "DELETE_CUSTOMER" -> {
                if (parts.size >= 2) {
                    val name = parts[1]
                    val contact = if (parts.size >= 3) parts[2] else ""
                    AiAgentAction.DeleteCustomer(name, contact)
                } else null
            }
            "DELETE_ITEM" -> {
                if (parts.size >= 2) {
                    val itemName = parts[1]
                    AiAgentAction.DeleteItem(itemName)
                } else null
            }
            else -> null
        }
    }

    /**
     * Removes all action tags from the text to provide a clean response to the user.
     */
    fun cleanText(text: String): String {
        return text.replace(Regex("\\[ACTION:.*?\\]"), "").trim()
    }

    private fun mapRouteToAction(route: String): AiAgentAction? {
        val destination = when (route.lowercase()) {
            "home" -> AppNavigation.Home
            "contributors" -> AppNavigation.Contributors
            "orders" -> AppNavigation.Orders
            "manage_order" -> AppNavigation.ManageOrder
            "customers" -> AppNavigation.Customers
            "services" -> AppNavigation.Services
            "items" -> AppNavigation.Items
            "table_data" -> AppNavigation.TableData
            "settings" -> AppNavigation.Settings
            else -> null
        }
        return destination?.let { AiAgentAction.Navigate(it) }
    }
}
