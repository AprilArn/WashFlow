// com/aprilarn/washflow/ui/contributors/ContributorDetailDialog.kt
package com.aprilarn.washflow.ui.contributors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun ContributorDetailDialog(
    contributor: ContributorUiModel,
    showKickButton: Boolean,
    onDismiss: () -> Unit,
    onKick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                // Foto Profil Besar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (contributor.photoUrl != null) {
                        AsyncImage(
                            model = contributor.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nama
                Text(
                    text = contributor.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    )
                )

                // Email
                Text(
                    text = contributor.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )

                // Role Badge
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (contributor.role == "owner") Color(0xFFFFCE74) else Color(0xFF77A4FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = contributor.role.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (showKickButton) {
                    com.aprilarn.washflow.ui.components.HoldToConfirmButton(
                        defaultText = "Hold to Kick (4s)",
                        progressText = "Kicking User...",
                        durationMillis = 4000,
                        onConfirm = onKick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tombol Cancel
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}
