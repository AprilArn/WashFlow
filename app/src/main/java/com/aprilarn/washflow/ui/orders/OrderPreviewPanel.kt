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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderPreviewPanel(
    uiState: OrdersUiState
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val borderColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.25f), shape = borderRadius)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = borderRadius
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=10.dp, bottom=4.dp, start=8.dp, end=8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = "On Queue",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = "Order menunggu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayBlue
                    )
                }
                Text(
                    text = "1",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = GrayBlue
                    )
                ) // Static "1" for demo purposes
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Item order list region
            val selectedServices = uiState.selectedItems.values
                .mapNotNull { item -> uiState.services.find { it.serviceId == item.serviceId } }
                .distinct()
                .joinToString(" + ") { it.serviceName } .ifEmpty { "No item selected" }
            val formattedDate = uiState.dueDate?.toDate()?.let {
                SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(it)
            } ?: "No due date"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(borderRadius)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = borderRadius
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = selectedServices,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray
                            )
                        )
                        Text(
                            text = uiState.selectedCustomer?.name ?: "No Customer",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayBlue
                            )
                        )
                    }
                    Text(
                        text = "${uiState.selectedItems.size}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 368, heightDp = 600)
@Composable
fun OrderPreviewPanelPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        // 1. Siapkan data sampel yang dibutuhkan oleh panel
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
            "item_4" to OrderItem(itemId = "item_4", serviceId = "D-01", itemName = "Jas", itemQuantity = 1, subtotal = 50000.0)
        )

        // 2. Buat OrdersUiState dengan data sampel
        val previewState = OrdersUiState(
            services = sampleServices,
            selectedCustomer = sampleCustomer,
            selectedItems = sampleSelectedItems,
            dueDate = Timestamp(Date()) // Menggunakan tanggal & waktu saat ini untuk preview
        )

        // 3. Panggil komponen dengan state yang sudah dibuat
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            OrderPreviewPanel(uiState = previewState)
        }
    }
}