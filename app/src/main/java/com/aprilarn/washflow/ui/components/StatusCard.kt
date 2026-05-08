package com.example.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun StatusCard(
    text: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(width = 140.dp, height = 180.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = Color.White,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp), // Padding dalam untuk teks
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f)) //ganti dengan gambar
            Text(text, color = GrayBlue)
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = GrayBlue
            )
        }
    }
}

@Preview
@Composable
fun StatusCardPreview() {
    StatusCard(text = "In Queue", count = 12, onClick = {})
}