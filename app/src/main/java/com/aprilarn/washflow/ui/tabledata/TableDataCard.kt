package com.aprilarn.washflow.ui.tabledata

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aprilarn.washflow.ui.tabledata.TableDataScreen
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun TableDataCard(
    text: String,
    subText: String,
    count: Int,
    onClick: () -> Unit
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val borderColor = Color.White
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .background(Color.White.copy(alpha = 0.25f), shape = borderRadius)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = borderRadius
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$text",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = GrayBlue
                    )
                )
                Text(
                    text = "$subText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayBlue
                )
            }
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = GrayBlue
                )
            )
        }
    }
}

@Preview()
@Composable
fun PreviewTableDataCard() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        TableDataCard(
            text = "Customers",
            subText = "Pelanggan",
            count = 16,
            onClick = {}
        )
    }
}