package com.example.data.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hani_jaya_security"
)

sealed interface PinChangeResult {
    data object Success : PinChangeResult
    data object WrongCurrentPin : PinChangeResult
    data object InvalidNewPin : PinChangeResult
}

class SecurityPreferences(private val context: Context) {

    private val pinHashKey = stringPreferencesKey("wallet_pin_hash")

    val isDefaultPinActive: Flow<Boolean> = context.securityDataStore.data.map { prefs ->
        val stored = prefs[pinHashKey]
        stored == null || stored == hashPin(DEFAULT_PIN)
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.securityDataStore.data.first()
        val storedHash = prefs[pinHashKey] ?: hashPin(DEFAULT_PIN)
        return hashPin(pin) == storedHash
    }

    suspend fun changePin(currentPin: String, newPin: String): PinChangeResult {
        if (newPin.length != PIN_LENGTH || !newPin.all { it.isDigit() }) {
            return PinChangeResult.InvalidNewPin
        }
        if (!verifyPin(currentPin)) {
            return PinChangeResult.WrongCurrentPin
        }
        val newHash = hashPin(newPin)
        context.securityDataStore.edit { prefs ->
            prefs[pinHashKey] = newHash
        }
        return PinChangeResult.Success
    }

    companion object {
        const val DEFAULT_PIN = "123456"
        const val PIN_LENGTH = 6
        private const val SALT = "cv_hani_jaya_offline_pin_v1"

        fun hashPin(pin: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$SALT:$pin".toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
