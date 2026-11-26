package com.aprilarn.washflow.ui.feature.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.core.theme.GrayBlue

@Composable
fun LoginScreen(
    state: LoginUiState = LoginUiState(),
    onGoogleSignInClick: () -> Unit,
    onRetryClick: () -> Unit = {},
    onTimeoutDialogDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    if (state.showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { /* Do nothing to force click OK */ },
            title = { Text("Error") },
            text = { Text("Stuck in loading, try again later") },
            confirmButton = {
                TextButton(onClick = onTimeoutDialogDismiss) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        // Hapus verticalArrangement = Arrangement.Center karena kita akan menggunakan weight
    ) {

        // 1. Spacer atas: mendorong logo agak ke bawah agar tidak mentok atas
        Spacer(modifier = Modifier.weight(1f))

        // Bagian atas: Teks "WashFlow"
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wash",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = GrayBlue,
                    fontStyle = FontStyle.Normal,
                ),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Flow",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontStyle = FontStyle.Italic
                ),
                fontWeight = FontWeight.Medium
            )
        }

        // 2. Spacer tengah: memisahkan logo dan tombol secara otomatis
        // Angka 1.5f berarti jarak di tengah akan sedikit lebih lebar daripada jarak di atas/bawah
        Spacer(modifier = Modifier.weight(1.5f))

        // Gunakan Box untuk menampung Tombol atau Loading Indicator di tempat yang sama.
        if (state.isWaitingForNetwork && !state.isCheckingWorkspace) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.networkErrorMessage
                        ?: "Koneksi internet terganggu. Menunggu jaringan kembali normal...",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp)
                )
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .width(200.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = GrayBlue
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Coba Lagi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isCheckingWorkspace) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Button(
                        onClick = { onGoogleSignInClick() },
                        modifier = Modifier.fillMaxSize(), // Tombol mengisi seluruh Box
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Sign in with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Spacer bawah: mendorong tombol agak ke atas agar tidak mentok bawah
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Preview(showBackground = true, widthDp = 960, heightDp = 600)
@Composable
fun LoginScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        LoginScreen(
            state = LoginUiState(showTimeoutDialog = true), // Preview saat timeout
            onGoogleSignInClick = {}
        )
    }
}