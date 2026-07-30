// com/aprilarn/washflow/AppNavigation.kt
package com.aprilarn.washflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppNavigation(val route: String, val icon: ImageVector, val label: String) {
    object Home : AppNavigation("home", Icons.Rounded.Home, "Home")

    object Contributors : AppNavigation("contributors", Icons.Rounded.Group, "Contributors")

    object Orders : AppNavigation("orders", Icons.Rounded.ShoppingCart, "Create Order")

    object ManageOrder : AppNavigation("manage_order", Icons.Rounded.Phone, "Manage Order")

    object Customers : AppNavigation("customers", Icons.Rounded.Person, "Customers")
    object Services : AppNavigation("services", Icons.AutoMirrored.Rounded.List, "Services")
    object Items : AppNavigation("items", Icons.Rounded.ShoppingCart, "Items")

    object TableData : AppNavigation("table_data", Icons.Rounded.Storage, "Data")

    object FinancialReport : AppNavigation("financial_report", Icons.Rounded.Assessment, "Report")

    object Settings : AppNavigation("settings", Icons.Rounded.Settings, "Settings")
}