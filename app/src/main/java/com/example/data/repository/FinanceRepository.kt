package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.backup.BackupManager
import com.example.data.backup.HaniJayaBackup
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val backupManager: BackupManager = BackupManager()
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun addTransaction(
        referenceTitle: String,
        foreignValueLabel: String,
        locationRoute: String,
        category: TransactionCategory,
        incomeAmount: Long,
        expenseAmount: Long
    ) {
        transactionDao.insertTransaction(
            TransactionEntity(
                referenceTitle = referenceTitle.trim(),
                foreignValueLabel = foreignValueLabel.trim(),
                locationRoute = locationRoute.trim(),
                category = category.code,
                incomeAmount = incomeAmount.coerceAtLeast(0L),
                expenseAmount = expenseAmount.coerceAtLeast(0L),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun exportBackupToUri(context: Context, uri: Uri): Result<Int> {
        val current = transactionDao.getAllTransactionsOnce()
        val json = backupManager.serializeTransactions(current)
        return backupManager.writeToUri(context, uri, json).map { current.size }
    }

    suspend fun readBackupFromUri(context: Context, uri: Uri): Result<HaniJayaBackup> {
        return backupManager.readFromUri(context, uri)
    }

    suspend fun restoreBackup(backup: HaniJayaBackup, replaceExisting: Boolean): Result<Int> {
        return runCatching {
            val entities = backup.transactions.map { dto ->
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
            if (replaceExisting) {
                transactionDao.replaceAll(entities)
            } else {
                transactionDao.insertAll(entities)
            }
            entities.size
        }
    }
}
