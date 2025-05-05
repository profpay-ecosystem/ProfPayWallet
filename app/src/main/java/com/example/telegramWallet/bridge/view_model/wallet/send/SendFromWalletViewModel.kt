package com.example.telegramWallet.bridge.view_model.wallet.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.BuildConfig
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.SendFromWalletRepo
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.example.telegramWallet.tron.EstimateBandwidthData
import com.example.telegramWallet.tron.EstimateEnergyData
import com.example.telegramWallet.tron.Tron
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferProto.TransferToken
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class SendFromWalletViewModel @Inject constructor(
    val addressRepo: AddressRepo,
    val profileRepo: ProfileRepo,
    val tokenRepo: TokenRepo,
    private val sendFromWalletRepo: SendFromWalletRepo,
    private val centralAddressRepo: CentralAddressRepo,
    val tron: Tron,
    val exchangeRatesRepo: ExchangeRatesRepo,
) : ViewModel() {
    private val _stateCommission =
        MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
    val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

    suspend fun trxToUsdtRate(): BigDecimal {
        val trxToUsdtRate =
            exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)
        return trxToUsdtRate.toBigDecimal()
    }

    private suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long) {
        sendFromWalletRepo.estimateCommission(address, bandwidth, energy)
        sendFromWalletRepo.estimateCommission.collect { comission ->
            _stateCommission.value = comission
        }
    }

    suspend fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String
    ): LiveData<AddressWithTokens> {
        return addressRepo.getGeneralAddressWithTokens(addressId, blockchainName)
    }

    suspend fun getAddressWithTokens(
        addressId: Long,
        blockchainName: String
    ): LiveData<AddressWithTokens> {
        return addressRepo.getAddressWithTokens(addressId, blockchainName)
    }

    suspend fun transferProcess(
        addressSenderId: Long, toAddress: String, amount: BigInteger, token: TransferToken,
        commission: BigInteger
    ): TransferResult {
        val userId = profileRepo.getProfileUserId()

        val addressSender = addressRepo.getAddressEntityById(addressSenderId)
            ?: return TransferResult.Failure(IllegalStateException("Адрес отправителя не найден"))

        val balance = tron.addressUtilities.getTrxBalance(addressSender.address).toTokenAmount()
        if (token == TransferToken.TRX) {
            val netAmount: Double = if (tron.addressUtilities.isAddressActivated(toAddress)) {
                (balance.toDouble() - amount.toTokenAmount().toDouble()) - commission.toTokenAmount().toDouble()
            } else {
                (balance.toDouble() - amount.toTokenAmount().toDouble()) - commission.toTokenAmount().toDouble() -
                        tron.addressUtilities.getCreateNewAccountFeeInSystemContract().toTokenAmount().toDouble()
            }

            if (netAmount < 0.0) {
                return TransferResult.Failure(IllegalStateException("Перевод невозможен, так как суммы недостаточно с учетом комиссии"))
            }
        } else {
            if (balance.toDouble() < commission.toTokenAmount().toDouble()) {
                return TransferResult.Failure(IllegalStateException("Недостаточно TRX для комиссии"))
            }
        }

        if (commission.toTokenAmount().toDouble() <= 0.0) {
            return TransferResult.Failure(IllegalArgumentException("Комиссия должна быть больше 0"))
        }

        val signedTxnBytesCommission = withContext(Dispatchers.IO) {
            tron.transactions.getSignedTrxTransaction(
                fromAddress = addressSender.address,
                toAddress = "TKPWECeokUbAUJUjyCnTEPxYQH4rDjSiT8",
                privateKey = addressSender.privateKey,
                amount = commission
            )
        }

        val estimateCommissionBandwidth = withContext(Dispatchers.IO) {
            tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = addressSender.address,
                toAddress = "TKPWECeokUbAUJUjyCnTEPxYQH4rDjSiT8",
                privateKey = addressSender.privateKey,
                amount = commission
            )
        }

        var estimateEnergy = EstimateEnergyData(0, BigInteger.ZERO)
        var estimateBandwidth = EstimateBandwidthData(300, 0.0)
        if (token == TransferToken.USDT_TRC20) {
            estimateEnergy = withContext(Dispatchers.IO) {
                tron.transactions.estimateEnergy(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }

            estimateBandwidth = withContext(Dispatchers.IO) {
                tron.transactions.estimateBandwidth(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }
        } else if (token == TransferToken.TRX) {
            estimateBandwidth = withContext(Dispatchers.IO) {
                tron.transactions.estimateBandwidthTrxTransaction(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }
        }

        val signedTxnBytes: ByteString? = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedUsdtTransaction(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }

            TransferToken.TRX -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }

            TransferToken.UNRECOGNIZED -> return TransferResult.Failure(IllegalArgumentException("Токен не был определен системой."))
        }

        if (signedTxnBytes == null) {
            return TransferResult.Failure(IllegalStateException("Не удалось подписать транзакцию"))
        }

        return withContext(Dispatchers.IO) {
            try {
                sendFromWalletRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transaction = TransactionData.newBuilder()
                        .setAddress(addressSender.address)
                        .setReceiverAddress(toAddress)
                        .setAmount(amount.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(addressSender.address)
                        .setBandwidthRequired(estimateCommissionBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytesCommission)
                        .setAmount(commission.toByteString())
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = "null"
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                tokenRepo.increaseTronFrozenBalanceViaId(
                    amount,
                    addressSender.addressId!!,
                    tokenType
                )
                return@withContext TransferResult.Success
            } catch (e: GrpcServerErrorSendTransactionExcpetion) {
                return@withContext TransferResult.Failure(e)
            } catch (e: GrpcClientErrorSendTransactionExcpetion) {
                return@withContext TransferResult.Failure(e)
            } catch (e: Exception) {
                val message = e.message ?: "Unknown client error"
                val cause = e.cause ?: Throwable("No cause provided")
                val exception = GrpcClientErrorSendTransactionExcpetion(message, cause)

                Sentry.captureException(exception)
                return@withContext TransferResult.Failure(e)
            }
        }
    }

    fun estimateCommissions(
        addressWithTokens: AddressWithTokens?,
        sumSending: String,
        addressSending: String,
        tokenNameModel: TokenName
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (addressWithTokens == null || sumSending.isEmpty() || !tron.addressUtilities.isValidTronAddress(
                    addressSending
                )
            ) return@launch

            val requiredBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = addressSending,
                privateKey = addressWithTokens.addressEntity.privateKey,
                amount = sumSending.toBigDecimal().toSunAmount()
            )

            val requiredEnergy = if (tokenNameModel.tokenName == "USDT") {
                tron.transactions.estimateEnergy(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = addressSending,
                    privateKey = addressWithTokens.addressEntity.privateKey,
                    amount = sumSending.toBigDecimal().toSunAmount()
                ).energy
            } else 0

            withContext(Dispatchers.Main) {
                estimateCommission(
                    address = addressWithTokens.addressEntity.address,
                    bandwidth = requiredBandwidth.bandwidth,
                    energy = requiredEnergy
                )
            }
        }
    }
}
