package com.aprilarn.washflow.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.BuildConfig
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogPrimaryButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.theme.AlertRed
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg

@Composable
fun AccountSection(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSupportDialog by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(bottom = 6.dp)) {
            SettingsSectionHeader(title = stringResource(R.string.settings_account))

            // 1. Support & About
            SettingsItem(
                icon = Icons.AutoMirrored.Rounded.HelpOutline,
                iconTint = PastelBlue,
                iconBgColor = PastelBlueIconBg,
                title = stringResource(R.string.settings_support),
                subtitle = "WashFlow v${BuildConfig.VERSION_NAME}",
                onClick = { showSupportDialog = true },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = PastelNeutralText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            HorizontalDivider(
                color = PastelNeutralBg,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 2. Logout
            SettingsItem(
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                title = stringResource(R.string.settings_logout),
                titleColor = AlertRed,
                iconTint = AlertRed,
                iconBgColor = PastelRoseBg,
                onClick = onSignOut,
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AlertRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }

    if (showSupportDialog) {
        Dialog(onDismissRequest = { showSupportDialog = false }) {
            WashFlowDialogSurface {
                WashFlowDialogHeader(
                    icon = Icons.AutoMirrored.Rounded.HelpOutline,
                    iconBgColor = PastelBlueIconBg,
                    iconTint = PastelBlue,
                    title = stringResource(R.string.settings_support_dialog_title),
                    subtitle = stringResource(R.string.settings_support_dialog_message)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = PastelBlueSoftBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_support_version_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.settings_support_tip),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PastelNeutralText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                WashFlowDialogPrimaryButton(
                    text = stringResource(R.string.common_close),
                    onClick = { showSupportDialog = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
