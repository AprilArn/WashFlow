package com.aprilarn.washflow.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    /**
     * Memformat angka menjadi format ribuan Indonesia.
     * Contoh: 60000.0 -> "60.000"
     */
    fun formatRupiah(amount: Double?): String {
        val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return format.format(amount ?: 0.0)
    }

    /**
     * Memformat angka menjadi format mata uang Rupiah dengan simbol.
     * Contoh: 60000.0 -> "Rp 60.000"
     */
    fun formatRupiahWithSymbol(amount: Double?): String {
        return "Rp ${formatRupiah(amount)}"
    }
}
