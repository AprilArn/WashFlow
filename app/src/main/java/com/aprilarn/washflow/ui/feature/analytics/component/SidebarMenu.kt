package com.aprilarn.washflow.ui.feature.analytics.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.animateFloatAsState
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg

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
            Triple("Order", stringResource(R.string.analytics_tab_summary), Icons.Rounded.ShoppingCart),
            Triple("Report", stringResource(R.string.analytics_tab_ledger), Icons.Rounded.Cloud),
            Triple("Tertunda", stringResource(R.string.analytics_tab_pending), Icons.Rounded.PendingActions)
        )

        menuItems.forEach { (tabKey, displayLabel, icon) ->
            val isSelected = selectedTab == tabKey

            // Animasi Warna
            val animatedIconColor by animateColorAsState(
                targetValue = if (isSelected) PastelBlue else GrayBlue.copy(alpha = 0.65f),
                animationSpec = tween(durationMillis = 300),
                label = "iconColor"
            )
            val animatedTextColor by animateColorAsState(
                targetValue = if (isSelected) GrayBlue else GrayBlue.copy(alpha = 0.68f),
                animationSpec = tween(durationMillis = 300),
                label = "textColor"
            )
            val animatedBgColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else PastelBlueSoftBg.copy(alpha = 0.85f),
                animationSpec = tween(durationMillis = 300),
                label = "bgColor"
            )
            // Animasi efek tab "masuk/tucked in" saat tidak dipilih (ukurannya mengecil dari arah luar)
            val animatedStartPadding by animateDpAsState(
                targetValue = if (isSelected) 0.dp else 16.dp,
                animationSpec = tween(durationMillis = 300),
                label = "startPadding"
            )

            // Geser sedikit ke kanan saat dipilih agar menutupi celah anti-aliasing dengan panel utama
            val animatedOffsetX by animateDpAsState(
                targetValue = if (isSelected) 1.dp else 0.dp,
                animationSpec = tween(durationMillis = 300),
                label = "offsetX"
            )

            // Alpha untuk animasi fillet (rounded corner ke arah luar)
            val filletAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "filletAlpha"
            )

            // Sudut kanan selalu siku-siku (0.dp) agar selalu menempel pada kontainer seperti label buku sungguhan
            val shape = RoundedCornerShape(
                topStart = 10.dp,
                bottomStart = 18.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isSelected) 1f else 0f)
                    .padding(start = animatedStartPadding) // Menambahkan ruang kosong di kiri agar tab memendek saat tidak aktif
                    .offset(x = animatedOffsetX)
                    .drawBehind {
                        if (filletAlpha > 0f) {
                            val cornerRadius = 16.dp.toPx()
                            val filletColor = Color.White.copy(alpha = filletAlpha)

                            // Top Fillet
                            val topPath = Path().apply {
                                moveTo(size.width, 0f)
                                lineTo(size.width, -cornerRadius)
                                arcTo(
                                    rect = Rect(
                                        left = size.width - 2 * cornerRadius,
                                        top = -2 * cornerRadius,
                                        right = size.width,
                                        bottom = 0f
                                    ),
                                    startAngleDegrees = 0f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                close()
                            }
                            drawPath(topPath, filletColor)

                            // Bottom Fillet
                            val bottomPath = Path().apply {
                                moveTo(size.width, size.height)
                                lineTo(size.width, size.height + cornerRadius)
                                arcTo(
                                    rect = Rect(
                                        left = size.width - 2 * cornerRadius,
                                        top = size.height,
                                        right = size.width,
                                        bottom = size.height + 2 * cornerRadius
                                    ),
                                    startAngleDegrees = 0f,
                                    sweepAngleDegrees = -90f,
                                    forceMoveTo = false
                                )
                                close()
                            }
                            drawPath(bottomPath, filletColor)
                        }
                    }
                    .clip(shape)
                    .background(animatedBgColor)
                    .clickable { onTabClick(tabKey) }
                    .padding(horizontal = 14.dp, vertical = 18.dp), // Padding internal disesuaikan agar konten muat
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(20.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedIconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = displayLabel,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = animatedTextColor,
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
