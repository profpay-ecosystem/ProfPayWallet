package com.example.telegramWallet.security

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object SecureDataStore {
    val Context.secureDataStore by preferencesDataStore(name = "secure_prefs")
    val PIN_CODE_KEY = stringPreferencesKey("pin_code_encrypted_base64")
}
