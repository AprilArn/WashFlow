package com.aprilarn.washflow.ui.feature.contributors

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.SuccessGreen

@Composable
fun ContributorCard(
    contributor: ContributorUiModel,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isOwner = contributor.role.equals("owner", ignoreCase = true)
    val cardShape = RoundedCornerShape(18.dp)
    val borderColor = if (contributor.isOnline) {
        PastelGreen.copy(alpha = 0.32f)
    } else {
        PastelNeutralBg
    }
    val containerColor = if (contributor.isOnline) {
        Color.White
    } else {
        Color(0xFFFAFBFD)
    }

    val roleLabel = when (contributor.role.lowercase()) {
        "owner" -> stringResource(R.string.contributors_role_owner)
        "member" -> stringResource(R.string.contributors_role_member)
        else -> contributor.role.replaceFirstChar { it.uppercase() }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (contributor.isOnline) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 11.dp else 13.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar + indikator online/offline di sudut kanan bawah
            ContributorAvatarWithStatus(
                name = contributor.name,
                photoUrl = contributor.photoUrl,
                isOnline = contributor.isOnline,
                avatarSize = if (compact) 44.dp else 48.dp,
                dotSize = if (compact) 11.dp else 12.dp
            )

            Spacer(modifier = Modifier.width(if (compact) 12.dp else 14.dp))

            // Nama, Email, dan Status Kehadiran
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = contributor.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = if (compact) 14.sp else 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (contributor.isOnline) MainFontBlack else GrayBlue
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isCurrentUser) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PastelBlueBg
                        ) {
                            Text(
                                text = stringResource(R.string.contributors_you_badge),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelBlue
                            )
                        }
                    }
                }

                Text(
                    text = contributor.email,
                    fontSize = if (compact) 11.sp else 12.sp,
                    color = PastelNeutralText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Baris status kehadiran live
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (contributor.isOnline) SuccessGreen
                                else PastelNeutralText.copy(alpha = 0.55f)
                            )
                    )
                    Text(
                        text = if (contributor.isOnline) {
                            formatOnlineStatusLabel(contributor.activeDeviceCount)
                        } else {
                            formatLastActiveLabel(contributor.lastActiveAtMillis)
                        },
                        fontSize = 11.sp,
                        fontWeight = if (contributor.isOnline) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (contributor.isOnline) PastelGreenDark else PastelNeutralText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Role Pill Badge (selaras dengan palet pastel WashFlow)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isOwner) PastelAmberBg else PastelBlueSoftBg
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = if (compact) 9.dp else 11.dp,
                        vertical = 5.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isOwner) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = PastelAmber,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = roleLabel,
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOwner) PastelAmber else PastelBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ContributorAvatarWithStatus(
    name: String,
    photoUrl: String?,
    isOnline: Boolean,
    avatarSize: Dp,
    dotSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(avatarSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(if (isOnline) PastelGreenIconBg else PastelBlueSoftBg)
                .border(
                    width = 1.5.dp,
                    color = if (isOnline) PastelGreen.copy(alpha = 0.45f) else PastelNeutralBg,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = stringResource(R.string.settings_profile_picture_cd),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = contributorInitials(name),
                    fontSize = (avatarSize.value * 0.34f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOnline) PastelGreenDark else PastelBlue
                )
            }
        }

        // Indikator dot online / offline dengan cincin putih di kanan bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(dotSize + 4.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            LiveStatusDot(
                isOnline = isOnline,
                dotSize = dotSize
            )
        }
    }
}

@Composable
fun LiveStatusDot(
    isOnline: Boolean,
    dotSize: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    if (isOnline) {
        val infiniteTransition = rememberInfiniteTransition(label = "online_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_scale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_alpha"
        )

        Box(
            modifier = modifier.size(dotSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = pulseAlpha))
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(SuccessGreen)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(PastelNeutralText.copy(alpha = 0.55f))
        )
    }
}

@Composable
fun ContributorSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    iconBgColor: Color,
    accentColor: Color,
    valueColor: Color,
    icon: ImageVector? = null,
    showOnlinePulse: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 12.dp else 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 36.dp else 42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (showOnlinePulse) {
                    LiveStatusDot(
                        isOnline = true,
                        dotSize = if (compact) 11.dp else 13.dp
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(if (compact) 19.dp else 22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontSize = if (compact) 20.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = if (compact) 10.sp else 11.sp,
                    color = PastelNeutralText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ContributorSectionHeader(
    isOnlineSection: Boolean,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isOnlineSection) SuccessGreen
                    else PastelNeutralText.copy(alpha = 0.55f)
                )
        )
        Text(
            text = if (isOnlineSection) {
                stringResource(R.string.contributors_section_online, count)
            } else {
                stringResource(R.string.contributors_section_offline, count)
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOnlineSection) PastelGreenDark else PastelNeutralText
        )
    }
}

@Composable
fun formatOnlineStatusLabel(activeDeviceCount: Int): String {
    return when {
        activeDeviceCount <= 0 -> stringResource(R.string.contributors_status_online)
        activeDeviceCount == 1 -> stringResource(R.string.contributors_status_online_device_single, 1)
        else -> stringResource(R.string.contributors_status_online_device_plural, activeDeviceCount)
    }
}

@Composable
fun formatOnlineSummarySubtitle(totalOnlineDevices: Int): String {
    return when {
        totalOnlineDevices <= 0 -> stringResource(R.string.contributors_online_none_sub)
        totalOnlineDevices == 1 -> stringResource(R.string.contributors_online_device_single, 1)
        else -> stringResource(R.string.contributors_online_device_plural, totalOnlineDevices)
    }
}

@Composable
fun formatLastActiveLabel(lastActiveAtMillis: Long?): String {
    if (lastActiveAtMillis == null) {
        return stringResource(R.string.contributors_status_offline)
    }
    val diffMs = (System.currentTimeMillis() - lastActiveAtMillis).coerceAtLeast(0L)
    val minutes = diffMs / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    return when {
        minutes < 1L -> stringResource(R.string.contributors_last_active_just_now)
        minutes < 60L -> stringResource(R.string.contributors_last_active_minutes, minutes.toInt())
        hours < 24L -> stringResource(R.string.contributors_last_active_hours, hours.toInt())
        else -> stringResource(R.string.contributors_last_active_days, days.toInt())
    }
}

fun contributorInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}

@Preview(showBackground = true)
@Composable
fun ContributorCardPreview() {
    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ContributorCard(
                contributor = ContributorUiModel(
                    uid = "1",
                    name = "April Arn",
                    email = "april@washflow.app",
                    photoUrl = null,
                    role = "owner",
                    isOnline = true,
                    activeDeviceCount = 2,
                    lastActiveAtMillis = System.currentTimeMillis()
                ),
                isCurrentUser = true,
                onClick = {}
            )
            ContributorCard(
                contributor = ContributorUiModel(
                    uid = "2",
                    name = "Jane Smith",
                    email = "jane@washflow.app",
                    photoUrl = null,
                    role = "member",
                    isOnline = false,
                    lastActiveAtMillis = System.currentTimeMillis() - 15 * 60_000L
                ),
                isCurrentUser = false,
                onClick = {}
            )
        }
    }
}
