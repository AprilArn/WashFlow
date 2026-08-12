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

val mockTransactions = listOf(
    Transaction(Icons.Rounded.LocalTaxi, "Taxi Trips", "03 Aug 2022, 15:43", "$56.50", "Transport"),
    Transaction(Icons.Rounded.DirectionsBus, "Public Transport", "01 Aug 2022, 12:58", "$2.50", "Transport"),
    Transaction(Icons.Rounded.Flight, "Plane Tickets", "28 Jul 2022, 21:40", "$70", "Travel"),
    Transaction(Icons.Rounded.LocalGasStation, "Gas Station", "28 Jul 2022, 09:28", "$30.75", "Utilities"),
    Transaction(Icons.Rounded.FitnessCenter, "Gym", "26 Jul 2022, 18:25", "$100.00", "Health")
)

val mockUpcomingPayments = listOf(
    UpcomingPayment(Icons.Rounded.Chair, "Freelance", "Unregular payment", "$1,500", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.Savings, "Salary", "Regular payment", "$4,000", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.ShoppingCart, "Shopping", "Monthly groceries", "$500", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.ElectricBolt, "Electricity", "Utility bill", "$120", Color(0xFFF4F5F8)),
    UpcomingPayment(Icons.Rounded.PhoneAndroid, "Internet", "Fiber optic", "$60", Color(0xFFF4F5F8))
)

val mockCards = listOf(
    CardInfo("98,500", "USD", "4141", "Mastercard"),
    CardInfo("76,280", "EUR", "8345", "VISA")
)
