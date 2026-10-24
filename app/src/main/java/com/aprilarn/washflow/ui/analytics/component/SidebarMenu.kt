package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun SidebarMenu(
    selectedTab: String,
    onTabClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp) // Lebar dipertajam agar lebih menyerupai tab buku ketimbang blok besar
            .padding(top = 40.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Jarak antar tab dirapatkan
    ) {
        val menuItems = listOf(
            "Order" to Icons.Rounded.ShoppingCart,
            "Report" to Icons.Rounded.Cloud
        )

        menuItems.forEach { (title, icon) ->
            val isSelected = selectedTab == title

            // Animasi Warna
            val animatedContentColor by animateColorAsState(
                targetValue = if (isSelected) GrayBlue else GrayBlue.copy(alpha = 0.6f),
                animationSpec = tween(durationMillis = 300),
                label = "contentColor"
            )
            val animatedBgColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFFD8DDE6), // Warna solid abu-abu yang mengesankan posisi di belakang/terkena bayangan
                animationSpec = tween(durationMillis = 300),
                label = "bgColor"
            )
            // Animasi efek tab "masuk/tucked in" saat tidak dipilih (ukurannya mengecil dari arah luar)
            val animatedStartPadding by animateDpAsState(
                targetValue = if (isSelected) 0.dp else 24.dp,
                animationSpec = tween(durationMillis = 300),
                label = "startPadding"
            )

            // Geser sedikit ke kanan saat dipilih agar menutupi celah anti-aliasing dengan panel utama
            val animatedOffsetX by animateDpAsState(
                targetValue = if (isSelected) 1.dp else 0.dp,
                animationSpec = tween(durationMillis = 300),
                label = "offsetX"
            )

            // Sudut kanan selalu siku-siku (0.dp) agar selalu menempel pada kontainer seperti label buku sungguhan
            val shape = RoundedCornerShape(
                topStart = 8.dp,
                bottomStart = 18.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = animatedStartPadding) // Menambahkan ruang kosong di kiri agar tab memendek saat tidak aktif
                    .offset(x = animatedOffsetX)
                    .clip(shape)
                    .background(animatedBgColor)
                    .clickable { onTabClick(title) }
                    .padding(horizontal = 16.dp, vertical = 18.dp), // Padding internal disesuaikan agar konten muat
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedContentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = animatedContentColor,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
