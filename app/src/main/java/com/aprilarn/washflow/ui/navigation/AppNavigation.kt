package com.aprilarn.washflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppNavigation(val route: String, val icon: ImageVector, val label: String) {
    object Home : AppNavigation("home", Icons.Default.Home, "Home")
    object Orders : AppNavigation("orders", Icons.Default.ShoppingCart, "Orders")
    object Customers : AppNavigation("customers", Icons.Default.Person, "Users")
    object Services : AppNavigation("services", Icons.Default.List, "Services")
    object Settings : AppNavigation("settings", Icons.Default.Settings, "Settings")
}