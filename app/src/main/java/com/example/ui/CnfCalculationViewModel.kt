package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CnfOfferEntity
import com.example.data.repository.FinanceRepository
import com.example.ui.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CnfCalculationUiState(
    val offers: List<CnfOfferEntity> = emptyList(),
    val expandedOfferIds: Set<Long> = emptySet(),
    val idrModeOfferIds: Set<Long> = emptySet(),
    val totalExpectedProfitIdr: Long = 0L,
    val feedbackMessage: String? = null
)

class CnfCalculationViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _expandedOfferIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _idrModeOfferIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _feedbackMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.ensurePreviewCnfOffersSeeded()
        }
    }

    val uiState: StateFlow<CnfCalculationUiState> = combine(
        repository.allCnfOffers,
        _expandedOfferIds,
        _idrModeOfferIds,
        _feedbackMessage
    ) { offers, expandedIds, idrIds, feedback ->
        val totalProfitIdr = offers.sumOf { it.profitLossIdr }
        CnfCalculationUiState(
            offers = offers,
            expandedOfferIds = expandedIds,
            idrModeOfferIds = idrIds,
            totalExpectedProfitIdr = totalProfitIdr,
            feedbackMessage = feedback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CnfCalculationUiState()
    )

    fun toggleCardExpanded(offerId: Long) {
        _expandedOfferIds.update { current ->
            if (offerId in current) current - offerId else current + offerId
        }
    }

    fun toggleCurrencyMode(offerId: Long) {
        _idrModeOfferIds.update { current ->
            if (offerId in current) current - offerId else current + offerId
        }
    }

    fun createOffer(
        offerName: String,
        serialNumber: String,
        mediatorName: String,
        customerName: String,
        route: String,
        offerDateMillis: Long
    ) {
        viewModelScope.launch {
            repository.addCnfOffer(
                offerName = offerName,
                serialNumber = serialNumber,
                mediatorName = mediatorName,
                customerName = customerName,
                route = route,
                offerDateMillis = offerDateMillis
            )
        }
    }

    fun updateOfferBreakdown(updatedOffer: CnfOfferEntity) {
        viewModelScope.launch {
            repository.updateCnfOffer(updatedOffer)
            _feedbackMessage.value = "Breakdown \"${updatedOffer.displayHeader}\" berhasil disimpan."
        }
    }

    fun deleteOffer(offerId: Long) {
        viewModelScope.launch {
            repository.deleteCnfOffer(offerId)
            _expandedOfferIds.update { it - offerId }
            _idrModeOfferIds.update { it - offerId }
            _feedbackMessage.value = "Penawaran berhasil dihapus dari database."
        }
    }

    fun exportOfferCsv(context: Context, uri: Uri, offer: CnfOfferEntity) {
        viewModelScope.launch {
            val csv = CurrencyFormatter.buildOfferExcelCsv(offer)
            val result = repository.exportOfferCsvToUri(context, uri, csv)
            _feedbackMessage.value = result.fold(
                onSuccess = { "File Excel (CSV) \"${offer.offerName}\" berhasil disimpan." },
                onFailure = { it.message ?: "Gagal mengekspor file Excel." }
            )
        }
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    class Factory(
        private val repository: FinanceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CnfCalculationViewModel(repository) as T
        }
    }
}
