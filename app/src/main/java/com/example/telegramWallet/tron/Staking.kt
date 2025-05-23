package com.example.telegramWallet.tron

import com.example.telegramWallet.BuildConfig
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.exceptions.IllegalException
import org.tron.trident.proto.Chain
import org.tron.trident.proto.Response

// Обработка Tron API касаемо раздела стейкинга.
class Staking {
    // Заморозка TRX.
    fun freezeTrxV2(value: Long, ownerAddress: String, privateKey: String): String? {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        val txnExt: Response.TransactionExtention = wrapper.freezeBalanceV2(ownerAddress, value, 1)

        if (Response.TransactionReturn.response_code.SUCCESS !== txnExt.result.code) {
            throw IllegalException(txnExt.result.message.toStringUtf8())
        }

        val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
        return wrapper.broadcastTransaction(signedTransaction);
    }

    // Разморозка TRX.
    fun unfreezeTrxV2(ownerAddress: String, privateKey: String, unfreezeBalance: Long): String {
         val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val txnExt: Response.TransactionExtention = wrapper.unfreezeBalanceV2(ownerAddress, unfreezeBalance, 1)

        if (Response.TransactionReturn.response_code.SUCCESS !== txnExt.result.code) {
            throw IllegalException(txnExt.result.message.toStringUtf8())
        }

        val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
        return wrapper.broadcastTransaction(signedTransaction);
     }
}