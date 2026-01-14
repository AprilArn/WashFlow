package com.aprilarn.washflow.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.ui.components.ColumnConfig
import com.aprilarn.washflow.ui.components.DataTablePanel
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.theme.GrayBlue

@Composable
fun ItemsScreen (
    uiState: ItemsUiState,
    onAddItemClick: (String, String, Double) -> Unit,
    onEditItemClick: (Items) -> Unit,
    onDeleteItemClick: (Items) -> Unit,
    onItemSelected: (Items) -> Unit,
    onDismissDialog: () -> Unit
) {
    var newItemService by remember { mutableStateOf("") }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf(0.0) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val itemCount = uiState.items.size
    val filteredItems = uiState.items.filter {
        it.itemName.contains(newItemName, ignoreCase = true)
                || it.serviceId.contains(newItemName, ignoreCase = true)
                || it.itemPrice.toString().contains(newItemName, ignoreCase = true)
    }

    val borderRadius = RoundedCornerShape(24.dp)
    val borderColor = Color.White

    // Dialog untuk Edit/Delete
    uiState.selectedItem?.let { itemToEdit ->
        // State untuk field di dalam dialog edit
        var editedService by remember { mutableStateOf(itemToEdit.serviceId) }
        var editedName by remember { mutableStateOf(itemToEdit.itemName) }
        var editedPrice by remember { mutableStateOf(itemToEdit.itemPrice) }

        // LaunchedEffect untuk mereset state jika item yang dipilih berganti
        LaunchedEffect(itemToEdit) {
            editedService = itemToEdit.serviceId
            editedName = itemToEdit.itemName
            editedPrice = itemToEdit.itemPrice
            showDeleteConfirmation = false
        }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                itemName = itemToEdit.itemName,
                onConfirm = {
                    onDeleteItemClick(itemToEdit)
                    showDeleteConfirmation = false
                    onDismissDialog()
                },
                onDismiss = {
                    showDeleteConfirmation = false
                }
            )
        } else {
            Dialog(onDismissRequest = onDismissDialog) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Edit ${itemToEdit.itemName}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        ServiceDropdown(
                            services = uiState.services,
                            selectedServiceId = editedService,
                            onServiceSelected = { editedService = it.serviceId },
                            label = "Service",
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Item Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = editedPrice.toString(),
                            onValueChange = { editedPrice = it.toDoubleOrNull() ?: 0.0 },
                            label = { Text("Item Price") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Tombol Delete
                            OutlinedButton(
                                onClick = {
                                    // Pemicu konfirmasi delete
                                    showDeleteConfirmation = true
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = SolidColor(Color(0xFFF44336))
                                )
                            ) {
                                Icon(Icons.Default.Delete, "Delete")
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                            // Tombol Done
                            Button(
                                onClick = {
                                    val updatedItem = itemToEdit.copy(
                                        serviceId = editedService,
                                        itemName = editedName,
                                        itemPrice = editedPrice
                                    )
                                    onEditItemClick(updatedItem)
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GrayBlue)
                            ) {
                                Icon(Icons.Default.Check, "Done")
                                Spacer(Modifier.width(4.dp))
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel: DataTablePanel
        Box(modifier = Modifier.weight(2f)) {
            val itemColumns = listOf(
                ColumnConfig<Items>(
                    header = "Service ID",
                    weight = 0.3f,
                    content = { item -> Text(item.serviceId, color = GrayBlue) }
                ),
                ColumnConfig<Items>(
                    header = "Item Name",
                    weight = 0.5f,
                    content = { item -> Text(item.itemName, color = GrayBlue) }
                ),
                ColumnConfig<Items>(
                    header = "Item Price",
                    weight = 0.3f,
                    content = { item -> Text(item.itemPrice.toString(), color = GrayBlue) }
                ),
                ColumnConfig<Items>(
                    header = "Edit", // Ubah header menjadi "Actions"
                    weight = 0.1f,
                    content = { item ->
                        // Hapus tombol dari sini karena aksi sekarang via onRowClick
                        // Bisa diganti dengan ikon atau indikator lain jika perlu
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Item",
                            tint = GrayBlue.copy(alpha = 0.7f)
                        )
                    }
                )
            )

            DataTablePanel(
                title = "Item",
                itemCount = itemCount,
                searchQuery = newItemName,
                onSearchQueryChange = { newItemName = it },
                searchPlaceholder = "Search by Service/Name/Price",
                columns = itemColumns,
                data = filteredItems, // Gunakan data yang sudah difilter
                isLoading = uiState.isLoading,
                onRowClick = { item -> onItemSelected(item) }
            )
        }

        // --- PANEL KANAN: DIBUAT SECARA MANUAL ---
        Box(modifier = Modifier.weight(1f)) {
            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = borderRadius
                    ),
                shape = borderRadius,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(vertical=24.dp, horizontal=22.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "Add New Item",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = GrayBlue
                        )
                    )
                    Text(
                        text = "Tambah item baru",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayBlue
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ServiceDropdown(
                        services = uiState.services,
                        selectedServiceId = newItemService,
                        onServiceSelected = { newItemService = it.serviceId },
                        label = "Service",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Nama Barang") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = if (newItemPrice == 0.0) "" else newItemPrice.toString(),
                        onValueChange = { newItemPrice = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Harga Barang") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            onAddItemClick(newItemService, newItemName, newItemPrice)
                            newItemService = ""
                            newItemName = ""
                            newItemPrice = 0.0
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrayBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Icon")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 600)
@Composable
fun ItemsScreenPreview() {
    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFB9E9FF), Color(0xFFFFD6BF))
            )
        )
    ) {
        // --- PERBAIKI BAGIAN INI ---
        val previewState = ItemsUiState(
            items = listOf(
                // Gunakan named arguments agar tidak salah urutan
                Items(itemId = "item_1", serviceId = "L-01", itemName = "Dress", itemPrice = 15000.0),
                Items(itemId = "item_2", serviceId = "L-02", itemName = "Baju", itemPrice = 12000.0),
                Items(itemId = "item_3", serviceId = "D-01", itemName = "Jas", itemPrice = 25000.0)
            ),
            // Perbaiki juga di sini
            selectedItem = Items(itemId = "item_1", serviceId = "L-01", itemName = "Dress", itemPrice = 15000.0)
        )
        ItemsScreen(
            uiState = previewState,
            onAddItemClick = { _, _, _ -> },
            onEditItemClick = {},
            onDeleteItemClick = {},
            onItemSelected = {},
            onDismissDialog = {}
        )
    }
}