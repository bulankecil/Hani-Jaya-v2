package com.example.data.backup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HaniJayaBackup(
    val schemaVersion: Int = 1,
    val appName: String = "Hani Jaya",
    val exportedAt: Long = System.currentTimeMillis(),
    val transactions: List<BackupTransactionDto> = emptyList()
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
