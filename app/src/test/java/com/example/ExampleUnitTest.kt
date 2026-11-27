package com.example

import com.example.data.backup.BackupManager
import com.example.data.local.CnfOfferEntity
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.data.security.SecurityPreferences
import com.example.ui.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun currencyFormatter_formatsIndonesianRupiahAndUsd() {
        assertEquals("Rp 0", CurrencyFormatter.formatRupiah(0L))
        assertEquals("Rp 322.218.562", CurrencyFormatter.formatRupiah(322_218_562L))
        assertEquals("-Rp 50.000", CurrencyFormatter.formatRupiah(-50_000L))
        assertEquals("$46,191", CurrencyFormatter.formatUsd(46_191L))
        assertEquals("$0", CurrencyFormatter.formatUsd(0L))
    }

    @Test
    fun cnfOfferFormulas_matchSpecification() {
        val offer = CnfOfferEntity(
            offerName = "SV512D ACH JKT",
            customerName = "MTEC",
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
            uangMasukIdr = 737_622_000L
        )
        assertEquals(17_154L, offer.effectiveRateIdr)
        assertEquals(710_000_000L, offer.unitPriceSellIdr)
        assertEquals(34_308_000L, offer.successFeeSellIdr)
        assertEquals(17_154_000L, offer.othersSellIdr)
        assertEquals(15_438_600L, offer.freightAutoIdr)
        assertEquals(20_890_790L, offer.freightSellIdr)
        assertEquals(792_352_790L, offer.totalSellIdr)
        assertEquals(748_500_000L, offer.totalCostIdr)
        assertEquals(43_000L, offer.dealPriceUsd)
        assertEquals(46_191L, offer.cnfPriceUsd)
        assertEquals(43_634L, offer.totalCostUsd)
        assertEquals(2_000L, offer.successFeeUsd)
        assertEquals(1_366L, offer.profitLossUsd)
    }

    @Test
    fun backupManager_roundTripJsonPreservesTransactionsAndCnfOffers() {
        val manager = BackupManager()
        val txInput = listOf(
            TransactionEntity(
                id = 1L,
                referenceTitle = "26 020 311D - 024 MTEC",
                foreignValueLabel = "$20,000",
                locationRoute = "Banjarmasin - Hai Phong",
                category = TransactionCategory.CNF_PRICE.code,
                incomeAmount = 354_686_500L,
                expenseAmount = 320_397_791L,
                createdAt = 1_700_000_000_000L
            )
        )
        val offerInput = listOf(
            CnfOfferEntity(
                id = 2L,
                offerName = "Penawaran Container 40HC",
                serialNumber = "SN-001",
                mediatorName = "Budi",
                customerName = "PT Maju",
                route = "Semarang - Singapura",
                exchangeRateIdr = 17_500L,
                spreadIdr = 1_000L,
                unitCount = 2,
                unit1Idr = 200_000_000L,
                unit2Idr = 250_000_000L,
                unitPriceCostIdr = 420_000_000L,
                successFeeMultiplier = 2,
                othersMultiplier = 1,
                uangMasukIdr = 495_000_000L,
                offerDateMillis = 1_700_000_000_000L
            )
        )
        val json = manager.serializeTransactions(txInput, offerInput)
        val parsed = manager.parseBackupJson(json)
        assertTrue(parsed.isSuccess)
        val backup = parsed.getOrThrow()
        assertEquals(1, backup.transactions.size)
        assertEquals(1, backup.cnfOffers.size)
        val restored = backup.cnfOffers.first()
        assertEquals(200_000_000L, restored.unit1Idr)
        assertEquals(250_000_000L, restored.unit2Idr)
        assertEquals(17_500L, restored.exchangeRateIdr)
        assertEquals(1_000L, restored.spreadIdr)
        assertEquals(2, restored.unitCount)
        assertEquals(495_000_000L, restored.uangMasukIdr)
    }

    @Test
    fun pinHasher_isDeterministicAndDistinct() {
        val hashA = SecurityPreferences.hashPin("123456")
        val hashB = SecurityPreferences.hashPin("123456")
        val hashC = SecurityPreferences.hashPin("654321")
        assertEquals(hashA, hashB)
        assertNotEquals(hashA, hashC)
    }
}
