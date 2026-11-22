package com.aprilarn.washflow.ui.phone.contributors

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.contributors.ContributorCard
import com.aprilarn.washflow.ui.contributors.ContributorDetailDialog
import com.aprilarn.washflow.ui.contributors.ContributorUiModel
import com.aprilarn.washflow.ui.contributors.ContributorsUiState
import com.aprilarn.washflow.ui.theme.GrayBlue

/**
 * Tampilan Daftar Kontributor khusus HP biasa (Batang / Portrait) dengan lebar responsif penuh.
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
    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Cari anggota...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, fontSize = 13.sp)
                    )
                },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            if (uiState.isCurrentUserOwner) {
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.height(50.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = GrayBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undang", color = GrayBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.85f)
        ) {
            Text(
                text = "Total Kontributor: ${uiState.contributors.size}",
                color = GrayBlue,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.filteredContributors) { contributor ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .clickable { onContributorClick(contributor) }
                    ) {
                        ContributorCard(
                            name = contributor.name,
                            photoUrl = contributor.photoUrl,
                            role = contributor.role
                        )
                    }
                }
            }
        }
    }

    if (uiState.selectedContributor != null) {
        val shouldShowKickButton = uiState.isCurrentUserOwner &&
                uiState.selectedContributor.uid != uiState.currentUserUid

        ContributorDetailDialog(
            contributor = uiState.selectedContributor,
            showKickButton = shouldShowKickButton,
            onDismiss = onDismissDialog,
            onKick = { onKickUser(uiState.selectedContributor) }
        )
    }
}
