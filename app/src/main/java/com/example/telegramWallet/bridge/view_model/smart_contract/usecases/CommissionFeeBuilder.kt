package com.example.telegramWallet.bridge.view_model.smart_contract.usecases

import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.tron.Tron
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import java.math.BigInteger
import javax.inject.Inject

class CommissionFeeBuilder @Inject constructor(
    private val addressRepo: AddressRepo,
    private val tron: Tron
) {
    suspend fun build(commission: BigInteger, userId: Long, deal: SmartContractProto.ContractDealListResponse): CommissionFeeBuilderResult {
        val address = if (userId == deal.buyer.userId) {
            deal.buyer.address
        } else {
            deal.seller.address
        }

        val addressData = withContext(Dispatchers.IO) {
            addressRepo.getAddressEntityByAddress(address)
        } ?: return CommissionFeeBuilderResult.Error("Address data is null")

        val signedTxnBytesCommission = withContext(Dispatchers.IO) {
            tron.transactions.getSignedTrxTransaction(
                fromAddress = addressData.address,
                toAddress = "TKPWECeokUbAUJUjyCnTEPxYQH4rDjSiT8",
                privateKey = addressData.privateKey,
                amount = commission
            )
        }

        val estimateCommissionBandwidth = withContext(Dispatchers.IO) {
            tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = addressData.address,
                toAddress = "TKPWECeokUbAUJUjyCnTEPxYQH4rDjSiT8",
                privateKey = addressData.privateKey,
                amount = commission
            )
        }
        return CommissionFeeBuilderResult.Success(
            executorAddress = address,
            requiredBandwidth = estimateCommissionBandwidth.bandwidth,
            transaction = signedTxnBytesCommission
        )
    }
}

sealed class CommissionFeeBuilderResult(
    open val executorAddress: String? = null,
    open val requiredBandwidth: Long? = null,
    open val transaction: ByteString? = null,
    open val errorMessage: String? = null
) {
    data class Success(
        override val executorAddress: String,
        override val requiredBandwidth: Long,
        override val transaction: ByteString
    ) : CommissionFeeBuilderResult(executorAddress, requiredBandwidth, transaction)

    data class Error(
        override val errorMessage: String
    ) : CommissionFeeBuilderResult(errorMessage = errorMessage)
}
