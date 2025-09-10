package com.aprilarn.washflow.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun LoginScreen(
    state: LoginUiState = LoginUiState(),
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Bagian atas: Teks "WashFlow"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 64.dp) // Memberi padding dari atas
        ) {
            Text(
                text = "Wash",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = GrayBlue, // Menggunakan GrayBlue
                    fontStyle = FontStyle.Normal,
                ),
                fontWeight = FontWeight.Medium // Font lebih tebal
            )
            Text(
                text = "Flow",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White, // Menggunakan warna putih
                    fontStyle = FontStyle.Italic
                ),
                fontWeight = FontWeight.Medium // Font lebih tebal
            )
        }

        Spacer(Modifier.weight(1f))

        // Tampilkan loading indicator jika sedang memeriksa workspace
        if (state.isCheckingWorkspace) {
            CircularProgressIndicator(color = Color.White)
        }

        Spacer(Modifier.weight(1f))

        // Bagian bawah: Tombol "Sign in with Google"
        // Anda bisa membuat composable terpisah untuk tombol ini atau menuliskannya langsung
        Button(
            enabled = !state.isCheckingWorkspace,
            onClick = { onGoogleSignInClick() },
            modifier = Modifier
                .width(240.dp) // Lebar tombol sekitar 60% dari lebar layar
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // Warna latar belakang putih
                contentColor = Color.Black // Warna teks hitam
            ),
            shape = RoundedCornerShape(25.dp) // Bentuk tombol bulat
        ) {
            // Placeholder untuk ikon Google. Anda mungkin perlu menambahkan resource drawable untuk ikon ini.
            // Image(
            //     painter = painterResource(id = R.drawable.google_icon), // Ganti dengan resource ikon Google Anda
            //     contentDescription = "Google Icon",
            //     modifier = Modifier.size(24.dp)
            // )
            // Spacer(Modifier.width(8.dp))
            Text(
                text = "Sign in with Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 960, heightDp = 600)
@Composable
fun LoginScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF)) // Warna gradient yang sama
            )
        )
    ) {
        LoginScreen(
            state = LoginUiState(),
            onGoogleSignInClick = {}
        )
    }
}