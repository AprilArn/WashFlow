// com/aprilarn/washflow/ui/items/ServiceDropdown.kt
package com.aprilarn.washflow.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.theme.MainFontBlack

@Composable
fun ServiceDropdown(
    services: List<Services>,
    selectedServiceId: String,
    onServiceSelected: (Services) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Menemukan objek Service yang terpilih berdasarkan ID
    val selectedService = services.find { it.serviceId == selectedServiceId }
    val displayText = selectedService?.let { "${it.serviceId} | ${it.serviceName}" } ?: ""

    // Menggunakan Column agar kotak dropdown (Surface) muncul tepat di bawah TextField
    Column(modifier = modifier.fillMaxWidth()) {

        // Box untuk menumpuk TextField dengan tombol klik transparan
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Icon",
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MainFontBlack,
                    unfocusedTextColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0f)
                )
            )

            // Lapisan transparan (Overlay) untuk menangkap klik di seluruh area TextField
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded }
            )
        }

        // Tampilan Dropdown (Surface + LazyColumn) - Persis seperti LocationSelectionPanel
        if (expanded && services.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp) // Beri jarak sedikit dari TextField
                    .heightIn(max = 250.dp), // Batasi tinggi maksimal agar bisa di-scroll
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                LazyColumn {
                    itemsIndexed(services) { index, service ->
                        Text(
                            text = "${service.serviceId} | ${service.serviceName}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onServiceSelected(service)
                                    expanded = false // Tutup dropdown otomatis saat dipilih
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MainFontBlack
                        )

                        // Garis Pemisah (Divider) transparan
                        if (index < services.size - 1) {
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}