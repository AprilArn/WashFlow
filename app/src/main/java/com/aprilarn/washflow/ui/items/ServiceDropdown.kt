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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
    // State untuk menyimpan ukuran lebar & tinggi TextField
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val selectedService = services.find { it.serviceId == selectedServiceId }
    val displayText = selectedService?.let { "${it.serviceId} | ${it.serviceName}" } ?: ""

    Column(modifier = modifier.fillMaxWidth()) {

        // Box pembungkus TextField
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // MENGUKUR UKURAN: Saat TextField digambar, simpan ukurannya ke variabel
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
        ) {
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
                    unfocusedContainerColor = Color.White.copy(alpha = 0f) // Transparan saat tidak fokus
                )
            )

            // Lapisan transparan untuk area klik
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded }
            )
        }

        // Tampilan Dropdown Melayang (Hover) menggunakan Popup
        if (expanded && services.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopStart,
                // MENDORONG POPUP KE BAWAH SEJAUH TINGGI TEXTFIELD
                offset = IntOffset(x = 0, y = textFieldSize.height.toInt()),
                // focusable = true memastikan jika user klik di luar kotak, dropdown akan tertutup
                properties = PopupProperties(focusable = true),
                onDismissRequest = { expanded = false }
            ) {
                Surface(
                    modifier = Modifier
                        // Terapkan ukuran lebar yang sama persis dengan TextField di atasnya
                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                        .padding(top = 8.dp) // Memberikan jarak (gap) visual antara teks field dan menu pop-up
                        .heightIn(max = 250.dp),
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
                                        expanded = false
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MainFontBlack
                            )

                            if (index < services.size - 1) {
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}