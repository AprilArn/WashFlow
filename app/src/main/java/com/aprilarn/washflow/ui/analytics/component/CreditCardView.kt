package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreditCardView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2D265A), Color(0xFF5A4B81), Color(0xFF8E6DA8))
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = "Chip",
                        tint = Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "Contactless",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "2984  5633  7859  4141",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(text = "CARD HOLDER", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                        Text(text = "Derrick Fisher", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                    // Mock Mastercard Logo
                    Row {
                        Box(modifier = Modifier.size(20.dp).background(Color(0xFFEB001B), CircleShape))
                        Box(modifier = Modifier.offset(x = (-8).dp).size(20.dp).background(Color(0xFFF79E1B).copy(alpha = 0.8f), CircleShape))
                    }
                }
            }
        }
    }
}
