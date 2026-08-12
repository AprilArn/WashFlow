package com.aprilarn.washflow.ui.analytics.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.analytics.CardInfo
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun MiniCardItem(card: CardInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White // Diubah menjadi putih agar menonjol di atas latar abu-abu
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = card.balance, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GrayBlue)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = card.currency, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "...${card.lastDigits}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                if (card.type == "Mastercard") {
                    Row {
                        Box(modifier = Modifier.size(16.dp).background(GrayBlue, CircleShape))
                        Box(modifier = Modifier.offset(x = (-6).dp).size(16.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
                    }
                } else {
                    Text(text = card.type, fontWeight = FontWeight.Black, color = GrayBlue, fontSize = 14.sp)
                }
            }
        }
    }
}
