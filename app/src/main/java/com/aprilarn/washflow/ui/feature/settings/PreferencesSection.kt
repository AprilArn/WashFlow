package com.aprilarn.washflow.ui.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.HapticSwitch
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSecondaryButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.utils.AppLanguage
import com.aprilarn.washflow.utils.AppLanguageManager

@Composable
fun PreferencesSection(
    locationName: String,
    onSetLocationClicked: () -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(AppLanguageManager.getLanguage(context)) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_preferences),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = GrayBlue,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // 1. Notifications and sounds (Switch)
                SettingsItem(
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(R.string.settings_notifications_sound),
                    trailingContent = {
                        HapticSwitch(
                            modifier = Modifier.height(24.dp),
                            checked = isSoundEnabled,
                            onCheckedChange = onSoundToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GrayBlue
                            )
                        )
                    }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 2. Language
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.settings_language),
                    onClick = { showLanguageDialog = true },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentLanguage.nativeLabel,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 3. Set location
                SettingsItem(
                    icon = Icons.Rounded.MyLocation,
                    title = stringResource(R.string.settings_set_location),
                    onClick = onSetLocationClicked,
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.widthIn(max = 200.dp)
                        ) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = locationName,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee()
                            )
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 24.dp))

                // 4. Theme
                SettingsItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.settings_theme),
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.settings_theme_light),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )
            }
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