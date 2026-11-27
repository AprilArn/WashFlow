package com.aprilarn.washflow.ui.core.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
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
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
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
                icon = Icons.Rounded.DeleteForever,
                iconBgColor = PastelRoseBg,
                iconTint = PastelRose,
                title = stringResource(R.string.dialog_delete_title)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.dialog_delete_message, itemName),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.dialog_delete_warning),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelNeutralText
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            HoldToConfirmButton(
                defaultText = stringResource(R.string.dialog_delete_hold_default),
                progressText = stringResource(R.string.dialog_delete_hold_progress),
                durationMillis = 1500,
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
