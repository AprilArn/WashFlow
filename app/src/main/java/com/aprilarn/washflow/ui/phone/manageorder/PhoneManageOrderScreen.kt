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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
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
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.domain.model.OrderQueueStatus
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.manage_order.ManageOrderUiState
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseText
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
        PhoneOrderDetailBottomSheet(
            order = order,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = PastelGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (order.alreadyPickedUp) "Sudah Diambil" else "Siap Diambil",
                            fontSize = 11.sp,
                            color = PastelGreen,
                            fontWeight = FontWeight.Bold
                        )
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: "Pelanggan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Status: ${order.status ?: "On Queue"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

            HorizontalDivider(color = PastelBlueSoftBg)

            // Toggle Status Pembayaran & Pengambilan
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PastelBlueSoftBg.copy(alpha = 0.55f)
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
                            colors = SwitchDefaults.colors(checkedTrackColor = PastelGreen)
                        )
                    }

                    if (order.status == "Done") {
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
                                colors = SwitchDefaults.colors(checkedTrackColor = PastelBlue)
                            )
                        }
                    }
                }
            }

            // Daftar Rincian Item
            Text(
                text = "Rincian Barang (${order.orderItems.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Bold
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.orderItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelBlueSoftBg.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.itemName ?: "Item",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = GrayBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "${item.itemQuantity ?: 1} x ${CurrencyUtils.formatRupiahWithSymbol(item.itemPrice ?: 0.0)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = PastelNeutralText)
                            )
                        }
                        Text(
                            text = CurrencyUtils.formatRupiahWithSymbol(item.subtotal ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PastelRoseBg.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Tagihan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice ?: 0.0),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PastelBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
