package com.aprilarn.washflow.ui.phone.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.ui.components.NoDeadlineConfirmationDialog
import com.aprilarn.washflow.ui.orders.OrderCustomerInputPanel
import com.aprilarn.washflow.ui.orders.OrderPanel
import com.aprilarn.washflow.ui.orders.OrderPreviewPanel
import com.aprilarn.washflow.ui.orders.OrdersUiState
import com.aprilarn.washflow.ui.orders.OrdersViewModel
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.utils.CurrencyUtils

/**
 * Tampilan Kasir / Orders khusus HP biasa (Batang / Portrait).
 * Menyusun Input Pelanggan & Deadline di atas, Katalog Layanan/Barang di tengah,
 * dan Sticky Cart Bar di bawah yang membuka Preview Nota dalam ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneOrdersScreen(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    var showPreviewSheet by rememberSaveable { mutableStateOf(false) }
    val totalItemTypes = uiState.selectedItems.size
    val totalQty = uiState.selectedItems.values.sumOf { it.itemQuantity ?: 0 }
    val totalPrice = uiState.selectedItems.values.sumOf { it.subtotal ?: 0.0 }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Panel Input Pelanggan & Deadline
        OrderCustomerInputPanel(
            uiState = uiState,
            viewModel = viewModel
        )

        // 2. Panel Katalog Layanan & Item (mengisi ruang utama)
        OrderPanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            uiState = uiState,
            viewModel = viewModel
        )

        // 3. Sticky Bar Ringkasan Nota di Bawah
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPreviewSheet = true },
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelBlueSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = "Preview Nota",
                            tint = PastelBlue
                        )
                    }
                    Column {
                        Text(
                            text = if (totalItemTypes > 0) "$totalItemTypes jenis ($totalQty pcs)" else "Belum ada item dipilih",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = CurrencyUtils.formatRupiahWithSymbol(totalPrice),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                Button(
                    onClick = { showPreviewSheet = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lihat Nota",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showPreviewSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPreviewSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderPreviewPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    uiState = uiState
                )
                Button(
                    onClick = {
                        showPreviewSheet = false
                        viewModel.createOrder()
                    },
                    enabled = !uiState.isCreatingOrder && uiState.selectedCustomer != null && uiState.selectedItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                ) {
                    Text(
                        text = if (uiState.editingOrderId != null) "Simpan Perubahan Order" else "Buat Order Sekarang",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    uiState.itemForQuantityInput?.let { item ->
        PhoneQuantityInputDialog(
            item = item,
            onDismiss = { viewModel.onDismissQuantityDialog() },
            onConfirm = { quantity -> viewModel.onQuantityConfirmed(quantity) }
        )
    }

    if (uiState.showNoDeadlineWarning) {
        NoDeadlineConfirmationDialog(
            onConfirm = { viewModel.proceedCreateOrder() },
            onDismiss = { viewModel.dismissNoDeadlineWarning() }
        )
    }
}

@Composable
private fun PhoneQuantityInputDialog(
    item: Items,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantityText by rememberSaveable(item.itemId) { mutableStateOf("1") }
    val currentQty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val subtotal = item.itemPrice * currentQty

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Masukkan Jumlah Barang",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = PastelNeutralText,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${CurrencyUtils.formatRupiahWithSymbol(item.itemPrice)} / unit",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                HorizontalDivider(color = PastelBlueBg, thickness = 1.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val btnShape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(btnShape)
                            .background(PastelBlueSoftBg)
                            .border(1.dp, PastelBlueBg, btnShape)
                            .clickable {
                                val next = (currentQty - 1).coerceAtLeast(1)
                                quantityText = next.toString()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Kurangi",
                            tint = PastelBlue
                        )
                    }

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = GrayBlue
                        ),
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GrayBlue,
                            unfocusedTextColor = GrayBlue,
                            focusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.45f),
                            unfocusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.25f),
                            focusedBorderColor = PastelBlue,
                            unfocusedBorderColor = PastelBlueBg,
                            focusedLabelColor = PastelBlue,
                            unfocusedLabelColor = PastelNeutralText,
                            cursorColor = PastelBlue
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(btnShape)
                            .background(PastelBlueSoftBg)
                            .border(1.dp, PastelBlueBg, btnShape)
                            .clickable {
                                val next = currentQty + 1
                                quantityText = next.toString()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Tambah",
                            tint = PastelBlue
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelBlueSoftBg.copy(alpha = 0.65f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PastelNeutralText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = CurrencyUtils.formatRupiahWithSymbol(subtotal),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = GrayBlue, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(currentQty) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                    ) {
                        Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
