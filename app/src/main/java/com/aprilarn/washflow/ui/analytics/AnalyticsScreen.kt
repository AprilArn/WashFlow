package com.aprilarn.washflow.ui.analytics

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun AnalyticsScreen(

) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Bar
        Column (
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "NavBar"
            )
        }

        // Panel Utama
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Panel Kiri
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            }

            // Panel Kanan
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GrayBlue, RoundedCornerShape(24.dp)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(16.dp)) // Berikan shape langsung ke background
                        .clip(RoundedCornerShape(16.dp)), // Opsional: jika ingin konten di dalamnya juga terpotong
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    Text(
                        text = "Daily Income",
                        style = MaterialTheme.typography.titleMedium,

                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "weekly",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 500)
@Composable
fun HomeScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        AnalyticsScreen()
    }
}
