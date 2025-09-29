package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.theme.GrayBlue


data class ColumnConfig<T>(
    val header: String,
    val weight: Float,
    val content: @Composable (T) -> Unit
)

@Composable
fun <T> DataTablePanel(
    title: String,
    itemCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    columns: List<ColumnConfig<T>>, // <- PARAMETER BARU
    data: List<T>,
    isLoading: Boolean,
    onRowClick: (T) -> Unit
) {
    val borderRadius = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top bar with Search and Item Count
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 2.dp,
                        color = GrayBlue.copy(alpha = 0.8f),
                        shape = borderRadius
                    ),
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(searchPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.LightGray,
                    disabledContainerColor = Color.LightGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            // Item Count
            Card(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = GrayBlue.copy(alpha = 0.8f),
                        shape = borderRadius
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "$title: $itemCount",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Data List Panel
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = GrayBlue.copy(alpha = 0.8f),
                    shape = borderRadius
                ),
            shape = borderRadius,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f))
        ) {
            Column (
                modifier = Modifier.padding(2.dp)
            ){
                // Table Header (Sekarang dibuat dari List<ColumnConfig>)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    columns.forEach { column ->
                        Text(
                            text = column.header,
                            modifier = Modifier.weight(column.weight),
                            fontWeight = FontWeight.Bold,
                            color = GrayBlue
                        )
                    }
                }
                Divider(color = Color.White.copy(alpha = 0.5f))

                // Table Rows
                LazyColumn {
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    } else {
                        items(data) { item ->
                            // Panel sekarang yang membuat Row untuk setiap item
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRowClick(item) }
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tampilkan konten setiap sel sesuai konfigurasi kolom
                                columns.forEach { column ->
                                    Box(modifier = Modifier.weight(column.weight)) {
                                        column.content(item)
                                    }
                                }
                            }
                            Divider(
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}