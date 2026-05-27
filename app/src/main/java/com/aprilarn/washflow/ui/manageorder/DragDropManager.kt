package com.aprilarn.washflow.ui.manageorder

import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.Orders

// KONSTANTA UI
val borderRadius = RoundedCornerShape(24.dp)
val borderColor = Color.White

// DATA CLASS UNTUK MENYIMPAN INFORMASI TARGET DROP
internal class DropTarget(
    val id: String,
    val bounds: Rect,
    val onDrop: (String) -> Unit
)

// STATE MANAGEMENT
internal class DragDropState<T> {
    var isDragging: Boolean by mutableStateOf(false)
    var isFinishing: Boolean by mutableStateOf(false) // State baru untuk animasi drop
    var itemData: T? by mutableStateOf(null)
    var fingerPosition: Offset by mutableStateOf(Offset.Zero)
    val dropTargets = mutableStateListOf<DropTarget>()
    var dragStartOffsetInItem: Offset by mutableStateOf(Offset.Zero)
    var draggedItemSize: IntSize by mutableStateOf(IntSize.Zero)

    fun startDrag(data: T, position: Offset, size: IntSize, offsetInItem: Offset) {
        itemData = data
        fingerPosition = position
        draggedItemSize = size
        dragStartOffsetInItem = offsetInItem
        isDragging = true
        isFinishing = false
    }

    fun stopDrag() {
        isDragging = false
        isFinishing = true // Mulai fase finishing (reverse bounce)
    }

    fun clear() {
        isDragging = false
        isFinishing = false
        itemData = null
        fingerPosition = Offset.Zero
        dragStartOffsetInItem = Offset.Zero
        draggedItemSize = IntSize.Zero
    }
}

@Composable
internal fun <T> rememberDragDropState(): DragDropState<T> {
    return remember { DragDropState() }
}

internal val LocalDragDropState = compositionLocalOf { DragDropState<Orders>() }

/**
 * Data class untuk membungkus nilai animasi drag-and-drop.
 */
data class DragDropTransition(
    val scale: Float,
    val alpha: Float
)

/**
 * Composable function untuk mengelola transisi animasi selama drag-and-drop.
 */
@Composable
internal fun <T> rememberDragDropTransition(state: DragDropState<T>): DragDropTransition {
    val scale by animateFloatAsState(
        targetValue = if (state.isDragging) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dragScale"
    )

    val dragAlpha by animateFloatAsState(
        targetValue = if (state.isDragging) 1f else 0f,
        animationSpec = if (state.isDragging) snap() else tween(300),
        label = "dragAlpha",
        finishedListener = {
            // Jika animasi fade-out selesai dan tidak sedang dragging, bersihkan state
            if (it == 0f && !state.isDragging && state.isFinishing) {
                state.clear()
            }
        }
    )

    return DragDropTransition(scale, dragAlpha)
}

/**
 * Composable function untuk mengelola alpha konten item yang sedang di-drag.
 * Digunakan agar item yang asli "menghilang" saat sedang di-drag.
 */
@Composable
internal fun rememberDragContentAlpha(isCurrentlyDragged: Boolean, isDragging: Boolean): State<Float> {
    return animateFloatAsState(
        targetValue = if (isCurrentlyDragged) 0f else 1f,
        animationSpec = if (isDragging) snap() else tween(300),
        label = "contentAlpha"
    )
}
