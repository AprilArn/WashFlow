package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import com.aprilarn.washflow.data.model.Items

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
            .fillMaxSize(),
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
