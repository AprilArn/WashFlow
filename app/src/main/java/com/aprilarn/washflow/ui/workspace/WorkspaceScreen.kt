package com.aprilarn.washflow.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkspaceScreen(
    state: WorkspaceUiState,
    onJoinClick: (String) -> Unit,
    onCreateWorkspaceClick: () -> Unit
) {
    // 1. State untuk menampung teks yang diinput oleh pengguna
    var inviteCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hi, ${state.displayName}",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Join an existing workspace or create a new one.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Light
        )

        Spacer(Modifier.height(24.dp))

        // 3. Gunakan Column di dalam Box agar layout rapi
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                // 2. Hubungkan TextField dengan state
                value = inviteCode,
                onValueChange = { inviteCode = it },
                label = { Text("Workspace invitation code") },
                placeholder = { Text("e.g., XXX-XXX") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                colors = TextFieldDefaults.colors(
                    // Untuk warna teks, label, dan cursor
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                    cursorColor = Color.White,

                    // Untuk warna background/container
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,

                    // Untuk warna border/garis bawah
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                )
            )

            Spacer(Modifier.height(16.dp))

            // 4. Tambahkan tombol untuk aksi "Join"
            Button(
                onClick = { onJoinClick(inviteCode) },
                modifier = Modifier
                    .width(90.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Text("Join")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(text = "or", color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(24.dp))

        // 4. Tambahkan tombol untuk aksi "Create"
        OutlinedButton(
            onClick = onCreateWorkspaceClick,
            border = BorderStroke(1.dp, Color.White)
        ) {
            Text("Create new workspace", color = Color.White)
        }
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 600)
@Composable
fun WorkspaceScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
            )
        )
    ) {
        WorkspaceScreen(
            state = WorkspaceUiState(displayName = "April Arn"),
            onJoinClick = {}, // Aksi dummy untuk preview
            onCreateWorkspaceClick = {} // Aksi dummy untuk preview
        )
    }
}