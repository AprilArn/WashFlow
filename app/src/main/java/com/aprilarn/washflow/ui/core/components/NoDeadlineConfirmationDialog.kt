package com.aprilarn.washflow.ui.core.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText

@Composable
fun NoDeadlineConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        playConfirmationSound(context)
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.Rounded.EventBusy,
                iconBgColor = PastelAmberBg,
                iconTint = PastelAmber,
                title = stringResource(R.string.dialog_no_deadline_title)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.dialog_no_deadline_message),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.dialog_no_deadline_confirm_prompt),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelNeutralText
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            HoldToConfirmButton(
                defaultText = stringResource(R.string.dialog_no_deadline_hold_default),
                progressText = stringResource(R.string.dialog_no_deadline_hold_progress),
                durationMillis = 1500,
                containerColor = PastelBlueBg,
                progressColor = GrayBlue,
                defaultTextColor = GrayBlue,
                onConfirm = onConfirm
            )

            Spacer(modifier = Modifier.height(10.dp))

            WashFlowDialogSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
