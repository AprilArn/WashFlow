package com.aprilarn.washflow.ui.manageorder

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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

    // Menggunakan helper animation dari DragDropManager
    val transition = rememberDragDropTransition(state)

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

            if (state.isDragging || state.isFinishing) {
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

                                // Terapkan animasi scale, alpha, dan shadow agar terlihat "melayang"
                                scaleX = transition.scale
                                scaleY = transition.scale
                                alpha = transition.alpha
                                shadowElevation = 8.dp.toPx()
                                shape = borderRadius
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
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var centerX by remember { mutableStateOf(0f) }

    val isHighlighted by remember(dragDropState.isDragging, dragDropState.fingerPosition) {
        derivedStateOf {
            dragDropState.isDragging &&
                    dragDropState.dropTargets.find { it.id == title }?.bounds?.contains(dragDropState.fingerPosition) == true
        }
    }

    // Animasi Tilt: Kolom miring ke arah finger jika tidak sedang di-highlight
    val targetRotation = if (dragDropState.isDragging && !isHighlighted) {
        val distance = dragDropState.fingerPosition.x - centerX
        (distance / 150f).coerceIn(-10f, 10f) // Max tilt 10 derajat
    } else {
        0f
    }

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "columnTilt"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted) GrayBlue.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.25f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "columnHighlightColor"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isHighlighted) GrayBlue.copy(alpha = 0.8f) else borderColor.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "columnBorderColor"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "columnBorderWidth"
    )

    val columnScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.01f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "columnScale"
    )

    // Efek getar saat kolom disorot
    LaunchedEffect(isHighlighted) {
        if (isHighlighted) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Efek untuk mendaftarkan & menghapus diri sebagai target drop
    DisposableEffect(key1 = title) {
        var dropTarget: DropTarget? = null
        onDispose {
            dropTarget?.let { dragDropState.dropTargets.remove(it) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = animatedRotation
                scaleX = columnScale
                scaleY = columnScale
                cameraDistance = 12f * density.density
            }
            .background(backgroundColor, shape = borderRadius)
            .border(
                width = animatedBorderWidth,
                color = animatedBorderColor,
                shape = borderRadius
            )
            .onGloballyPositioned {
                val windowPosition = it.positionInWindow()
                val bounds = Rect(windowPosition, it.size.toSize())
                centerX = windowPosition.x + it.size.width / 2f

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
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = subTitle,
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
                        modifier = Modifier.animateItem(),
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
    modifier: Modifier = Modifier,
    order: Orders,
    services: List<Services>,
    onClick: () -> Unit
) {
    val dragDropState = LocalDragDropState.current
    val haptic = LocalHapticFeedback.current
    var startPosition by remember { mutableStateOf(Offset.Zero) }
    var itemSize by remember { mutableStateOf(IntSize.Zero) } // <- State untuk menyimpan ukuran kartu ini

    val isCurrentlyDragged = (dragDropState.isDragging || dragDropState.isFinishing) && dragDropState.itemData?.orderId == order.orderId
    
    // Menggunakan helper animation dari DragDropManager
    val contentAlpha by rememberDragContentAlpha(
        isCurrentlyDragged = isCurrentlyDragged,
        isDragging = dragDropState.isDragging
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemSize = it }
            .onGloballyPositioned {
                startPosition = it.positionInWindow()
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress (
                    onDragStart = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragDropState.startDrag(
                            data = order,
                            position = startPosition + offset,
                            size = itemSize,
                            offsetInItem = offset
                        )
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
                alpha = contentAlpha
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
                width = 2.dp,
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
                text = totalItemTypes.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }

        // --- TAG PAID ---
        if (order.alreadyPaid) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp),
//                    .border(
//                        width = 1.dp,
//                        color = borderColor,
//                        shape = RoundedCornerShape(6.dp)
//                    ),
                shape = RoundedCornerShape(6.dp),
                // shadowElevation = 8.dp,
                color = Color(0xFF4EB0FF) // Biru Pastel Sangat Muda
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White, // Biru Tua
                            fontSize = 10.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
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
        alreadyPaid = true,
        orderDate = Timestamp.now(),
        orderDueDate = Timestamp(Date(System.currentTimeMillis() + 86400000)), // Besok
        orderItems = listOf(
            OrderItem(itemId = "item_1", serviceId = "L-01", itemQuantity = 3),
            OrderItem(itemId = "item_4", serviceId = "D-01", itemQuantity = 1)
        )
    )

    val isHighlighted = true // Force true for preview

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

    

    val isHighlighted = true // Force true for preview

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