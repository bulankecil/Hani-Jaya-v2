package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.HaniJayaBackup
import com.example.data.repository.FinanceRepository
import com.example.data.security.PinChangeResult
import com.example.data.security.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WalletUiState(
    val pinInput: String = "",
    val pinError: String? = null,
    val isUnlocked: Boolean = false,
    val isDefaultPinActive: Boolean = true,
    val totalTransactions: Int = 0,
    val netBalance: Long = 0L,
    val isBusy: Boolean = false,
    val pendingRestoreBackup: HaniJayaBackup? = null,
    val statusMessage: String? = null,
    val isErrorStatus: Boolean = false,
    val pinChangeMessage: String? = null,
    val isPinChangeSuccess: Boolean = false
)

class WalletViewModel(
    private val repository: FinanceRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _pinInput = MutableStateFlow("")
    private val _pinError = MutableStateFlow<String?>(null)
    private val _isUnlocked = MutableStateFlow(false)
    private val _isBusy = MutableStateFlow(false)
    private val _pendingRestore = MutableStateFlow<HaniJayaBackup?>(null)
    private val _status = MutableStateFlow<Pair<String?, Boolean>>(null to false)
    private val _pinChangeStatus = MutableStateFlow<Pair<String?, Boolean>>(null to false)

    val uiState: StateFlow<WalletUiState> = combine(
        _pinInput,
        _pinError,
        _isUnlocked,
        securityPreferences.isDefaultPinActive,
        repository.allTransactions,
        _isBusy,
        _pendingRestore,
        _status,
        _pinChangeStatus
    ) { values ->
        val pinInput = values[0] as String
        val pinError = values[1] as String?
        val isUnlocked = values[2] as Boolean
        val isDefaultPin = values[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val transactions = values[4] as List<com.example.data.local.TransactionEntity>
        val isBusy = values[5] as Boolean
        val pendingRestore = values[6] as HaniJayaBackup?
        @Suppress("UNCHECKED_CAST")
        val status = values[7] as Pair<String?, Boolean>
        @Suppress("UNCHECKED_CAST")
        val pinChange = values[8] as Pair<String?, Boolean>

        val totalIncome = transactions.sumOf { it.incomeAmount }
        val totalExpense = transactions.sumOf { it.expenseAmount }

        WalletUiState(
            pinInput = pinInput,
            pinError = pinError,
            isUnlocked = isUnlocked,
            isDefaultPinActive = isDefaultPin,
            totalTransactions = transactions.size,
            netBalance = totalIncome - totalExpense,
            isBusy = isBusy,
            pendingRestoreBackup = pendingRestore,
            statusMessage = status.first,
            isErrorStatus = status.second,
            pinChangeMessage = pinChange.first,
            isPinChangeSuccess = pinChange.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WalletUiState()
    )

    fun onDigitPressed(digit: Char) {
        if (!digit.isDigit() || _isUnlocked.value) return
        val current = _pinInput.value
        if (current.length >= SecurityPreferences.PIN_LENGTH) return

        val updated = current + digit
        _pinInput.value = updated
        _pinError.value = null

        if (updated.length == SecurityPreferences.PIN_LENGTH) {
            verifyEnteredPin(updated)
        }
    }

    fun onBackspacePressed() {
        val current = _pinInput.value
        if (current.isNotEmpty()) {
            _pinInput.value = current.dropLast(1)
            _pinError.value = null
        }
    }

    fun onClearPin() {
        _pinInput.value = ""
        _pinError.value = null
    }

    private fun verifyEnteredPin(pin: String) {
        viewModelScope.launch {
            val valid = securityPreferences.verifyPin(pin)
            if (valid) {
                _isUnlocked.value = true
                _pinInput.value = ""
                _pinError.value = null
            } else {
                _pinInput.value = ""
                _pinError.value = "PIN salah. Silakan coba lagi."
            }
        }
    }

    fun lockWallet() {
        _isUnlocked.value = false
        _pinInput.value = ""
        _pinError.value = null
        _status.value = null to false
        _pinChangeStatus.value = null to false
        _pendingRestore.value = null
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.exportBackupToUri(context, uri)
            _isBusy.value = false
            result.fold(
                onSuccess = { count ->
                    _status.value = "Berhasil mengekspor $count data (transaksi & penawaran CnF) ke file JSON." to false
                },
                onFailure = { err ->
                    _status.value = (err.message ?: "Gagal mengekspor data.") to true
                }
            )
        }
    }

    fun prepareImportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.readBackupFromUri(context, uri)
            _isBusy.value = false
            result.fold(
                onSuccess = { backup ->
                    _pendingRestore.value = backup
                },
                onFailure = { err ->
                    _status.value = (err.message ?: "File JSON backup tidak valid.") to true
                }
            )
        }
    }

    fun confirmImportBackup(replaceExisting: Boolean) {
        val backup = _pendingRestore.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            _pendingRestore.value = null
            val result = repository.restoreBackup(backup, replaceExisting)
            _isBusy.value = false
            result.fold(
                onSuccess = { count ->
                    val mode = if (replaceExisting) "dipulihkan" else "ditambahkan"
                    _status.value = "Berhasil: $count data $mode ke database lokal." to false
                },
                onFailure = { err ->
                    _status.value = (err.message ?: "Gagal mengimpor data.") to true
                }
            )
        }
    }

    fun dismissImportPreview() {
        _pendingRestore.value = null
    }

    fun clearStatusMessage() {
        _status.value = null to false
    }

    fun updatePin(currentPin: String, newPin: String, confirmPin: String) {
        if (newPin.length != SecurityPreferences.PIN_LENGTH || !newPin.all { it.isDigit() }) {
            _pinChangeStatus.value = "PIN baru harus terdiri dari 6 digit angka." to false
            return
        }
        if (newPin != confirmPin) {
            _pinChangeStatus.value = "Konfirmasi PIN baru tidak cocok." to false
            return
        }
        viewModelScope.launch {
            when (securityPreferences.changePin(currentPin, newPin)) {
                PinChangeResult.Success -> {
                    _pinChangeStatus.value = "PIN keamanan berhasil diperbarui." to true
                }
                PinChangeResult.WrongCurrentPin -> {
                    _pinChangeStatus.value = "PIN saat ini salah." to false
                }
                PinChangeResult.InvalidNewPin -> {
                    _pinChangeStatus.value = "PIN baru harus 6 digit angka." to false
                }
            }
        }
    }

    fun clearPinChangeMessage() {
        _pinChangeStatus.value = null to false
    }

    class Factory(
        private val repository: FinanceRepository,
        private val securityPreferences: SecurityPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WalletViewModel(repository, securityPreferences) as T
        }
    }
}
