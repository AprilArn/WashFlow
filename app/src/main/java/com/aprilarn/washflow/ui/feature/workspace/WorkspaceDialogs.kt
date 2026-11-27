package com.aprilarn.washflow.ui.feature.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.ui.core.components.HapticSwitch
import com.aprilarn.washflow.ui.core.components.WashFlowDatePickerDialog
import com.aprilarn.washflow.ui.core.components.WashFlowDialogActionRow
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.components.WashFlowTimePickerDialog
import com.aprilarn.washflow.ui.core.theme.AlertRed
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.core.theme.PastelSwitchOffTrack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun WorkspaceOptionsDropdown(
    expanded: Boolean,
    isOwner: Boolean,
    popupOffset: IntOffset,
    onDismiss: () -> Unit,
    onRenameClicked: () -> Unit,
    onContributorsClicked: () -> Unit,
    onOperationalHoursClicked: () -> Unit,
    onAddContributorClicked: () -> Unit,
    onLeaveWorkspaceClicked: () -> Unit,
    onDeleteWorkspaceClicked: () -> Unit
) {
    if (expanded) {
        Popup(
            alignment = Alignment.TopEnd,
            offset = popupOffset,
            properties = PopupProperties(focusable = true),
            onDismissRequest = onDismiss
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 8.dp,
                color = Color.White,
                border = BorderStroke(1.dp, PastelNeutralBg)
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .padding(vertical = 6.dp)
                ) {
                    if (isOwner) {
                        WorkspaceDropdownItem(
                            text = stringResource(R.string.workspace_menu_rename),
                            icon = Icons.Rounded.Edit,
                            onClick = {
                                onRenameClicked()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = PastelNeutralBg, modifier = Modifier.padding(horizontal = 12.dp))

                        WorkspaceDropdownItem(
                            text = stringResource(R.string.workspace_menu_operational_hours),
                            icon = Icons.Rounded.Schedule,
                            onClick = {
                                onOperationalHoursClicked()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = PastelNeutralBg, modifier = Modifier.padding(horizontal = 12.dp))
                    }

                    WorkspaceDropdownItem(
                        text = stringResource(R.string.workspace_menu_contributors),
                        icon = Icons.Rounded.Groups,
                        onClick = {
                            onContributorsClicked()
                            onDismiss()
                        }
                    )

                    if (!isOwner) {
                        HorizontalDivider(color = PastelNeutralBg, modifier = Modifier.padding(horizontal = 12.dp))
                        WorkspaceDropdownItem(
                            text = stringResource(R.string.workspace_menu_leave),
                            icon = Icons.AutoMirrored.Rounded.Logout,
                            color = PastelRose,
                            iconTint = PastelRose,
                            onClick = {
                                onLeaveWorkspaceClicked()
                                onDismiss()
                            }
                        )
                    }

                    if (isOwner) {
                        HorizontalDivider(color = PastelNeutralBg, modifier = Modifier.padding(horizontal = 12.dp))
                        WorkspaceDropdownItem(
                            text = stringResource(R.string.workspace_menu_delete),
                            icon = Icons.Rounded.DeleteOutline,
                            color = AlertRed,
                            iconTint = AlertRed,
                            onClick = {
                                onDeleteWorkspaceClicked()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceDropdownItem(
    text: String,
    color: Color = MainFontBlack,
    icon: ImageVector? = null,
    iconTint: Color = PastelBlue,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color
        )
    }
}

@Composable
private fun workspaceDialogTextFieldColors(
    focusedBorderColor: Color = PastelBlue,
    focusedLabelColor: Color = PastelBlue
) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = PastelNeutralBg.copy(alpha = 0.55f),
    unfocusedContainerColor = PastelNeutralBg.copy(alpha = 0.55f),
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = MainFontBlack,
    unfocusedTextColor = MainFontBlack,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = PastelNeutralText,
    cursorColor = GrayBlue
)

@Composable
fun RenameWorkspaceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface(horizontalAlignment = Alignment.Start) {
            WashFlowDialogHeader(
                icon = Icons.Rounded.Edit,
                iconBgColor = PastelBlueIconBg,
                iconTint = PastelBlue,
                title = stringResource(R.string.workspace_rename_title)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.workspace_rename_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = workspaceDialogTextFieldColors()
            )

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = stringResource(R.string.common_apply),
                onConfirm = { onApply(newName.trim()) },
                confirmEnabled = newName.isNotBlank()
            )
        }
    }
}

@Composable
fun ActiveInviteDialog(
    invite: Invites,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val noExpiryText = stringResource(R.string.workspace_invite_no_expiry)
    val copiedToastText = stringResource(R.string.workspace_invite_copied)
    val formattedExpiry = remember(invite.expiresAt, noExpiryText) {
        invite.expiresAt?.toDate()?.let {
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: noExpiryText
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface(horizontalAlignment = Alignment.Start) {
            WashFlowDialogHeader(
                icon = Icons.Rounded.Key,
                iconBgColor = PastelBlueIconBg,
                iconTint = PastelBlue,
                title = stringResource(R.string.workspace_invite_active_title)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Code Display and Copy Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = PastelBlueSoftBg,
                border = BorderStroke(1.dp, PastelBlueBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SelectionContainer {
                        Text(
                            text = invite.inviteId ?: "------",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(invite.inviteId ?: ""))
                            Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelBlueIconBg)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.workspace_invite_copy_cd),
                            tint = PastelBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Info Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelNeutralBg
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.workspace_invite_valid_for, invite.maxContributors ?: 0),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MainFontBlack,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = stringResource(R.string.workspace_invite_expires_on, formattedExpiry),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_delete),
                onDismiss = {
                    onDelete()
                    onDismiss()
                },
                confirmText = stringResource(R.string.common_close),
                onConfirm = onDismiss,
                dismissContainerColor = PastelRoseBg,
                dismissContentColor = PastelRose
            )
        }
    }
}

/**
 * Dialog to create a new invitation code.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInviteDialog(
    onDismiss: () -> Unit,
    onGenerate: (Int, Date) -> Unit
) {
    var maxContributors by remember { mutableStateOf("1") }
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    var expiryDate by remember { mutableStateOf(calendar.time) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiryDate.time)
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    if (showDatePicker) {
        WashFlowDatePickerDialog(
            datePickerState = datePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.timeInMillis = it
                    val currentCal = Calendar.getInstance()
                    currentCal.time = expiryDate
                    currentCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
                    expiryDate = currentCal.time
                }
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        WashFlowTimePickerDialog(
            title = stringResource(R.string.workspace_select_time),
            timePickerState = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val currentCal = Calendar.getInstance()
                currentCal.time = expiryDate
                currentCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                currentCal.set(Calendar.MINUTE, timePickerState.minute)
                expiryDate = currentCal.time
                showTimePicker = false
            }
        )
    }

    val formatter = remember { SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface(horizontalAlignment = Alignment.Start) {
            WashFlowDialogHeader(
                icon = Icons.Rounded.PersonAdd,
                iconBgColor = PastelGreenIconBg,
                iconTint = PastelGreen,
                title = stringResource(R.string.workspace_invite_create_title)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.workspace_invite_max_contributors),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PastelNeutralText
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentMax = maxContributors.toIntOrNull() ?: 1
            val canDecrease = currentMax > 1
            val stepperBtnShape = RoundedCornerShape(14.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = {
                        val next = (currentMax - 1).coerceAtLeast(1)
                        maxContributors = next.toString()
                    },
                    enabled = canDecrease,
                    modifier = Modifier.size(48.dp),
                    shape = stepperBtnShape,
                    color = if (canDecrease) PastelBlueSoftBg else PastelNeutralBg,
                    border = BorderStroke(
                        1.dp,
                        if (canDecrease) PastelBlueBg else Color.Transparent
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = stringResource(R.string.orders_qty_decrease_cd),
                            tint = if (canDecrease) PastelBlue else PastelNeutralText.copy(alpha = 0.5f)
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = maxContributors,
                    onValueChange = { if (it.all { char -> char.isDigit() }) maxContributors = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GrayBlue,
                        unfocusedTextColor = GrayBlue,
                        focusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.45f),
                        unfocusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.25f),
                        focusedBorderColor = PastelBlue,
                        unfocusedBorderColor = PastelBlueBg,
                        cursorColor = PastelBlue
                    )
                )

                Surface(
                    onClick = {
                        val next = (currentMax + 1).coerceAtLeast(1)
                        maxContributors = next.toString()
                    },
                    modifier = Modifier.size(48.dp),
                    shape = stepperBtnShape,
                    color = PastelBlueSoftBg,
                    border = BorderStroke(1.dp, PastelBlueBg)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.orders_qty_increase_cd),
                            tint = PastelBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.workspace_invite_expires_at),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PastelNeutralText
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg,
                border = BorderStroke(1.dp, PastelBlueBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = PastelBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formatter.format(expiryDate),
                        color = GrayBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = stringResource(R.string.workspace_invite_generate),
                onConfirm = {
                    val maxUsers = (maxContributors.toIntOrNull() ?: 1).coerceAtLeast(1)
                    onGenerate(maxUsers, expiryDate)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationalHoursDialog(
    openTime: String?,
    closeTime: String?,
    onDismiss: () -> Unit,
    onApply: (String?, String?) -> Unit
) {
    var useOperationalHours by remember { mutableStateOf(openTime != null && closeTime != null) }
    var isEditingOpenTime by remember { mutableStateOf(false) }
    var isEditingCloseTime by remember { mutableStateOf(false) }

    val openParts = (openTime ?: "09:00").split(":").map { it.toIntOrNull() ?: 0 }
    val closeParts = (closeTime ?: "17:00").split(":").map { it.toIntOrNull() ?: 0 }

    val openTimeState = rememberTimePickerState(
        initialHour = if (openParts.size >= 2) openParts[0] else 9,
        initialMinute = if (openParts.size >= 2) openParts[1] else 0,
        is24Hour = true
    )

    val closeTimeState = rememberTimePickerState(
        initialHour = if (closeParts.size >= 2) closeParts[0] else 17,
        initialMinute = if (closeParts.size >= 2) closeParts[1] else 0,
        is24Hour = true
    )

    if (isEditingOpenTime) {
        WashFlowTimePickerDialog(
            title = stringResource(R.string.workspace_hours_set_open),
            timePickerState = openTimeState,
            onDismiss = { isEditingOpenTime = false },
            onConfirm = { isEditingOpenTime = false }
        )
    }

    if (isEditingCloseTime) {
        WashFlowTimePickerDialog(
            title = stringResource(R.string.workspace_hours_set_close),
            timePickerState = closeTimeState,
            onDismiss = { isEditingCloseTime = false },
            onConfirm = { isEditingCloseTime = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface(horizontalAlignment = Alignment.Start) {
            WashFlowDialogHeader(
                icon = Icons.Rounded.Schedule,
                iconBgColor = PastelBlueIconBg,
                iconTint = PastelBlue,
                title = stringResource(R.string.workspace_hours_title)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PastelNeutralBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.workspace_hours_toggle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MainFontBlack,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    HapticSwitch(
                        checked = useOperationalHours,
                        onCheckedChange = { useOperationalHours = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PastelBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = PastelSwitchOffTrack,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (useOperationalHours) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkspaceTimeTile(
                        label = stringResource(R.string.workspace_hours_open_label),
                        timeText = String.format(Locale.getDefault(), "%02d:%02d", openTimeState.hour, openTimeState.minute),
                        onClick = { isEditingOpenTime = true },
                        modifier = Modifier.weight(1f)
                    )
                    WorkspaceTimeTile(
                        label = stringResource(R.string.workspace_hours_close_label),
                        timeText = String.format(Locale.getDefault(), "%02d:%02d", closeTimeState.hour, closeTimeState.minute),
                        onClick = { isEditingCloseTime = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = stringResource(R.string.common_apply),
                onConfirm = {
                    if (useOperationalHours) {
                        val newOpen = String.format(Locale.getDefault(), "%02d:%02d", openTimeState.hour, openTimeState.minute)
                        val newClose = String.format(Locale.getDefault(), "%02d:%02d", closeTimeState.hour, closeTimeState.minute)
                        onApply(newOpen, newClose)
                    } else {
                        onApply(null, null)
                    }
                }
            )
        }
    }
}

@Composable
private fun WorkspaceTimeTile(
    label: String,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PastelBlueSoftBg,
        border = BorderStroke(1.dp, PastelBlueBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = PastelNeutralText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GrayBlue
            )
        }
    }
}

@Composable
fun DeleteWorkspaceDialog(
    workspaceName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    val expectedName = workspaceName.trim()
    val isDeleteButtonEnabled = expectedName.isNotEmpty() && confirmText.trim() == expectedName

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.Rounded.DeleteForever,
                iconBgColor = PastelRoseBg,
                iconTint = PastelRose,
                title = stringResource(R.string.workspace_delete_title),
                subtitle = stringResource(R.string.workspace_delete_message)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.workspace_delete_confirm_label, expectedName),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MainFontBlack,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmText,
                onValueChange = { confirmText = it },
                placeholder = {
                    Text(
                        text = expectedName,
                        color = PastelNeutralText
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = workspaceDialogTextFieldColors(
                    focusedBorderColor = PastelRose,
                    focusedLabelColor = PastelRose
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = stringResource(R.string.common_delete),
                onConfirm = onConfirm,
                confirmEnabled = isDeleteButtonEnabled,
                confirmContainerColor = AlertRed
            )
        }
    }
}
