package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.analytics.UpcomingPayment
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun UpcomingPaymentCard(payment: UpcomingPayment) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = payment.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GrayBlue, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(payment.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(text = payment.title, fontWeight = FontWeight.Bold, color = GrayBlue, fontSize = 14.sp)
                Text(text = payment.subtitle, fontSize = 9.sp, color = Color.Gray)
            }

            Text(text = payment.amount, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = GrayBlue)
        }
    }
}
