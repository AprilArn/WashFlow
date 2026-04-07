package com.aprilarn.washflow.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.theme.MainFontBlack

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
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MainFontBlack,
                unfocusedTextColor = Color.Gray,
                cursorColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            services.forEachIndexed { index, service ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${service.serviceId} | ${service.serviceName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MainFontBlack
                        )
                    },
                    onClick = {
                        onServiceSelected(service)
                        expanded = false
                    }
                )

                // Tambahkan Garis Pemisah (Divider) persis seperti di Maps
                // Jangan tambahkan divider di item paling akhir
                if (index < services.size - 1) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}