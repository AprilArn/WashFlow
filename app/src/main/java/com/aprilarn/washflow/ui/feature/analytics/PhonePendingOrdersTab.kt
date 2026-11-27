package com.aprilarn.washflow.ui.feature.analytics

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Customers
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.ui.feature.analytics.component.PendingActionConfirm
import com.aprilarn.washflow.ui.feature.analytics.component.PendingActionConfirmDialog
import com.aprilarn.washflow.ui.core.theme.GrayBlue
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
import java.util.concurrent.TimeUnit

private enum class PhonePendingFilter(@param:StringRes val labelRes: Int) {
    ALL(R.string.analytics_filter_all),
    UNPAID(R.string.analytics_filter_unpaid),
    UNPICKED(R.string.analytics_filter_unpicked),
    OVERDUE_30(R.string.analytics_filter_overdue_30)
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
                        text = stringResource(R.string.analytics_pending_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GrayBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.analytics_pending_phone_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PastelNeutralText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 2. Ringkasan Proporsional: Hero Full-Width (Total Piutang) + 2 Kartu Kompak
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
                                text = stringResource(R.string.analytics_pending_receivables_title),
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
                                text = if (hasUnpaid) {
                                    stringResource(R.string.analytics_pending_unpaid_orders_sub, totalUnpaidOrders.size)
                                } else {
                                    stringResource(R.string.analytics_pending_all_paid_sub)
                                },
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
                        title = stringResource(R.string.analytics_pending_ready_title),
                        value = stringResource(R.string.analytics_report_order_count_value, readyUnpickedCount),
                        subtitle = if (readyUnpickedCount > 0) {
                            stringResource(R.string.analytics_pending_phone_waiting_sub)
                        } else {
                            stringResource(R.string.analytics_pending_all_picked_sub)
                        },
                        containerColor = PastelBlueSoftBg,
                        iconBgColor = PastelBlueIconBg,
                        accentColor = PastelBlue,
                        valueColor = GrayBlue
                    )

                    val hasOld = oldPendingCount > 0
                    PhonePendingCompactStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.WarningAmber,
                        title = stringResource(R.string.analytics_pending_overdue30_title),
                        value = stringResource(R.string.analytics_report_order_count_value, oldPendingCount),
                        subtitle = if (hasOld) {
                            stringResource(R.string.analytics_pending_follow_up_sub)
                        } else {
                            stringResource(R.string.analytics_pending_phone_safe_sub)
                        },
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
                placeholder = {
                    Text(
                        text = stringResource(R.string.analytics_pending_phone_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PastelNeutralText,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                                contentDescription = stringResource(R.string.analytics_pending_clear_search_cd),
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
                    focusedPlaceholderColor = PastelNeutralText,
                    unfocusedPlaceholderColor = PastelNeutralText,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = PastelBlueSoftBg,
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
                            text = stringResource(filter.labelRes),
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
                            text = stringResource(
                                if (allPendingOrders.isEmpty()) {
                                    R.string.analytics_pending_phone_empty_all
                                } else {
                                    R.string.analytics_pending_phone_empty_filter
                                }
                            ),
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
                            Toast.makeText(
                                context,
                                context.getString(R.string.analytics_pending_msg_no_whatsapp),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pendingConfirm = PhonePendingConfirm.SendWhatsApp(order, contact)
                        }
                    }
                )
            }
        }
    }

    // Dialog Konfirmasi Aksi (Lunas / Diambil / WhatsApp) — konsisten dengan versi Tablet
    pendingConfirm?.let { confirm ->
        val action = when (confirm) {
            is PhonePendingConfirm.MarkPaid -> PendingActionConfirm.MarkPaid(confirm.order)
            is PhonePendingConfirm.MarkPickedUp -> PendingActionConfirm.MarkPickedUp(confirm.order)
            is PhonePendingConfirm.SendWhatsApp -> PendingActionConfirm.SendWhatsApp(confirm.order, confirm.contact)
        }

        PendingActionConfirmDialog(
            action = action,
            onDismiss = { pendingConfirm = null },
            onConfirm = {
                when (confirm) {
                    is PhonePendingConfirm.MarkPaid -> onTogglePayment(confirm.order.orderId, true)
                    is PhonePendingConfirm.MarkPickedUp -> onTogglePickup(confirm.order.orderId, true)
                    is PhonePendingConfirm.SendWhatsApp -> openPhoneWhatsAppReminder(context, confirm.order, confirm.contact)
                }
                pendingConfirm = null
            }
        )
    }
}

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
                            text = order.customerName ?: stringResource(R.string.analytics_customer_fallback),
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
                                text = stringResource(R.string.analytics_pending_age_short, ageDays),
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
                                text = stringResource(R.string.analytics_report_service_pcs, contact, totalQty),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PastelNeutralText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.analytics_report_item_pcs, totalQty),
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
                        text = stringResource(
                            if (order.alreadyPaid) R.string.manage_order_paid else R.string.manage_order_unpaid
                        ),
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
                        text = stringResource(
                            if (order.alreadyPickedUp) R.string.manage_order_picked_up
                            else R.string.manage_order_ready_pickup
                        ),
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
                                text = stringResource(R.string.manage_order_paid),
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
                                text = stringResource(R.string.analytics_pending_mark_picked_up),
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
                            contentDescription = stringResource(R.string.analytics_pending_whatsapp_cd),
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
