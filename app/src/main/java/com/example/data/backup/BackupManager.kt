package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.CnfOfferEntity
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupManager {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(HaniJayaBackup::class.java).indent("  ")

    fun serializeTransactions(
        transactions: List<TransactionEntity>,
        cnfOffers: List<CnfOfferEntity> = emptyList()
    ): String {
        val txDtos = transactions.map { entity ->
            BackupTransactionDto(
                referenceTitle = entity.referenceTitle,
                foreignValueLabel = entity.foreignValueLabel,
                locationRoute = entity.locationRoute,
                category = TransactionCategory.fromCode(entity.category).code,
                incomeAmount = entity.incomeAmount.coerceAtLeast(0L),
                expenseAmount = entity.expenseAmount.coerceAtLeast(0L),
                createdAt = entity.createdAt
            )
        }
        val offerDtos = cnfOffers.map { offer ->
            BackupCnfOfferDto(
                offerName = offer.offerName,
                serialNumber = offer.serialNumber,
                mediatorName = offer.mediatorName,
                customerName = offer.customerName,
                route = offer.route,
                offerDateMillis = offer.offerDateMillis,
                exchangeRateIdr = offer.safeExchangeRateIdr,
                spreadIdr = offer.spreadIdr.coerceAtLeast(0L),
                unitCount = offer.unitCount.coerceIn(1, 4),
                unit1Idr = offer.unit1Idr.coerceAtLeast(0L),
                unit2Idr = offer.unit2Idr.coerceAtLeast(0L),
                unit3Idr = offer.unit3Idr.coerceAtLeast(0L),
                unit4Idr = offer.unit4Idr.coerceAtLeast(0L),
                unitPriceCostIdr = offer.unitPriceCostIdr.coerceAtLeast(0L),
                mob1Idr = offer.mob1Idr.coerceAtLeast(0L),
                mob2Idr = offer.mob2Idr.coerceAtLeast(0L),
                mob3Idr = offer.mob3Idr.coerceAtLeast(0L),
                mobilisationManualIdr = offer.mobilisationManualIdr.coerceAtLeast(0L),
                mobilisationCostIdr = offer.mobilisationCostIdr.coerceAtLeast(0L),
                stuffingSellIdr = offer.stuffingSellIdr.coerceAtLeast(0L),
                stuffingCostIdr = offer.stuffingCostIdr.coerceAtLeast(0L),
                freightContainersSpec = offer.freightContainersSpec,
                freightManualIdr = offer.freightManualIdr.coerceAtLeast(0L),
                freightCostIdr = offer.freightCostIdr.coerceAtLeast(0L),
                successFeeMultiplier = offer.successFeeMultiplier.coerceAtLeast(0),
                othersMultiplier = offer.othersMultiplier.coerceAtLeast(0),
                othersCostIdr = offer.othersCostIdr.coerceAtLeast(0L),
                uangMasukIdr = offer.uangMasukIdr.coerceAtLeast(0L),
                createdAt = offer.createdAt
            )
        }
        val backup = HaniJayaBackup(
            schemaVersion = 4,
            appName = "Hani Jaya",
            exportedAt = System.currentTimeMillis(),
            transactions = txDtos,
            cnfOffers = offerDtos
        )
        return adapter.toJson(backup)
    }

    fun parseBackupJson(json: String): Result<HaniJayaBackup> {
        return runCatching {
            val parsed = adapter.fromJson(json)
                ?: throw IllegalArgumentException("Format file JSON tidak dikenali.")
            if (parsed.schemaVersion < 1) {
                throw IllegalArgumentException("Versi backup tidak valid.")
            }
            val cleanedTransactions = parsed.transactions.map { item ->
                if (item.referenceTitle.isBlank()) {
                    throw IllegalArgumentException("Terdapat transaksi tanpa judul referensi.")
                }
                item.copy(
                    referenceTitle = item.referenceTitle.trim(),
                    foreignValueLabel = item.foreignValueLabel.trim(),
                    locationRoute = item.locationRoute.trim(),
                    category = TransactionCategory.fromCode(item.category).code,
                    incomeAmount = item.incomeAmount.coerceAtLeast(0L),
                    expenseAmount = item.expenseAmount.coerceAtLeast(0L)
                )
            }
            val cleanedOffers = parsed.cnfOffers.map { offer ->
                if (offer.offerName.isBlank()) {
                    throw IllegalArgumentException("Terdapat penawaran CnF tanpa nama penawaran.")
                }
                offer.copy(
                    offerName = offer.offerName.trim(),
                    serialNumber = offer.serialNumber.trim(),
                    mediatorName = offer.mediatorName.trim(),
                    customerName = offer.customerName.trim(),
                    route = offer.route.trim(),
                    exchangeRateIdr = offer.exchangeRateIdr.coerceAtLeast(1L),
                    spreadIdr = offer.spreadIdr.coerceAtLeast(0L),
                    unitCount = offer.unitCount.coerceIn(1, 4),
                    unit1Idr = offer.unit1Idr.coerceAtLeast(0L),
                    unit2Idr = offer.unit2Idr.coerceAtLeast(0L),
                    unit3Idr = offer.unit3Idr.coerceAtLeast(0L),
                    unit4Idr = offer.unit4Idr.coerceAtLeast(0L),
                    unitPriceCostIdr = offer.unitPriceCostIdr.coerceAtLeast(0L),
                    mob1Idr = offer.mob1Idr.coerceAtLeast(0L),
                    mob2Idr = offer.mob2Idr.coerceAtLeast(0L),
                    mob3Idr = offer.mob3Idr.coerceAtLeast(0L),
                    mobilisationManualIdr = offer.mobilisationManualIdr.coerceAtLeast(0L),
                    mobilisationCostIdr = offer.mobilisationCostIdr.coerceAtLeast(0L),
                    stuffingSellIdr = offer.stuffingSellIdr.coerceAtLeast(0L),
                    stuffingCostIdr = offer.stuffingCostIdr.coerceAtLeast(0L),
                    freightManualIdr = offer.freightManualIdr.coerceAtLeast(0L),
                    freightCostIdr = offer.freightCostIdr.coerceAtLeast(0L),
                    successFeeMultiplier = offer.successFeeMultiplier.coerceAtLeast(0),
                    othersMultiplier = offer.othersMultiplier.coerceAtLeast(0),
                    othersCostIdr = offer.othersCostIdr.coerceAtLeast(0L),
                    uangMasukIdr = offer.uangMasukIdr.coerceAtLeast(0L)
                )
            }
            parsed.copy(
                transactions = cleanedTransactions,
                cnfOffers = cleanedOffers
            )
        }
    }

    suspend fun writeToUri(context: Context, uri: Uri, json: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: error("Gagal membuka tujuan file ekspor.")
            }
        }

    suspend fun writeCsvToUri(context: Context, uri: Uri, csvContent: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
                    stream.write(utf8Bom)
                    stream.write(csvContent.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: error("Gagal menyimpan file Excel CSV.")
            }
        }

    suspend fun readFromUri(context: Context, uri: Uri): Result<HaniJayaBackup> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: error("Gagal membaca file impor.")
                parseBackupJson(raw).getOrThrow()
            }
        }
}
