package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.OrderRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import com.google.firebase.Timestamp
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel // Kirim instance ViewModel untuk callback
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- PANEL KIRI ---
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Panel
            OrderCustomerInputPanel(uiState = uiState, viewModel = viewModel)
            // Preview Panel
            OrderPreviewPanel(uiState = uiState)
        }

        // --- PANEL KANAN ---
        OrderPanel(
            modifier = Modifier.weight(2f),
            uiState = uiState,
            viewModel = viewModel
        )
    }
    uiState.itemForQuantityInput?.let { item ->
        QuantityInputDialog(
            item = item,
            onDismiss = { viewModel.onDismissQuantityDialog() },
            onConfirm = { quantity -> viewModel.onQuantityConfirmed(quantity) }
        )
    }
}

@Composable
private fun QuantityInputDialog(
    item: Items,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enter Quantity for", style = MaterialTheme.typography.labelMedium)
                Text(item.itemName, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(quantityText.toIntOrNull() ?: 1)
                    }) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

//// --- PREVIEW BARU DITAMBAHKAN DI SINI ---
//@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
//@Composable
//fun OrdersScreenPreview() {
//    // 1. Siapkan data sampel untuk semua koleksi
//    val sampleCustomers = listOf(
//        Customers(customerId = "cust_1", name = "Raphael", contact = "081234567890"),
//        Customers(customerId = "cust_2", name = "Budi Santoso", contact = "087654321098")
//    )
//    val sampleServices = listOf(
//        Services(serviceId = "L-01", serviceName = "Laundry Satuan"),
//        Services(serviceId = "L-02", serviceName = "Laundry Kiloan"),
//        Services(serviceId = "D-01", serviceName = "Dry Clean")
//    )
//    val sampleItems = listOf(
//        Items(itemId = "item_1", serviceId = "L-01", itemName = "Kemeja", itemPrice = 15000.0),
//        Items(itemId = "item_2", serviceId = "L-01", itemName = "Celana Panjang", itemPrice = 20000.0),
//        Items(itemId = "item_3", serviceId = "L-02", itemName = "Pakaian Harian (kg)", itemPrice = 8000.0),
//        Items(itemId = "item_4", serviceId = "D-01", itemName = "Jas", itemPrice = 50000.0),
//        Items(itemId = "item_5", serviceId = "D-01", itemName = "Gaun Malam", itemPrice = 75000.0)
//    )
//
//    // 2. Buat state UI untuk preview, simulasikan interaksi pengguna
//    val previewState = OrdersUiState(
//        customers = sampleCustomers,
//        services = sampleServices,
//        items = sampleItems,
//        isLoading = false,
//        customerSearchQuery = "Raphael",
//        selectedCustomer = sampleCustomers.first(),
//        dueDate = Timestamp(Date(1758681600000L)), // Contoh tanggal di masa depan
//        activeServiceTabId = "D-01", // Tab "Dry Clean" aktif
//        selectedItems = mapOf( // Beberapa item sudah dipilih
//            "item_4" to sampleItems[3],
//            "item_1" to sampleItems[0]
//        )
//    )
//
//    // 3. Buat instance ViewModel palsu untuk memenuhi parameter
//    // (Tidak perlu implementasi nyata karena kita mengontrol state secara manual)
//    val dummyViewModel: OrdersViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return OrdersViewModel(
//                    CustomerRepository(),
//                    ServiceRepository(),
//                    ItemRepository(),
//                    OrderRepository()
//                ) as T
//            }
//        }
//    )
//
//    // 4. Panggil OrdersScreen dengan state dan ViewModel palsu
//    MaterialTheme {
//        Box(modifier = Modifier.fillMaxSize()) {
//            OrdersScreen(uiState = previewState, viewModel = dummyViewModel)
//        }
//    }
//}