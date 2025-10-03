package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import java.text.SimpleDateFormat
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
    var dragPosition: Offset by mutableStateOf(Offset.Zero)

    // Menyimpan daftar semua kolom yang bisa menjadi target
    val dropTargets = mutableStateListOf<DropTarget>()
}

@Composable
internal fun <T> rememberDragDropState(): DragDropState<T> {
    return remember { DragDropState() }
}

internal val LocalDragDropState = compositionLocalOf { DragDropState<Orders>() }
val borderRadius = RoundedCornerShape(24.dp)

// Main Container
@Composable
fun DragDropContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberDragDropState<Orders>()

    CompositionLocalProvider(LocalDragDropState provides state) {
        Box(modifier = modifier.fillMaxSize()) {
            content()

            if (state.isDragging && state.itemData != null) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = state.dragPosition.x
                        translationY = state.dragPosition.y
                    }
                ) {
                    OrderCardContent(order = state.itemData!!)
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
    onDrop: (orderId: String) -> Unit
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

    val isHighlighted by remember(dragDropState.isDragging, dragDropState.dragPosition) {
        derivedStateOf {
            dragDropState.isDragging &&
                    dragDropState.dropTargets.find { it.id == title }?.bounds?.contains(dragDropState.dragPosition) == true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isHighlighted) Color.LightGray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.25f), shape = borderRadius)
            .border(
                width = 1.dp,
                color = GrayBlue.copy(alpha = 0.8f),
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
                    DraggableOrderCard(order = order)
                }
            }
        }
    }
}

// Draggable Card
@Composable
fun DraggableOrderCard(order: Orders) {
    val dragDropState = LocalDragDropState.current
    var startPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(borderRadius)
            .border(
                width = 1.dp,
                color = GrayBlue.copy(alpha = 0.8f),
                shape = borderRadius
            )
            .onGloballyPositioned {
                startPosition = it.positionInWindow()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragDropState.itemData = order
                        dragDropState.isDragging = true
                        dragDropState.dragPosition = startPosition + offset
                    },
                    onDragEnd = {
                        dragDropState.itemData?.let { draggedItem ->
                            val target = dragDropState.dropTargets.find {
                                it.bounds.contains(dragDropState.dragPosition)
                            }
                            if (target != null) {
                                target.onDrop(draggedItem.orderId)
                            }
                        }
                        dragDropState.isDragging = false
                        dragDropState.itemData = null
                        dragDropState.dragPosition = Offset.Zero
                    },
                    onDragCancel = {
                        dragDropState.isDragging = false
                        dragDropState.itemData = null
                        dragDropState.dragPosition = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragDropState.dragPosition += dragAmount
                    }
                )
            }
            .graphicsLayer {
                alpha = if (dragDropState.isDragging && dragDropState.itemData?.orderId == order.orderId) 0.0f else 1f
            },
        shape = borderRadius,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        OrderCardContent(order = order)
    }
}

// Card Content UI
@Composable
fun OrderCardContent(order: Orders) {
    val formattedDate = remember(order.orderDate) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(order.orderDate.toDate())
    } // ubah ke duedate
    val totalQuantity = order.orderItems.sumOf { it.itemQuantity ?: 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(borderRadius)
            .border(
                width = 1.dp,
                color = GrayBlue.copy(alpha = 0.8f),
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
                    text = "selectedServices (ex: Laundry Kiloan + Dry Clean)", // Ganti dengan data sebenarnya
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
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayBlue
                    )
                )
            }
            Text(
                text = "${totalQuantity}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
    }
}

@Preview
@Composable
fun OrderCardContentPreview() {
    val sampleOrder = Orders(
        orderId = "1",
        customerName = "Budi",
        orderDate = com.google.firebase.Timestamp.now(),
        orderItems = listOf(
            com.aprilarn.washflow.data.model.OrderItem(itemQuantity = 3),
            com.aprilarn.washflow.data.model.OrderItem(itemQuantity = 2)
        )
    ) // Contoh data
    OrderCardContent(
        order = sampleOrder
    )
}

@Preview
@Composable
fun OrderStatusColumnPreview() {
    val sampleOrders = listOf(
        Orders(
            orderId = "1",
            customerName = "Budi",
            orderDate = com.google.firebase.Timestamp.now(),
            orderItems = listOf(
                com.aprilarn.washflow.data.model.OrderItem(itemQuantity = 3),
                com.aprilarn.washflow.data.model.OrderItem(itemQuantity = 2)
            )
        ),
        Orders(
            orderId = "2",
            customerName = "Citra",
            orderDate = com.google.firebase.Timestamp.now(),
            orderItems = listOf(
                com.aprilarn.washflow.data.model.OrderItem(itemQuantity = 1)
            )
        )
    )
    DragDropContainer(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OrderStatusColumn(
            modifier = Modifier.fillMaxSize(),
            title = "On Queue",
            subTitle = "Order menunggu",
            orders = sampleOrders,
            onDrop = { orderId -> /* Handle drop action */ }
        )
    }
}