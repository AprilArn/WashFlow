package com.aprilarn.washflow.ui.feature.workspace

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.components.performLightHaptic
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.MornYellow
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import java.util.Locale

internal const val INVITE_CODE_LENGTH = 6

internal fun sanitizeInviteCode(raw: String): String {
    return raw
        .uppercase(Locale.ROOT)
        .filter { it in 'A'..'Z' || it in '0'..'9' }
        .take(INVITE_CODE_LENGTH)
}

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
    val sanitizedCode = sanitizeInviteCode(inviteCode)
    val canJoin = sanitizedCode.length == INVITE_CODE_LENGTH && !isLoading

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (horizontalActionLayout) 24.dp else 20.dp,
                    vertical = 20.dp
                )
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
                        inviteCode = sanitizedCode,
                        onInviteCodeChange = onInviteCodeChange,
                        isLoading = isLoading,
                        canJoin = canJoin,
                        isCompact = false,
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
                    inviteCode = sanitizedCode,
                    onInviteCodeChange = onInviteCodeChange,
                    isLoading = isLoading,
                    canJoin = canJoin,
                    isCompact = true,
                    onJoinClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

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
    isCompact: Boolean,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val clipboardEmptyText = stringResource(R.string.workspace_invite_clipboard_empty)

    var isFocused by remember { mutableStateOf(false) }

    val boxHeight = if (isCompact) 48.dp else 54.dp
    val boxSpacing = if (isCompact) 6.dp else 8.dp
    val boxCorner = RoundedCornerShape(12.dp)

    val cursorTransition = rememberInfiniteTransition(label = "otpCursorBlink")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "otpCursorAlpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp)
    ) {
        BasicTextField(
            value = TextFieldValue(
                text = inviteCode,
                selection = TextRange(inviteCode.length)
            ),
            onValueChange = { newValue ->
                val sanitized = sanitizeInviteCode(newValue.text)
                if (sanitized != inviteCode) {
                    onInviteCodeChange(sanitized)
                }
            },
            enabled = !isLoading,
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (canJoin) onJoinClick() }
            ),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(boxSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(INVITE_CODE_LENGTH) { index ->
                        val char = inviteCode.getOrNull(index)
                        val isFilled = char != null
                        val isCurrentSlot = isFocused && !isLoading && (
                            index == inviteCode.length ||
                                (inviteCode.length == INVITE_CODE_LENGTH && index == INVITE_CODE_LENGTH - 1)
                            )

                        val containerColor = when {
                            isLoading -> PastelNeutralBg.copy(alpha = 0.45f)
                            isFilled || isCurrentSlot -> PastelBlueSoftBg
                            else -> PastelNeutralBg.copy(alpha = 0.72f)
                        }
                        val borderColor = when {
                            isLoading -> Color.Transparent
                            isCurrentSlot -> PastelBlue
                            isFilled -> PastelBlueBg
                            else -> PastelBlueBg.copy(alpha = 0.55f)
                        }
                        val borderWidth = if (isCurrentSlot) 1.5.dp else 1.dp

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(boxHeight)
                                .clip(boxCorner)
                                .background(containerColor)
                                .border(borderWidth, borderColor, boxCorner),
                            contentAlignment = Alignment.Center
                        ) {
                            if (char != null) {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isLoading) PastelNeutralText else GrayBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isCompact) 18.sp else 20.sp
                                    )
                                )
                            } else if (isFocused && !isLoading && index == inviteCode.length) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(if (isCompact) 18.dp else 20.dp)
                                        .alpha(cursorAlpha)
                                        .background(PastelBlue, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }
        )

        // Tombol Paste di sebelah kanan 6 kotak input
        Surface(
            onClick = {
                val clipText = clipboardManager.getText()?.text.orEmpty()
                val pasted = sanitizeInviteCode(clipText)
                if (pasted.isNotEmpty()) {
                    performLightHaptic(view, context)
                    onInviteCodeChange(pasted)
                } else {
                    Toast.makeText(context, clipboardEmptyText, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isLoading,
            modifier = Modifier.size(boxHeight),
            shape = boxCorner,
            color = if (isLoading) PastelNeutralBg.copy(alpha = 0.45f) else PastelBlueSoftBg,
            border = BorderStroke(
                width = 1.dp,
                color = if (isLoading) Color.Transparent else PastelBlueBg
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.ContentPaste,
                    contentDescription = stringResource(R.string.workspace_invite_paste_cd),
                    tint = if (isLoading) PastelNeutralText else PastelBlue,
                    modifier = Modifier.size(if (isCompact) 20.dp else 22.dp)
                )
            }
        }
    }
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
