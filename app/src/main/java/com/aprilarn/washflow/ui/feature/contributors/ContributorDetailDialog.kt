package com.aprilarn.washflow.ui.feature.contributors

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.HoldToConfirmButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSecondaryButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.SuccessGreen

@Composable
fun ContributorDetailDialog(
    contributor: ContributorUiModel,
    isCurrentUser: Boolean,
    showKickButton: Boolean,
    onDismiss: () -> Unit,
    onKick: () -> Unit
) {
    val isOwner = contributor.role.equals("owner", ignoreCase = true)
    val roleLabel = when (contributor.role.lowercase()) {
        "owner" -> stringResource(R.string.contributors_role_owner)
        "member" -> stringResource(R.string.contributors_role_member)
        else -> contributor.role.replaceFirstChar { it.uppercase() }
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
                ContributorAvatarWithStatus(
                    name = contributor.name,
                    photoUrl = contributor.photoUrl,
                    isOnline = contributor.isOnline,
                    avatarSize = 84.dp,
                    dotSize = 15.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = contributor.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MainFontBlack
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (isCurrentUser) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PastelBlueBg
                        ) {
                            Text(
                                text = stringResource(R.string.contributors_you_badge),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = contributor.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PastelNeutralText
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Role Badge & Presence Badge berdampingan
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isOwner) PastelAmberBg else PastelBlueSoftBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isOwner) {
                                Icon(
                                    imageVector = Icons.Rounded.WorkspacePremium,
                                    contentDescription = null,
                                    tint = PastelAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOwner) PastelAmber else PastelBlue
                                )
                            )
                        }
                    }

                    Surface(
                        color = if (contributor.isOnline) PastelGreenSoftBg else PastelNeutralBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (contributor.isOnline) SuccessGreen
                                        else PastelNeutralText.copy(alpha = 0.6f)
                                    )
                            )
                            Text(
                                text = if (contributor.isOnline) {
                                    formatOnlineStatusLabel(contributor.activeDeviceCount)
                                } else {
                                    formatLastActiveLabel(contributor.lastActiveAtMillis)
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (contributor.isOnline) PastelGreenDark else PastelNeutralText
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
                HorizontalDivider(color = PastelNeutralBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                if (showKickButton) {
                    HoldToConfirmButton(
                        defaultText = stringResource(R.string.contributors_hold_kick_default),
                        progressText = stringResource(R.string.contributors_hold_kick_progress),
                        durationMillis = 4000,
                        onConfirm = onKick
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

            WashFlowDialogSecondaryButton(
                text = stringResource(R.string.common_close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
