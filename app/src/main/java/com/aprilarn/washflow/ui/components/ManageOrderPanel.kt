package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.aprilarn.washflow.data.model.Orders
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

// CONTAINER UTAMA
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

// KOLOM TARGET
@Composable
fun OrderStatusColumn(
    modifier: Modifier = Modifier,
    title: String,
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

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlighted) Color.LightGray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.25f))
            .padding(8.dp)
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$title (${orders.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp),
            color = GrayBlue
        )
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

// ITEM YANG BISA DI-DRAG
@Composable
fun DraggableOrderCard(order: Orders) {
    val dragDropState = LocalDragDropState.current
    var startPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        OrderCardContent(order = order)
    }
}

// KONTEN VISUAL KARTU
@Composable
fun OrderCardContent(order: Orders) {
    val formattedDate = remember(order.orderDate) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(order.orderDate.toDate())
    }
    val totalQuantity = order.orderItems.sumOf { it.itemQuantity ?: 0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = order.customerName ?: "N/A",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayBlue
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = GrayBlue
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$totalQuantity items",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayBlue
        )
    }
}