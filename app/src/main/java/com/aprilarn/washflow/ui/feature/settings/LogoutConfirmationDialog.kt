package com.aprilarn.washflow.ui.feature.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.HoldToConfirmButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSecondaryButton
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.core.components.playConfirmationSound
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg

@Composable
fun LogoutConfirmationDialog(
    onLogoutConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        playConfirmationSound(context)
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = Icons.AutoMirrored.Rounded.Logout,
                iconBgColor = PastelRoseBg,
                iconTint = PastelRose,
                title = stringResource(R.string.settings_logout_title),
                subtitle = stringResource(R.string.settings_logout_message)
            )

            Spacer(modifier = Modifier.height(22.dp))

            HoldToConfirmButton(
                defaultText = stringResource(R.string.settings_logout_hold_default),
                progressText = stringResource(R.string.settings_logout_hold_progress),
                durationMillis = 1500,
                onConfirm = onLogoutConfirmed
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
