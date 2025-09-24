package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.Items

@Composable
fun OrderPanel(
    modifier: Modifier = Modifier,
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    Card(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Service Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.services) { service ->
                    FilterChip(
                        selected = service.serviceId == uiState.activeServiceTabId,
                        onClick = { viewModel.onServiceTabSelected(service.serviceId) },
                        label = { Text(service.serviceName) }
                    )
                }
            }

            // Item List
            LazyColumn(
                modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val itemsForActiveService = uiState.items.filter {
                    it.serviceId == uiState.activeServiceTabId
                }
                items(itemsForActiveService, key = { it.itemId }) { item ->
                    ItemCheckRow(
                        item = item,
                        isChecked = uiState.selectedItems.containsKey(item.itemId),
                        onToggle = { viewModel.onItemToggled(item) }
                    )
                }
            }

            // Footer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${uiState.selectedItems.size} item selected", modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.createOrder() },
                    enabled = !uiState.isCreatingOrder
                ) {
                    if (uiState.isCreatingOrder) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Create Order")
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCheckRow(item: Items, isChecked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = isChecked, onCheckedChange = { onToggle() })
        Text(item.itemName, modifier = Modifier.weight(1f))
        Text("Rp ${item.itemPrice}")
    }
}