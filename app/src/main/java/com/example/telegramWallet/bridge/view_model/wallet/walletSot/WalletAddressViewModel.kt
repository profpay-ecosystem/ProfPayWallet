package com.example.telegramWallet.bridge.view_model.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
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

    fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens>{
        return liveData(Dispatchers.IO) {
            emitSource(addressRepo.getAddressWithTokensByAddressLD(address))
        }
    }

    fun getTransactionsByAddressSenderAndTokenLD(walletId: Long, senderAddress: String, tokenName: String): LiveData<List<TransactionModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(transactionsRepo.getTransactionsByAddressSenderAndTokenLD(walletId, senderAddress, tokenName))
        }
    }

    fun getTransactionsByAddressReceiverAndTokenLD(walletId: Long, receiverAddress: String, tokenName: String): LiveData<List<TransactionModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(transactionsRepo.getTransactionsByAddressReceiverAndTokenLD(walletId, receiverAddress, tokenName))
        }
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