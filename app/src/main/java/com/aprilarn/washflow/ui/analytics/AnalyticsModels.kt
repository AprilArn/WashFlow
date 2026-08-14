package com.aprilarn.washflow.ui.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Transaction(
    val icon: ImageVector,
    val title: String,
    val date: String,
    val amount: String,
    val category: String
)

data class UpcomingPayment(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val amount: String,
    val color: Color
)

data class CardInfo(
    val balance: String,
    val currency: String,
    val lastDigits: String,
    val type: String
)

val mockRecentOrders = listOf(
    Transaction(Icons.Rounded.History, "Andi Setiawan", "03 Aug 2026, 15:43", "Rp 56.500", "Completed"),
    Transaction(Icons.Rounded.History, "Budi Santoso", "01 Aug 2026, 12:58", "Rp 25.000", "Completed"),
    Transaction(Icons.Rounded.History, "Citra Lestari", "28 Jul 2026, 21:40", "Rp 70.000", "Completed"),
    Transaction(Icons.Rounded.History, "Dewi Anggraini", "28 Jul 2026, 09:28", "Rp 30.750", "Completed"),
    Transaction(Icons.Rounded.History, "Eko Prasetyo", "26 Jul 2026, 18:25", "Rp 100.000", "Completed")
)

val mockUpcomingDeadlines = listOf(
    UpcomingPayment(Icons.Rounded.LocalLaundryService, "Fajar Sidik", "Ready for pickup", "Rp 45.000", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.DryCleaning, "Gita Permata", "In progress", "Rp 120.000", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.Iron, "Hadi Wijaya", "In queue", "Rp 35.000", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.Wash, "Indah Sari", "Ready for pickup", "Rp 60.000", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.LocalLaundryService, "Joko Susilo", "In progress", "Rp 85.000", Color(0xFFF4F5F8))
)

val mockCards = listOf(
    CardInfo("98,500", "USD", "4141", "Mastercard"),
    CardInfo("76,280", "EUR", "8345", "VISA")
)
