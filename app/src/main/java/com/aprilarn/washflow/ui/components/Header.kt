// File: ui/components/WashFlowTopBar.kt (File Baru)

package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Komponen Top Bar yang menampilkan judul aplikasi "WashFlow".
 */
@Composable
fun Header(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            // Padding agar tidak terlalu mepet ke tepi layar
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Wash",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = GrayBlue,
                fontSize = 22.sp,
                fontStyle = FontStyle.Normal,
            ),
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = "Flow",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontSize = 22.sp,
                fontStyle = FontStyle.Italic
            ),
            fontWeight = FontWeight.Normal,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB9E9FF)
@Composable
fun HeaderPreview() {
    Header()
}