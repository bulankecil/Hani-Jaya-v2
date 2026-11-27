package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.backup.BackupManager
import com.example.data.backup.HaniJayaBackup
import com.example.data.local.CnfOfferDao
import com.example.data.local.CnfOfferEntity
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.security.SecurityPreferences
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val cnfOfferDao: CnfOfferDao,
    private val securityPreferences: SecurityPreferences,
    private val backupManager: BackupManager = BackupManager()
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCnfOffers: Flow<List<CnfOfferEntity>> = cnfOfferDao.getAllOffers()

    suspend fun ensurePreviewCnfOffersSeeded() {
        if (securityPreferences.hasSeededCnfPreview()) return
        if (cnfOfferDao.getOfferCount() == 0) {
            val now = System.currentTimeMillis()
            cnfOfferDao.insertAll(
                listOf(
                    CnfOfferEntity(
                        offerName = "SV512D ACH JKT",
                        serialNumber = "SN-SV512D-01",
                        mediatorName = "",
                        customerName = "MTEC",
                        route = "Banjarmasin - Hai Phong",
                        offerDateMillis = now,
                        exchangeRateIdr = 18_154L,
                        spreadIdr = 1_000L,
                        unitCount = 2,
                        unit1Idr = 350_000_000L,
                        unit2Idr = 360_000_000L,
                        unitPriceCostIdr = 710_000_000L,
                        mob1Idr = 0L,
                        mob2Idr = 0L,
                        mob3Idr = 0L,
                        mobilisationManualIdr = 0L,
                        mobilisationCostIdr = 0L,
                        stuffingSellIdr = 10_000_000L,
                        stuffingCostIdr = 7_500_000L,
                        freightContainersSpec = "40'HC|900|1",
                        freightManualIdr = 20_890_790L,
                        freightCostIdr = 21_000_000L,
                        successFeeMultiplier = 2,
                        othersMultiplier = 1,
                        othersCostIdr = 10_000_000L,
                        uangMasukIdr = 737_622_000L,
                        createdAt = now
                    ),
                    CnfOfferEntity(
                        offerName = "966F",
                        serialNumber = "SN-966F-02",
                        mediatorName = "",
                        customerName = "Wali",
                        route = "Tarakan - Karachi",
                        offerDateMillis = now - 86_400_000L,
                        exchangeRateIdr = 17_000L,
                        spreadIdr = 1_000L,
                        unitCount = 1,
                        unit1Idr = 360_000_000L,
                        unitPriceCostIdr = 350_000_000L,
                        mob1Idr = 5_000_000L,
                        mob2Idr = 3_000_000L,
                        mob3Idr = 0L,
                        mobilisationManualIdr = 0L,
                        mobilisationCostIdr = 6_000_000L,
                        stuffingSellIdr = 5_000_000L,
                        stuffingCostIdr = 4_000_000L,
                        freightContainersSpec = "40'HC|937|1",
                        freightManualIdr = 15_000_000L,
                        freightCostIdr = 14_680_000L,
                        successFeeMultiplier = 1,
                        othersMultiplier = 0,
                        othersCostIdr = 7_000_000L,
                        uangMasukIdr = 400_000_000L,
                        createdAt = now - 86_400_000L
                    )
                )
            )
        }
        securityPreferences.markCnfPreviewSeeded()
    }

    suspend fun addCnfOffer(
        offerName: String,
        serialNumber: String,
        mediatorName: String,
        customerName: String,
        route: String,
        offerDateMillis: Long
    ) {
        cnfOfferDao.insertOffer(
            CnfOfferEntity(
                offerName = offerName.trim(),
                serialNumber = serialNumber.trim(),
                mediatorName = mediatorName.trim(),
                customerName = customerName.trim(),
                route = route.trim(),
                offerDateMillis = offerDateMillis,
                exchangeRateIdr = CnfOfferEntity.DEFAULT_EXCHANGE_RATE_IDR,
                spreadIdr = CnfOfferEntity.DEFAULT_SPREAD_IDR,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCnfOffer(offer: CnfOfferEntity) {
        cnfOfferDao.updateOffer(offer)
    }

    suspend fun deleteCnfOffer(id: Long) {
        cnfOfferDao.deleteOfferById(id)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun exportOfferCsvToUri(context: Context, uri: Uri, csvContent: String): Result<Unit> {
        return backupManager.writeCsvToUri(context, uri, csvContent)
    }

    suspend fun exportBackupToUri(context: Context, uri: Uri): Result<Int> {
        val currentTx = transactionDao.getAllTransactionsOnce()
        val currentOffers = cnfOfferDao.getAllOffersOnce()
        val json = backupManager.serializeTransactions(currentTx, currentOffers)
        return backupManager.writeToUri(context, uri, json).map {
            currentTx.size + currentOffers.size
        }
    }

    suspend fun readBackupFromUri(context: Context, uri: Uri): Result<HaniJayaBackup> {
        return backupManager.readFromUri(context, uri)
    }

    suspend fun restoreBackup(backup: HaniJayaBackup, replaceExisting: Boolean): Result<Int> {
        return runCatching {
            val txEntities = backup.transactions.map { dto ->
                TransactionEntity(
                    referenceTitle = dto.referenceTitle,
                    foreignValueLabel = dto.foreignValueLabel,
                    locationRoute = dto.locationRoute,
                    category = TransactionCategory.fromCode(dto.category).code,
                    incomeAmount = dto.incomeAmount,
                    expenseAmount = dto.expenseAmount,
                    createdAt = dto.createdAt
                )
            }
            val offerEntities = backup.cnfOffers.map { dto ->
                CnfOfferEntity(
                    offerName = dto.offerName,
                    serialNumber = dto.serialNumber,
                    mediatorName = dto.mediatorName,
                    customerName = dto.customerName,
                    route = dto.route,
                    offerDateMillis = dto.offerDateMillis,
                    exchangeRateIdr = dto.exchangeRateIdr.coerceAtLeast(1L),
                    spreadIdr = dto.spreadIdr.coerceAtLeast(0L),
                    unitCount = dto.unitCount.coerceIn(1, 4),
                    unit1Idr = dto.unit1Idr.coerceAtLeast(0L),
                    unit2Idr = dto.unit2Idr.coerceAtLeast(0L),
                    unit3Idr = dto.unit3Idr.coerceAtLeast(0L),
                    unit4Idr = dto.unit4Idr.coerceAtLeast(0L),
                    unitPriceCostIdr = dto.unitPriceCostIdr.coerceAtLeast(0L),
                    mob1Idr = dto.mob1Idr.coerceAtLeast(0L),
                    mob2Idr = dto.mob2Idr.coerceAtLeast(0L),
                    mob3Idr = dto.mob3Idr.coerceAtLeast(0L),
                    mobilisationManualIdr = dto.mobilisationManualIdr.coerceAtLeast(0L),
                    mobilisationCostIdr = dto.mobilisationCostIdr.coerceAtLeast(0L),
                    stuffingSellIdr = dto.stuffingSellIdr.coerceAtLeast(0L),
                    stuffingCostIdr = dto.stuffingCostIdr.coerceAtLeast(0L),
                    freightContainersSpec = dto.freightContainersSpec,
                    freightManualIdr = dto.freightManualIdr.coerceAtLeast(0L),
                    freightCostIdr = dto.freightCostIdr.coerceAtLeast(0L),
                    successFeeMultiplier = dto.successFeeMultiplier.coerceAtLeast(0),
                    othersMultiplier = dto.othersMultiplier.coerceAtLeast(0),
                    othersCostIdr = dto.othersCostIdr.coerceAtLeast(0L),
                    uangMasukIdr = dto.uangMasukIdr.coerceAtLeast(0L),
                    createdAt = dto.createdAt
                )
            }
            if (replaceExisting) {
                transactionDao.replaceAll(txEntities)
                cnfOfferDao.replaceAll(offerEntities)
            } else {
                if (txEntities.isNotEmpty()) transactionDao.insertAll(txEntities)
                if (offerEntities.isNotEmpty()) cnfOfferDao.insertAll(offerEntities)
            }
            securityPreferences.markCnfPreviewSeeded()
            txEntities.size + offerEntities.size
        }
    }
}
