package com.aprilarn.washflow.domain.model

import androidx.annotation.StringRes
import com.aprilarn.washflow.R

/**
 * Status antrian pesanan pada layer Domain.
 * `label` tetap dijaga konstan ("On Queue", "On Process", "Done") untuk kompatibilitas Firestore.
 */
enum class OrderQueueStatus(
    val label: String,
    @param:StringRes val displayLabelRes: Int,
    @param:StringRes val subTitleRes: Int
) {
    ON_QUEUE(
        label = "On Queue",
        displayLabelRes = R.string.home_status_on_queue,
        subTitleRes = R.string.home_status_on_queue_sub
    ),
    ON_PROCESS(
        label = "On Process",
        displayLabelRes = R.string.home_status_on_process,
        subTitleRes = R.string.home_status_on_process_sub
    ),
    DONE(
        label = "Done",
        displayLabelRes = R.string.home_status_done,
        subTitleRes = R.string.home_status_done_sub
    );

    companion object {
        fun fromLabel(label: String?): OrderQueueStatus {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: ON_QUEUE
        }
    }
}
