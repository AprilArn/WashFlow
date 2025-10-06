package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun OrderPanel(
    modifier: Modifier = Modifier,
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    val borderRadius = RoundedCornerShape(24.dp)
    val borderColor = Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.25f), shape = borderRadius)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = borderRadius
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Services",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = GrayBlue,
                        //fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(24.dp))
                // Service Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.services) { service ->
                        FilterChip(
                            modifier = Modifier
                                .padding(vertical = 2.dp),
                            selected = service.serviceId == uiState.activeServiceTabId,
                            onClick = { viewModel.onServiceTabSelected(service.serviceId) },
                            label = {
                                Text(
                                    text = service.serviceName,
                                    color = GrayBlue
                                )
                            }
                        )
                    }
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
                        onClick = { viewModel.handleItemClick(item) }
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
private fun ItemCheckRow(
    item: Items,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = { null })
        Text(
            text = item.itemName,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = GrayBlue,
                fontSize = 18.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Rp ${item.itemPrice}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = GrayBlue,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.size(12.dp))
    }
}