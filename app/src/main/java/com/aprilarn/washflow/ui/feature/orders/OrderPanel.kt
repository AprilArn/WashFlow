package com.aprilarn.washflow.ui.feature.orders

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueMedium
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.utils.CurrencyUtils

@Composable
fun OrderPanel(
    modifier: Modifier = Modifier,
    uiState: OrdersUiState,
    viewModel: OrdersViewModel,
    showFooter: Boolean = true,
    isCompact: Boolean = false
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val coroutineScope = rememberCoroutineScope()
    val serviceTabsListState = rememberLazyListState()

    val services = uiState.services
    val activeServiceIndex = remember(services, uiState.activeServiceTabId) {
        services.indexOfFirst { it.serviceId == uiState.activeServiceTabId }.coerceAtLeast(0)
    }

    val pagerState = rememberPagerState(
        initialPage = activeServiceIndex,
        pageCount = { services.size.coerceAtLeast(1) }
    )

    if (isCompact && services.isNotEmpty()) {
        // Sinkronisasi saat user menggeser (swipe) HorizontalPager -> perbarui tab aktif & scroll chip
        LaunchedEffect(pagerState.settledPage, services) {
            val settledService = services.getOrNull(pagerState.settledPage)
            if (settledService != null && settledService.serviceId != uiState.activeServiceTabId) {
                viewModel.onServiceTabSelected(settledService.serviceId)
            }
        }
        LaunchedEffect(pagerState.currentPage, services.size) {
            if (pagerState.currentPage in services.indices) {
                serviceTabsListState.animateScrollToItem(pagerState.currentPage)
            }
        }
        // Sinkronisasi saat activeServiceTabId berubah dari luar (mis. data baru dimuat)
        LaunchedEffect(activeServiceIndex, services.size) {
            if (!pagerState.isScrollInProgress &&
                activeServiceIndex in services.indices &&
                pagerState.currentPage != activeServiceIndex
            ) {
                pagerState.scrollToPage(activeServiceIndex)
            }
        }
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
                .padding(
                    vertical = if (isCompact) 14.dp else 18.dp,
                    horizontal = if (isCompact) 16.dp else 22.dp
                )
        ) {
            // --- HEADER & SERVICE TABS ---
            if (isCompact) {
                // Tampilan HP: Judul di baris atas, chip kategori layanan full-width di bawahnya
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = "Services & Items",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Pilih atau geser kategori layanan",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    state = serviceTabsListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(services, key = { _, service -> service.serviceId }) { index, service ->
                        val isSelected = index == pagerState.currentPage
                        val selectedCountInService = remember(uiState.selectedItems, service.serviceId) {
                            uiState.selectedItems.values.count { it.serviceId == service.serviceId }
                        }
                        ServiceTabChip(
                            name = service.serviceName,
                            isSelected = isSelected,
                            selectedCount = selectedCountInService,
                            onClick = {
                                viewModel.onServiceTabSelected(service.serviceId)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            } else {
                // Tampilan Tablet: Judul di kiri, chip kategori di kanan
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
                        items(services, key = { it.serviceId }) { service ->
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
            }

            HorizontalDivider(
                color = PastelBlueBg,
                thickness = 1.dp
            )

            // --- ITEM LIST ---
            if (isCompact && services.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val serviceForPage = services[page]
                    val itemsForPage = remember(uiState.items, serviceForPage.serviceId) {
                        uiState.items.filter { it.serviceId == serviceForPage.serviceId }
                    }
                    ServiceItemsList(
                        items = itemsForPage,
                        selectedItems = uiState.selectedItems,
                        onItemClick = { viewModel.handleItemClick(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                val itemsForActiveService = remember(uiState.items, uiState.activeServiceTabId) {
                    uiState.items.filter { it.serviceId == uiState.activeServiceTabId }
                }
                ServiceItemsList(
                    items = itemsForActiveService,
                    selectedItems = uiState.selectedItems,
                    onItemClick = { viewModel.handleItemClick(it) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            if (showFooter) {
                HorizontalDivider(
                    color = PastelBlueBg,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- FOOTER ACTION BUTTON (Khusus Tablet; ringkasan total sudah ada di Nota Pesanan) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
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
}

@Composable
private fun ServiceItemsList(
    items: List<Items>,
    selectedItems: Map<String, OrderItem>,
    onItemClick: (Items) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier,
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
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.itemId }) { item ->
                val selectedOrderItem = selectedItems[item.itemId]
                ItemCheckRow(
                    item = item,
                    selectedOrderItem = selectedOrderItem,
                    onClick = { onItemClick(item) }
                )
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
