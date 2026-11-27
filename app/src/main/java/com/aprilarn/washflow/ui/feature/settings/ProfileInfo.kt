package com.aprilarn.washflow.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.feature.login.UserData

@Composable
fun ProfileInfoCard(
    userData: UserData,
    modifier: Modifier = Modifier,
    workspaceName: String? = null,
    isOwner: Boolean? = null
) {
    val resolvedName = userData.displayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.settings_profile_no_name)
    val initial = resolvedName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bagian Kiri: Foto Profil atau Fallback Inisial Pastel
            if (!userData.profilePictureUrl.isNullOrBlank()) {
                AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = stringResource(R.string.settings_profile_picture_cd),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PastelBlueIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Bagian Kanan: Nama, Email, dan Pill Workspace/Role
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = resolvedName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MainFontBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = userData.email ?: stringResource(R.string.settings_profile_no_email),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PastelNeutralText,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!workspaceName.isNullOrBlank() || isOwner != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!workspaceName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PastelBlueSoftBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = null,
                                        tint = PastelBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = workspaceName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GrayBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (isOwner != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOwner) PastelAmberBg else PastelGreenBg
                            ) {
                                Text(
                                    text = stringResource(
                                        if (isOwner) R.string.contributors_role_owner
                                        else R.string.contributors_role_member
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isOwner) PastelAmber else PastelGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
