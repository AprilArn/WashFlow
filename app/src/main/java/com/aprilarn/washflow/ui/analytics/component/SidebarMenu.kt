package com.aprilarn.washflow.ui.analytics.component

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
            .width(160.dp)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val menuItems = listOf(
            "Order" to Icons.Rounded.ShoppingCart,
            "Report" to Icons.Rounded.Cloud
        )

        menuItems.forEach { (title, icon) ->
            val isSelected = selectedTab == title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier.border(1.dp, GrayBlue.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        } else {
                            Modifier
                        }
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) GrayBlue.copy(alpha = 0.05f) else Color.Transparent)
                    .clickable { onTabClick(title) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) GrayBlue else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) GrayBlue else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}
