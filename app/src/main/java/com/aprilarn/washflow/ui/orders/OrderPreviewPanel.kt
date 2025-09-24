package com.aprilarn.washflow.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderPreviewPanel(uiState: OrdersUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Preview", style = MaterialTheme.typography.titleMedium)
            Text("On Queue", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            val selectedServices = uiState.selectedItems.values
                .mapNotNull { item -> uiState.services.find { it.serviceId == item.serviceId } }
                .distinct()
                .joinToString(" + ") { it.serviceName }

            Text(selectedServices, style = MaterialTheme.typography.bodyLarge)
            Text(uiState.selectedCustomer?.name ?: "No Customer", style = MaterialTheme.typography.titleLarge)
            Text("${uiState.selectedItems.size} items", style = MaterialTheme.typography.bodyMedium)

            val formattedDate = uiState.dueDate?.toDate()?.let {
                SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(it)
            } ?: "No due date"
            Text(formattedDate, style = MaterialTheme.typography.bodySmall)
        }
    }
}