package com.aprilarn.washflow.ui.feature.contributors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelSlateBg

/**
 * Tampilan Daftar Kontributor khusus HP (Portrait) dengan gaya modern selaras layar WashFlow lainnya.
 */
@Composable
fun PhoneContributorsScreen(
    uiState: ContributorsUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onContributorClick: (ContributorUiModel) -> Unit,
    onDismissDialog: () -> Unit,
    onKickUser: (ContributorUiModel) -> Unit
) {
    val ownerCount = remember(uiState.contributors) {
        uiState.contributors.count { it.role.equals("owner", ignoreCase = true) }
    }
    val memberCount = remember(uiState.contributors, ownerCount) {
        (uiState.contributors.size - ownerCount).coerceAtLeast(0)
    }
    val onlineContributors = remember(uiState.filteredContributors) {
        uiState.filteredContributors.filter { it.isOnline }
    }
    val offlineContributors = remember(uiState.filteredContributors) {
        uiState.filteredContributors.filter { !it.isOnline }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Header Editorial + Tombol Invite (Owner)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(34.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.nav_contributors),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayBlue
                        )
                        Text(
                            text = stringResource(R.string.contributors_subtitle),
                            fontSize = 11.sp,
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                if (uiState.isCurrentUserOwner) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = stringResource(R.string.contributors_invite),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.contributors_invite),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Kartu Ringkasan Kompak (Online Now + Total Members)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val hasOnline = uiState.onlineCount > 0
                ContributorSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.contributors_online_now),
                    value = uiState.onlineCount.toString(),
                    subtitle = formatOnlineSummarySubtitle(uiState.totalOnlineDevices),
                    containerColor = if (hasOnline) PastelGreenSoftBg else PastelNeutralBg,
                    iconBgColor = if (hasOnline) PastelGreenIconBg else PastelSlateBg,
                    accentColor = if (hasOnline) PastelGreen else PastelNeutralText,
                    valueColor = if (hasOnline) PastelGreenDark else GrayBlue,
                    icon = if (hasOnline) null else Icons.Rounded.Groups,
                    showOnlinePulse = hasOnline,
                    compact = true
                )

                ContributorSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.contributors_total_title),
                    value = uiState.contributors.size.toString(),
                    subtitle = stringResource(
                        R.string.contributors_role_breakdown,
                        ownerCount,
                        memberCount
                    ),
                    containerColor = PastelBlueSoftBg,
                    iconBgColor = PastelBlueIconBg,
                    accentColor = PastelBlue,
                    valueColor = GrayBlue,
                    icon = Icons.Rounded.Groups,
                    compact = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.contributors_search_placeholder),
                        fontSize = 13.sp,
                        color = PastelNeutralText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.common_search),
                        tint = PastelNeutralText,
                        modifier = Modifier.size(19.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.common_clear_search),
                                tint = PastelNeutralText,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = PastelNeutralBg.copy(alpha = 0.65f),
                    focusedBorderColor = PastelBlue.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = GrayBlue,
                    unfocusedTextColor = GrayBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PastelNeutralBg, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Daftar Kontributor (Online di atas, Offline di bawah)
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PastelBlue)
                    }
                }

                uiState.filteredContributors.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PastelBlueSoftBg,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PersonSearch,
                                        contentDescription = null,
                                        tint = PastelBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.contributors_empty_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayBlue
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) {
                                    stringResource(R.string.contributors_empty_search_subtitle)
                                } else {
                                    stringResource(R.string.contributors_empty_workspace_subtitle)
                                },
                                fontSize = 12.sp,
                                color = PastelNeutralText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    val listState = rememberLazyListState()
                    LaunchedEffect(onlineContributors.firstOrNull()?.uid, uiState.searchQuery) {
                        if (listState.firstVisibleItemIndex <= 2) {
                            listState.scrollToItem(0)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        if (onlineContributors.isNotEmpty()) {
                            item(key = "header_online") {
                                ContributorSectionHeader(
                                    isOnlineSection = true,
                                    count = onlineContributors.size
                                )
                            }
                            items(
                                items = onlineContributors,
                                key = { it.uid }
                            ) { contributor ->
                                ContributorCard(
                                    contributor = contributor,
                                    isCurrentUser = contributor.uid == uiState.currentUserUid,
                                    onClick = { onContributorClick(contributor) },
                                    compact = true,
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        if (offlineContributors.isNotEmpty()) {
                            item(key = "header_offline") {
                                ContributorSectionHeader(
                                    isOnlineSection = false,
                                    count = offlineContributors.size,
                                    modifier = Modifier.padding(
                                        top = if (onlineContributors.isNotEmpty()) 8.dp else 0.dp
                                    )
                                )
                            }
                            items(
                                items = offlineContributors,
                                key = { it.uid }
                            ) { contributor ->
                                ContributorCard(
                                    contributor = contributor,
                                    isCurrentUser = contributor.uid == uiState.currentUserUid,
                                    onClick = { onContributorClick(contributor) },
                                    compact = true,
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val selectedContributor = uiState.selectedContributor
    if (selectedContributor != null) {
        val shouldShowKickButton =
            uiState.isCurrentUserOwner && selectedContributor.uid != uiState.currentUserUid

        ContributorDetailDialog(
            contributor = selectedContributor,
            isCurrentUser = selectedContributor.uid == uiState.currentUserUid,
            showKickButton = shouldShowKickButton,
            onDismiss = onDismissDialog,
            onKick = { onKickUser(selectedContributor) }
        )
    }
}
