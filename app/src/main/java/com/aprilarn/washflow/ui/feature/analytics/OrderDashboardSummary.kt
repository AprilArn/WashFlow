package com.aprilarn.washflow.ui.feature.analytics

import androidx.annotation.StringRes
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.model.Orders
import com.aprilarn.washflow.data.model.Services
import com.aprilarn.washflow.ui.core.utils.UiText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Ringkasan data order untuk panel kiri halaman Analytics (tab Order).
 * Seluruh nilai dihitung dari data order realtime yang sudah dimuat oleh [AnalyticsViewModel].
 */
data class OrderDashboardSummary(
    /** Total pendapatan bulan berjalan (tanggal 1 s.d. sekarang). */
    val monthRevenue: Double = 0.0,
    /** Total pendapatan bulan lalu pada periode yang sama (tanggal 1 s.d. tanggal hari ini). */
    val lastMonthSamePeriodRevenue: Double = 0.0,
    /** Jumlah order yang masuk bulan ini. */
    val monthOrderCount: Int = 0,
    /** Jumlah order yang masih On Queue / On Process. */
    val activeOrderCount: Int = 0,
    /** Total nominal order yang belum dibayar. */
    val unpaidAmount: Double = 0.0,
    val unpaidCount: Int = 0,
    /** Order yang sudah selesai tetapi belum diambil pelanggan. */
    val readyForPickupCount: Int = 0,
    /** Order aktif yang memiliki deadline, diurutkan dari yang paling mendesak. */
    val upcomingDeadlines: List<Orders> = emptyList()
) {
    /** Persentase perubahan pendapatan vs periode yang sama bulan lalu, null bila tidak ada pembanding. */
    val revenueGrowthPercent: Double?
        get() = if (lastMonthSamePeriodRevenue > 0) {
            ((monthRevenue - lastMonthSamePeriodRevenue) / lastMonthSamePeriodRevenue) * 100
        } else null
}

enum class RecentOrderFilter(@param:StringRes val labelRes: Int) {
    ALL(R.string.analytics_filter_all),
    ACTIVE(R.string.analytics_filter_active),
    READY_FOR_PICKUP(R.string.analytics_filter_ready_pickup),
    UNPAID(R.string.analytics_filter_unpaid)
}

enum class DeadlineUrgency { OVERDUE, TODAY, UPCOMING }

data class DeadlineInfo(val label: UiText, val urgency: DeadlineUrgency)

// ------------------------------------------------------------------
// Helper status order
// ------------------------------------------------------------------

internal fun Orders.isDone(): Boolean = status.equals("Done", ignoreCase = true)

internal fun Orders.isActive(): Boolean = !isDone()

internal fun Orders.isReadyForPickup(): Boolean = isDone() && !alreadyPickedUp

// ------------------------------------------------------------------
// Kalkulasi ringkasan
// ------------------------------------------------------------------

fun buildOrderDashboardSummary(
    orders: List<Orders>,
    now: Date = Date(),
    maxDeadlines: Int = 10
): OrderDashboardSummary {
    val nowCal = Calendar.getInstance().apply { time = now }

    val startOfThisMonth = (nowCal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        startOfDay()
    }.timeInMillis

    val startOfLastMonthCal = (nowCal.clone() as Calendar).apply {
        add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, 1)
        startOfDay()
    }
    val startOfLastMonth = startOfLastMonthCal.timeInMillis

    // Batas akhir periode pembanding: tanggal yang sama di bulan lalu (dibatasi jumlah hari bulan lalu)
    val endOfLastMonthSamePeriod = (startOfLastMonthCal.clone() as Calendar).apply {
        val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, minOf(nowCal.get(Calendar.DAY_OF_MONTH), maxDay))
        set(Calendar.HOUR_OF_DAY, nowCal.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, nowCal.get(Calendar.MINUTE))
        set(Calendar.SECOND, nowCal.get(Calendar.SECOND))
    }.timeInMillis

    var monthRevenue = 0.0
    var lastMonthRevenue = 0.0
    var monthOrderCount = 0
    var activeCount = 0
    var unpaidAmount = 0.0
    var unpaidCount = 0
    var readyCount = 0

    orders.forEach { order ->
        val orderTime = order.orderDate.toDate().time
        val price = order.totalPrice ?: 0.0

        if (orderTime >= startOfThisMonth) {
            monthRevenue += price
            monthOrderCount++
        } else if (orderTime in startOfLastMonth..endOfLastMonthSamePeriod) {
            lastMonthRevenue += price
        }

        if (order.isActive()) activeCount++
        if (order.isReadyForPickup()) readyCount++
        if (!order.alreadyPaid) {
            unpaidAmount += price
            unpaidCount++
        }
    }

    val deadlines = orders
        .filter { it.isActive() && it.orderDueDate != null }
        .sortedBy { it.orderDueDate!!.toDate().time }
        .take(maxDeadlines)

    return OrderDashboardSummary(
        monthRevenue = monthRevenue,
        lastMonthSamePeriodRevenue = lastMonthRevenue,
        monthOrderCount = monthOrderCount,
        activeOrderCount = activeCount,
        unpaidAmount = unpaidAmount,
        unpaidCount = unpaidCount,
        readyForPickupCount = readyCount,
        upcomingDeadlines = deadlines
    )
}

fun filterRecentOrders(
    orders: List<Orders>,
    filter: RecentOrderFilter,
    limit: Int = 30
): List<Orders> = orders
    .asSequence()
    .filter { order ->
        when (filter) {
            RecentOrderFilter.ALL -> true
            RecentOrderFilter.ACTIVE -> order.isActive()
            RecentOrderFilter.READY_FOR_PICKUP -> order.isReadyForPickup()
            RecentOrderFilter.UNPAID -> !order.alreadyPaid
        }
    }
    .sortedByDescending { it.orderDate.toDate().time }
    .take(limit)
    .toList()

/**
 * Menghasilkan label deadline yang mudah dibaca, contoh:
 * "Terlambat 2 hari", "Hari ini, 15:00", "Besok, 09:00", "3 hari lagi".
 */
fun deadlineInfo(due: Date, now: Date = Date()): DeadlineInfo {
    val locale = Locale.getDefault()
    val timeText = SimpleDateFormat("HH:mm", locale).format(due)

    val dueDay = Calendar.getInstance().apply { time = due; startOfDay() }.timeInMillis
    val today = Calendar.getInstance().apply { time = now; startOfDay() }.timeInMillis
    val diffDays = TimeUnit.MILLISECONDS.toDays(dueDay - today).toInt()

    return when {
        diffDays < 0 -> DeadlineInfo(
            UiText.StringResource(R.string.analytics_deadline_overdue_days, -diffDays),
            DeadlineUrgency.OVERDUE
        )
        diffDays == 0 && due.before(now) -> DeadlineInfo(
            UiText.StringResource(R.string.analytics_deadline_overdue_time, timeText),
            DeadlineUrgency.OVERDUE
        )
        diffDays == 0 -> DeadlineInfo(
            UiText.StringResource(R.string.analytics_deadline_today_time, timeText),
            DeadlineUrgency.TODAY
        )
        diffDays == 1 -> DeadlineInfo(
            UiText.StringResource(R.string.analytics_deadline_tomorrow_time, timeText),
            DeadlineUrgency.UPCOMING
        )
        else -> DeadlineInfo(
            UiText.StringResource(R.string.analytics_deadline_in_days, diffDays),
            DeadlineUrgency.UPCOMING
        )
    }
}

/**
 * Performa satu layanan pada bulan berjalan, digunakan pada bagian "Top Services".
 */
data class ServicePerformance(
    val serviceId: String,
    val serviceName: String,
    val revenue: Double,
    val quantity: Int,
    /** Porsi pendapatan layanan ini terhadap total pendapatan bulan ini (0f..1f). */
    val share: Float
)

@StringRes
fun topServicesPeriodLabelRes(period: String): Int = when (period) {
    "Day" -> R.string.analytics_period_today
    "Week" -> R.string.analytics_period_this_week
    else -> R.string.analytics_period_this_month
}

/**
 * Mengelompokkan item order pada periode berjalan ([period]: "Day", "Week", atau "Month")
 * berdasarkan layanan, diurutkan dari pendapatan terbesar.
 * Item dengan layanan yang sudah dihapus / tidak dikenal digabung ke [otherServiceLabel].
 */
fun buildTopServices(
    orders: List<Orders>,
    services: List<Services>,
    period: String = "Month",
    otherServiceLabel: String = "Other",
    now: Date = Date()
): List<ServicePerformance> {
    val startOfPeriod = Calendar.getInstance().apply {
        time = now
        when (period) {
            "Day" -> Unit
            "Week" -> {
                val daysToMonday = (get(Calendar.DAY_OF_WEEK) + 5) % 7
                add(Calendar.DAY_OF_YEAR, -daysToMonday)
            }
            else -> set(Calendar.DAY_OF_MONTH, 1)
        }
        startOfDay()
    }.timeInMillis

    val serviceNames = services.associate { it.serviceId to it.serviceName }
    val revenueById = mutableMapOf<String, Double>()
    val quantityById = mutableMapOf<String, Int>()

    orders
        .filter { it.orderDate.toDate().time >= startOfPeriod }
        .forEach { order ->
            order.orderItems.forEach { item ->
                val key = item.serviceId?.takeIf { it in serviceNames } ?: OTHER_SERVICE_KEY
                val qty = item.itemQuantity ?: 0
                val subtotal = item.subtotal?.takeIf { it > 0 } ?: ((item.itemPrice ?: 0.0) * qty)
                revenueById[key] = (revenueById[key] ?: 0.0) + subtotal
                quantityById[key] = (quantityById[key] ?: 0) + qty
            }
        }

    val totalRevenue = revenueById.values.sum()

    return revenueById
        .map { (id, revenue) ->
            ServicePerformance(
                serviceId = id,
                serviceName = serviceNames[id] ?: otherServiceLabel,
                revenue = revenue,
                quantity = quantityById[id] ?: 0,
                share = if (totalRevenue > 0) (revenue / totalRevenue).toFloat() else 0f
            )
        }
        .sortedByDescending { it.revenue }
}

private const val OTHER_SERVICE_KEY = "__other__"

private fun Calendar.startOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
