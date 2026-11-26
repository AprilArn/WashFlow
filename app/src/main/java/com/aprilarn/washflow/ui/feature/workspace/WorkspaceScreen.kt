package com.aprilarn.washflow.ui.feature.workspace

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.MornYellow
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText

@Composable
fun WorkspaceScreen(
    state: WorkspaceUiState,
    onJoinClick: (String) -> Unit,
    onCreateWorkspaceClick: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    var inviteCode by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wordmark WashFlow mengambang di atas background langit
            WashFlowWorkspaceWordmark(fontSize = 30.sp)

            Spacer(modifier = Modifier.height(4.dp))

            // Kartu 1: Profil Pengguna + Tombol Switch Account di sisi kanan
            WorkspaceProfileCard(
                displayName = state.displayName,
                email = state.email,
                photoUrl = state.photoUrl,
                isLoading = state.isLoading,
                inlineSwitchAction = true,
                onSwitchAccountClick = onSwitchAccountClick,
                avatarSize = 56.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Kartu 2: Gabung Workspace
            WorkspaceJoinCard(
                inviteCode = inviteCode,
                onInviteCodeChange = { inviteCode = it },
                isLoading = state.isLoading,
                horizontalActionLayout = true,
                onJoinClick = { onJoinClick(inviteCode.trim()) },
                modifier = Modifier.fillMaxWidth()
            )

            // Kartu 3: Buat Workspace Baru (clickable, gaya TableDataCard)
            WorkspaceCreateCard(
                isLoading = state.isLoading,
                onClick = onCreateWorkspaceClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun WashFlowWorkspaceWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Wash",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = fontSize,
                color = GrayBlue,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal
            )
        )
        Text(
            text = "Flow",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = fontSize,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        )
    }
}

@Composable
internal fun WorkspaceProfileCard(
    displayName: String,
    email: String,
    photoUrl: String?,
    isLoading: Boolean,
    inlineSwitchAction: Boolean,
    onSwitchAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 56.dp
) {
    val resolvedName = displayName.ifBlank { "User" }
    val initial = resolvedName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = stringResource(R.string.settings_profile_picture_cd),
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(PastelBlueIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.workspace_greeting, resolvedName),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MainFontBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = email.ifBlank { stringResource(R.string.workspace_setup_subtitle) },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PastelNeutralText,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.width(12.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = GrayBlue
                    )
                } else if (inlineSwitchAction) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        onClick = onSwitchAccountClick,
                        shape = RoundedCornerShape(12.dp),
                        color = PastelBlueSoftBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = null,
                                tint = GrayBlue,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = stringResource(R.string.workspace_switch_account),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GrayBlue
                            )
                        }
                    }
                }
            }

            // Pada HP (lebar sempit), tombol Switch Account diletakkan rapi sebagai baris aksi di bawah profil
            if (!inlineSwitchAction) {
                HorizontalDivider(
                    color = PastelNeutralBg,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoading, onClick = onSwitchAccountClick)
                        .padding(horizontal = 24.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = if (isLoading) PastelNeutralText else GrayBlue,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.workspace_switch_account),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isLoading) PastelNeutralText else GrayBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = PastelNeutralText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun WorkspaceJoinCard(
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    isLoading: Boolean,
    horizontalActionLayout: Boolean,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canJoin = inviteCode.isNotBlank() && !isLoading

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header Editorial bergaya TableDataCard
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .background(PastelBlue, RoundedCornerShape(2.dp))
                )
                Column {
                    Text(
                        text = stringResource(R.string.workspace_join_full_button),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = stringResource(R.string.workspace_join_card_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PastelNeutralText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (horizontalActionLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WorkspaceInviteCodeField(
                        inviteCode = inviteCode,
                        onInviteCodeChange = onInviteCodeChange,
                        isLoading = isLoading,
                        canJoin = canJoin,
                        onJoinClick = onJoinClick,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onJoinClick,
                        enabled = canJoin,
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayBlue,
                            contentColor = Color.White,
                            disabledContainerColor = GrayBlue.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.workspace_join_button),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                WorkspaceInviteCodeField(
                    inviteCode = inviteCode,
                    onInviteCodeChange = onInviteCodeChange,
                    isLoading = isLoading,
                    canJoin = canJoin,
                    onJoinClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onJoinClick,
                    enabled = canJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrayBlue,
                        contentColor = Color.White,
                        disabledContainerColor = GrayBlue.copy(alpha = 0.35f),
                        disabledContentColor = Color.White.copy(alpha = 0.85f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.workspace_join_full_button),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceInviteCodeField(
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    isLoading: Boolean,
    canJoin: Boolean,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = inviteCode,
        onValueChange = onInviteCodeChange,
        enabled = !isLoading,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.workspace_invite_code_label) +
                    " (" + stringResource(R.string.workspace_invite_code_placeholder) + ")",
                color = PastelNeutralText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Key,
                contentDescription = null,
                tint = PastelBlue,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = {
            if (inviteCode.isNotEmpty() && !isLoading) {
                IconButton(onClick = { onInviteCodeChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.common_clear_search),
                        tint = PastelNeutralText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { if (canJoin) onJoinClick() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PastelNeutralBg.copy(alpha = 0.65f),
            unfocusedContainerColor = PastelNeutralBg.copy(alpha = 0.65f),
            disabledContainerColor = PastelNeutralBg.copy(alpha = 0.45f),
            focusedBorderColor = PastelBlue,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedTextColor = MainFontBlack,
            unfocusedTextColor = MainFontBlack,
            cursorColor = GrayBlue
        )
    )
}

@Composable
internal fun WorkspaceCreateCard(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.White,
        shadowElevation = 6.dp,
        enabled = !isLoading,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            // Ilustrasi pastel hijau lembut bergaya TableDataCard
            WorkspaceCreateBannerAccent(
                modifier = Modifier.matchParentSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .background(PastelGreen, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.workspace_create_new),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = GrayBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.workspace_create_card_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B8575)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PastelGreenIconBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.workspace_create_new),
                            tint = PastelGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = PastelGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceCreateBannerAccent(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height

        drawRect(color = Color.White)

        // Halo pastel lembut di kanan-tengah (senada dengan TableDataBannerIllustration)
        drawCircle(
            color = PastelGreenSoftBg,
            radius = h * 0.65f,
            center = Offset(w * 0.78f, h * 0.50f)
        )

        val hillPath = Path().apply {
            moveTo(w * 0.50f, h)
            quadraticTo(w * 0.72f, h * 0.35f, w * 1.05f, h)
            close()
        }
        drawPath(path = hillPath, color = PastelGreenBg.copy(alpha = 0.75f))

        // Scrim putih lembut di sisi kiri agar teks tetap bersih & kontras
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to Color.White,
                    0.46f to Color.White.copy(alpha = 0.92f),
                    0.68f to Color.Transparent,
                    1.0f to Color.Transparent
                )
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 640)
@Composable
fun WorkspaceScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(MainBlue, MornYellow)
            )
        )
    ) {
        WorkspaceScreen(
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
