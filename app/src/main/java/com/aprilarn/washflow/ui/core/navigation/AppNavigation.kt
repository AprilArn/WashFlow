package com.aprilarn.washflow.ui.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.aprilarn.washflow.R

sealed class AppNavigation(
    val route: String,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int
) {
    object Home : AppNavigation("home", Icons.Rounded.Home, R.string.nav_home)

    object Contributors : AppNavigation("contributors", Icons.Rounded.Group, R.string.nav_contributors)

    object Orders : AppNavigation("orders", Icons.Rounded.ShoppingCart, R.string.nav_create_order)

    object ManageOrder : AppNavigation("manage_order", Icons.Rounded.Phone, R.string.nav_manage_order)

    object Customers : AppNavigation("customers", Icons.Rounded.Person, R.string.nav_customers)
    object Services : AppNavigation("services", Icons.AutoMirrored.Rounded.List, R.string.nav_services)
    object Items : AppNavigation("items", Icons.Rounded.ShoppingCart, R.string.nav_items)

    object TableData : AppNavigation("table_data", Icons.Rounded.Storage, R.string.nav_data)

    object Analytics : AppNavigation("analytics", Icons.Rounded.Assessment, R.string.nav_analytics)

    object Settings : AppNavigation("settings", Icons.Rounded.Settings, R.string.nav_settings)
}
