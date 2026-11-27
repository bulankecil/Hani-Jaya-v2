package com.example.data.backup

import com.example.data.local.CnfOfferEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HaniJayaBackup(
    val schemaVersion: Int = 4,
    val appName: String = "Hani Jaya",
    val exportedAt: Long = System.currentTimeMillis(),
    val transactions: List<BackupTransactionDto> = emptyList(),
    val cnfOffers: List<BackupCnfOfferDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BackupTransactionDto(
    val referenceTitle: String,
    val foreignValueLabel: String = "",
    val locationRoute: String = "",
    val category: String = "GENERAL",
    val incomeAmount: Long = 0L,
    val expenseAmount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class BackupCnfOfferDto(
    val offerName: String,
    val serialNumber: String = "",
    val mediatorName: String = "",
    val customerName: String = "",
    val route: String = "",
    val offerDateMillis: Long = System.currentTimeMillis(),
    val exchangeRateIdr: Long = CnfOfferEntity.DEFAULT_EXCHANGE_RATE_IDR,
    val spreadIdr: Long = CnfOfferEntity.DEFAULT_SPREAD_IDR,
    val unitCount: Int = 1,
    val unit1Idr: Long = 0L,
    val unit2Idr: Long = 0L,
    val unit3Idr: Long = 0L,
    val unit4Idr: Long = 0L,
    val unitPriceCostIdr: Long = 0L,
    val mob1Idr: Long = 0L,
    val mob2Idr: Long = 0L,
    val mob3Idr: Long = 0L,
    val mobilisationManualIdr: Long = 0L,
    val mobilisationCostIdr: Long = 0L,
    val stuffingSellIdr: Long = 0L,
    val stuffingCostIdr: Long = 0L,
    val freightContainersSpec: String = CnfOfferEntity.DEFAULT_FREIGHT_SPEC,
    val freightManualIdr: Long = 0L,
    val freightCostIdr: Long = 0L,
    val successFeeMultiplier: Int = 0,
    val othersMultiplier: Int = 0,
    val othersCostIdr: Long = 0L,
    val uangMasukIdr: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
