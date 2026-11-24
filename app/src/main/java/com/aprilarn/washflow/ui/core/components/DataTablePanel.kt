package com.aprilarn.washflow.ui.core.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.TableDataBannerIllustration
import com.aprilarn.washflow.ui.feature.masterdata.tabledata.resolveTableDataTheme
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText

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
    columns: List<ColumnConfig<T>>,
    data: List<T>,
    isLoading: Boolean,
    onRowClick: (T) -> Unit,
    subTitle: String? = null
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val theme = remember(title) { resolveTableDataTheme(title) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = borderRadius,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Editorial dengan ilustrasi khas kategori (Customers / Services / Items)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                TableDataBannerIllustration(
                    theme = theme,
                    modifier = Modifier.matchParentSize()
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(34.dp)
                                .background(theme.accentColor, RoundedCornerShape(2.dp))
                        )
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = theme.titleColor
                                )
                            )
                            Text(
                                text = subTitle ?: theme.defaultSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = theme.subtitleColor
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.badgeBgColor.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = "Total",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Text(
                            text = itemCount.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = theme.countColor
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = PastelNeutralBg, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = searchPlaceholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelNeutralText,
                                fontSize = 13.sp
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = theme.accentColor
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search",
                                    tint = PastelNeutralText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GrayBlue,
                        unfocusedTextColor = GrayBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = theme.softBgColor,
                        focusedBorderColor = theme.accentColor,
                        unfocusedBorderColor = theme.badgeBgColor,
                        cursorColor = theme.accentColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Table Column Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.softBgColor)
                        .border(1.dp, theme.badgeBgColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    columns.forEach { column ->
                        Text(
                            text = column.header,
                            modifier = Modifier.weight(column.weight),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = theme.accentColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table Rows
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = theme.accentColor,
                            strokeWidth = 2.5.dp
                        )
                    }
                } else if (data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Tidak ada data yang cocok dengan \"$searchQuery\""
                            } else {
                                "Belum ada data tersimpan"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(data) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onRowClick(item) }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                columns.forEach { column ->
                                    Box(
                                        modifier = Modifier.weight(column.weight),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        column.content(item)
                                    }
                                }
                            }
                            if (index < data.lastIndex) {
                                HorizontalDivider(
                                    color = PastelNeutralBg,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
