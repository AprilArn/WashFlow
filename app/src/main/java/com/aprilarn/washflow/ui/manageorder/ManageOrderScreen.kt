//// com/aprilarn/washflow/ui/manage_order/ManageOrderScreen.kt
//package com.aprilarn.washflow.ui.manage_order
//
//import android.util.Log
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.aprilarn.washflow.ui.components.DragDropContainer
//import com.aprilarn.washflow.ui.components.OrderStatusColumn
//
//@Composable
//fun ManageOrderScreen(
//    viewModel: ManageOrderViewModel // Dapatkan dari dependency injection
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    LaunchedEffect(uiState.errorMessage) {
//        uiState.errorMessage?.let {
//            snackbarHostState.showSnackbar(it)
//            viewModel.onErrorMessageShown()
//        }
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
//    ) { paddingValues ->
//        if (uiState.isLoading) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else {
//            DragDropContainer(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    // Column untuk "On Queue"
//                    OrderStatusColumn(
//                        modifier = Modifier.weight(1f),
//                        title = "On Queue",
//                        orders = uiState.ordersOnQueue,
//                        onDrop = { orderId ->
//                            // LOG #3: Untuk mengecek apakah lambda onDrop sampai ke Screen
//                            Log.d("DragDropDebug", "ManageOrderScreen - onDrop triggered for 'On Queue'. Calling ViewModel.")
//                            viewModel.changeOrderStatus(orderId, "On Queue")
//                        }
//                    )
//
//                    // Column untuk "On Process"
//                    OrderStatusColumn(
//                        modifier = Modifier.weight(1f),
//                        title = "On Process",
//                        orders = uiState.ordersOnProcess,
//                        onDrop = { orderId ->
//                            // LOG #3: Untuk mengecekan apakah lambda onDrop sampai ke Screen
//                            Log.d("DragDropDebug", "ManageOrderScreen - onDrop triggered for 'On Process'. Calling ViewModel.")
//                            viewModel.changeOrderStatus(orderId, "On Process")
//                        }
//                    )
//
//                    // Column untuk "Done"
//                    OrderStatusColumn(
//                        modifier = Modifier.weight(1f),
//                        title = "Done",
//                        orders = uiState.ordersDone,
//                        onDrop = { orderId ->
//                            // LOG #3: Untuk mengecek apakah lambda onDrop sampai ke Screen
//                            Log.d("DragDropDebug", "ManageOrderScreen - onDrop triggered for 'Done'. Calling ViewModel.")
//                            viewModel.changeOrderStatus(orderId, "Done")
//                        }
//                    )
//                }
//            }
//        }
//    }
//}

package com.aprilarn.washflow.ui.manage_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.components.DragDropContainer
import com.aprilarn.washflow.ui.components.OrderStatusColumn

@Composable
fun ManageOrderScreen(
    // --- PERUBAHAN DI SINI ---
    uiState: ManageOrderUiState,
    onDrop: (orderId: String, newStatus: String) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        DragDropContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
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
                    onDrop = { orderId -> onDrop(orderId, "On Queue") }
                )

                // Column untuk "On Process"
                OrderStatusColumn(
                    modifier = Modifier.weight(1f),
                    title = "On Process",
                    subTitle = "Sedang diproses",
                    orders = uiState.ordersOnProcess,
                    onDrop = { orderId -> onDrop(orderId, "On Process") }
                )

                // Column untuk "Done"
                OrderStatusColumn(
                    modifier = Modifier.weight(1f),
                    title = "Done",
                    subTitle = "Selesai",
                    orders = uiState.ordersDone,
                    onDrop = { orderId -> onDrop(orderId, "Done") }
                )
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
                onDrop = { _, _ -> }, // Tidak melakukan apa-apa di preview
            )
        }
    }
}
