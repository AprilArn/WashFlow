// com/aprilarn/washflow/AppNavigation.kt
package com.aprilarn.washflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppNavigation(val route: String, val icon: ImageVector, val label: String) {
    object Home : AppNavigation("home", Icons.Default.Home, "Home")

    object Contributors : AppNavigation("contributors", Icons.Default.Group, "Contributors")

    object Orders : AppNavigation("orders", Icons.Default.ShoppingCart, "Orders")

    object ManageOrder : AppNavigation("manage_order", Icons.Default.Phone, "Manage Order")

    object Customers : AppNavigation("customers", Icons.Default.Person, "Users")
    object Services : AppNavigation("services", Icons.Default.List, "Services")
    object Items : AppNavigation("items", Icons.Default.ShoppingCart, "Items")

    // Tambahkan rute baru untuk 'TableDataScreen'
    object TableData : AppNavigation("table_data", Icons.Default.Storage, "Data")

    object Settings : AppNavigation("settings", Icons.Default.Settings, "Settings")
}