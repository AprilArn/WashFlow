package com.aprilarn.washflow

sealed class AppNavigation(val route: String) {
    object Home : AppNavigation("home")
    object Orders : AppNavigation("orders")
    object Customers : AppNavigation("customers")
    object Settings : AppNavigation("settings")
}