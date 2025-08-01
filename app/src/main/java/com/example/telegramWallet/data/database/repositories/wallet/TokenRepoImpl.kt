package com.example.telegramWallet.data.database.repositories.wallet

import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.data.database.dao.wallet.TokenDao
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import com.example.telegramWallet.tron.Tron
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface TokenRepo {
    suspend fun insertNewTokenEntity(tokenEntity: TokenEntity): Long
    suspend fun updateTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)
    suspend fun updateTokenBalanceFromBlockchain(address: String, token: TokenName)
    suspend fun getTokenIdByAddressIdAndTokenName(addressId: Long, tokenName: String): Long
}

@Singleton
class TokenRepoImpl @Inject constructor(private val tokenDao: TokenDao,
                                        private val tron: Tron,
                                        private val addressRepo: AddressRepo
): TokenRepo {
    override suspend fun insertNewTokenEntity(tokenEntity: TokenEntity): Long {
        return tokenDao.insertNewTokenEntity(tokenEntity)
    }

    override suspend fun updateTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String) {
        return tokenDao.updateTronBalanceViaId(amount, addressId, tokenName)
    }

    override suspend fun updateTokenBalanceFromBlockchain(
        address: String,
        token: TokenName
    ) {
        return withContext(Dispatchers.IO) {
            val addressId = addressRepo.getAddressEntityByAddress(address)?.addressId ?: return@withContext
            val balance = if (token == TokenName.USDT) {
                tron.addressUtilities.getUsdtBalance(address)
            } else tron.addressUtilities.getTrxBalance(address)
            // TODO: Обновления можно отменять если цифры блокчейна и БД сходятся
            updateTronBalanceViaId(balance, addressId, token.shortName)
        }
    }

    override suspend fun getTokenIdByAddressIdAndTokenName(
        addressId: Long,
        tokenName: String
    ): Long {
        return withContext(Dispatchers.IO) {
            return@withContext tokenDao.getTokenIdByAddressIdAndTokenName(addressId, tokenName)
        }
    }
}