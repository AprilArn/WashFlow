package com.aprilarn.washflow.ui.analytics.component

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.core.net.toUri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.analytics.isDone
import com.aprilarn.washflow.ui.theme.Gray
import com.aprilarn.washflow.ui.theme.GrayBlue
import com.aprilarn.washflow.ui.theme.MainFontBlack
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
import com.aprilarn.washflow.ui.theme.PastelRose
import com.aprilarn.washflow.ui.theme.PastelRoseBg
import com.aprilarn.washflow.ui.theme.PastelRoseText
import com.aprilarn.washflow.ui.theme.PastelSlateBg
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private enum class PendingFilter(val label: String) {
    ALL("Semua"),
    UNPAID("Belum Lunas"),
    UNPICKED("Belum Diambil"),
    OVERDUE_30("Lama (> 30 hari)")
}

private sealed interface PendingActionConfirm {
    data class MarkPaid(val order: Orders) : PendingActionConfirm
    data class MarkPickedUp(val order: Orders) : PendingActionConfirm
    data class SendWhatsApp(val order: Orders, val contact: String) : PendingActionConfirm
}

@Composable
fun PendingOrdersPanel(
    activeOrders: List<Orders>,
    customers: List<Customers>,
    isLoading: Boolean,
    onTogglePayment: (orderId: String, isPaid: Boolean) -> Unit,
    onTogglePickup: (orderId: String, isPickedUp: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by rememberSaveable { mutableStateOf(PendingFilter.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var pendingConfirm by remember { mutableStateOf<PendingActionConfirm?>(null) }

    val nowMillis = remember(activeOrders) { System.currentTimeMillis() }

    val customerMap = remember(customers) {
        customers.associateBy { it.customerId }
    }

    // Hanya order yang sudah Done tetapi belum lunas atau belum diambil
    val allPendingOrders = remember(activeOrders) {
        activeOrders
            .filter { order -> order.isDone() && (!order.alreadyPaid || !order.alreadyPickedUp) }
            .sortedBy { (it.orderFinishDate ?: it.orderDate).toDate().time } // Selesai terlama di atas agar prioritas tindak lanjut jelas
    }

    val totalUnpaidOrders = remember(allPendingOrders) { allPendingOrders.filter { !it.alreadyPaid } }
    val totalUnpaidAmount = remember(totalUnpaidOrders) { totalUnpaidOrders.sumOf { it.totalPrice ?: 0.0 } }
    val readyUnpickedCount = remember(allPendingOrders) {
        allPendingOrders.count { !it.alreadyPickedUp }
    }
    val oldPendingCount = remember(allPendingOrders, nowMillis) {
        allPendingOrders.count { orderAgeDays(it, nowMillis) > 30 }
    }

    val filteredOrders = remember(allPendingOrders, selectedFilter, searchQuery, customerMap, nowMillis) {
        allPendingOrders.filter { order ->
            val matchesFilter = when (selectedFilter) {
                PendingFilter.ALL -> true
                PendingFilter.UNPAID -> !order.alreadyPaid
                PendingFilter.UNPICKED -> !order.alreadyPickedUp
                PendingFilter.OVERDUE_30 -> orderAgeDays(order, nowMillis) > 30
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Order Tertunda & Piutang",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GrayBlue
            )
            Text(
                text = "Pantau order selesai yang belum dibayar atau belum diambil lintas bulan",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3 Kartu Ringkasan Pastel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val hasUnpaid = totalUnpaidOrders.isNotEmpty()
            PendingSummaryCard(
                modifier = Modifier.weight(1.2f),
                title = "Total Piutang",
                value = CurrencyUtils.formatRupiahWithSymbol(totalUnpaidAmount),
                subtitle = if (hasUnpaid) "${totalUnpaidOrders.size} order belum bayar" else "Semua sudah lunas",
                containerColor = if (hasUnpaid) PastelAmberBg else PastelGreenSoftBg,
                iconBgColor = if (hasUnpaid) PastelAmber.copy(alpha = 0.14f) else PastelGreenIconBg,
                accentColor = if (hasUnpaid) PastelAmber else PastelGreen,
                valueColor = if (hasUnpaid) PastelAmber else PastelGreenDark,
                icon = if (hasUnpaid) Icons.Rounded.Payments else Icons.Rounded.CheckCircle
            )

            PendingSummaryCard(
                modifier = Modifier.weight(1f),
                title = "Siap Diambil",
                value = "$readyUnpickedCount Order",
                subtitle = if (readyUnpickedCount > 0) "Selesai, menunggu diambil" else "Tidak ada cucian menumpuk",
                containerColor = PastelBlueSoftBg,
                iconBgColor = PastelBlueIconBg,
                accentColor = PastelBlue,
                valueColor = GrayBlue,
                icon = Icons.Rounded.Storefront
            )

            val hasOldPending = oldPendingCount > 0
            PendingSummaryCard(
                modifier = Modifier.weight(1f),
                title = "Tertunda > 30 Hari",
                value = "$oldPendingCount Order",
                subtitle = if (hasOldPending) "Perlu segera dihubungi" else "Tidak ada tunggakan lama",
                containerColor = if (hasOldPending) PastelRoseBg else PastelNeutralBg,
                iconBgColor = if (hasOldPending) PastelRose.copy(alpha = 0.14f) else PastelSlateBg,
                accentColor = if (hasOldPending) PastelRoseText else GrayBlue,
                valueColor = if (hasOldPending) PastelRoseText else GrayBlue,
                icon = Icons.Rounded.WarningAmber
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips & Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PendingFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrayBlue,
                            selectedLabelColor = Color.White,
                            containerColor = PastelNeutralBg,
                            labelColor = GrayBlue
                        )
                    )
                }
            }

            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = GrayBlue),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .width(260.dp)
                            .height(44.dp)
                            .background(PastelNeutralBg, RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text("Cari nama/kontak/item...", fontSize = 13.sp, color = Color.Gray)
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daftar Order Tertunda
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            when {
                isLoading && allPendingOrders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GrayBlue, strokeWidth = 3.dp)
                    }
                }
                filteredOrders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(PastelGreenBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DoneAll,
                                    contentDescription = null,
                                    tint = PastelGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (allPendingOrders.isEmpty()) "Semua order sudah beres"
                                else "Tidak ada order pada filter \"${selectedFilter.label}\"",
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (allPendingOrders.isEmpty()) "Tidak ada tagihan atau cucian tertunda saat ini."
                                else "Coba pilih filter atau kata kunci pencarian lain.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredOrders, key = { it.orderId }) { order ->
                            val customer = customerMap[order.customerId]
                            val ageDays = orderAgeDays(order, nowMillis)
                            PendingOrderRow(
                                order = order,
                                customerContact = customer?.contact,
                                ageDays = ageDays,
                                onRemindWhatsApp = {
                                    val contact = customer?.contact
                                    val rawDigits = contact.orEmpty().filter { it.isDigit() }
                                    if (rawDigits.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Nomor WhatsApp pelanggan belum tersedia",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        pendingConfirm = PendingActionConfirm.SendWhatsApp(
                                            order = order,
                                            contact = contact.orEmpty()
                                        )
                                    }
                                },
                                onMarkPaid = {
                                    pendingConfirm = PendingActionConfirm.MarkPaid(order)
                                },
                                onMarkPickedUp = {
                                    pendingConfirm = PendingActionConfirm.MarkPickedUp(order)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingConfirm?.let { action ->
        PendingActionConfirmDialog(
            action = action,
            onDismiss = { pendingConfirm = null },
            onConfirm = {
                when (action) {
                    is PendingActionConfirm.MarkPaid -> onTogglePayment(action.order.orderId, true)
                    is PendingActionConfirm.MarkPickedUp -> onTogglePickup(action.order.orderId, true)
                    is PendingActionConfirm.SendWhatsApp -> openWhatsAppReminder(
                        context = context,
                        order = action.order,
                        contact = action.contact
                    )
                }
                pendingConfirm = null
            }
        )
    }
}

@Composable
private fun PendingSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    iconBgColor: Color,
    accentColor: Color,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = Gray)
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PendingOrderRow(
    order: Orders,
    customerContact: String?,
    ageDays: Long,
    onRemindWhatsApp: () -> Unit,
    onMarkPaid: () -> Unit,
    onMarkPickedUp: () -> Unit
) {
    val customerName = order.customerName?.takeIf { it.isNotBlank() } ?: "Tanpa nama"
    val initial = customerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }
    val finishDateText = order.orderFinishDate?.let { dateFormat.format(it.toDate()) }
    val dateSummaryText = if (finishDateText != null) {
        "Selesai $finishDateText"
    } else {
        "Masuk ${dateFormat.format(order.orderDate.toDate())}"
    }
    val itemsPreview = remember(order.orderItems) {
        if (order.orderItems.isEmpty()) "Tidak ada item"
        else order.orderItems.joinToString(", ") { "${it.itemName ?: "Item"} (${it.itemQuantity ?: 0})" }
    }

    val ageLabel = if (order.orderFinishDate != null) {
        if (ageDays >= 1) "Selesai $ageDays hari lalu" else "Selesai hari ini"
    } else {
        if (ageDays >= 1) "Masuk $ageDays hari lalu" else "Masuk hari ini"
    }
    val (ageBg, ageColor) = when {
        ageDays > 30 -> PastelRoseBg to PastelRoseText
        ageDays >= 7 -> PastelAmberBg to PastelAmber
        else -> PastelSlateBg to GrayBlue
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PastelNeutralBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GrayBlue
                )
            }

            // Info Pelanggan & Order
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MainFontBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = ageBg) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = ageColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = ageLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ageColor
                            )
                        }
                    }
                    if (!customerContact.isNullOrBlank()) {
                        Text(
                            text = "• $customerContact",
                            fontSize = 11.sp,
                            color = Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "$dateSummaryText  •  $itemsPreview",
                    fontSize = 12.sp,
                    color = Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentBadge(alreadyPaid = order.alreadyPaid)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (order.alreadyPickedUp) PastelGreenBg else PastelAmberBg
                    ) {
                        Text(
                            text = if (order.alreadyPickedUp) "Diambil" else "Belum Diambil",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.alreadyPickedUp) PastelGreen else PastelAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Nominal + Tombol Aksi Tindak Lanjut
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = GrayBlue
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!order.alreadyPaid) {
                        Surface(
                            onClick = onMarkPaid,
                            shape = RoundedCornerShape(10.dp),
                            color = PastelBlueBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Payments,
                                    contentDescription = null,
                                    tint = PastelBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Tandai Lunas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PastelBlue
                                )
                            }
                        }
                    }

                    if (order.isDone() && !order.alreadyPickedUp) {
                        Surface(
                            onClick = onMarkPickedUp,
                            shape = RoundedCornerShape(10.dp),
                            color = PastelGreenBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Storefront,
                                    contentDescription = null,
                                    tint = PastelGreenDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Diambil",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PastelGreenDark
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onRemindWhatsApp,
                        shape = RoundedCornerShape(10.dp),
                        color = if (!customerContact.isNullOrBlank()) Color.White else Color.White.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Chat,
                                contentDescription = "Ingatkan WhatsApp",
                                tint = if (!customerContact.isNullOrBlank()) PastelGreenDark else Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "WhatsApp",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!customerContact.isNullOrBlank()) PastelGreenDark else Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingActionConfirmDialog(
    action: PendingActionConfirm,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val customerName: String
    val title: String
    val description: String
    val confirmLabel: String
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val badgeBgColor: Color
    val accentColor: Color

    when (action) {
        is PendingActionConfirm.MarkPaid -> {
            customerName = action.order.customerName?.takeIf { it.isNotBlank() } ?: "Pelanggan"
            val totalText = CurrencyUtils.formatRupiahWithSymbol(action.order.totalPrice)
            title = "Konfirmasi Pelunasan"
            description = "Tandai order milik $customerName sebesar $totalText sebagai sudah dibayar?"
            confirmLabel = "Ya, Tandai Lunas"
            icon = Icons.Rounded.Payments
            badgeBgColor = PastelBlueBg
            accentColor = PastelBlue
        }
        is PendingActionConfirm.MarkPickedUp -> {
            customerName = action.order.customerName?.takeIf { it.isNotBlank() } ?: "Pelanggan"
            title = "Konfirmasi Pengambilan"
            description = "Tandai cucian milik $customerName sebagai sudah diambil oleh pelanggan?"
            confirmLabel = "Ya, Sudah Diambil"
            icon = Icons.Rounded.Storefront
            badgeBgColor = PastelGreenBg
            accentColor = PastelGreenDark
        }
        is PendingActionConfirm.SendWhatsApp -> {
            customerName = action.order.customerName?.takeIf { it.isNotBlank() } ?: "Pelanggan"
            title = "Kirim Pengingat WhatsApp"
            description = "Buka WhatsApp untuk mengirim pengingat ke $customerName (${action.contact})?"
            confirmLabel = "Buka WhatsApp"
            icon = Icons.AutoMirrored.Rounded.Chat
            badgeBgColor = PastelGreenBg
            accentColor = PastelGreenDark
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(badgeBgColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GrayBlue
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Gray),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Batal",
                            color = Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = badgeBgColor,
                            contentColor = accentColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = confirmLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun orderAgeDays(order: Orders, nowMillis: Long): Long {
    val referenceTimestamp = order.orderFinishDate ?: order.orderDate
    val diff = (nowMillis - referenceTimestamp.toDate().time).coerceAtLeast(0L)
    return TimeUnit.MILLISECONDS.toDays(diff)
}

private fun openWhatsAppReminder(
    context: android.content.Context,
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

@Preview(showBackground = true, widthDp = 1000, heightDp = 680)
@Composable
fun PendingOrdersPanelPreview() {
    val dayMillis = 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()
    val sampleOrders = listOf(
        Orders(
            orderId = "o1",
            customerId = "c1",
            customerName = "Pak Wira",
            orderDate = Timestamp(Date(now - 125 * dayMillis)),
            orderFinishDate = Timestamp(Date(now - 120 * dayMillis)),
            status = "Done",
            alreadyPaid = false,
            alreadyPickedUp = false,
            totalPrice = 1496000.0,
            orderItems = listOf(OrderItem(itemName = "Bed Cover", itemQuantity = 4))
        ),
        Orders(
            orderId = "o2",
            customerId = "c2",
            customerName = "Troya",
            orderDate = Timestamp(Date(now - 18 * dayMillis)),
            orderFinishDate = Timestamp(Date(now - 15 * dayMillis)),
            status = "Done",
            alreadyPaid = true,
            alreadyPickedUp = false,
            totalPrice = 60000.0,
            orderItems = listOf(OrderItem(itemName = "Cuci Kiloan", itemQuantity = 6))
        ),
        Orders(
            orderId = "o3",
            customerId = "c3",
            customerName = "Siti Aminah",
            orderDate = Timestamp(Date(now - 5 * dayMillis)),
            orderFinishDate = Timestamp(Date(now - 3 * dayMillis)),
            status = "Done",
            alreadyPaid = false,
            alreadyPickedUp = true,
            totalPrice = 85000.0,
            orderItems = listOf(OrderItem(itemName = "Jas", itemQuantity = 1))
        ),
        Orders(
            orderId = "o4",
            customerId = "c4",
            customerName = "Budi (Masih Diproses)",
            orderDate = Timestamp(Date(now - 2 * dayMillis)),
            status = "On Process",
            alreadyPaid = false,
            alreadyPickedUp = false,
            totalPrice = 40000.0,
            orderItems = listOf(OrderItem(itemName = "Kemeja", itemQuantity = 2))
        )
    )
    val sampleCustomers = listOf(
        Customers(customerId = "c1", name = "Pak Wira", contact = "081234567890"),
        Customers(customerId = "c2", name = "Troya", contact = "089876543210")
    )

    MaterialTheme {
        Box(modifier = Modifier.background(Color.White)) {
            PendingOrdersPanel(
                activeOrders = sampleOrders,
                customers = sampleCustomers,
                isLoading = false,
                onTogglePayment = { _, _ -> },
                onTogglePickup = { _, _ -> }
            )
        }
    }
}
