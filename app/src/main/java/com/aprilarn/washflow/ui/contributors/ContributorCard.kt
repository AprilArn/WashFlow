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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    val roleColor = if (isOwner) Color(0xFFFFCE74) else Color(0xFF77A4FF)

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .clip(borderRadius)
            .background(roleColor)
            .padding(end = 8.dp),
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(borderRadius)
                .background(Color.White)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto Profil
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                modifier = Modifier.padding(end = 24.dp),
                text = role.replaceFirstChar { it.uppercase() }, // Capitalize
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = roleColor
                )
            )
        }
    }
    }


@Preview()
@Composable
fun ContributorCardPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        ContributorCard("Rakun D0ng0", null, "owner")
    }
}