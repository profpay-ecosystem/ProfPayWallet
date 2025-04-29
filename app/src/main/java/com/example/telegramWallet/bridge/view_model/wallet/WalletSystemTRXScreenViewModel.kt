package com.example.telegramWallet.bridge.view_model.wallet

import androidx.lifecycle.ViewModel
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletSystemTRXScreenViewModel @Inject constructor(
    val centralAddressRepo: CentralAddressRepo,
    val transactionsRepo: TransactionsRepo
): ViewModel() {
}