package com.example.data.backup

import android.content.Context
import android.net.Uri
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

    fun serializeTransactions(transactions: List<TransactionEntity>): String {
        val dtoList = transactions.map { entity ->
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
        val backup = HaniJayaBackup(
            schemaVersion = 1,
            appName = "Hani Jaya",
            exportedAt = System.currentTimeMillis(),
            transactions = dtoList
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
            val cleaned = parsed.transactions.map { item ->
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
            parsed.copy(transactions = cleaned)
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
