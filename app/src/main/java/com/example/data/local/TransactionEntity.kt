package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionCategory(val code: String, val label: String) {
    GENERAL("GENERAL", "Transaksi Umum"),
    CNF_CALCULATION("CNF_CALCULATION", "CnF Calculation"),
    CNF_PRICE("CNF_PRICE", "CnF Price");

    companion object {
        fun fromCode(code: String): TransactionCategory =
            entries.firstOrNull { it.code == code } ?: GENERAL
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceTitle: String,
    val foreignValueLabel: String = "",
    val locationRoute: String = "",
    val category: String = TransactionCategory.GENERAL.code,
    val incomeAmount: Long = 0L,
    val expenseAmount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val netBalance: Long
        get() = incomeAmount - expenseAmount

    val displayTitle: String
        get() = if (foreignValueLabel.isNotBlank()) {
            "$referenceTitle - $foreignValueLabel"
        } else {
            referenceTitle
        }
}
