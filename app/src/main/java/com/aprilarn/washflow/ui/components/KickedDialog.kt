package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainBLue
import com.aprilarn.washflow.ui.theme.WashFlowTheme

@Composable
fun KickedDialog(
    workspaceName: String,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = {}) { // Tidak bisa di-dismiss dengan klik luar atau back button
        KickedDialogContent(workspaceName = workspaceName, onConfirm = onConfirm)
    }
}

@Composable
fun KickedDialogContent(
    workspaceName: String,
    onConfirm: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Judul
            Text(
                text = "Akses Dicabut",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F) // Merah
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pesan
            Text(
                text = "Anda telah dikeluarkan dari workspace",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"$workspaceName\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            )


            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Konfirmasi
            OutlinedButton(
                onClick = onConfirm,
                border = BorderStroke(1.dp, GrayBlue)
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Create Icon",
                        tint = GrayBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp) )
                    Text("Back to workspace screen", color = GrayBlue)
                }
            }
//            Button(
//                onClick = onConfirm,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = GrayBlue),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Text("Kembali ke halaman Workspace", color = Color.White)
//            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
fun KickedDialogPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        WashFlowTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                KickedDialogContent(
                    workspaceName = "WashFlow Central",
                    onConfirm = {}
                )
            }
        }
    }
}
