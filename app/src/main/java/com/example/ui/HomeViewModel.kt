package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    init {
        viewModelScope.launch {
            repository.ensurePreviewCnfOffersSeeded()
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allTransactions,
        repository.allCnfOffers
    ) { transactions, cnfOffers ->
        val income = transactions.sumOf { it.incomeAmount }
        val expense = transactions.sumOf { it.expenseAmount }
        HomeUiState(
            netBalance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            cnfCalculationCount = cnfOffers.size,
            cnfPriceCount = transactions.count { it.category == TransactionCategory.CNF_PRICE.code },
            recentTransactions = transactions.take(5),
            allTransactions = transactions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

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
