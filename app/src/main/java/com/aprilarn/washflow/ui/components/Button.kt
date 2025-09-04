package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Tombol aksi utama yang isinya dapat diubah secara dinamis.
 *
 * @param text Teks yang akan ditampilkan di dalam tombol.
 * @param onClick Aksi yang dijalankan ketika tombol diklik.
 * @param modifier Modifier untuk kustomisasi dari luar.
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GrayBlue
        )
    ) {
        Text(text)
    }
}

@Preview
@Composable
fun ButtonPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Button(text = "Enter Data", onClick = { /* aksi preview */ })
    }
}