package com.aprilarn.washflow.ui.core.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg

@Composable
fun LeaveWorkspaceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.AutoMirrored.Rounded.Logout,
                iconBgColor = PastelRoseBg,
                iconTint = PastelRose,
                title = stringResource(R.string.dialog_leave_workspace_title),
                subtitle = stringResource(R.string.dialog_leave_workspace_message)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Tombol Hold-to-Leave (4 Detik)
            HoldToConfirmButton(
                defaultText = stringResource(R.string.dialog_leave_workspace_hold_default),
                progressText = stringResource(R.string.dialog_leave_workspace_hold_progress),
                durationMillis = 4000,
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
