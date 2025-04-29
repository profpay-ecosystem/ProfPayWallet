package com.example.telegramWallet.bridge.view_model.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WalletAddressViewModel @Inject constructor(
    private val addressRepo: AddressRepo,
    private val transactionsRepo: TransactionsRepo,
    val tron: Tron
) : ViewModel() {

    suspend fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens>{
        return addressRepo.getAddressWithTokensByAddressLD(address)
    }

    suspend fun getTransactionsByAddressSenderAndTokenLD(walletId: Long, senderAddress: String, tokenName: String): LiveData<List<TransactionModel>> {
        return transactionsRepo.getTransactionsByAddressSenderAndTokenLD(walletId, senderAddress, tokenName)
    }

    suspend fun getTransactionsByAddressReceiverAndTokenLD(walletId: Long, receiverAddress: String, tokenName: String): LiveData<List<TransactionModel>> {
        return transactionsRepo.getTransactionsByAddressReceiverAndTokenLD(walletId, receiverAddress, tokenName)
    }

    suspend fun getListTransactionToTimestamp(listTransactions: List<TransactionModel>): List<List<TransactionModel?>> {
        var listListTransactions: List<List<TransactionModel>> = listOf(emptyList())

        withContext(Dispatchers.IO) {
            if (listTransactions.isEmpty()) return@withContext
            listListTransactions = listTransactions.sortedByDescending { it.timestamp }
                .groupBy { it.transactionDate }.values.toList()
        }
        return listListTransactions
    }

}