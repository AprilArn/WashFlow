package com.aprilarn.washflow.ui.feature.analytics

import androidx.annotation.StringRes
import com.aprilarn.washflow.R

const val ANALYTICS_ALL_MONTHS_KEY = "ALL_MONTHS"
const val ANALYTICS_ALL_STATUS_KEY = "ALL"

enum class ReportSortColumn(@param:StringRes val labelRes: Int) {
    DATE(R.string.analytics_sort_date),
    TIME(R.string.analytics_sort_time),
    CUSTOMER_NAME(R.string.analytics_sort_customer),
    ORDER_ITEMS(R.string.analytics_sort_order),
    TOTAL_PRICE(R.string.analytics_sort_total),
    STATUS(R.string.analytics_sort_status),
    FINISH_DATE(R.string.analytics_sort_finish_date)
}
