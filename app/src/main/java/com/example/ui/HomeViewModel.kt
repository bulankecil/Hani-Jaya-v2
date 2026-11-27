package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val netBalance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val cnfCalculationCount: Int = 0,
    val cnfPriceCount: Int = 0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.allTransactions
        .map { list ->
            val income = list.sumOf { it.incomeAmount }
            val expense = list.sumOf { it.expenseAmount }
            HomeUiState(
                netBalance = income - expense,
                totalIncome = income,
                totalExpense = expense,
                cnfCalculationCount = list.count { it.category == TransactionCategory.CNF_CALCULATION.code },
                cnfPriceCount = list.count { it.category == TransactionCategory.CNF_PRICE.code },
                recentTransactions = list.take(5),
                allTransactions = list,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun addTransaction(
        referenceTitle: String,
        foreignValueLabel: String,
        locationRoute: String,
        category: TransactionCategory,
        incomeAmount: Long,
        expenseAmount: Long
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                referenceTitle = referenceTitle,
                foreignValueLabel = foreignValueLabel,
                locationRoute = locationRoute,
                category = category,
                incomeAmount = incomeAmount,
                expenseAmount = expenseAmount
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    class Factory(
        private val repository: FinanceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
