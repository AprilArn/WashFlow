package com.aprilarn.washflow.ui.phone.manageorder

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.domain.model.OrderQueueStatus
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.manage_order.ManageOrderUiState
import com.aprilarn.washflow.ui.orders.ReceiptDashedDivider
import com.aprilarn.washflow.ui.orders.ReceiptServiceSection
import com.aprilarn.washflow.ui.orders.groupOrderItemsByService
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRoseText
import com.aprilarn.washflow.ui.theme.PastelSwitchOffTrack
import com.aprilarn.washflow.utils.CurrencyUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tampilan Antrian & Proses Cucian (Manage Order) khusus HP biasa (Batang / Portrait).
 * Menggunakan TabRow + HorizontalPager untuk 3 status (On Queue, On Process, Done)
 * serta tombol cepat pemindah status di setiap kartu order & ModalBottomSheet detail order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneManageOrderScreen(
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
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val stages = OrderQueueStatus.entries
    val pagerState = rememberPagerState(pageCount = { stages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. TabRow 3 Status dengan Badge Jumlah Order
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = GrayBlue,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(3.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(PastelBlue)
                        )
                    }
                }
            ) {
                stages.forEachIndexed { index, stage ->
                    val count = when (stage) {
                        OrderQueueStatus.ON_QUEUE -> uiState.ordersOnQueue.size
                        OrderQueueStatus.ON_PROCESS -> uiState.ordersOnProcess.size
                        OrderQueueStatus.DONE -> uiState.ordersDone.size
                    }
                    val isSelected = pagerState.currentPage == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stage.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) PastelBlueBg else PastelBlueSoftBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelBlue
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        // 2. HorizontalPager Daftar Order per Status
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val currentStage = stages[page]
            val ordersForStage = when (currentStage) {
                OrderQueueStatus.ON_QUEUE -> uiState.ordersOnQueue
                OrderQueueStatus.ON_PROCESS -> uiState.ordersOnProcess
                OrderQueueStatus.DONE -> uiState.ordersDone
            }

            if (ordersForStage.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.88f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tidak ada order di tahap ${currentStage.label}",
                            color = PastelNeutralText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(ordersForStage, key = { it.orderId }) { order ->
                        PhoneOrderKanbanCard(
                            order = order,
                            currentStage = currentStage,
                            onClick = { onOrderClick(order) },
                            onMoveStatus = { newStatus -> onDrop(order.orderId, newStatus) }
                        )
                    }
                }
            }
        }
    }

    // 3. ModalBottomSheet Detail Order Responsif untuk HP
    uiState.selectedOrderForDetail?.let { order ->
        val customer = uiState.customers.find { it.customerId == order.customerId }
        PhoneOrderDetailBottomSheet(
            order = order,
            customer = customer,
            services = uiState.services,
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

@Composable
private fun PhoneOrderKanbanCard(
    order: Orders,
    currentStage: OrderQueueStatus,
    onClick: () -> Unit,
    onMoveStatus: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val totalQty = order.orderItems.sumOf { it.itemQuantity ?: 0 }

    val prevStage = when (currentStage) {
        OrderQueueStatus.ON_QUEUE -> null
        OrderQueueStatus.ON_PROCESS -> OrderQueueStatus.ON_QUEUE
        OrderQueueStatus.DONE -> OrderQueueStatus.ON_PROCESS
    }
    val nextStage = when (currentStage) {
        OrderQueueStatus.ON_QUEUE -> OrderQueueStatus.ON_PROCESS
        OrderQueueStatus.ON_PROCESS -> OrderQueueStatus.DONE
        OrderQueueStatus.DONE -> null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: "Pelanggan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$totalQty item • ${CurrencyUtils.formatRupiahWithSymbol(order.totalPrice ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Badge Status Bayar
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (order.alreadyPaid) PastelGreenBg else PastelAmberBg
                ) {
                    Text(
                        text = if (order.alreadyPaid) "Lunas" else "Belum Bayar",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (order.alreadyPaid) PastelGreen else PastelAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            order.orderDueDate?.let { due ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = PastelNeutralText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Deadline: ${dateFormat.format(due.toDate())}",
                        style = MaterialTheme.typography.labelSmall.copy(color = PastelNeutralText)
                    )
                }
            }

            HorizontalDivider(color = PastelBlueSoftBg)

            // Tombol Aksi Cepat Pindah Status (pengganti Drag & Drop di layar HP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prevStage != null) {
                    OutlinedButton(
                        onClick = { onMoveStatus(prevStage.label) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = GrayBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = prevStage.label,
                            fontSize = 11.sp,
                            color = GrayBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (nextStage != null) {
                    Button(
                        onClick = { onMoveStatus(nextStage.label) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Ke ${nextStage.label}",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                    }
                } else {
                    val pickupBgColor = if (order.alreadyPickedUp) PastelGreenBg else PastelAmberBg
                    val pickupContentColor = if (order.alreadyPickedUp) PastelGreen else PastelAmber
                    val pickupIcon = if (order.alreadyPickedUp) {
                        Icons.Rounded.CheckCircle
                    } else {
                        Icons.Rounded.Storefront
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = pickupBgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = pickupIcon,
                                contentDescription = null,
                                tint = pickupContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (order.alreadyPickedUp) "Sudah Diambil" else "Siap Diambil",
                                fontSize = 11.sp,
                                color = pickupContentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneOrderDetailBottomSheet(
    order: Orders,
    customer: Customers?,
    services: List<Services>,
    onTogglePayment: (String, Boolean) -> Unit,
    onTogglePickup: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEditOrder: () -> Unit
) {
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            itemName = "Order milik ${order.customerName ?: "Pelanggan"}",
            onConfirm = {
                onDelete()
                showDeleteConfirmation = false
                onDismiss()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
        return
    }

    val groupedItemsByService = remember(order.orderItems, services) {
        groupOrderItemsByService(order.orderItems, services)
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDueDate = remember(order.orderDueDate) {
        order.orderDueDate?.toDate()?.let { dateFormat.format(it) } ?: "Tanpa batas waktu"
    }
    val totalQty = remember(order.orderItems) {
        order.orderItems.sumOf { it.itemQuantity ?: 0 }
    }
    val status = order.status ?: "On Queue"
    val (statusBg, statusColor, statusLabel) = when {
        status.equals("Done", ignoreCase = true) -> Triple(PastelGreenBg, PastelGreen, "Selesai")
        status.equals("On Process", ignoreCase = true) -> Triple(PastelBlueBg, PastelBlue, "Diproses")
        else -> Triple(PastelAmberBg, PastelAmber, "Dalam Antrian")
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Nota Pesanan (senada dengan OrderPreviewPanel)
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
                            .height(34.dp)
                            .background(PastelBlue, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Nota Pesanan",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = GrayBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBg
                            ) {
                                Text(
                                    text = statusLabel,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Order #${order.orderId.take(8)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEditOrder) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Order",
                            tint = PastelBlue
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Order",
                            tint = PastelRoseText
                        )
                    }
                }
            }

            // 2. Ringkasan Pelanggan & Batas Waktu (senada dengan header info di OrderPreviewPanel)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = order.customerName ?: "Pelanggan",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val contact = customer?.contact
                    if (!contact.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = PastelNeutralText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = contact,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PastelNeutralText,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
                            text = "Batas Waktu: $formattedDueDate",
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

            ReceiptDashedDivider(color = PastelBlueBg)

            // 3. Header & Daftar Rincian Item Nota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rincian Barang",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PastelBlueSoftBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = PastelBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${order.orderItems.size} item",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedItemsByService.forEach { (serviceName, itemsInService) ->
                    ReceiptServiceSection(
                        serviceName = serviceName,
                        items = itemsInService
                    )
                }
            }

            ReceiptDashedDivider(color = PastelBlueBg)

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
                            text = "Total Tagihan",
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
                        text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice ?: 0.0),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PastelBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            // 5. Toggle Status Pembayaran & Pengambilan
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sudah Dibayar (Lunas)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Switch(
                            checked = order.alreadyPaid,
                            onCheckedChange = { onTogglePayment(order.orderId, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PastelGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = PastelSwitchOffTrack,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    if (status.equals("Done", ignoreCase = true)) {
                        HorizontalDivider(color = PastelBlueBg.copy(alpha = 0.7f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sudah Diambil Pelanggan",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = GrayBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Switch(
                                checked = order.alreadyPickedUp,
                                onCheckedChange = { onTogglePickup(order.orderId, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PastelGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = PastelSwitchOffTrack,
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
