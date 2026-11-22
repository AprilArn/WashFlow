package com.aprilarn.washflow.ui.orders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Inventory2
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueMedium
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.utils.CurrencyUtils

@Composable
fun OrderPanel(
    modifier: Modifier = Modifier,
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val totalAmount = remember(uiState.selectedItems) {
        uiState.selectedItems.values.sumOf { it.subtotal ?: 0.0 }
    }
    val totalQuantity = remember(uiState.selectedItems) {
        uiState.selectedItems.values.sumOf { it.itemQuantity ?: 0 }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = borderRadius,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 18.dp, horizontal = 22.dp)
        ) {
            // --- HEADER & SERVICE TABS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(32.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = "Services & Items",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Pilih layanan dan barang cucian",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Service Pill Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(uiState.services, key = { it.serviceId }) { service ->
                        val isSelected = service.serviceId == uiState.activeServiceTabId
                        val selectedCountInService = remember(uiState.selectedItems, service.serviceId) {
                            uiState.selectedItems.values.count { it.serviceId == service.serviceId }
                        }
                        ServiceTabChip(
                            name = service.serviceName,
                            isSelected = isSelected,
                            selectedCount = selectedCountInService,
                            onClick = { viewModel.onServiceTabSelected(service.serviceId) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = PastelBlueBg,
                thickness = 1.dp
            )

            // --- ITEM LIST ---
            val itemsForActiveService = remember(uiState.items, uiState.activeServiceTabId) {
                uiState.items.filter { it.serviceId == uiState.activeServiceTabId }
            }

            if (itemsForActiveService.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Inventory2,
                            contentDescription = null,
                            tint = PastelNeutralText.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Belum ada barang untuk layanan ini",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemsForActiveService, key = { it.itemId }) { item ->
                        val selectedOrderItem = uiState.selectedItems[item.itemId]
                        ItemCheckRow(
                            item = item,
                            selectedOrderItem = selectedOrderItem,
                            onClick = { viewModel.handleItemClick(item) }
                        )
                    }
                }
            }

            HorizontalDivider(
                color = PastelBlueBg,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- FOOTER SUMMARY & ACTION BUTTON ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (uiState.selectedItems.isNotEmpty()) PastelGreenBg else PastelNeutralBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${uiState.selectedItems.size} jenis ($totalQuantity pcs/kg)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.selectedItems.isNotEmpty()) PastelGreen else PastelNeutralText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (totalAmount > 0.0) {
                        Column {
                            Text(
                                text = "Estimasi Total",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PastelNeutralText,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = CurrencyUtils.formatRupiahWithSymbol(totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GrayBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.createOrder() },
                    enabled = !uiState.isCreatingOrder,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelBlue,
                        contentColor = Color.White,
                        disabledContainerColor = PastelBlueBg
                    )
                ) {
                    if (uiState.isCreatingOrder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (uiState.editingOrderId != null) Icons.Rounded.CheckCircle else Icons.Rounded.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.editingOrderId != null) "Update Order" else "Create Order",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceTabChip(
    name: String,
    isSelected: Boolean,
    selectedCount: Int,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PastelBlue else PastelBlueSoftBg,
        animationSpec = tween(durationMillis = 220),
        label = "serviceTabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else GrayBlue,
        animationSpec = tween(durationMillis = 220),
        label = "serviceTabContent"
    )
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isSelected) PastelBlue else PastelBlueBg,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp
            )
        )
        if (selectedCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.24f) else PastelBlue.copy(alpha = 0.14f)
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$selectedCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) Color.White else PastelBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ItemCheckRow(
    item: Items,
    selectedOrderItem: OrderItem?,
    onClick: () -> Unit
) {
    val isChecked = selectedOrderItem != null
    val rowShape = RoundedCornerShape(14.dp)
    val rowBg by animateColorAsState(
        targetValue = if (isChecked) PastelBlueSoftBg else PastelNeutralBg.copy(alpha = 0.55f),
        animationSpec = tween(durationMillis = 200),
        label = "itemRowBg"
    )
    val rowBorder by animateColorAsState(
        targetValue = if (isChecked) PastelBlueMedium else Color(0xFFE6ECF5),
        animationSpec = tween(durationMillis = 200),
        label = "itemRowBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(rowBg)
            .border(width = if (isChecked) 1.5.dp else 1.dp, color = rowBorder, shape = rowShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Custom Pastel Checkbox Indicator
        val checkShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(checkShape)
                .background(if (isChecked) PastelBlue else Color.White)
                .border(
                    width = 1.5.dp,
                    color = if (isChecked) PastelBlue else PastelBlueBg,
                    shape = checkShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = GrayBlue,
                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${CurrencyUtils.formatRupiahWithSymbol(item.itemPrice)} / unit",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelNeutralText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            )
        }

        if (selectedOrderItem != null) {
            // Tampilkan badge kuantitas & subtotal ketika sudah dipilih
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelBlue)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${selectedOrderItem.itemQuantity ?: 1}x",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = CurrencyUtils.formatRupiahWithSymbol(selectedOrderItem.subtotal ?: item.itemPrice),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PastelBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        } else {
            Text(
                text = CurrencyUtils.formatRupiahWithSymbol(item.itemPrice),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
        }
    }
}
