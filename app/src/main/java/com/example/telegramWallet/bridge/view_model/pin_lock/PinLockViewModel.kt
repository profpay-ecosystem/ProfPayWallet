package com.example.telegramWallet.bridge.view_model.pin_lock

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.data.services.AppLockManager
import com.example.telegramWallet.security.KeystoreEncryptionUtils
import com.example.telegramWallet.security.SecureDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PinLockViewModel @Inject constructor(@ApplicationContext context: Context, private val dataStore: DataStore<Preferences>) : ViewModel() {
    private val _navigationState = MutableStateFlow<LockState>(LockState.None)
    private val keystore = KeystoreEncryptionUtils()
    val navigationState: StateFlow<LockState> = _navigationState.asStateFlow()

    private fun launchIO(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        block()
    }

    fun checkPinState() = launchIO {
        val savedPin = dataStore.data
            .map { it[SecureDataStore.PIN_CODE_KEY] }
            .firstOrNull()

        _navigationState.value = if (AppLockManager.isAppLocked()) {
            if (savedPin == null) LockState.RequireCreation else LockState.RequireUnlock
        } else {
            LockState.None
        }
    }

    fun unlockSession() {
        AppLockManager.unlock()
        _navigationState.value = LockState.None
    }

    fun saveNewPin(pin: String) = launchIO {
        val encrypted = keystore.encrypt(pin.toByteArray())
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)

        dataStore.edit { prefs ->
            prefs[SecureDataStore.PIN_CODE_KEY] = encoded
        }

        unlockSession()
    }

    fun validatePin(entered: String, callback: (Boolean) -> Unit) = launchIO {
        val saved = dataStore.data.map { it[SecureDataStore.PIN_CODE_KEY] }.firstOrNull()
        val isCorrect = saved?.let {
            try {
                val decryptedBytes = keystore.decrypt(Base64.decode(it, Base64.DEFAULT))
                val decryptedPin = String(decryptedBytes)
                decryptedPin == entered
            } catch (e: Exception) {
                false
            }
        } ?: false

        withContext(Dispatchers.Main) {
            callback(isCorrect)
        }
    }
}

enum class LockState {
    RequireUnlock,
    RequireCreation,
    None
}