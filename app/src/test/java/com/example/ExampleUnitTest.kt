package com.example

import com.example.data.backup.BackupManager
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
    fun currencyFormatter_formatsIndonesianRupiah() {
        assertEquals("Rp 0", CurrencyFormatter.formatRupiah(0L))
        assertEquals("Rp 322.218.562", CurrencyFormatter.formatRupiah(322_218_562L))
        assertEquals("-Rp 50.000", CurrencyFormatter.formatRupiah(-50_000L))
    }

    @Test
    fun backupManager_roundTripJsonPreservesTransactions() {
        val manager = BackupManager()
        val input = listOf(
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
        val json = manager.serializeTransactions(input)
        val parsed = manager.parseBackupJson(json)
        assertTrue(parsed.isSuccess)
        val backup = parsed.getOrThrow()
        assertEquals(1, backup.transactions.size)
        assertEquals("26 020 311D - 024 MTEC", backup.transactions.first().referenceTitle)
        assertEquals(354_686_500L, backup.transactions.first().incomeAmount)
        assertEquals(TransactionCategory.CNF_PRICE.code, backup.transactions.first().category)
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
