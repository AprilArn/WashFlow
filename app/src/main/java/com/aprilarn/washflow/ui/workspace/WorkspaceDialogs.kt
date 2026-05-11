package com.aprilarn.washflow.ui.workspace

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aprilarn.washflow.data.model.Invites
import com.aprilarn.washflow.ui.theme.MainFontBlack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun WorkspaceOptionsDropdown(
    expanded: Boolean,
    isOwner: Boolean,
    popupOffset: IntOffset, // <--- Terima Parameter Baru
    onDismiss: () -> Unit,
    onRenameClicked: () -> Unit,
    onContributorsClicked: () -> Unit,
    onOperationalHoursClicked: () -> Unit, // <--- Parameter Baru
    onAddContributorClicked: () -> Unit,
    onLeaveWorkspaceClicked: () -> Unit,
    onDeleteWorkspaceClicked: () -> Unit
) {
    if (expanded) {
        Popup(
            alignment = Alignment.TopEnd, // Rata Kanan mengikuti ujung teks
            offset = popupOffset,         // Posisi persis di bawah teks
            properties = PopupProperties(focusable = true),
            onDismissRequest = onDismiss
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                ) {
                    if (isOwner) {
                        WorkspaceDropdownItem(
                            text = "Rename workspace",
                            onClick = {
                                onRenameClicked()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                        WorkspaceDropdownItem(
                            text = "Jam Operasional",
                            onClick = {
                                onOperationalHoursClicked()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }

                    WorkspaceDropdownItem(
                        text = "Contributors",
                        onClick = {
                            onContributorsClicked()
                            onDismiss()
                        }
                    )

                    if (!isOwner) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        WorkspaceDropdownItem(
                            text = "Leave Workspace",
                            onClick = {
                                onLeaveWorkspaceClicked()
                                onDismiss()
                            }
                        )
                    }

                    if (isOwner) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        WorkspaceDropdownItem(
                            text = "Delete Workspace",
                            color = MaterialTheme.colorScheme.error,
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

// Fungsi Bantuan untuk Item Menu (Agar rapi dan tidak mengulang kode)
@Composable
fun WorkspaceDropdownItem(
    text: String,
    color: Color = MainFontBlack,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

@Composable
fun RenameWorkspaceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)),
        onDismissRequest = onDismiss,
        title = { Text("Ubah Nama Workspace") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nama workspace baru") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onApply(newName) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ActiveInviteDialog(
    invite: Invites,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val formattedExpiry = remember(invite.expiresAt) {
        invite.expiresAt?.toDate()?.let {
            SimpleDateFormat("EEE, dd MMM yyyy 'at' HH:mm", Locale.getDefault()).format(it)
        } ?: "No expiry"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Active Invitation Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Code Display and Copy Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SelectionContainer {
                        Text(
                            text = invite.inviteId ?: "------",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(invite.inviteId ?: ""))
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                }
                // Info Section
                Column {
                    Text("Code is valid for ${invite.maxContributors} users.", style = MaterialTheme.typography.bodyMedium)
                    Text("Expires on: $formattedExpiry", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Dialog to create a new invitation code (Image 1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInviteDialog(
    onDismiss: () -> Unit,
    onGenerate: (Int, Date) -> Unit
) {
    var maxContributors by remember { mutableStateOf("1") }
    val calendar = Calendar.getInstance()
    // Default expiry: 1 day from now
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    var expiryDate by remember { mutableStateOf(calendar.time) }

    // --- Date & Time Picker States ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiryDate.time)
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // --- Dialogs ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = it
                        val currentCal = Calendar.getInstance()
                        currentCal.time = expiryDate
                        currentCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
                        expiryDate = currentCal.time
                    }
                    showTimePicker = true // Show time picker after date is selected
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog( // Wrap time picker in a dialog for better control
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val currentCal = Calendar.getInstance()
                    currentCal.time = expiryDate
                    currentCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    currentCal.set(Calendar.MINUTE, timePickerState.minute)
                    expiryDate = currentCal.time
                    showTimePicker = false
                }) { Text("OK") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Invitation Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = maxContributors,
                    onValueChange = { if (it.all { char -> char.isDigit() }) maxContributors = it },
                    label = { Text("Max Contributors") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                val formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
                Text("Expires at:")
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formatter.format(expiryDate))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val maxUsers = maxContributors.toIntOrNull() ?: 1
                onGenerate(maxUsers, expiryDate)
            }) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
        AlertDialog(
            onDismissRequest = { isEditingOpenTime = false },
            title = { Text("Set Jam Buka") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = openTimeState)
                }
            },
            confirmButton = {
                TextButton(onClick = { isEditingOpenTime = false }) { Text("OK") }
            }
        )
    }

    if (isEditingCloseTime) {
        AlertDialog(
            onDismissRequest = { isEditingCloseTime = false },
            title = { Text("Set Jam Tutup") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = closeTimeState)
                }
            },
            confirmButton = {
                TextButton(onClick = { isEditingCloseTime = false }) { Text("OK") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jam Operasional") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gunakan jam operasional", style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.material3.Switch(
                        checked = useOperationalHours,
                        onCheckedChange = { useOperationalHours = it }
                    )
                }

                if (useOperationalHours) {
                    Column {
                        Text("Jam Buka:", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { isEditingOpenTime = true }
                        ) {
                            Text(String.format(Locale.getDefault(), "%02d:%02d", openTimeState.hour, openTimeState.minute))
                        }
                    }

                    Column {
                        Text("Jam Tutup:", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { isEditingCloseTime = true }
                        ) {
                            Text(String.format(Locale.getDefault(), "%02d:%02d", closeTimeState.hour, closeTimeState.minute))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (useOperationalHours) {
                    val newOpen = String.format(Locale.getDefault(), "%02d:%02d", openTimeState.hour, openTimeState.minute)
                    val newClose = String.format(Locale.getDefault(), "%02d:%02d", closeTimeState.hour, closeTimeState.minute)
                    onApply(newOpen, newClose)
                } else {
                    onApply(null, null)
                }
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteWorkspaceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    // Tombol delete hanya aktif jika teks = "delete"
    val isDeleteButtonEnabled = confirmText == "delete"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure want to delete current workspace?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "This action will kick/delete all contributors in this workspace and than delete this workspace.",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Field untuk konfirmasi
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    label = { Text("Type 'delete' to confirm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                // Tombol hanya aktif jika teks diisi dengan benar
                enabled = isDeleteButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}