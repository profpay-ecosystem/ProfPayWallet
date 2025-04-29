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
    suspend fun increaseTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)
    suspend fun decreaseTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)
    suspend fun updateTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)
    suspend fun increaseTronFrozenBalanceViaId(frozenBalance: BigInteger, addressId: Long, tokenName: String)
    suspend fun decreaseTronFrozenBalanceViaId(frozenBalance: BigInteger, addressId: Long, tokenName: String)
    suspend fun getTronFrozenBalanceViaId(addressId: Long, tokenName: String): BigInteger
    suspend fun updateTronFrozenBalanceViaId(addressId: Long, tokenName: String)
    suspend fun updateTokenBalanceFromBlockchain(address: String, token: TokenName)
}

@Singleton
class TokenRepoImpl @Inject constructor(private val tokenDao: TokenDao,
                                        private val tron: Tron,
                                        private val addressRepo: AddressRepo
): TokenRepo {
    override suspend fun insertNewTokenEntity(tokenEntity: TokenEntity): Long {
        return tokenDao.insertNewTokenEntity(tokenEntity)
    }

    override suspend fun increaseTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String) {
        return tokenDao.increaseTronBalanceViaId(amount, addressId, tokenName)
    }

    override suspend fun decreaseTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String) {
        return tokenDao.decreaseTronBalanceViaId(amount, addressId, tokenName)
    }

    override suspend fun updateTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String) {
        return tokenDao.updateTronBalanceViaId(amount, addressId, tokenName)
    }

    override suspend fun increaseTronFrozenBalanceViaId(
        frozenBalance: BigInteger,
        addressId: Long,
        tokenName: String
    ) {
        return tokenDao.increaseTronFrozenBalanceViaId(frozenBalance, addressId, tokenName)
    }

    override suspend fun decreaseTronFrozenBalanceViaId(
        frozenBalance: BigInteger,
        addressId: Long,
        tokenName: String
    ) {
        val oldBalance = getTronFrozenBalanceViaId(addressId, tokenName)
        if (oldBalance - frozenBalance < BigInteger.ZERO) return

        return tokenDao.decreaseTronFrozenBalanceViaId(frozenBalance, addressId, tokenName)
    }

    override suspend fun getTronFrozenBalanceViaId(addressId: Long, tokenName: String): BigInteger {
        return tokenDao.getTronFrozenBalanceViaId(addressId, tokenName)
    }

    override suspend fun updateTronFrozenBalanceViaId(addressId: Long, tokenName: String) {
        return tokenDao.updateTronFrozenBalanceViaId(addressId, tokenName)
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
}