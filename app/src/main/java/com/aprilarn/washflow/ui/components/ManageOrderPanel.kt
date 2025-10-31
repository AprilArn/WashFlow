package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.SoftRed
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DATA CLASS UNTUK MENYIMPAN INFORMASI TARGET DROP (diubah menjadi internal)
internal class DropTarget(
    val id: String,
    val bounds: Rect,
    val onDrop: (String) -> Unit
)

// STATE MANAGEMENT
internal class DragDropState<T> {
    var isDragging: Boolean by mutableStateOf(false)
    var itemData: T? by mutableStateOf(null)
    var fingerPosition: Offset by mutableStateOf(Offset.Zero)
    val dropTargets = mutableStateListOf<DropTarget>()
   var dragStartOffsetInItem: Offset by mutableStateOf(Offset.Zero)
    var draggedItemSize: IntSize by mutableStateOf(IntSize.Zero)
    fun stopDrag() {
        isDragging = false
        itemData = null
        fingerPosition = Offset.Zero
        dragStartOffsetInItem = Offset.Zero // Reset offset
        draggedItemSize = IntSize.Zero // Reset ukuran
    }
}

@Composable
internal fun <T> rememberDragDropState(): DragDropState<T> {
    return remember { DragDropState() }
}

internal val LocalDragDropState = compositionLocalOf { DragDropState<Orders>() }

val borderRadius = RoundedCornerShape(24.dp)
val borderColor = Color.White

// Main Container
@Composable
fun DragDropContainer(
    modifier: Modifier = Modifier,
    services: List<Services>,
    content: @Composable () -> Unit
) {
    val state = rememberDragDropState<Orders>()
    val density = LocalDensity.current
    var containerPositionInWindow by remember { mutableStateOf(Offset.Zero) }

    CompositionLocalProvider(LocalDragDropState provides state) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    // Catat posisi kontainer di layar
                    containerPositionInWindow = it.positionInWindow()
                }
        ) {
            content()

            if (state.isDragging) {
                state.itemData?.let { data ->
                    // Konversi ukuran dari pixel ke Dp
                    val draggedItemWidthDp = with(density) { state.draggedItemSize.width.toDp() }
                    val draggedItemHeightDp = with(density) { state.draggedItemSize.height.toDp() }

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                val localTouchPosition = state.fingerPosition - containerPositionInWindow
                                val topLeft = localTouchPosition - state.dragStartOffsetInItem
                                translationX = topLeft.x
                                translationY = topLeft.y
                            }
                            .size(width = draggedItemWidthDp, height = draggedItemHeightDp)
                    ) {
                        OrderCardContent(
                            order = data,
                            services = services,
                            alpha = 1f
                        )
                    }
                }
            }
        }
    }
}

// Status Panel
@Composable
fun OrderStatusColumn(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    orders: List<Orders>,
    services: List<Services>,
    onDrop: (orderId: String) -> Unit,
    onOrderClick: (Orders) -> Unit
) {
    val dragDropState = LocalDragDropState.current
    val currentOnDrop by rememberUpdatedState(onDrop)

    // Efek untuk mendaftarkan & menghapus diri sebagai target drop
    DisposableEffect(key1 = title) {
        var dropTarget: DropTarget? = null
        onDispose {
            dropTarget?.let { dragDropState.dropTargets.remove(it) }
        }
    }

    val isHighlighted by remember(dragDropState.isDragging, dragDropState.fingerPosition) {
        derivedStateOf {
            dragDropState.isDragging &&
                    dragDropState.dropTargets.find { it.id == title }?.bounds?.contains(dragDropState.fingerPosition) == true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isHighlighted) Color.LightGray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.25f), shape = borderRadius)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = borderRadius
            )
            .onGloballyPositioned {
                val windowPosition = it.positionInWindow()
                val bounds = Rect(windowPosition, it.size.toSize())

                // Hapus pendaftaran lama & daftarkan yang baru dengan bounds terbaru
                dragDropState.dropTargets.removeAll { t -> t.id == title }
                val newTarget = DropTarget(
                    id = title,
                    bounds = bounds,
                    onDrop = { orderId -> currentOnDrop(orderId) }
                )
                dragDropState.dropTargets.add(newTarget)
            },
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column (
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
                        text = "${title}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = "${subTitle}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayBlue
                    )
                }
                Text(
                    text = "${orders.size}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = GrayBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.orderId }) { order ->
                    DraggableOrderCard(
                        order = order,
                        services = services,
                        onClick = { onOrderClick(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableOrderCard(
    order: Orders,
    services: List<Services>,
    onClick: () -> Unit
) {
    val dragDropState = LocalDragDropState.current
    var startPosition by remember { mutableStateOf(Offset.Zero) }
    var itemSize by remember { mutableStateOf(IntSize.Zero) } // <- State untuk menyimpan ukuran kartu ini

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { itemSize = it }
            .onGloballyPositioned {
                startPosition = it.positionInWindow()
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress (
                    onDragStart = { offset ->
                        dragDropState.isDragging = true
                        dragDropState.itemData = order
                        dragDropState.fingerPosition = startPosition + offset
                        dragDropState.draggedItemSize = itemSize
                        dragDropState.dragStartOffsetInItem = offset
                    },
                    onDragEnd = {
                        dragDropState.itemData?.let { draggedItem ->
                            val target = dragDropState.dropTargets.find {
                                it.bounds.contains(dragDropState.fingerPosition)
                            }
                            if (target != null) {
                                target.onDrop(draggedItem.orderId)
                            }
                        }
                        dragDropState.stopDrag() },
                    onDragCancel = { dragDropState.stopDrag() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragDropState.fingerPosition += dragAmount
                    }
                )
            }
            .graphicsLayer {
                alpha = if (dragDropState.isDragging && dragDropState.itemData?.orderId == order.orderId) 0.0f else 1f
            }
            .clip(borderRadius)
            .clickable(onClick = onClick)
    ) {
        OrderCardContent(
            order = order,
            services = services
        )
    }
}


// Card Content UI
@Composable
fun OrderCardContent(
    order: Orders,
    services: List<Services>,
    alpha: Float = 0.1f // Nilai alpha default untuk kartu yang tidak sedang di-drag
) {
    val formattedOrderDate = remember(order.orderDate) {
        SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.getDefault()).format(order.orderDate.toDate())
    }
    val formattedDueDate = remember(order.orderDueDate) {
        order.orderDueDate?.toDate()?.let {
            SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.getDefault()).format(it)
        } ?: "No due date"
    }
    // val totalQuantity = order.orderItems.sumOf { it.itemQuantity ?: 0 }
    val totalItemTypes = order.orderItems.size

    val serviceNames = remember(order.orderItems, services) {
        order.orderItems
            .map { orderItem -> services.find { it.serviceId == orderItem.serviceId }?.serviceId } //serviceName jika ingin menggunakan nama
            .filterNotNull()
            .distinct()
            .joinToString(" + ")
    }

    Box(
        modifier = Modifier
            //.fillMaxWidth()
            .clip(borderRadius)
            .background(Color.White.copy(alpha = alpha), shape = borderRadius)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = borderRadius
            ),
    ) {
        Row(
            modifier = Modifier
                //.fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = if (serviceNames.isEmpty()) "No services" else serviceNames, // Ganti dengan data sebenarnya
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Gray
                    )
                )
                Text(
                    text = order.customerName ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrayBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formattedOrderDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayBlue
                    )
                )
                Text(
                    text = formattedDueDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftRed
                    )
                )
            }
            Text(
                text = "${totalItemTypes}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
    }
}

// --- PRATINJAU UNTUK SATU KARTU PESANAN ---
@Preview(showBackground = true, name = "Order Card Preview")
@Composable
fun OrderCardContentPreview() {
    // Siapkan data sampel
    val sampleServices = listOf(
        Services(serviceId = "L-01", serviceName = "Laundry Satuan"),
        Services(serviceId = "D-01", serviceName = "Dry Clean")
    )
    val sampleOrder = Orders(
        orderId = "1",
        customerName = "Budi Santoso",
        orderDate = Timestamp.now(),
        orderDueDate = Timestamp(Date(System.currentTimeMillis() + 86400000)), // Besok
        orderItems = listOf(
            OrderItem(itemId = "item_1", serviceId = "L-01", itemQuantity = 3),
            OrderItem(itemId = "item_4", serviceId = "D-01", itemQuantity = 1)
        )
    )

    MaterialTheme {
        Box(modifier = Modifier.padding(8.dp)) {
            // Panggil komponen konten dengan data sampel
            OrderCardContent(
                order = sampleOrder,
                services = sampleServices
            )
        }
    }
}

// --- PRATINJAU UNTUK SATU KOLOM STATUS ---
@Preview(showBackground = true, name = "Order Status Column Preview", widthDp = 360, heightDp = 700)
@Composable
fun OrderStatusColumnPreview() {
    // Siapkan data sampel
    val sampleServices = listOf(
        Services(serviceId = "L-01", serviceName = "Laundry Satuan")
    )
    val sampleOrders = listOf(
        Orders(orderId = "1", customerName = "Budi", status = "On Queue", orderItems = listOf(OrderItem(serviceId = "L-01", itemQuantity = 3))),
        Orders(orderId = "2", customerName = "Citra", status = "On Queue", orderItems = listOf(OrderItem(serviceId = "L-01", itemQuantity = 1)))
    )

    MaterialTheme {
        // DragDropContainer dibutuhkan karena komponen di dalamnya menggunakan state dari sana
        DragDropContainer(
            modifier = Modifier.padding(8.dp),
            services = sampleServices
        ) {
            OrderStatusColumn(
                modifier = Modifier.fillMaxSize(),
                title = "On Queue",
                subTitle = "Order menunggu",
                orders = sampleOrders,
                services = sampleServices,
                onDrop = { },
                onOrderClick = { }
            )
        }
    }
}