// com/aprilarn/washflow/ui/contributors/ContributorCard.kt
package com.aprilarn.washflow.ui.contributors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MornYellow

@Composable
fun ContributorCard(
    name: String,
    photoUrl: String?,
    role: String
) {
    val borderRadius = RoundedCornerShape(24.dp)

    val isOwner = role == "owner"
    // Warna teks role: Kuning untuk Owner, Biru untuk Member
    val roleColor = if (isOwner) MornYellow else Color(0xFF8EC5FC)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(borderRadius)
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto Profil
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "No Photo",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Nama
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = GrayBlue
            ),
            modifier = Modifier.weight(1f)
        )

        // Role (Owner/Member)
        Text(
            text = role.replaceFirstChar { it.uppercase() }, // Capitalize
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = roleColor
            )
        )
    }
}