package com.aprilarn.washflow.ui.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Items
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.WashFlowTheme
import com.aprilarn.washflow.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    viewModel: OrdersViewModel
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- PANEL KIRI ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Panel
            OrderCustomerInputPanel(uiState = uiState, viewModel = viewModel)
            // Preview Panel (Nota Pesanan yang mengisi sisa tinggi secara sinkron dengan panel kanan)
            OrderPreviewPanel(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                onRemoveItem = { viewModel.removeSelectedItem(it) }
            )
        }

        // --- PANEL KANAN ---
        OrderPanel(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            uiState = uiState,
            viewModel = viewModel
        )
    }

    uiState.itemForQuantityInput?.let { item ->
        QuantityInputDialog(
            item = item,
            onDismiss = { viewModel.onDismissQuantityDialog() },
            onConfirm = { quantity -> viewModel.onQuantityConfirmed(quantity) }
        )
    }

    if (uiState.showNoDeadlineWarning) {
        com.aprilarn.washflow.ui.core.components.NoDeadlineConfirmationDialog(
            onConfirm = { viewModel.proceedCreateOrder() },
            onDismiss = { viewModel.dismissNoDeadlineWarning() }
        )
    }
}

@Composable
private fun QuantityInputDialog(
    item: Items,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        QuantityInputDialogContent(
            item = item,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun QuantityInputDialogContent(
    item: Items,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantityText by androidx.compose.runtime.saveable.rememberSaveable(item.itemId) { mutableStateOf("1") }
    val currentQty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val subtotal = item.itemPrice * currentQty

    Surface(
        modifier = Modifier
            .width(360.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.orders_qty_dialog_title),
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
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.orders_price_per_unit,
                        CurrencyUtils.formatRupiahWithSymbol(item.itemPrice)
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PastelNeutralText,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            HorizontalDivider(color = PastelBlueBg, thickness = 1.dp)

            // Stepper (-) [Input] (+)
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
                        contentDescription = stringResource(R.string.orders_qty_decrease_cd),
                        tint = PastelBlue
                    )
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                    label = { Text(stringResource(R.string.orders_qty_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    ),
                    modifier = Modifier.width(104.dp),
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
                        contentDescription = stringResource(R.string.orders_qty_increase_cd),
                        tint = PastelBlue
                    )
                }
            }

            // Estimasi Subtotal
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
                        text = stringResource(R.string.orders_subtotal),
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
                    Text(stringResource(R.string.common_cancel), color = GrayBlue, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onConfirm(currentQty) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelBlue)
                ) {
                    Text(stringResource(R.string.common_save), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantityInputDialogPreview() {
    WashFlowTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            QuantityInputDialogContent(
                item = Items(
                    itemId = "1",
                    itemName = "Jas Pria",
                    itemPrice = 50000.0,
                    serviceId = "s1"
                ),
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}
