package com.aprilarn.washflow.ui.phone.analytics

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.analytics.isDone
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.PastelAmber
import com.aprilarn.washflow.ui.theme.PastelAmberBg
import com.aprilarn.washflow.ui.theme.PastelBlue
import com.aprilarn.washflow.ui.theme.PastelBlueBg
import com.aprilarn.washflow.ui.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.theme.PastelGreen
import com.aprilarn.washflow.ui.theme.PastelGreenBg
import com.aprilarn.washflow.ui.theme.PastelGreenDark
import com.aprilarn.washflow.ui.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.theme.PastelNeutralText
import com.aprilarn.washflow.ui.theme.PastelRose
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseText
import com.aprilarn.washflow.ui.theme.PastelSlateBg
import com.aprilarn.washflow.utils.CurrencyUtils
import java.util.concurrent.TimeUnit

private enum class PhonePendingFilter(val label: String) {
    ALL("Semua"),
    UNPAID("Belum Lunas"),
    UNPICKED("Belum Diambil"),
    OVERDUE_30("Lama (> 30 hari)")
}

private sealed interface PhonePendingConfirm {
    data class MarkPaid(val order: Orders) : PhonePendingConfirm
    data class MarkPickedUp(val order: Orders) : PhonePendingConfirm
    data class SendWhatsApp(val order: Orders, val contact: String) : PhonePendingConfirm
}

/**
 * Tampilan Order Tertunda & Piutang khusus HP biasa (Portrait).
 * Menyusun kartu piutang secara hero full-width di atas 2 kartu ringkasan sejajar (Siap Diambil & >30 Hari)
 * agar semua teks & angka terbaca lega tanpa terpotong.
 */
@Composable
fun PhonePendingOrdersTab(
    activeOrders: List<Orders>,
    customers: List<Customers>,
    isLoading: Boolean,
    onTogglePayment: (orderId: String, isPaid: Boolean) -> Unit,
    onTogglePickup: (orderId: String, isPickedUp: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by rememberSaveable { mutableStateOf(PhonePendingFilter.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var pendingConfirm by remember { mutableStateOf<PhonePendingConfirm?>(null) }

    val nowMillis = remember(activeOrders) { System.currentTimeMillis() }
    val customerMap = remember(customers) { customers.associateBy { it.customerId } }

    val allPendingOrders = remember(activeOrders) {
        activeOrders
            .filter { order -> order.isDone() && (!order.alreadyPaid || !order.alreadyPickedUp) }
            .sortedBy { (it.orderFinishDate ?: it.orderDate).toDate().time }
    }

    val totalUnpaidOrders = remember(allPendingOrders) { allPendingOrders.filter { !it.alreadyPaid } }
    val totalUnpaidAmount = remember(totalUnpaidOrders) { totalUnpaidOrders.sumOf { it.totalPrice ?: 0.0 } }
    val readyUnpickedCount = remember(allPendingOrders) { allPendingOrders.count { !it.alreadyPickedUp } }
    val oldPendingCount = remember(allPendingOrders, nowMillis) {
        allPendingOrders.count { phoneOrderAgeDays(it, nowMillis) > 30 }
    }

    val filteredOrders = remember(allPendingOrders, selectedFilter, searchQuery, customerMap, nowMillis) {
        allPendingOrders.filter { order ->
            val matchesFilter = when (selectedFilter) {
                PhonePendingFilter.ALL -> true
                PhonePendingFilter.UNPAID -> !order.alreadyPaid
                PhonePendingFilter.UNPICKED -> !order.alreadyPickedUp
                PhonePendingFilter.OVERDUE_30 -> phoneOrderAgeDays(order, nowMillis) > 30
            }
            if (!matchesFilter) return@filter false

            if (searchQuery.isBlank()) return@filter true
            val customer = customerMap[order.customerId]
            val nameMatch = (order.customerName ?: "").contains(searchQuery, ignoreCase = true)
            val contactMatch = (customer?.contact ?: "").contains(searchQuery, ignoreCase = true)
            val itemsMatch = order.orderItems.any { (it.itemName ?: "").contains(searchQuery, ignoreCase = true) }
            nameMatch || contactMatch || itemsMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Editorial Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(34.dp)
                        .background(PastelBlue, RoundedCornerShape(2.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order Tertunda & Piutang",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "Order selesai yang belum dibayar atau diambil",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 2. Ringkasan Proporsional: Hero Full-Width (Total Piutang) + 2 Kartu Kompak (Siap Diambil & >30 Hari)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val hasUnpaid = totalUnpaidOrders.isNotEmpty()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = if (hasUnpaid) PastelAmberBg else PastelGreenSoftBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (hasUnpaid) PastelAmber.copy(alpha = 0.14f) else PastelGreenIconBg
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasUnpaid) Icons.Rounded.Payments else Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (hasUnpaid) PastelAmber else PastelGreen,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Total Piutang",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (hasUnpaid) PastelAmber else PastelGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = CurrencyUtils.formatRupiahWithSymbol(totalUnpaidAmount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (hasUnpaid) PastelAmber else PastelGreenDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (hasUnpaid) "${totalUnpaidOrders.size} order belum bayar" else "Semua order sudah lunas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = (if (hasUnpaid) PastelAmber else PastelGreen).copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PhonePendingCompactStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Storefront,
                        title = "Siap Diambil",
                        value = "$readyUnpickedCount Order",
                        subtitle = if (readyUnpickedCount > 0) "Menunggu diambil" else "Semua diambil",
                        containerColor = PastelBlueSoftBg,
                        iconBgColor = PastelBlueIconBg,
                        accentColor = PastelBlue,
                        valueColor = GrayBlue
                    )

                    val hasOld = oldPendingCount > 0
                    PhonePendingCompactStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.WarningAmber,
                        title = "Tertunda >30 Hari",
                        value = "$oldPendingCount Order",
                        subtitle = if (hasOld) "Perlu dihubungi" else "Aman terkendali",
                        containerColor = if (hasOld) PastelRoseBg else PastelNeutralBg,
                        iconBgColor = if (hasOld) PastelRose.copy(alpha = 0.14f) else PastelSlateBg,
                        accentColor = if (hasOld) PastelRoseText else GrayBlue,
                        valueColor = if (hasOld) PastelRoseText else GrayBlue
                    )
                }
            }
        }

        // 3. Search Bar Full-Width
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama, kontak, atau item...", fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Hapus pencarian",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GrayBlue,
                    unfocusedTextColor = GrayBlue,
                    focusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.5f),
                    unfocusedContainerColor = PastelBlueSoftBg.copy(alpha = 0.35f),
                    focusedBorderColor = PastelBlue,
                    unfocusedBorderColor = PastelBlueBg,
                    focusedLeadingIconColor = PastelBlue,
                    unfocusedLeadingIconColor = PastelNeutralText,
                    focusedTrailingIconColor = PastelBlue,
                    unfocusedTrailingIconColor = PastelNeutralText,
                    cursorColor = PastelBlue
                )
            )
        }

        // 4. Filter Chips (Horizontal Scrollable)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PhonePendingFilter.entries) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedFilter = filter },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PastelBlue else PastelBlueSoftBg.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (isSelected) PastelBlue else PastelBlueBg)
                    ) {
                        Text(
                            text = filter.label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else GrayBlue,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 5. Daftar Order Tertunda
        if (isLoading && activeOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PastelBlue)
                }
            }
        } else if (filteredOrders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = PastelGreenSoftBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = null,
                            tint = PastelGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (allPendingOrders.isEmpty()) {
                                "Semua order selesai sudah lunas dan diambil!"
                            } else {
                                "Tidak ada order tertunda untuk filter ini."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PastelGreenDark,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.orderId }) { order ->
                val customer = customerMap[order.customerId]
                val ageDays = phoneOrderAgeDays(order, nowMillis)
                PhonePendingOrderCard(
                    order = order,
                    contact = customer?.contact,
                    ageDays = ageDays,
                    onMarkPaidClick = { pendingConfirm = PhonePendingConfirm.MarkPaid(order) },
                    onMarkPickedUpClick = { pendingConfirm = PhonePendingConfirm.MarkPickedUp(order) },
                    onWhatsAppClick = {
                        val contact = customer?.contact.orEmpty()
                        if (contact.isBlank()) {
                            Toast.makeText(context, "Nomor WhatsApp pelanggan belum tersedia", Toast.LENGTH_SHORT).show()
                        } else {
                            pendingConfirm = PhonePendingConfirm.SendWhatsApp(order, contact)
                        }
                    }
                )
            }
        }
    }

    // Dialog Konfirmasi Aksi (Lunas / Diambil / WhatsApp)
    pendingConfirm?.let { confirm ->
        val (title, message, confirmText, confirmColor) = when (confirm) {
            is PhonePendingConfirm.MarkPaid -> Quadruple(
                "Konfirmasi Pembayaran",
                "Tandai order milik ${confirm.order.customerName ?: "Pelanggan"} sebesar ${CurrencyUtils.formatRupiahWithSymbol(confirm.order.totalPrice)} sebagai LUNAS?",
                "Tandai Lunas",
                PastelGreen
            )
            is PhonePendingConfirm.MarkPickedUp -> Quadruple(
                "Konfirmasi Pengambilan",
                "Tandai cucian milik ${confirm.order.customerName ?: "Pelanggan"} sudah DIAMBIL oleh pelanggan?",
                "Sudah Diambil",
                PastelBlue
            )
            is PhonePendingConfirm.SendWhatsApp -> Quadruple(
                "Kirim Pengingat WhatsApp",
                "Buka WhatsApp untuk mengirim pengingat order ke ${confirm.order.customerName ?: "Pelanggan"} (${confirm.contact})?",
                "Buka WhatsApp",
                PastelGreen
            )
        }

        AlertDialog(
            onDismissRequest = { pendingConfirm = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = title,
                    color = GrayBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = message,
                    color = GrayBlue.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (confirm) {
                            is PhonePendingConfirm.MarkPaid -> onTogglePayment(confirm.order.orderId, true)
                            is PhonePendingConfirm.MarkPickedUp -> onTogglePickup(confirm.order.orderId, true)
                            is PhonePendingConfirm.SendWhatsApp -> openPhoneWhatsAppReminder(context, confirm.order, confirm.contact)
                        }
                        pendingConfirm = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                ) {
                    Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirm = null }) {
                    Text("Batal", color = PastelNeutralText, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun PhonePendingCompactStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    iconBgColor: Color,
    accentColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = valueColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PhonePendingOrderCard(
    order: Orders,
    contact: String?,
    ageDays: Long,
    onMarkPaidClick: () -> Unit,
    onMarkPickedUpClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val isOverdue = ageDays > 30
    val totalQty = remember(order.orderItems) { order.orderItems.sumOf { it.itemQuantity ?: 0 } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isOverdue) PastelRose.copy(alpha = 0.45f) else PastelBlueBg),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Nama + Badge Umur Tertunda + Harga
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = order.customerName ?: "Pelanggan",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = GrayBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOverdue) PastelRoseBg else PastelNeutralBg
                        ) {
                            Text(
                                text = "${ageDays}h",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isOverdue) PastelRoseText else PastelNeutralText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    if (!contact.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = PastelNeutralText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "$contact • $totalQty pcs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PastelNeutralText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = "$totalQty pcs",
                            style = MaterialTheme.typography.labelSmall.copy(color = PastelNeutralText)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice ?: 0.0),
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (!order.alreadyPaid) PastelAmber else GrayBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )
            }

            // Status Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (order.alreadyPaid) PastelGreenBg else PastelAmberBg
                ) {
                    Text(
                        text = if (order.alreadyPaid) "Sudah Lunas" else "Belum Lunas",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (order.alreadyPaid) PastelGreen else PastelAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (order.alreadyPickedUp) PastelGreenBg else PastelBlueSoftBg
                ) {
                    Text(
                        text = if (order.alreadyPickedUp) "Sudah Diambil" else "Siap Diambil",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (order.alreadyPickedUp) PastelGreen else PastelBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            HorizontalDivider(color = PastelBlueSoftBg)

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!order.alreadyPaid) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onMarkPaidClick),
                        shape = RoundedCornerShape(12.dp),
                        color = PastelGreenBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                tint = PastelGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lunas",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PastelGreen,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                if (!order.alreadyPickedUp) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onMarkPickedUpClick),
                        shape = RoundedCornerShape(12.dp),
                        color = PastelBlueSoftBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storefront,
                                contentDescription = null,
                                tint = PastelBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Diambil",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PastelBlue,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onWhatsAppClick),
                    shape = RoundedCornerShape(12.dp),
                    color = PastelGreenSoftBg,
                    border = BorderStroke(1.dp, PastelGreen.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = "WhatsApp Pengingat",
                            tint = PastelGreen,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun phoneOrderAgeDays(order: Orders, nowMillis: Long): Long {
    val referenceTimestamp = order.orderFinishDate ?: order.orderDate
    val diff = (nowMillis - referenceTimestamp.toDate().time).coerceAtLeast(0L)
    return TimeUnit.MILLISECONDS.toDays(diff)
}

private fun openPhoneWhatsAppReminder(
    context: Context,
    order: Orders,
    contact: String?
) {
    val rawDigits = contact.orEmpty().filter { it.isDigit() }
    if (rawDigits.isBlank()) {
        Toast.makeText(context, "Nomor WhatsApp pelanggan belum tersedia", Toast.LENGTH_SHORT).show()
        return
    }
    val formattedPhone = when {
        rawDigits.startsWith("0") -> "62" + rawDigits.drop(1)
        rawDigits.startsWith("8") -> "62$rawDigits"
        else -> rawDigits
    }

    val customerName = order.customerName?.takeIf { it.isNotBlank() } ?: "Kak"
    val totalText = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice)
    val message = when {
        order.isDone() && !order.alreadyPickedUp && !order.alreadyPaid ->
            "Halo $customerName, cucian Anda di laundry sudah selesai dan siap diambil dengan total tagihan $totalText. Terima kasih."
        order.isDone() && !order.alreadyPickedUp ->
            "Halo $customerName, cucian Anda di laundry sudah selesai dan siap diambil ya. Terima kasih."
        !order.alreadyPaid ->
            "Halo $customerName, kami ingin mengingatkan untuk tagihan order laundry sebesar $totalText yang belum dilunasi. Terima kasih."
        else ->
            "Halo $customerName, kami ingin mengabarkan informasi terkait order laundry Anda (total $totalText). Terima kasih."
    }

    val url = "https://wa.me/$formattedPhone?text=${Uri.encode(message)}"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}
