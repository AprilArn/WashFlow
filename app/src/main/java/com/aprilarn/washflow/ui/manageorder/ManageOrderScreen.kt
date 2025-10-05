package com.aprilarn.washflow.ui.manage_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.components.DragDropContainer
import com.aprilarn.washflow.ui.components.OrderStatusColumn
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ManageOrderScreen(
    uiState: ManageOrderUiState,
    onDrop: (orderId: String, newStatus: String) -> Unit,
    onOrderClick: (Orders) -> Unit,
    onDismissDialog: () -> Unit,
    onDeleteOrder: (String) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        DragDropContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            services = uiState.services
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column untuk "On Queue"
                OrderStatusColumn(
                    modifier = Modifier.weight(1f),
                    title = "On Queue",
                    subTitle = "Dalam antrian",
                    orders = uiState.ordersOnQueue,
                    services = uiState.services,
                    onDrop = { orderId -> onDrop(orderId, "On Queue") },
                    onOrderClick = onOrderClick
                )

                // Column untuk "On Process"
                OrderStatusColumn(
                    modifier = Modifier.weight(1f),
                    title = "On Process",
                    subTitle = "Sedang diproses",
                    orders = uiState.ordersOnProcess,
                    services = uiState.services,
                    onDrop = { orderId -> onDrop(orderId, "On Process") },
                    onOrderClick = onOrderClick
                )

                // Column untuk "Done"
                OrderStatusColumn(
                    modifier = Modifier.weight(1f),
                    title = "Done",
                    subTitle = "Selesai",
                    orders = uiState.ordersDone,
                    services = uiState.services,
                    onDrop = { orderId -> onDrop(orderId, "Done") },
                    onOrderClick = onOrderClick
                )
            }
        }
        // Tampilkan dialog jika ada order yang dipilih
        uiState.selectedOrderForDetail?.let { order ->
            OrderDetailDialog(
                order = order,
                services = uiState.services,
                onDismiss = onDismissDialog,
                onDelete = { onDeleteOrder(order.orderId) }
            )
        }
    }
}

@Composable
fun OrderDetailDialog(
    order: Orders,
    services: List<Services>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val serviceNames = remember(order.orderItems, services) {
        order.orderItems
            .mapNotNull { orderItem -> services.find { it.serviceId == orderItem.serviceId }?.serviceName }
            .distinct()
            .joinToString(" + ")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Order Details", style = MaterialTheme.typography.titleLarge)
                Text("Customer: ${order.customerName ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)
                Text("Services: $serviceNames", style = MaterialTheme.typography.bodyMedium)
                Text("Total Items: ${order.orderItems.sumOf { it.itemQuantity ?: 0 }}", style = MaterialTheme.typography.bodyMedium)
                Text("Total Price: Rp ${order.totalPrice ?: 0.0}", style = MaterialTheme.typography.bodyMedium)
                val formattedDueDate = order.orderDueDate?.toDate()?.let {
                    SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm", Locale.getDefault()).format(it)
                } ?: "No due date"
                Text("Due Date: $formattedDueDate", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Order")
                    }
                }
            }
        }
    }
}

// --- PREVIEW BARU DITAMBAHKAN DI SINI ---
@Preview(showBackground = true, widthDp = 960, heightDp = 500)
@Composable
fun ManageOrderScreenPreview() {
    // 1. Buat data sampel untuk setiap kolom
    val sampleQueueOrders = listOf(
        Orders(orderId = "1", customerName = "Budi", status = "On Queue", orderItems = listOf(OrderItem(itemQuantity = 3))),
        Orders(orderId = "2", customerName = "Citra", status = "On Queue", orderItems = listOf(OrderItem(itemQuantity = 1)))
    )
    val sampleProcessOrders = listOf(
        Orders(orderId = "3", customerName = "Dewi", status = "On Process", orderItems = listOf(OrderItem(itemQuantity = 5)))
    )
    val sampleDoneOrders = listOf(
        Orders(orderId = "4", customerName = "Eka", status = "Done", orderItems = listOf(OrderItem(itemQuantity = 2)))
    )

    // 2. Buat state UI untuk preview
    val previewState = ManageOrderUiState(
        isLoading = false,
        ordersOnQueue = sampleQueueOrders,
        ordersOnProcess = sampleProcessOrders,
        ordersDone = sampleDoneOrders
    )

    // 3. Panggil ManageOrderScreen dengan data sampel
    MaterialTheme {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
                )
            )
        ) {
            ManageOrderScreen(
                uiState = previewState,
                onDrop = { _, _ -> },
                onOrderClick = {},
                onDismissDialog = {},
                onDeleteOrder = {}
            )
        }
    }
}
