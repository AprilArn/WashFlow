package com.aprilarn.washflow.domain.model

/**
 * Status antrian pesanan pada layer Domain.
 */
enum class OrderQueueStatus(val label: String, val subTitleId: String) {
    ON_QUEUE("On Queue", "Dalam antrian"),
    ON_PROCESS("On Process", "Sedang diproses"),
    DONE("Done", "Selesai");

    companion object {
        fun fromLabel(label: String?): OrderQueueStatus {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: ON_QUEUE
        }
    }
}
