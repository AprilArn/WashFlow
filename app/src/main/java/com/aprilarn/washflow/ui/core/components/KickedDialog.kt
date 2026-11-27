package com.aprilarn.washflow.ui.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.core.theme.WashFlowTheme

@Composable
fun KickedDialog(
    workspaceName: String,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = {}) { // Tidak bisa di-dismiss dengan klik luar atau back button
        KickedDialogContent(workspaceName = workspaceName, onConfirm = onConfirm)
    }
}

@Composable
fun KickedDialogContent(
    workspaceName: String,
    onConfirm: () -> Unit
) {
    WashFlowDialogSurface {
        WashFlowDialogHeader(
            icon = Icons.Rounded.PersonRemove,
            iconBgColor = PastelRoseBg,
            iconTint = PastelRose,
            title = stringResource(R.string.dialog_kicked_title),
            subtitle = stringResource(R.string.dialog_kicked_message)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PastelRoseBg.copy(alpha = 0.65f)
        ) {
            Text(
                text = "\"$workspaceName\"",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PastelRose,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        WashFlowDialogPrimaryButton(
            text = stringResource(R.string.dialog_kicked_back_button),
            onClick = onConfirm,
            containerColor = GrayBlue,
            leadingIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
fun KickedDialogPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        WashFlowTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                KickedDialogContent(
                    workspaceName = "WashFlow Central",
                    onConfirm = {}
                )
            }
        }
    }
}
