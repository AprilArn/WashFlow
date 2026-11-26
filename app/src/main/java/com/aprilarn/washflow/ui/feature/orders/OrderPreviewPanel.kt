package com.aprilarn.washflow.ui.feature.orders

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.core.theme.PastelRoseText
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderPreviewPanel(
    modifier: Modifier = Modifier,
    uiState: OrdersUiState,
    onRemoveItem: ((String) -> Unit)? = null
) {
    val borderRadius = RoundedCornerShape(24.dp)

    val selectedItemsList = remember(uiState.selectedItems) {
        uiState.selectedItems.values.toList()
    }

    val otherServiceLabel = stringResource(R.string.orders_receipt_other_service)
    val groupedItemsByService = remember(selectedItemsList, uiState.services, otherServiceLabel) {
        groupOrderItemsByService(selectedItemsList, uiState.services, otherServiceLabel)
    }

    val noDueDateText = stringResource(R.string.orders_receipt_no_due_date)
    val formattedDate = remember(uiState.dueDate, noDueDateText) {
        uiState.dueDate?.toDate()?.let {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(it)
        } ?: noDueDateText
    }

    val totalQty = remember(selectedItemsList) {
        selectedItemsList.sumOf { it.itemQuantity ?: 0 }
    }

    val totalAmount = remember(selectedItemsList) {
        selectedItemsList.sumOf { it.subtotal ?: 0.0 }
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
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // 1. Header Nota Pesanan
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
                            .height(32.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.orders_receipt_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = GrayBlue
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.orders_receipt_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = PastelNeutralText
                            ),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = PastelBlueSoftBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.orders_receipt_items_count, selectedItemsList.size),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PastelBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Ringkasan Pelanggan & Batas Waktu di Header Nota
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = uiState.selectedCustomer?.name ?: stringResource(R.string.orders_receipt_no_customer),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.selectedCustomer != null) GrayBlue else PastelNeutralText,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = PastelNeutralText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReceiptDashedDivider(color = PastelBlueBg)

            // 3. Daftar Item Nota (Scrollable)
            if (selectedItemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelBlueSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = PastelBlue.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.orders_receipt_empty_title),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.orders_receipt_empty_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = groupedItemsByService,
                        key = { (serviceName, _) -> serviceName }
                    ) { (serviceName, itemsInService) ->
                        ReceiptServiceSection(
                            serviceName = serviceName,
                            items = itemsInService,
                            onRemoveItem = onRemoveItem
                        )
                    }
                }
            }

            ReceiptDashedDivider(color = PastelBlueBg)

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Footer Total Nota
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.orders_receipt_estimated_total),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = stringResource(R.string.orders_receipt_pcs_count, totalQty.coerceAtLeast(0)),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PastelBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Mengelompokkan daftar OrderItem berdasarkan kategori layanan (Services).
 */
fun groupOrderItemsByService(
    items: List<OrderItem>,
    services: List<Services>,
    otherServiceLabel: String = "Other Service"
): List<Pair<String, List<OrderItem>>> {
    val serviceNameById = services.associate { it.serviceId to it.serviceName }
    return items
        .groupBy { item ->
            serviceNameById[item.serviceId]?.takeIf { it.isNotBlank() } ?: otherServiceLabel
        }
        .entries
        .map { it.key to it.value }
}

/**
 * Blok kategori layanan pada Nota Pesanan (header kategori + daftar item di bawahnya).
 */
@Composable
fun ReceiptServiceSection(
    serviceName: String,
    items: List<OrderItem>,
    onRemoveItem: ((String) -> Unit)? = null
) {
    val sectionQty = remember(items) {
        items.sumOf { it.itemQuantity ?: 0 }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = PastelBlueSoftBg.copy(alpha = 0.75f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PastelBlue)
                    )
                    Text(
                        text = serviceName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PastelBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.orders_receipt_pcs_count, sectionQty),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PastelBlue.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEachIndexed { index, item ->
                ReceiptItemRow(
                    item = item,
                    serviceName = null,
                    onRemove = onRemoveItem?.let { remove -> { remove(item.itemId) } }
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        color = PastelBlueSoftBg,
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptItemRow(
    item: OrderItem,
    serviceName: String?,
    onRemove: (() -> Unit)? = null
) {
    val qty = item.itemQuantity ?: 1
    val unitPrice = item.itemPrice ?: ((item.subtotal ?: 0.0) / qty.coerceAtLeast(1))
    val subtotal = item.subtotal ?: (unitPrice * qty)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.itemName ?: stringResource(R.string.orders_receipt_item_fallback),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!serviceName.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PastelBlueSoftBg)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = serviceName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Text(
                text = "$qty × ${CurrencyUtils.formatRupiahWithSymbol(unitPrice)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PastelNeutralText,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = CurrencyUtils.formatRupiahWithSymbol(subtotal),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Bold
                )
            )
            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(PastelRoseBg)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.orders_receipt_remove_item_cd),
                        tint = PastelRoseText,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptDashedDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = size.height,
            pathEffect = dashEffect
        )
    }
}

@Preview(showBackground = true, widthDp = 368, heightDp = 380)
@Composable
fun OrderPreviewPanelPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF88ACC8), Color(0xFFE2D8CE))
            )
        )
    ) {
        val sampleCustomer =
            Customers(customerId = "cust_1", name = "Raphael", contact = "081234567890")
        val sampleServices = listOf(
            Services(serviceId = "L-02", serviceName = "Laundry Kiloan"),
            Services(serviceId = "D-01", serviceName = "Dry Clean")
        )
        val sampleSelectedItems = mapOf(
            "item_3" to OrderItem(
                itemId = "item_3",
                serviceId = "L-02",
                itemName = "Pakaian Harian (kg)",
                itemPrice = 8000.0,
                itemQuantity = 3,
                subtotal = 24000.0
            ),
            "item_4" to OrderItem(
                itemId = "item_4",
                serviceId = "D-01",
                itemName = "Jas",
                itemPrice = 50000.0,
                itemQuantity = 1,
                subtotal = 50000.0
            )
        )

        val previewState = OrdersUiState(
            services = sampleServices,
            selectedCustomer = sampleCustomer,
            selectedItems = sampleSelectedItems,
            dueDate = Timestamp(Date())
        )

        Box(modifier = Modifier.padding(16.dp)) {
            OrderPreviewPanel(
                uiState = previewState,
                onRemoveItem = {}
            )
        }
    }
}
