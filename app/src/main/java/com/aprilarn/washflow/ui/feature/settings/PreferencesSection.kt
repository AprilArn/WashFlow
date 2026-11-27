package com.aprilarn.washflow.ui.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.HapticSwitch
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSecondaryButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelSwitchOffTrack
import com.aprilarn.washflow.utils.AppLanguage
import com.aprilarn.washflow.utils.AppLanguageManager

@Composable
fun PreferencesSection(
    locationName: String,
    isAutoLocationMode: Boolean,
    onSetLocationClicked: () -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(AppLanguageManager.getLanguage(context)) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(bottom = 6.dp)) {
            SettingsSectionHeader(title = stringResource(R.string.settings_preferences))

            // 1. Notifications and sounds (Switch)
            SettingsItem(
                icon = Icons.Rounded.NotificationsActive,
                iconTint = PastelBlue,
                iconBgColor = PastelBlueIconBg,
                title = stringResource(R.string.settings_notifications_sound),
                trailingContent = {
                    HapticSwitch(
                        modifier = Modifier.height(28.dp),
                        checked = isSoundEnabled,
                        onCheckedChange = onSoundToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GrayBlue,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = PastelSwitchOffTrack,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            )

            HorizontalDivider(
                color = PastelNeutralBg,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 2. Language
            SettingsItem(
                icon = Icons.Rounded.Language,
                iconTint = PastelBlue,
                iconBgColor = PastelBlueIconBg,
                title = stringResource(R.string.settings_language),
                onClick = { showLanguageDialog = true },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentLanguage.nativeLabel,
                            color = PastelNeutralText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = PastelNeutralText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            HorizontalDivider(
                color = PastelNeutralBg,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 3. Set location (dengan alamat di subtitle & badge mode Auto/Manual)
            SettingsItem(
                icon = Icons.Rounded.MyLocation,
                iconTint = PastelGreen,
                iconBgColor = PastelGreenIconBg,
                title = stringResource(R.string.settings_set_location),
                subtitle = locationName,
                subtitleMarquee = true,
                onClick = onSetLocationClicked,
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAutoLocationMode) PastelGreenBg else PastelBlueSoftBg
                        ) {
                            Text(
                                text = stringResource(
                                    if (isAutoLocationMode) R.string.settings_location_mode_auto
                                    else R.string.settings_location_mode_manual
                                ),
                                color = if (isAutoLocationMode) PastelGreen else PastelBlue,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = PastelNeutralText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            HorizontalDivider(
                color = PastelNeutralBg,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 4. Theme (badge non-interaktif, tanpa panah palsu)
            SettingsItem(
                icon = Icons.Rounded.Palette,
                iconTint = PastelAmber,
                iconBgColor = PastelAmberBg,
                title = stringResource(R.string.settings_theme),
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PastelNeutralBg
                    ) {
                        Text(
                            text = stringResource(R.string.settings_theme_light),
                            color = PastelNeutralText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            )
        }
    }

    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            WashFlowDialogSurface {
                WashFlowDialogHeader(
                    icon = Icons.Rounded.Language,
                    iconBgColor = PastelBlueIconBg,
                    iconTint = PastelBlue,
                    title = stringResource(R.string.settings_language_dialog_title)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { language ->
                        val isSelected = language == currentLanguage
                        Surface(
                            onClick = {
                                showLanguageDialog = false
                                if (language != currentLanguage) {
                                    currentLanguage = language
                                    AppLanguageManager.setLanguage(context, language)
                                    context.findActivity()?.recreate()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PastelBlueSoftBg else PastelNeutralBg.copy(alpha = 0.65f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) PastelBlue.copy(alpha = 0.5f) else PastelBlueBg.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = language.nativeLabel,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (isSelected) GrayBlue else MainFontBlack,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                                Icon(
                                    imageVector = if (isSelected) {
                                        Icons.Rounded.CheckCircle
                                    } else {
                                        Icons.Rounded.RadioButtonUnchecked
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) PastelBlue else PastelNeutralText.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                WashFlowDialogSecondaryButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = { showLanguageDialog = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
