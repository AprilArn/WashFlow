package com.aprilarn.washflow.ui.feature.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.core.theme.MainBlue
import com.aprilarn.washflow.ui.core.theme.MornYellow

/**
 * Tampilan Pemilihan / Pembuatan Workspace khusus HP (Portrait),
 * bergaya 3 kartu putih terpisah yang ringkas selaras PhoneSettingsScreen & PhoneTableDataScreen.
 */
@Composable
fun PhoneWorkspaceScreen(
    state: WorkspaceUiState,
    onJoinClick: (String) -> Unit,
    onCreateWorkspaceClick: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    var inviteCode by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        WashFlowWorkspaceWordmark(fontSize = 26.sp)

        Spacer(modifier = Modifier.height(2.dp))

        // Kartu 1: Profil Pengguna + Baris Aksi Switch Account
        WorkspaceProfileCard(
            displayName = state.displayName,
            email = state.email,
            photoUrl = state.photoUrl,
            isLoading = state.isLoading,
            inlineSwitchAction = false,
            onSwitchAccountClick = onSwitchAccountClick,
            avatarSize = 50.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Kartu 2: Gabung Workspace
        WorkspaceJoinCard(
            inviteCode = inviteCode,
            onInviteCodeChange = { inviteCode = it },
            isLoading = state.isLoading,
            horizontalActionLayout = false,
            onJoinClick = { onJoinClick(inviteCode.trim()) },
            modifier = Modifier.fillMaxWidth()
        )

        // Kartu 3: Buat Workspace Baru (clickable, selaras TableDataCard)
        WorkspaceCreateCard(
            isLoading = state.isLoading,
            onClick = onCreateWorkspaceClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Phone Workspace", device = Devices.PHONE, showBackground = true)
@Composable
private fun PhoneWorkspaceScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(MainBlue, MornYellow)
            )
        )
    ) {
        PhoneWorkspaceScreen(
            state = WorkspaceUiState(
                displayName = "April Arn",
                email = "april@washflow.app"
            ),
            onJoinClick = {},
            onCreateWorkspaceClick = {},
            onSwitchAccountClick = {}
        )
    }
}
