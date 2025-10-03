// com/aprilarn/washflow/ui/manage_order/ManageOrderScreen.kt
package com.aprilarn.washflow.ui.manage_order

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.ui.components.DragDropContainer
import com.aprilarn.washflow.ui.components.OrderStatusColumn

@Composable
fun ManageOrderScreen(
    viewModel: ManageOrderViewModel // Dapatkan dari dependency injection
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DragDropContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Column untuk "On Queue"
                    OrderStatusColumn(
                        modifier = Modifier.weight(1f),
                        title = "On Queue",
                        orders = uiState.ordersOnQueue,
                        onDrop = { orderId ->
                            // LOG #3: Untuk mengecek apakah lambda onDrop sampai ke Screen
                            Log.d("DragDropDebug", "ManageOrderScreen - onDrop triggered for 'On Queue'. Calling ViewModel.")
                            viewModel.changeOrderStatus(orderId, "On Queue")
                        }
                    )

                    // Column untuk "On Process"
                    OrderStatusColumn(
                        modifier = Modifier.weight(1f),
                        title = "On Process",
                        orders = uiState.ordersOnProcess,
                        onDrop = { orderId ->
                            // LOG #3: Untuk mengecekan apakah lambda onDrop sampai ke Screen
                            Log.d("DragDropDebug", "ManageOrderScreen - onDrop triggered for 'On Process'. Calling ViewModel.")
                            viewModel.changeOrderStatus(orderId, "On Process")
                        }
                    )

                    // Column untuk "Done"
                    OrderStatusColumn(
                        modifier = Modifier.weight(1f),
                        title = "Done",
                        orders = uiState.ordersDone,
                        onDrop = { orderId ->
                            // LOG #3: Untuk mengecek apakah lambda onDrop sampai ke Screen
                            Log.d("DragDropDebug", "ManageOrderScreen - onDrop triggered for 'Done'. Calling ViewModel.")
                            viewModel.changeOrderStatus(orderId, "Done")
                        }
                    )
                }
            }
        }
    }
}
