package com.aprilarn.washflow.ui.feature.analytics.component

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.OrderItem
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.core.components.WashFlowDialogActionRow
import com.aprilarn.washflow.ui.core.components.WashFlowDialogHeader
import com.aprilarn.washflow.ui.core.components.WashFlowDialogSurface
import com.aprilarn.washflow.ui.feature.analytics.isDone
import com.aprilarn.washflow.ui.core.theme.Gray
import com.aprilarn.washflow.ui.core.theme.GrayBlue
import com.aprilarn.washflow.ui.core.theme.MainFontBlack
import com.aprilarn.washflow.ui.core.theme.PastelAmber
import com.aprilarn.washflow.ui.core.theme.PastelAmberBg
import com.aprilarn.washflow.ui.core.theme.PastelBlue
import com.aprilarn.washflow.ui.core.theme.PastelBlueBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueIconBg
import com.aprilarn.washflow.ui.core.theme.PastelBlueSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelGreen
import com.aprilarn.washflow.ui.core.theme.PastelGreenBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenDark
import com.aprilarn.washflow.ui.core.theme.PastelGreenIconBg
import com.aprilarn.washflow.ui.core.theme.PastelGreenSoftBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralBg
import com.aprilarn.washflow.ui.core.theme.PastelNeutralText
import com.aprilarn.washflow.ui.core.theme.PastelRose
import com.aprilarn.washflow.ui.core.theme.PastelRoseBg
import com.aprilarn.washflow.ui.core.theme.PastelRoseText
import com.aprilarn.washflow.ui.core.theme.PastelSlateBg
import com.aprilarn.washflow.utils.CurrencyUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private enum class PendingFilter(@param:StringRes val labelRes: Int) {
    ALL(R.string.analytics_filter_all),
    UNPAID(R.string.analytics_filter_unpaid),
    UNPICKED(R.string.analytics_filter_unpicked),
    OVERDUE_30(R.string.analytics_filter_overdue_30)
}

internal sealed interface PendingActionConfirm {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .background(PastelBlue, RoundedCornerShape(2.dp))
            )
            Column {
                Text(
                    text = stringResource(R.string.analytics_pending_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayBlue
                )
                Text(
                    text = stringResource(R.string.analytics_pending_subtitle),
                    fontSize = 13.sp,
                    color = PastelNeutralText,
                    fontWeight = FontWeight.Medium
                )
            }
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
                title = stringResource(R.string.analytics_pending_receivables_title),
                value = CurrencyUtils.formatRupiahWithSymbol(totalUnpaidAmount),
                subtitle = if (hasUnpaid) {
                    stringResource(R.string.analytics_pending_unpaid_orders_sub, totalUnpaidOrders.size)
                } else {
                    stringResource(R.string.analytics_pending_all_paid_sub)
                },
                containerColor = if (hasUnpaid) PastelAmberBg else PastelGreenSoftBg,
                iconBgColor = if (hasUnpaid) PastelAmber.copy(alpha = 0.14f) else PastelGreenIconBg,
                accentColor = if (hasUnpaid) PastelAmber else PastelGreen,
                valueColor = if (hasUnpaid) PastelAmber else PastelGreenDark,
                icon = if (hasUnpaid) Icons.Rounded.Payments else Icons.Rounded.CheckCircle
            )

            PendingSummaryCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_pending_ready_title),
                value = stringResource(R.string.analytics_report_order_count_value, readyUnpickedCount),
                subtitle = if (readyUnpickedCount > 0) {
                    stringResource(R.string.analytics_pending_waiting_pickup_sub)
                } else {
                    stringResource(R.string.analytics_pending_all_picked_sub)
                },
                containerColor = PastelBlueSoftBg,
                iconBgColor = PastelBlueIconBg,
                accentColor = PastelBlue,
                valueColor = GrayBlue,
                icon = Icons.Rounded.Storefront
            )

            val hasOldPending = oldPendingCount > 0
            PendingSummaryCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_pending_overdue30_title),
                value = stringResource(R.string.analytics_report_order_count_value, oldPendingCount),
                subtitle = if (hasOldPending) {
                    stringResource(R.string.analytics_pending_follow_up_sub)
                } else {
                    stringResource(R.string.analytics_pending_no_overdue_sub)
                },
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
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedFilter = filter },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) PastelBlue else PastelBlueSoftBg.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (isSelected) PastelBlue else PastelBlueBg)
                    ) {
                        Text(
                            text = stringResource(filter.labelRes),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else GrayBlue,
                            maxLines = 1
                        )
                    }
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
                            .height(42.dp)
                            .background(PastelBlueSoftBg.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .border(1.dp, PastelBlueBg, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PastelNeutralText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    stringResource(R.string.analytics_pending_search_placeholder),
                                    fontSize = 13.sp,
                                    color = PastelNeutralText
                                )
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
                                    contentDescription = stringResource(R.string.analytics_pending_clear_search_cd),
                                    modifier = Modifier.size(16.dp),
                                    tint = PastelNeutralText
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
                .border(1.dp, PastelBlueBg, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            when {
                isLoading && allPendingOrders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PastelBlue, strokeWidth = 3.dp)
                    }
                }
                filteredOrders.isEmpty() -> {
                    val selectedFilterLabel = stringResource(selectedFilter.labelRes)
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
                                text = if (allPendingOrders.isEmpty()) {
                                    stringResource(R.string.analytics_pending_empty_all_title)
                                } else {
                                    stringResource(R.string.analytics_pending_empty_filter_title, selectedFilterLabel)
                                },
                                color = GrayBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (allPendingOrders.isEmpty()) {
                                    stringResource(R.string.analytics_pending_empty_all_subtitle)
                                } else {
                                    stringResource(R.string.analytics_pending_empty_filter_subtitle)
                                },
                                color = PastelNeutralText,
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
                                            context.getString(R.string.analytics_pending_msg_no_whatsapp),
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
    val customerName = order.customerName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.orders_unnamed_customer)
    val initial = customerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val currentLocale = Locale.getDefault()
    val dateFormat = remember(currentLocale) { SimpleDateFormat("dd MMM yyyy", currentLocale) }
    val finishDateText = order.orderFinishDate?.let { dateFormat.format(it.toDate()) }
    val dateSummaryText = if (finishDateText != null) {
        stringResource(R.string.analytics_report_completed_prefix, finishDateText)
    } else {
        stringResource(R.string.analytics_report_received_prefix, dateFormat.format(order.orderDate.toDate()))
    }
    val noItemsLabel = stringResource(R.string.analytics_pending_no_items)
    val defaultItemLabel = stringResource(R.string.analytics_report_default_item)
    val itemsPreview = remember(order.orderItems, noItemsLabel, defaultItemLabel) {
        if (order.orderItems.isEmpty()) noItemsLabel
        else order.orderItems.joinToString(", ") { "${it.itemName ?: defaultItemLabel} (${it.itemQuantity ?: 0})" }
    }

    val ageLabel = if (order.orderFinishDate != null) {
        if (ageDays >= 1) stringResource(R.string.analytics_pending_done_days_ago, ageDays)
        else stringResource(R.string.analytics_pending_done_today)
    } else {
        if (ageDays >= 1) stringResource(R.string.analytics_pending_received_days_ago, ageDays)
        else stringResource(R.string.analytics_pending_received_today)
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
                            text = stringResource(
                                if (order.alreadyPickedUp) R.string.manage_order_picked_up
                                else R.string.manage_order_not_picked_up
                            ),
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
                                    text = stringResource(R.string.analytics_pending_mark_paid),
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
                                    text = stringResource(R.string.analytics_pending_mark_picked_up),
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
                                contentDescription = stringResource(R.string.analytics_pending_whatsapp_cd),
                                tint = if (!customerContact.isNullOrBlank()) PastelGreenDark else Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.analytics_pending_whatsapp),
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
internal fun PendingActionConfirmDialog(
    action: PendingActionConfirm,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val customerFallback = stringResource(R.string.analytics_customer_fallback)
    val customerName: String
    val title: String
    val description: String
    val confirmLabel: String
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val badgeBgColor: Color
    val accentColor: Color

    when (action) {
        is PendingActionConfirm.MarkPaid -> {
            customerName = action.order.customerName?.takeIf { it.isNotBlank() } ?: customerFallback
            val totalText = CurrencyUtils.formatRupiahWithSymbol(action.order.totalPrice)
            title = stringResource(R.string.analytics_pending_confirm_paid_title)
            description = stringResource(R.string.analytics_pending_confirm_paid_msg, customerName, totalText)
            confirmLabel = stringResource(R.string.analytics_pending_confirm_paid_btn)
            icon = Icons.Rounded.Payments
            badgeBgColor = PastelBlueBg
            accentColor = PastelBlue
        }
        is PendingActionConfirm.MarkPickedUp -> {
            customerName = action.order.customerName?.takeIf { it.isNotBlank() } ?: customerFallback
            title = stringResource(R.string.analytics_pending_confirm_pickup_title)
            description = stringResource(R.string.analytics_pending_confirm_pickup_msg, customerName)
            confirmLabel = stringResource(R.string.analytics_pending_confirm_pickup_btn)
            icon = Icons.Rounded.Storefront
            badgeBgColor = PastelGreenBg
            accentColor = PastelGreenDark
        }
        is PendingActionConfirm.SendWhatsApp -> {
            customerName = action.order.customerName?.takeIf { it.isNotBlank() } ?: customerFallback
            title = stringResource(R.string.analytics_pending_confirm_wa_title)
            description = stringResource(R.string.analytics_pending_confirm_wa_msg, customerName, action.contact)
            confirmLabel = stringResource(R.string.analytics_pending_confirm_wa_btn)
            icon = Icons.AutoMirrored.Rounded.Chat
            badgeBgColor = PastelGreenBg
            accentColor = PastelGreenDark
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        WashFlowDialogSurface {
            WashFlowDialogHeader(
                icon = icon,
                iconBgColor = badgeBgColor,
                iconTint = accentColor,
                title = title,
                subtitle = description
            )

            Spacer(modifier = Modifier.height(22.dp))

            WashFlowDialogActionRow(
                dismissText = stringResource(R.string.common_cancel),
                onDismiss = onDismiss,
                confirmText = confirmLabel,
                onConfirm = onConfirm,
                confirmContainerColor = accentColor,
                confirmContentColor = Color.White
            )
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
        Toast.makeText(context, context.getString(R.string.analytics_pending_msg_no_whatsapp), Toast.LENGTH_SHORT).show()
        return
    }
    val formattedPhone = when {
        rawDigits.startsWith("0") -> "62" + rawDigits.drop(1)
        rawDigits.startsWith("8") -> "62$rawDigits"
        else -> rawDigits
    }

    val customerName = order.customerName?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.analytics_customer_fallback)
    val totalText = CurrencyUtils.formatRupiahWithSymbol(order.totalPrice)
    val message = when {
        order.isDone() && !order.alreadyPickedUp && !order.alreadyPaid ->
            context.getString(R.string.analytics_wa_ready_unpaid, customerName, totalText)
        order.isDone() && !order.alreadyPickedUp ->
            context.getString(R.string.analytics_wa_ready_paid, customerName)
        !order.alreadyPaid ->
            context.getString(R.string.analytics_wa_unpaid, customerName, totalText)
        else ->
            context.getString(R.string.analytics_wa_general, customerName, totalText)
    }

    val url = "https://wa.me/$formattedPhone?text=${Uri.encode(message)}"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.analytics_pending_msg_wa_failed), Toast.LENGTH_SHORT).show()
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
