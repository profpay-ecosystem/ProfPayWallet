package com.example.telegramWallet.ui.shared

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.telegramWallet.R

@Composable
fun isDarkTheme(): Boolean {
    val sharedPref = LocalContext.current.getSharedPreferences(
        ContextCompat.getString(LocalContext.current, R.string.preference_file_key),
        Context.MODE_PRIVATE
    )
    val themeShared =
        sharedPref.getInt(stringResource(R.string.VALUE_THEME), 2)
    var isDarkTheme = false
    when (themeShared) {
        0 -> isDarkTheme = false
        1 -> isDarkTheme = true
        2 -> isDarkTheme = isSystemInDarkTheme()
    }
    return isDarkTheme
}