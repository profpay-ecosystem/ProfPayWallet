package com.example.telegramWallet.data.database.repositories.wallet

import androidx.lifecycle.LiveData
import com.example.telegramWallet.data.database.dao.wallet.WalletPrivateKeyAndChainCodeModel
import com.example.telegramWallet.data.database.dao.wallet.WalletProfileDao
import com.example.telegramWallet.data.database.dao.wallet.WalletProfileModel
import com.example.telegramWallet.data.database.entities.wallet.WalletProfileEntity
import com.example.telegramWallet.security.KeystoreEncryptionUtils
import com.example.telegramWallet.tron.AddressesWithKeysForM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface WalletProfileRepo {
    suspend fun insertNewWalletProfileEntity(name: String, addressesWithKeysForM: AddressesWithKeysForM): Long
    suspend fun getWalletNameById(walletId: Long): String?
    suspend fun getListAllWallets(): LiveData<List<WalletProfileModel>>
    suspend fun getCountRecords(): Long
    suspend fun getWalletPrivateKeyAndChainCodeById(id: Long): WalletPrivateKeyAndChainCodeModel
    suspend fun updateNameById(id: Long, newName: String)
    suspend fun deleteWalletProfile(id: Long)
    suspend fun getWalletDecryptedEntropy(id: Long): ByteArray?
}

@Singleton
class WalletProfileRepoImpl @Inject constructor(private val walletProfileDao: WalletProfileDao) :
    WalletProfileRepo {
    override suspend fun insertNewWalletProfileEntity(name: String, addressesWithKeysForM: AddressesWithKeysForM): Long {
        val keystore = KeystoreEncryptionUtils()
        val encodedEntropy = keystore.encrypt(addressesWithKeysForM.entropy)

        return walletProfileDao.insertNewWalletProfileEntity(WalletProfileEntity(
            name = name,
            privKeyBytes = addressesWithKeysForM.privKeyBytes,
            entropy = encodedEntropy,
            chainCode = addressesWithKeysForM.chainCode
        ))
    }

    override suspend fun getWalletNameById(walletId: Long): String? {
        return walletProfileDao.getWalletNameById(walletId)
    }

    override suspend fun getListAllWallets(): LiveData<List<WalletProfileModel>> {
        return walletProfileDao.getListAllWallets()
    }

    override suspend fun getCountRecords(): Long {
        return walletProfileDao.getCountRecords()
    }

    override suspend fun getWalletPrivateKeyAndChainCodeById(id: Long): WalletPrivateKeyAndChainCodeModel {
        return walletProfileDao.getWalletPrivateKeyAndChainCodeById(id)
    }

    override suspend fun updateNameById(id: Long, newName: String) {
        walletProfileDao.updateNameById(id, newName)
    }

    override suspend fun deleteWalletProfile(id: Long) {
        return withContext(Dispatchers.IO) {
            return@withContext walletProfileDao.deleteWalletProfile(id)
        }
    }

    override suspend fun getWalletDecryptedEntropy(id: Long): ByteArray? {
        return withContext(Dispatchers.IO) {
            val keystore = KeystoreEncryptionUtils()
            val encryptedEntropy = walletProfileDao.getWalletEncryptedEntropy(id) ?: return@withContext null

            return@withContext keystore.decrypt(encryptedEntropy)
        }
    }
}