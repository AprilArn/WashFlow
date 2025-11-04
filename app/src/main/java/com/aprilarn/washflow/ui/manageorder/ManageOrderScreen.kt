package com.aprilarn.washflow.ui.manage_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.components.DragDropContainer
import com.aprilarn.washflow.ui.components.OrderStatusColumn
import com.aprilarn.washflow.ui.theme.GrayBlue
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
                .fillMaxSize(),
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
                uiState = uiState,
                onDismiss = onDismissDialog,
                onDelete = { onDeleteOrder(order.orderId) }
            )
        }
    }
}

@Composable
fun OrderDetailDialog(
    order: Orders,
    uiState: ManageOrderUiState,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val customer = remember(order.customerId, uiState.customers) {
        uiState.customers.find { it.customerId == order.customerId }
    }
    val groupedItemsByService = remember(order.orderItems, uiState.services) {
        order.orderItems.groupBy { orderItem ->
            uiState.services.find { it.serviceId == orderItem.serviceId }
        }.filterKeys { it != null } as Map<Services, List<OrderItem>>
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Row(
            modifier = Modifier
                .width(1000.dp)
                .height(600.dp) // Beri tinggi tetap agar tidak melebihi layar
        ) {
            // Gunakan weight untuk membagi ruang
            LeftDetailPanel(
                modifier = Modifier.weight(6f),
                order = order,
                customer = customer,
                services = groupedItemsByService.keys.toList(),
                onCancel = onDismiss,
                onDelete = onDelete
            )
            Spacer(Modifier.width(16.dp))
            RightDetailPanel(
                modifier = Modifier.weight(4f),
                groupedItems = groupedItemsByService,
                totalPrice = order.totalPrice
            )
        }
    }
}

@Composable
private fun LeftDetailPanel(
    modifier: Modifier = Modifier,
    order: Orders,
    customer: Customers?,
    services: List<Services>,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    // Panel kiri menggunakan Box sebagai dasar
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy())
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Spacer akan mendorong tombol ke bawah
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                InfoRow("Nama Pelanggan", order.customerName ?: "N/A")
                InfoRow("No Telp/WhatsApp", customer?.contact ?: "N/A")
                InfoRow("Tanggal Order Dibuat", SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(order.orderDate.toDate()))
                InfoRow("Batas Waktu", order.orderDueDate?.let { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(it.toDate()) } ?: "N/A")
                InfoRow("ID Order", order.orderId)
                InfoRow("Status", order.status ?: "N/A")
                InfoRow("Layanan", services.joinToString(" + ") { it.serviceName })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(
                    onClick = onCancel
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun RightDetailPanel(
    modifier: Modifier = Modifier,
    groupedItems: Map<Services, List<OrderItem>>,
    totalPrice: Double?
) {
    // Panel kanan menggunakan Box sebagai dasar
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
            ) {
                groupedItems.forEach { (service, items) ->
                    item {
                        Column(modifier = Modifier.padding(bottom = 24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = service.serviceName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = GrayBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${items.sumOf { it.itemQuantity ?: 0 }} item",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            items.forEach { orderItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .weight(5f),
                                        text = "• ${orderItem.itemName} (${orderItem.itemPrice} x ${orderItem.itemQuantity})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        modifier = Modifier
                                            .weight(2f),
                                        text = "Rp. ${orderItem.subtotal}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrayBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 36.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total harga:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Rp. ${totalPrice ?: 0.0}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = GrayBlue
            )
        )
    }
}

// --- PREVIEW BARU DITAMBAHKAN DI SINI ---
@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
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

@Preview
@Composable
fun LeftDetailPanelPreview() {
    val sampleOrder = Orders(
        orderId = "123",
        customerId = "cust1",
        customerName = "John Doe",
        orderDate = com.google.firebase.Timestamp.now(),
        orderDueDate = com.google.firebase.Timestamp.now(),
        status = "On Queue",
        orderItems = listOf(
            OrderItem(itemId = "item1", itemName = "Shirt", itemPrice = 5000.0, serviceId = "serv1", itemQuantity = 2,
                subtotal = 10000.0),
            OrderItem(itemId = "item2", itemName = "Pants", itemPrice = 7000.0, serviceId = "serv2", itemQuantity = 1,
                subtotal = 7000.0)
        ),
        totalPrice = 17000.0
    )
    val sampleCustomer = Customers(
        customerId = "cust1",
        name = "John Doe",
        contact = "08123456789"
    )
    val sampleServices = listOf(
        Services(serviceId = "serv1", serviceName = "Laundry"),
        Services(serviceId = "serv2", serviceName = "Dry Cleaning")
    )

    MaterialTheme {
        LeftDetailPanel(
            order = sampleOrder,
            customer = sampleCustomer,
            services = sampleServices,
            onCancel = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
fun RightDetailPanelPreview() {
    val sampleGroupedItems = mapOf(
        Services(serviceId = "serv1", serviceName = "Laundry") to listOf(
            OrderItem(itemId = "item1", itemName = "Shirt", itemPrice = 5000.0, serviceId = "serv1", itemQuantity = 2,
                subtotal = 10000.0),
            OrderItem(itemId = "item3", itemName = "Jacket", itemPrice = 15000.0, serviceId = "serv1", itemQuantity = 1,
                subtotal = 15000.0)
        ),
        Services(serviceId = "serv2", serviceName = "Dry Cleaning") to listOf(
            OrderItem(itemId = "item2", itemName = "Pants", itemPrice = 7000.0, serviceId = "serv2", itemQuantity = 1,
                subtotal = 7000.0)
        )
    )

    MaterialTheme {
        RightDetailPanel(
            groupedItems = sampleGroupedItems,
            totalPrice = 32000.0
        )
    }
}

@Preview
@Composable
fun OrderDetailDialogPreview() {
    val sampleOrder = Orders(
        orderId = "123",
        customerId = "cust1",
        customerName = "John Doe",
        orderDate = com.google.firebase.Timestamp.now(),
        orderDueDate = com.google.firebase.Timestamp.now(),
        status = "On Queue",
        orderItems = listOf(
            OrderItem(itemId = "item1", itemName = "Shirt", itemPrice = 5000.0, serviceId = "serv1", itemQuantity = 2,
                subtotal = 10000.0),
            OrderItem(itemId = "item2", itemName = "Pants", itemPrice = 7000.0, serviceId = "serv2", itemQuantity = 1,
                subtotal = 7000.0)
        ),
        totalPrice = 17000.0
    )
    val sampleCustomer = Customers(
        customerId = "cust1",
        name = "John Doe",
        contact = "08123456789"
    )
    val sampleServices = listOf(
        Services(serviceId = "serv1", serviceName = "Laundry"),
        Services(serviceId = "serv2", serviceName = "Dry Cleaning")
    )
    val sampleUiState = ManageOrderUiState(
        isLoading = false,
        customers = listOf(sampleCustomer),
        services = sampleServices
    )

    MaterialTheme {
        OrderDetailDialog(
            order = sampleOrder,
            uiState = sampleUiState,
            onDismiss = {},
            onDelete = {}
        )
    }
}
