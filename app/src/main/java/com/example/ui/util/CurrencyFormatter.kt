package com.example.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val indonesianSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val numberFormat = DecimalFormat("#,##0", indonesianSymbols)

    fun formatRupiah(amount: Long): String {
        val absFormatted = numberFormat.format(kotlin.math.abs(amount))
        return if (amount < 0L) "-Rp $absFormatted" else "Rp $absFormatted"
    }

    fun formatBackupFileName(timestampMillis: Long = System.currentTimeMillis()): String {
        val datePart = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date(timestampMillis))
        return "hani_jaya_backup_$datePart.json"
    }

    fun formatDateTime(timestampMillis: Long): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")).format(Date(timestampMillis))
    }
}
