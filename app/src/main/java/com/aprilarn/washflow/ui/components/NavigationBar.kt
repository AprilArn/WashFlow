package com.aprilarn.washflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    onClick: () -> Unit
) {
    // Animasi perubahan warna background - Dibuat lebih stiff agar tidak terlalu bouncy
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy, // Menghilangkan efek bouncy
            stiffness = Spring.StiffnessMedium
        ),
        label = "bgColorAnimation"
    )

    // Animasi perubahan warna konten (ikon & teks)
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) GrayBlue else Color.White.copy(alpha = 0.7f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "contentColorAnimation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp)) // Pill shape
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Matikan ripple effect
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        // Animasi ekspansi teks saat dipilih
        AnimatedVisibility(visible = isSelected) {
            Row(
                verticalAlignment = Alignment.CenterVertically // Sejajarkan teks secara vertikal dengan ikon
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
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

@Preview(name = "Data Selected", showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun NavigationBarDataSelectedPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                AppNavigation.Home,
                AppNavigation.Orders,
                AppNavigation.TableData,
                AppNavigation.Settings
            )

            items.forEach { screen ->
                NavItem(
                    icon = screen.icon,
                    label = screen.label,
                    isSelected = screen == AppNavigation.TableData, // Simulasi tombol Data dipilih
                    onClick = {}
                )
            }
        }
    }
}
