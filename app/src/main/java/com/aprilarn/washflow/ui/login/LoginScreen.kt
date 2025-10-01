// com/aprilarn/washflow/ui/login/LoginScreen.kt

package com.aprilarn.washflow.ui.login

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
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun LoginScreen(
    state: LoginUiState = LoginUiState(),
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
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
            modifier = Modifier.padding(top = 64.dp)
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

        Spacer(Modifier.weight(1f)) // Spacer ini akan mendorong konten di bawahnya ke dasar

        // --- AREA PERBAIKAN ---
        // Gunakan Box untuk menampung Tombol atau Loading Indicator di tempat yang sama.
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
            state = LoginUiState(isCheckingWorkspace = true), // Preview saat loading
            onGoogleSignInClick = {}
        )
    }
}