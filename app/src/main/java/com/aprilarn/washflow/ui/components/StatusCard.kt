package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.components.StatusWorkflowCardIllustration
import com.aprilarn.washflow.ui.components.resolveStatusWorkflowTheme

@Composable
fun StatusCard(
    text: String,
    count: Int,
    onClick: () -> Unit
) {
    val theme = remember(text) { resolveStatusWorkflowTheme(text) }

    Surface(
        modifier = Modifier.size(width = 140.dp, height = 180.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = Color.White,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ilustrasi bergaya Google Calendar (Material Editorial) di bagian atas kartu
            StatusWorkflowCardIllustration(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
            )

            // Label status & jumlah order di bagian bawah
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.labelColor
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = theme.countColor
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 490, heightDp = 220)
@Composable
fun StatusCardPreview() {
    Row(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StatusCard(text = "On Queue", count = 3, onClick = {})
        StatusCard(text = "On Process", count = 3, onClick = {})
        StatusCard(text = "Done", count = 3, onClick = {})
    }
}
