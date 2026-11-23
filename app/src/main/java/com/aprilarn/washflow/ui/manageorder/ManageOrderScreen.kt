package com.aprilarn.washflow.ui.manage_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.components.HapticSwitch
import com.aprilarn.washflow.ui.manageorder.DragDropContainer
import com.aprilarn.washflow.ui.manageorder.OrderStatusColumn
import com.aprilarn.washflow.ui.orders.ReceiptDashedDivider
import com.aprilarn.washflow.ui.orders.ReceiptServiceSection
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelBlueTrack
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelGreenMedium
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseText
import com.aprilarn.washflow.ui.theme.PastelSlateBg
import com.aprilarn.washflow.ui.theme.PastelSwitchOffTrack
import com.aprilarn.washflow.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ManageOrderScreen(
    uiState: ManageOrderUiState,
    onDrop: (orderId: String, newStatus: String) -> Unit,
    onOrderClick: (Orders) -> Unit,
    onTogglePayment: (String, Boolean) -> Unit,
    onTogglePickup: (String, Boolean) -> Unit,
    onDismissDialog: () -> Unit,
    onDeleteOrder: (String) -> Unit,
    onEditOrder: (String) -> Unit
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
                onTogglePayment = onTogglePayment,
                onTogglePickup = onTogglePickup,
                onDismiss = onDismissDialog,
                onDelete = { onDeleteOrder(order.orderId) },
                onEditOrder = {
                    onDismissDialog()
                    onEditOrder(order.orderId)
                }
            )
        }
    }
}

@Composable
fun OrderDetailDialog(
    order: Orders,
    uiState: ManageOrderUiState,
    onTogglePayment: (String, Boolean) -> Unit,
    onTogglePickup: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEditOrder: () -> Unit
) {
    val customer = remember(order.customerId, uiState.customers) {
        uiState.customers.find { it.customerId == order.customerId }
    }
    val groupedItemsByService = remember(order.orderItems, uiState.services) {
        order.orderItems.groupBy { orderItem ->
            uiState.services.find { it.serviceId == orderItem.serviceId }
        }.filterKeys { it != null } as Map<Services, List<OrderItem>>
    }

    // --- STATE UNTUK KONFIRMASI DELETE ---
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // --- LOGIKA PEMILIHAN DIALOG ---
    if (showDeleteConfirmation) {
        // Tampilkan Dialog Konfirmasi (Tombol Tahan 2 Detik)
        DeleteConfirmationDialog(
            itemName = "Order milik ${order.customerName ?: "Pelanggan"}",
            onConfirm = {
                onDelete() // Hapus data sebenarnya
                showDeleteConfirmation = false
                onDismiss() // Tutup dialog detail
            },
            onDismiss = {
                showDeleteConfirmation = false // Kembali ke detail order
            }
        )
    } else {
        // Tampilkan Detail Order seperti biasa
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .width(1000.dp)
                    .height(600.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LeftDetailPanel(
                        modifier = Modifier.weight(6f),
                        order = order,
                        customer = customer,
                        onTogglePayment = onTogglePayment,
                        onTogglePickup = onTogglePickup,
                        onCancel = onDismiss,
                        onDelete = {
                            // Ubah state untuk memicu dialog konfirmasi
                            showDeleteConfirmation = true
                        },
                        onEditOrder = onEditOrder
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
    }
}

@Composable
private fun LeftDetailPanel(
    modifier: Modifier = Modifier,
    order: Orders,
    customer: Customers?,
    onTogglePayment: (String, Boolean) -> Unit,
    onTogglePickup: (String, Boolean) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onEditOrder: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header: Judul + Status Badge + Tombol Edit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
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
                            .height(30.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = "Detail Order",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GrayBlue
                        )
                    )
                    OrderStatusChip(status = order.status ?: "On Queue")
                }

                Surface(
                    onClick = onEditOrder,
                    shape = RoundedCornerShape(10.dp),
                    color = PastelNeutralBg,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Order",
                            tint = GrayBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GrayBlue
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Hero Profil Pelanggan + ID Order
                item {
                    CustomerProfileCard(
                        customerName = order.customerName ?: "Pelanggan",
                        contact = customer?.contact,
                        orderId = order.orderId
                    )
                }

                // 2. Grid Waktu Ringkas (3 Kolom Sejajar)
                item {
                    OrderTimelineRow(
                        orderDate = order.orderDate,
                        dueDate = order.orderDueDate,
                        finishDate = order.orderFinishDate
                    )
                }

                // 3. Toggle Pembayaran
                item {
                    PaymentStatusRow(
                        isPaid = order.alreadyPaid,
                        paidDate = order.orderPaidDate,
                        onToggle = { isPaid -> onTogglePayment(order.orderId, isPaid) }
                    )
                }

                // 4. Toggle Pengambilan
                item {
                    PickupStatusRow(
                        isPickedUp = order.alreadyPickedUp,
                        pickupDate = order.orderPickupDate,
                        orderStatus = order.status ?: "On Queue",
                        onToggle = { isPickedUp -> onTogglePickup(order.orderId, isPickedUp) }
                    )
                }
            }

            // Footer Aksi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Tutup",
                        color = GrayBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelRoseBg,
                        contentColor = PastelRoseText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Order", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun OrderStatusChip(status: String) {
    val (bgColor, textColor, label) = when {
        status.equals("Done", ignoreCase = true) -> Triple(PastelGreenBg, PastelGreen, "Selesai")
        status.equals("On Process", ignoreCase = true) -> Triple(PastelBlueBg, PastelBlue, "Diproses")
        else -> Triple(PastelAmberBg, PastelAmber, "Dalam Antrian")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

@Composable
private fun CustomerProfileCard(
    customerName: String,
    contact: String?,
    orderId: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PastelNeutralBg
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = PastelBlueIconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = PastelBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MainFontBlack
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null,
                        tint = Gray,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = contact?.takeIf { it.isNotBlank() } ?: "Tidak ada kontak",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Gray
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Text(
                    text = "#$orderId",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GrayBlue
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun OrderTimelineRow(
    orderDate: com.google.firebase.Timestamp,
    dueDate: com.google.firebase.Timestamp?,
    finishDate: com.google.firebase.Timestamp?
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val orderDateObj = orderDate.toDate()
    val dueDateObj = dueDate?.toDate()
    val finishDateObj = finishDate?.toDate()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TimelineCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.CalendarToday,
            label = "Tanggal Masuk",
            dateText = dateFormat.format(orderDateObj),
            timeText = timeFormat.format(orderDateObj)
        )
        TimelineCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Timer,
            label = "Batas Waktu",
            dateText = dueDateObj?.let { dateFormat.format(it) } ?: "-",
            timeText = dueDateObj?.let { timeFormat.format(it) } ?: "Belum diatur"
        )
        TimelineCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.TaskAlt,
            label = "Waktu Selesai",
            dateText = finishDateObj?.let { dateFormat.format(it) } ?: "Belum selesai",
            timeText = finishDateObj?.let { timeFormat.format(it) } ?: "-"
        )
    }
}

@Composable
private fun TimelineCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    dateText: String,
    timeText: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PastelNeutralBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GrayBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Gray
                    ),
                    maxLines = 1
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MainFontBlack
                    ),
                    maxLines = 1
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PastelNeutralText
                    ),
                    maxLines = 1
                )
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
    val serviceSections = remember(groupedItems) {
        groupedItems.entries.map { (service, items) -> service.serviceName to items }
    }
    val totalItemTypes = remember(groupedItems) {
        groupedItems.values.sumOf { it.size }
    }
    val totalQty = remember(groupedItems) {
        groupedItems.values.flatten().sumOf { it.itemQuantity ?: 0 }
    }

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // 1. Header Nota Pesanan (senada dengan OrderPreviewPanel di Create Order)
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
                            text = "Nota Pesanan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = GrayBlue
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Rincian item per layanan",
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
                            text = "$totalItemTypes item",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PastelBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReceiptDashedDivider(color = PastelBlueBg)

            // 2. Daftar Item Nota Dikelompokkan per Kategori Layanan
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = serviceSections,
                    key = { (serviceName, _) -> serviceName }
                ) { (serviceName, itemsInService) ->
                    ReceiptServiceSection(
                        serviceName = serviceName,
                        items = itemsInService
                    )
                }
            }

            ReceiptDashedDivider(color = PastelBlueBg)

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Footer Total Nota
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Pembayaran",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = if (totalQty > 0) "$totalQty pcs" else "0 pcs",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(totalPrice),
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

@Composable
private fun PaymentStatusRow(
    isPaid: Boolean,
    paidDate: com.google.firebase.Timestamp?,
    onToggle: (Boolean) -> Unit
) {
    val backgroundColor = if (isPaid) PastelBlueBg else PastelAmberBg
    val contentColor = if (isPaid) PastelBlue else PastelAmber

    val dateText = if (isPaid && paidDate != null) {
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        format.format(paidDate.toDate())
    } else null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = contentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Payments,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Status Pembayaran",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isPaid) "Lunas" else "Belum Dibayar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    )
                    if (dateText != null) {
                        Text(
                            text = "• $dateText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            HapticSwitch(
                checked = isPaid,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PastelBlueTrack,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = PastelSwitchOffTrack,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun PickupStatusRow(
    isPickedUp: Boolean,
    pickupDate: com.google.firebase.Timestamp?,
    orderStatus: String,
    onToggle: (Boolean) -> Unit
) {
    val isDone = orderStatus.equals("Done", ignoreCase = true)
    val displayState = if (isDone) isPickedUp else false

    val bgColor = if (!isDone) PastelNeutralBg
    else if (displayState) PastelGreenBg
    else PastelAmberBg
    val contentColor = if (!isDone) PastelNeutralText
    else if (displayState) PastelGreen
    else PastelAmber
    val icon = if (displayState) Icons.Rounded.CheckCircle else Icons.Rounded.Storefront

    val dateText = if (displayState && pickupDate != null) {
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        format.format(pickupDate.toDate())
    } else null

    val statusTitle = if (!isDone) "Belum bisa diambil"
    else if (displayState) "Sudah Diambil"
    else "Belum Diambil"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(contentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Status Pengambilan",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    )
                    if (dateText != null) {
                        Text(
                            text = "• $dateText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            HapticSwitch(
                checked = displayState,
                onCheckedChange = {
                    if (isDone) {
                        onToggle(it)
                    }
                },
                enabled = isDone,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PastelGreenMedium,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = PastelSwitchOffTrack,
                    uncheckedBorderColor = Color.Transparent,
                    disabledCheckedTrackColor = Color(0xFFE2E8F0),
                    disabledUncheckedTrackColor = Color(0xFFE2E8F0),
                    disabledCheckedThumbColor = Color(0xFFCBD5E1),
                    disabledUncheckedThumbColor = Color(0xFFCBD5E1)
                )
            )
        }
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
                onTogglePayment = { _, _ -> },
                onTogglePickup = { _, _ -> },
                onDismissDialog = {},
                onDeleteOrder = {},
                onEditOrder = {}
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
    MaterialTheme {
        LeftDetailPanel(
            order = sampleOrder,
            customer = sampleCustomer,
            onTogglePayment = { _, _ -> },
            onTogglePickup = { _, _ -> },
            onCancel = {},
            onDelete = {},
            onEditOrder = {}
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
            onTogglePayment = { _, _ -> },
            onTogglePickup = { _, _ -> },
            onDismiss = {},
            onDelete = {},
            onEditOrder = {}
        )
    }
}
