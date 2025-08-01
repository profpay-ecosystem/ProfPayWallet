package com.example.telegramWallet.data.database.repositories

import androidx.lifecycle.LiveData
import com.example.telegramWallet.data.database.dao.TransactionsDao
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.models.TransactionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface TransactionsRepo {
    suspend fun insertNewTransaction(transactionsEntity: TransactionEntity)
    suspend fun transactionExistsViaTxid(txid: String): Int
    suspend fun getAllRelatedTransactions(walletId: Long): LiveData<List<TransactionModel>>
    suspend fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity>
    suspend fun transactionSetProcessedUpdateTrueById(id: Long)
    suspend fun transactionSetProcessedUpdateFalseByTxId(txId: String)
    suspend fun getTransactionByTxId(txId: String): TransactionEntity
    suspend fun transactionSetProcessedUpdateFalseById(id: Long)
    suspend fun transactionSetProcessedUpdateTrueByTxId(txId: String)
    suspend fun getTransactionsByAddressAndTokenLD(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean
    ): LiveData<List<TransactionModel>>
}

@Singleton
class TransactionsRepoImpl @Inject constructor(private val transactionsDao: TransactionsDao) :
    TransactionsRepo {
    override suspend fun insertNewTransaction(transactionsEntity: TransactionEntity) {
        withContext(Dispatchers.IO) {
            transactionsDao.insertNewTransaction(transactionsEntity)
        }
    }

    override suspend fun transactionExistsViaTxid(txid: String): Int {
        return withContext(Dispatchers.IO) {
            return@withContext transactionsDao.transactionExistsViaTxid(txid)
        }
    }

    override suspend fun getAllRelatedTransactions(
        walletId: Long
    ): LiveData<List<TransactionModel>> {
        return withContext(Dispatchers.IO) {
            return@withContext transactionsDao.getAllRelatedTransactionsLD(walletId)
        }
    }

    override suspend fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext transactionsDao.getTransactionLiveDataById(transactionId)
        }
    }

    override suspend fun transactionSetProcessedUpdateTrueById(id: Long) {
        withContext(Dispatchers.IO) {
            transactionsDao.transactionSetProcessedUpdateTrueById(id)
        }
    }

    override suspend fun transactionSetProcessedUpdateFalseByTxId(txId: String) {
        return transactionsDao.transactionSetProcessedUpdateFalseByTxId(txId)
    }

    override suspend fun getTransactionByTxId(txId: String): TransactionEntity {
        return transactionsDao.getTransactionByTxId(txId)
    }

    override suspend fun transactionSetProcessedUpdateFalseById(id: Long) {
        return transactionsDao.transactionSetProcessedUpdateFalseById(id)
    }

    override suspend fun transactionSetProcessedUpdateTrueByTxId(txId: String) {
        return transactionsDao.transactionSetProcessedUpdateTrueByTxId(txId)
    }

    override suspend fun getTransactionsByAddressAndTokenLD(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean
    ): LiveData<List<TransactionModel>> {
        return transactionsDao.getTransactionsByAddressAndTokenLD(
            walletId = walletId,
            address = address,
            tokenName = tokenName,
            isSender = isSender,
            isCentralAddress = isCentralAddress
        )
    }
}