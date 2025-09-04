package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aprilarn.washflow.AppNavigation
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun NavigationBar(
    navController: NavController, // Tambahkan NavController
    modifier: Modifier = Modifier
) {
    // Dapatkan rute saat ini untuk menentukan item mana yang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .wrapContentWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Definisikan setiap item navigasi
            val items = listOf(
                AppNavigation.Home to Icons.Default.Home,
                AppNavigation.Orders to Icons.Default.ShoppingCart,
                AppNavigation.Customers to Icons.Default.Person, // Rute ke CustomersScreen
                AppNavigation.Settings to Icons.Default.Settings
            )

            // Loop untuk membuat setiap item
            items.forEach { (screen, icon) ->
                val label = when (screen) {
                    AppNavigation.Home -> "Home"
                    AppNavigation.Orders -> "Orders"
                    AppNavigation.Customers -> "Users"
                    AppNavigation.Settings -> "Settings"
                }

                BottomNavItem(
                    icon = icon,
                    label = label,
                    isSelected = currentRoute == screen.route,
                    onClick = {
                        // Navigasi hanya jika belum berada di layar tersebut
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                // Pop up ke start destination untuk menghindari tumpukan back stack
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit // Tambahkan onClick
) {
    val contentColor = if (isSelected) GrayBlue else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick) // Buat item bisa diklik
            .padding(horizontal = 8.dp) // Beri sedikit padding agar area klik lebih luas
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 12.sp
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun BottomNavigationBarPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        // Beri NavController palsu untuk preview
        NavigationBar(navController = rememberNavController())
    }
}