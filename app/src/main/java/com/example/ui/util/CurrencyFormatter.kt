package com.example.ui.util

import com.example.data.local.CnfOfferEntity
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

    private val usSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val idrFormat = DecimalFormat("#,##0", indonesianSymbols)
    private val usdFormat = DecimalFormat("#,##0", usSymbols)

    fun formatRupiah(amount: Long): String {
        val absFormatted = idrFormat.format(kotlin.math.abs(amount))
        return if (amount < 0L) "-Rp $absFormatted" else "Rp $absFormatted"
    }

    fun formatUsd(amount: Long): String {
        val absFormatted = usdFormat.format(kotlin.math.abs(amount))
        return if (amount < 0L) "-$$absFormatted" else "$$absFormatted"
    }

    fun formatBackupFileName(timestampMillis: Long = System.currentTimeMillis()): String {
        val datePart = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date(timestampMillis))
        return "hani_jaya_backup_$datePart.json"
    }

    fun formatGroupNumber(amount: Long): String {
        return idrFormat.format(amount.coerceAtLeast(0L))
    }

    fun formatDateDayMonthYear(timestampMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(timestampMillis))
    }

    fun formatExcelFileName(offerName: String): String {
        val safeName = offerName.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "penawaran" }
        return "cnf_${safeName}.csv"
    }

    fun formatDateTime(timestampMillis: Long): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")).format(Date(timestampMillis))
    }

    fun formatDateSlashed(timestampMillis: Long): String {
        return SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date(timestampMillis))
    }

    fun formatDateMedium(timestampMillis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale("in", "ID")).format(Date(timestampMillis))
    }

    fun buildOfferExcelCsv(offer: CnfOfferEntity): String {
        fun esc(value: String): String = "\"${value.replace("\"", "\"\"")}\""
        val lines = listOf(
            "CV. HANI JAYA - Heavy Machinery",
            "Laporan Breakdown CnF Calculation",
            "Penawaran,${esc(offer.offerName)}",
            "Customer,${esc(offer.customerName.ifBlank { "-" })}",
            "Serial Number,${esc(offer.serialNumber.ifBlank { "-" })}",
            "Mediator,${esc(offer.mediatorName.ifBlank { "-" })}",
            "Rute,${esc(offer.route.ifBlank { "-" })}",
            "Tanggal,${esc(formatDateMedium(offer.offerDateMillis))}",
            "Kurs Saat Ini (Rp/USD),${offer.safeExchangeRateIdr}",
            "",
            "Komponen Biaya,Harga Jual (Rp),Harga Asli (Rp)",
            "Unit Price,${offer.unitPriceSellIdr},${offer.unitPriceCostIdr}",
            "Mobilisation,${offer.mobilisationSellIdr},${offer.mobilisationCostIdr}",
            "Stuffing,${offer.stuffingSellIdr},${offer.stuffingCostIdr}",
            "Freight,${offer.freightSellIdr},${offer.freightCostIdr}",
            "Success Fee,${offer.successFeeSellIdr},-",
            "Others,${offer.othersSellIdr},${offer.othersCostIdr}",
            "",
            "Ringkasan,USD,IDR",
            "CnF Price,${offer.cnfPriceUsd},${offer.totalSellIdr}",
            "Total Harga Asli,${offer.totalCostUsd},${offer.totalCostIdr}",
            "Harga Deal,${offer.dealPriceUsd},${offer.dealPriceIdr}",
            "Ekspektasi R/L,${offer.profitLossUsd},${offer.profitLossIdr}"
        )
        return lines.joinToString("\n")
    }
}
