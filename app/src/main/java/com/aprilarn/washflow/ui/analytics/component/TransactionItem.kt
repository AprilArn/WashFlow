package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.analytics.Transaction
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(transaction.icon, contentDescription = null, tint = GrayBlue, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.title, fontWeight = FontWeight.Bold, color = GrayBlue, fontSize = 14.sp)
            Text(text = transaction.date, fontSize = 11.sp, color = Color.Gray)
        }

        Text(text = transaction.amount, fontWeight = FontWeight.Bold, color = GrayBlue, fontSize = 14.sp)

        Spacer(modifier = Modifier.width(16.dp))

        Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray)
    }
}
