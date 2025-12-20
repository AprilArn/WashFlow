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
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Dapatkan rute saat ini untuk menentukan item mana yang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .wrapContentWidth()
            //.height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            //.background(Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Definisikan setiap item navigasi
            // Daftar item sekarang lebih dinamis
            val items = listOf(
                AppNavigation.Home,
                AppNavigation.Orders,
                AppNavigation.TableData,
                AppNavigation.Settings
            )

            // Loop untuk membuat setiap item
            items.forEach { screen ->
                NavItem(
                    icon = screen.icon,
                    label = screen.label,
                    isSelected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
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
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit // Tambahkan onClick
) {
    val contentColor = if (isSelected) GrayBlue else Color.White.copy(alpha = 0.7f)
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    Box( // Gunakan Box dan padding untuk area klik yang lebih baik
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(8.dp) // Padding internal ikon
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(28.dp) // Ukuran ikon sedikit diperkecil agar muat di header
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun NavigationBarPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        // Beri NavController palsu untuk preview
        NavigationBar(navController = rememberNavController())
    }
}