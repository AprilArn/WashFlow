package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
            .width(180.dp)
            .padding(top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val menuItems = listOf(
            "Order" to Icons.Rounded.ShoppingCart,
            "Report" to Icons.Rounded.Cloud
        )

        menuItems.forEach { (title, icon) ->
            val isSelected = selectedTab == title
            
            // Animasi Warna & Radius
            val animatedContentColor by animateColorAsState(
                targetValue = if (isSelected) GrayBlue.copy(alpha = 0.9f) else GrayBlue.copy(alpha = 0.5f),
                animationSpec = tween(durationMillis = 300),
                label = "contentColor"
            )
            val animatedBgColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Transparent,
                animationSpec = tween(durationMillis = 300),
                label = "bgColor"
            )
            val animatedBorderColor by animateColorAsState(
                targetValue = if (isSelected) Color.Transparent else animatedContentColor,
                animationSpec = tween(durationMillis = 300),
                label = "borderColor"
            )
            val animatedEndRadius by animateDpAsState(
                targetValue = if (isSelected) 12.dp else 32.dp,
                animationSpec = tween(durationMillis = 300),
                label = "endRadius"
            )

            val shape = RoundedCornerShape(
                topStart = 32.dp,
                bottomStart = 32.dp,
                topEnd = animatedEndRadius,
                bottomEnd = animatedEndRadius
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = animatedBorderColor,
                        shape = shape
                    )
                    .clip(shape)
                    .background(animatedBgColor)
                    .clickable { onTabClick(title) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedContentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = animatedContentColor,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
