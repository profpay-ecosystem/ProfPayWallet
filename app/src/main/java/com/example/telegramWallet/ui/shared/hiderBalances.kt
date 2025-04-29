package com.example.telegramWallet.ui.shared

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.telegramWallet.R
import com.example.telegramWallet.data.utils.toTokenAmount
import java.math.BigInteger

@Composable
fun hiderBalances(balance: BigInteger): String {
    val sharedHideBalances: SharedPreferences = sharedPref()
    return if (sharedHideBalances.getBoolean(stringResource(R.string.IS_HIDDEN_BALANCES), false)) {
        "****"
    } else balance.toTokenAmount().toString()
}



