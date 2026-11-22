package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.components.StatusWorkflowBannerIllustration
import com.aprilarn.washflow.ui.components.resolveStatusWorkflowTheme
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderPreviewPanel(
    modifier: Modifier = Modifier,
    uiState: OrdersUiState
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val theme = remember { resolveStatusWorkflowTheme("On Queue") }

    val selectedServices = remember(uiState.selectedItems, uiState.services) {
        uiState.selectedItems.values
            .mapNotNull { item -> uiState.services.find { it.serviceId == item.serviceId } }
            .distinct()
            .joinToString(" + ") { it.serviceName }
            .ifEmpty { "Belum ada layanan dipilih" }
    }

    val formattedDate = remember(uiState.dueDate) {
        uiState.dueDate?.toDate()?.let {
            SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.getDefault()).format(it)
        } ?: "Tanpa batas waktu"
    }

    val totalAmount = remember(uiState.selectedItems) {
        uiState.selectedItems.values.sumOf { it.subtotal ?: 0.0 }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = borderRadius,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header panel dengan ilustrasi alur kerja "On Queue" senada dengan ManageOrderPanel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
            ) {
                StatusWorkflowBannerIllustration(
                    theme = theme,
                    modifier = Modifier.matchParentSize(),
                    baseColor = Color.White
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(theme.accentColor, RoundedCornerShape(2.dp))
                        )
                        Column {
                            Text(
                                text = "On Queue",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = theme.labelColor
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "Pratinjau kartu antrean",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = theme.subtitleColor
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = "1",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = theme.countColor
                        )
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFEDF1F7),
                thickness = 1.dp
            )

            // Area Kartu Pratinjau Order
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val cardShape = RoundedCornerShape(18.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(PastelNeutralBg.copy(alpha = 0.65f))
                        .border(1.5.dp, Color(0xFFE2E8F2), cardShape)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedServices,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PastelNeutralText,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.selectedCustomer?.name ?: "Pilih Pelanggan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = theme.primaryTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Badge jumlah item terpilih
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelAmberBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.selectedItems.size}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PastelAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Info batas waktu & estimasi biaya
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
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (totalAmount > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                tint = PastelAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = CurrencyUtils.formatRupiahWithSymbol(totalAmount),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PastelAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 368, heightDp = 320)
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
                itemQuantity = 3,
                subtotal = 24000.0
            ),
            "item_4" to OrderItem(
                itemId = "item_4",
                serviceId = "D-01",
                itemName = "Jas",
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
            OrderPreviewPanel(uiState = previewState)
        }
    }
}
