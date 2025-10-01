package com.aprilarn.washflow.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.Services

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDropdown(
    services: List<Services>,
    selectedServiceId: String,
    onServiceSelected: (Services) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Menemukan objek Service yang terpilih berdasarkan ID untuk ditampilkan di TextField
    val selectedService = services.find { it.serviceId == selectedServiceId }
    val displayText = selectedService?.let { "${it.serviceId} | ${it.serviceName}" } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            services.forEach { service ->
                DropdownMenuItem(
                    text = { Text("${service.serviceId} | ${service.serviceName}") },
                    onClick = {
                        onServiceSelected(service)
                        expanded = false
                    }
                )
            }
        }
    }
}