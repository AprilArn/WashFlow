package com.aprilarn.washflow.ui.core.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TimePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.AlertRed
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg

internal fun playConfirmationSound(context: Context) {
    val prefs = context.getSharedPreferences("WashFlowPrefs", Context.MODE_PRIVATE)
    val isSoundEnabled = prefs.getBoolean("SOUND_ENABLED", true)
    if (!isSoundEnabled) return

    try {
        val mediaPlayer = MediaPlayer.create(context, R.raw.confirmation)
        mediaPlayer?.let { player ->
            player.setOnCompletionListener { it.release() }
            player.start()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Kontainer dialog standar WashFlow:
 * putih bersih, radius 24.dp, elevasi 8.dp, lebar maksimal 400.dp, padding 24.dp.
 */
@Composable
fun WashFlowDialogSurface(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/**
 * Header dialog terpusat dengan badge ikon 52.dp (radius 16.dp), judul GrayBlue,
 * dan subjudul opsional.
 */
@Composable
fun WashFlowDialogHeader(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(iconBgColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = GrayBlue
            ),
            textAlign = TextAlign.Center
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PastelNeutralText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tombol sekunder standar dialog (Batal / Tutup) dengan background pastel netral.
 */
@Composable
fun WashFlowDialogSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PastelNeutralBg,
    contentColor: Color = GrayBlue
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Tombol utama standar dialog (Terapkan / Konfirmasi / Simpan / Hapus).
 */
@Composable
fun WashFlowDialogPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = GrayBlue,
    contentColor: Color = Color.White,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor = contentColor.copy(alpha = 0.85f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Baris 2 tombol aksi dialog simetris: sekunder di kiri, utama di kanan.
 */
@Composable
fun WashFlowDialogActionRow(
    dismissText: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    dismissContainerColor: Color = PastelNeutralBg,
    dismissContentColor: Color = GrayBlue,
    confirmContainerColor: Color = GrayBlue,
    confirmContentColor: Color = Color.White
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WashFlowDialogSecondaryButton(
            text = dismissText,
            onClick = onDismiss,
            containerColor = dismissContainerColor,
            contentColor = dismissContentColor,
            modifier = Modifier.weight(1f)
        )
        WashFlowDialogPrimaryButton(
            text = confirmText,
            onClick = onConfirm,
            enabled = confirmEnabled,
            containerColor = confirmContainerColor,
            contentColor = confirmContentColor,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * DatePickerDialog standar WashFlow bertema putih + aksen GrayBlue/PastelBlue.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WashFlowDatePickerDialog(
    datePickerState: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        colors = DatePickerDefaults.colors(containerColor = Color.White),
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrayBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.common_ok),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = PastelNeutralText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = PastelNeutralText,
                headlineContentColor = GrayBlue,
                weekdayContentColor = PastelNeutralText,
                subheadContentColor = GrayBlue,
                navigationContentColor = GrayBlue,
                yearContentColor = MainFontBlack,
                currentYearContentColor = PastelBlue,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = GrayBlue,
                dayContentColor = MainFontBlack,
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = GrayBlue,
                todayContentColor = PastelBlue,
                todayDateBorderColor = PastelBlue
            )
        )
    }
}

/**
 * TimePickerDialog standar WashFlow bertema putih + aksen GrayBlue/PastelBlue.
 * Selalu menggunakan TimePickerLayoutType.Vertical agar clock dial tetap bulat sempurna
 * baik di layar HP maupun tablet landscape, serta menyediakan toggle ke input keyboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WashFlowTimePickerDialog(
    title: String,
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        WashFlowTimePickerDialogContent(
            title = title,
            timePickerState = timePickerState,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WashFlowTimePickerDialogContent(
    title: String,
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var useKeyboardInput by remember { mutableStateOf(false) }

    val timePickerColors = TimePickerDefaults.colors(
        clockDialColor = PastelBlueSoftBg,
        clockDialSelectedContentColor = Color.White,
        clockDialUnselectedContentColor = MainFontBlack,
        selectorColor = GrayBlue,
        containerColor = Color.White,
        periodSelectorBorderColor = PastelBlueIconBg,
        periodSelectorSelectedContainerColor = PastelBlueIconBg,
        periodSelectorSelectedContentColor = GrayBlue,
        periodSelectorUnselectedContainerColor = PastelNeutralBg,
        periodSelectorUnselectedContentColor = PastelNeutralText,
        timeSelectorSelectedContainerColor = PastelBlueIconBg,
        timeSelectorSelectedContentColor = GrayBlue,
        timeSelectorUnselectedContainerColor = PastelNeutralBg,
        timeSelectorUnselectedContentColor = PastelNeutralText
    )

    WashFlowDialogSurface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .widthIn(max = 360.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PastelBlueIconBg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GrayBlue
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelNeutralBg)
                        .clickable(
                            role = Role.Button,
                            onClick = { useKeyboardInput = !useKeyboardInput }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (useKeyboardInput) Icons.Rounded.Schedule else Icons.Rounded.Keyboard,
                        contentDescription = null,
                        tint = GrayBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (useKeyboardInput) {
                TimeInput(
                    state = timePickerState,
                    colors = timePickerColors
                )
            } else {
                TimePicker(
                    state = timePickerState,
                    colors = timePickerColors,
                    layoutType = TimePickerLayoutType.Vertical
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = stringResource(R.string.common_ok),
                onConfirm = onConfirm
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, backgroundColor = 0xFFEFF4FB)
@Composable
private fun WashFlowDialogStyleShowcasePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                iconBgColor = PastelRoseBg,
                iconTint = PastelRose,
                title = "Exit Application",
                subtitle = "Are you sure you want to close WashFlow?"
            )
            Spacer(modifier = Modifier.height(22.dp))
            WashFlowDialogActionRow(
                dismissText = "Cancel",
                onDismiss = {},
                confirmText = "Exit",
                onConfirm = {},
                confirmContainerColor = AlertRed
            )
        }

        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.Rounded.Language,
                iconBgColor = PastelBlueIconBg,
                iconTint = PastelBlue,
                title = "Choose Language",
                subtitle = "Select your preferred display language."
            )
            Spacer(modifier = Modifier.height(22.dp))
            WashFlowDialogSecondaryButton(
                text = "Cancel",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 960, heightDp = 640, backgroundColor = 0xFFEFF4FB)
@Composable
private fun WashFlowTimePickerTabletLandscapePreview() {
    val state = rememberTimePickerState(initialHour = 14, initialMinute = 51, is24Hour = true)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        WashFlowTimePickerDialogContent(
            title = "Set Due Time",
            timePickerState = state,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640, backgroundColor = 0xFFEFF4FB)
@Composable
private fun WashFlowTimePickerPhonePreview() {
    val state = rememberTimePickerState(initialHour = 14, initialMinute = 51, is24Hour = true)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        WashFlowTimePickerDialogContent(
            title = "Set Due Time",
            timePickerState = state,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
