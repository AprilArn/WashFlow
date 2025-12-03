// com/aprilarn/washflow/ui/contributors/ContributorsScreen.kt
package com.aprilarn.washflow.ui.contributors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.theme.GrayBlue

val borderRadius = RoundedCornerShape(24.dp)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributorsScreen(
    uiState: ContributorsUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onContributorClick: (ContributorUiModel) -> Unit,
    onDismissDialog: () -> Unit,
    onKickUser: (ContributorUiModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp), // Padding kiri-kanan sesuai desain
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Row (Search + Tombol + Counter) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Search by Name",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, fontSize = 14.sp)
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp), // Tinggi lebih kecil
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.LightGray,
                    disabledContainerColor = Color.LightGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // 2. Tombol Add (Hanya jika user adalah Owner)
            if (uiState.isCurrentUserOwner) {
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.height(50.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = GrayBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = GrayBlue, fontWeight = FontWeight.Bold)
                }
            }

            // 3. Counter Badge
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.height(50.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Contributors: ${uiState.contributors.size}",
                        color = GrayBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // --- Daftar Kontributor ---
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.filteredContributors) { contributor ->
                    // Bungkus ContributorCard dengan Box untuk menangani klik
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(borderRadius) // Samakan radius dengan Card
                            .clickable {
                                // Event klik dikirim ke ViewModel
                                onContributorClick(contributor)
                            }
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

    // --- TAMPILKAN DIALOG JIKA ADA USER TERPILIH ---
    if (uiState.selectedContributor != null) {
        ContributorDetailDialog(
            contributor = uiState.selectedContributor!!,
            onDismiss = onDismissDialog,
            onKick = { onKickUser(uiState.selectedContributor!!) }
        )
    }
}