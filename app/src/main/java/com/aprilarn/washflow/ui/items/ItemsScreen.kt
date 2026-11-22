package com.aprilarn.washflow.ui.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.ui.components.ColumnConfig
import com.aprilarn.washflow.ui.components.DataTablePanel
import com.aprilarn.washflow.ui.components.DeleteConfirmationDialog
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRose
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseLight
import com.aprilarn.washflow.utils.CurrencyUtils

@Composable
fun ItemsScreen(
    uiState: ItemsUiState,
    onAddItemClick: (String, String, Double) -> Unit,
    onEditItemClick: (Items) -> Unit,
    onDeleteItemClick: (Items) -> Unit,
    onItemSelected: (Items) -> Unit,
    onDismissDialog: () -> Unit
) {
    var newItemService by rememberSaveable { mutableStateOf("") }
    var newItemName by rememberSaveable { mutableStateOf("") }
    var newItemPrice by rememberSaveable { mutableStateOf("") }

    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val itemCount = uiState.items.size
    val filteredItems = uiState.items.filter {
        it.itemName.contains(newItemName, ignoreCase = true)
                || it.serviceId.contains(newItemName, ignoreCase = true)
                || it.itemPrice.toString().contains(newItemName, ignoreCase = true)
    }

    val borderRadius = RoundedCornerShape(24.dp)
    val itemSoftBg = Color(0xFFFCF7F0)

    // Dialog untuk Edit/Delete
    uiState.selectedItem?.let { itemToEdit ->
        var editedService by rememberSaveable(itemToEdit.itemId) { mutableStateOf(itemToEdit.serviceId) }
        var editedName by rememberSaveable(itemToEdit.itemId) { mutableStateOf(itemToEdit.itemName) }
        var editedPrice by rememberSaveable(itemToEdit.itemId) { mutableStateOf(itemToEdit.itemPrice.toString().replace(".0", "")) }

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
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Editorial Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(30.dp)
                                    .background(PastelAmber, RoundedCornerShape(2.dp))
                            )
                            Column {
                                Text(
                                    text = "Edit ${itemToEdit.itemName}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color = GrayBlue
                                    )
                                )
                                Text(
                                    text = "Perbarui layanan, nama, atau harga barang",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PastelNeutralText,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = PastelNeutralBg, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = GrayBlue,
                                    unfocusedTextColor = GrayBlue,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = itemSoftBg,
                                    focusedBorderColor = PastelAmber,
                                    unfocusedBorderColor = PastelAmberBg,
                                    focusedLabelColor = PastelAmber,
                                    unfocusedLabelColor = PastelNeutralText,
                                    cursorColor = PastelAmber
                                )
                            )
                            OutlinedTextField(
                                value = editedPrice,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        editedPrice = newValue
                                    }
                                },
                                label = { Text("Item Price") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = GrayBlue,
                                    unfocusedTextColor = GrayBlue,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = itemSoftBg,
                                    focusedBorderColor = PastelAmber,
                                    unfocusedBorderColor = PastelAmberBg,
                                    focusedLabelColor = PastelAmber,
                                    unfocusedLabelColor = PastelNeutralText,
                                    cursorColor = PastelAmber
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = PastelRoseBg,
                                    contentColor = PastelRose
                                ),
                                border = BorderStroke(1.dp, PastelRoseLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Delete", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val updatedItem = itemToEdit.copy(
                                        serviceId = editedService,
                                        itemName = editedName,
                                        itemPrice = editedPrice.toDoubleOrNull() ?: 0.0
                                    )
                                    onEditItemClick(updatedItem)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PastelAmber,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Done",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel: DataTablePanel
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            val itemColumns = listOf(
                ColumnConfig<Items>(
                    header = "Service ID",
                    weight = 0.28f,
                    content = { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelAmberBg
                        ) {
                            Text(
                                text = item.serviceId,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PastelAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                ),
                ColumnConfig<Items>(
                    header = "Item Name",
                    weight = 0.50f,
                    content = { item ->
                        Text(
                            text = item.itemName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                ),
                ColumnConfig<Items>(
                    header = "Item Price",
                    weight = 0.30f,
                    content = { item ->
                        Text(
                            text = CurrencyUtils.formatRupiah(item.itemPrice),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                ),
                ColumnConfig<Items>(
                    header = "Edit",
                    weight = 0.12f,
                    content = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(PastelAmberBg, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Item",
                                tint = PastelAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            )

            DataTablePanel(
                title = "Items",
                itemCount = itemCount,
                searchQuery = newItemName,
                onSearchQueryChange = { newItemName = it },
                searchPlaceholder = "Cari kode layanan, nama barang, atau harga...",
                columns = itemColumns,
                data = filteredItems,
                isLoading = uiState.isLoading,
                onRowClick = { item -> onItemSelected(item) }
            )
        }

        // Right Panel: Add New Item Surface
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = borderRadius,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    // Editorial Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(PastelAmber, RoundedCornerShape(2.dp))
                        )
                        Column {
                            Text(
                                text = "Add New Item",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = GrayBlue
                                )
                            )
                            Text(
                                text = "Tambah item baru",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PastelNeutralText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = PastelNeutralBg, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ServiceDropdown(
                            services = uiState.services,
                            selectedServiceId = newItemService,
                            onServiceSelected = { newItemService = it.serviceId },
                            label = "Service",
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text("Nama Barang") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GrayBlue,
                                unfocusedTextColor = GrayBlue,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = itemSoftBg,
                                focusedBorderColor = PastelAmber,
                                unfocusedBorderColor = PastelAmberBg,
                                focusedLabelColor = PastelAmber,
                                unfocusedLabelColor = PastelNeutralText,
                                cursorColor = PastelAmber
                            )
                        )
                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() }) {
                                    newItemPrice = newValue
                                }
                            },
                            label = { Text("Harga Barang") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GrayBlue,
                                unfocusedTextColor = GrayBlue,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = itemSoftBg,
                                focusedBorderColor = PastelAmber,
                                unfocusedBorderColor = PastelAmberBg,
                                focusedLabelColor = PastelAmber,
                                unfocusedLabelColor = PastelNeutralText,
                                cursorColor = PastelAmber
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onAddItemClick(
                                newItemService,
                                newItemName,
                                newItemPrice.toDoubleOrNull() ?: 0.0
                            )
                            newItemService = ""
                            newItemName = ""
                            newItemPrice = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PastelAmber,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Item",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
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
        val previewState = ItemsUiState(
            items = listOf(
                Items(itemId = "item_1", serviceId = "L-01", itemName = "Dress", itemPrice = 15000.0),
                Items(itemId = "item_2", serviceId = "L-02", itemName = "Baju", itemPrice = 12000.0),
                Items(itemId = "item_3", serviceId = "D-01", itemName = "Jas", itemPrice = 25000.0)
            ),
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
