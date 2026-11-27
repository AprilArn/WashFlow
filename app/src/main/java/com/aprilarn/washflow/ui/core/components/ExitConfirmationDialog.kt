package com.aprilarn.washflow.ui.core.components

import android.app.Activity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.AlertRed
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        playConfirmationSound(context)
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                iconBgColor = PastelRoseBg,
                iconTint = PastelRose,
                title = stringResource(R.string.dialog_exit_title),
                subtitle = stringResource(R.string.dialog_exit_message)
            )

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = stringResource(R.string.dialog_exit_confirm),
                onConfirm = { activity?.finish() },
                confirmContainerColor = AlertRed
            )
        }
    }
}
