package com.example.telegramWallet.bridge.view_model.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.models.HasTronCredentials
import com.example.telegramWallet.data.database.models.TokenWithPendingTransactions
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.TransactionStatusResult
import com.example.telegramWallet.data.flow_db.repo.WalletAddressRepo
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.example.telegramWallet.tron.EstimateBandwidthData
import com.example.telegramWallet.tron.EstimateEnergyData
import com.example.telegramWallet.tron.SignedTransactionData
import com.example.telegramWallet.tron.Tron
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
class WalletAddressViewModel @Inject constructor(
    private val walletAddressRepo: WalletAddressRepo,
    val addressRepo: AddressRepo,
    val transactionsRepo: TransactionsRepo,
    private val centralAddressRepo: CentralAddressRepo,
    private val profileRepo: ProfileRepo,
    private val tokenRepo: TokenRepo,
    private val pendingTransactionRepo: PendingTransactionRepo,
    val tron: Tron,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    private val _isActivated = MutableStateFlow<Boolean>(false)
    val isActivated: StateFlow<Boolean> = _isActivated

    private val _stateCommission =
        MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
    val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

    fun checkActivation(address: String) {
        viewModelScope.launch {
            _isActivated.value = withContext(Dispatchers.IO) {
                tron.addressUtilities.isAddressActivated(address)
            }
        }
    }

    suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long) {
        walletAddressRepo.estimateCommission(address, bandwidth = bandwidth, energy = energy)
        walletAddressRepo.estimateCommission.collect { comission ->
            _stateCommission.value = comission
        }
    }

    fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens>{
        return liveData(Dispatchers.IO) {
            emitSource(addressRepo.getAddressWithTokensByAddressLD(address))
        }
    }

    fun getTransactionsByAddressAndTokenLD(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean
    ): LiveData<List<TransactionModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(transactionsRepo.getTransactionsByAddressAndTokenLD(
                walletId = walletId,
                address = address,
                tokenName = tokenName,
                isSender = isSender,
                isCentralAddress = isCentralAddress
            ))
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

    suspend fun rejectTransaction(
        toAddress: String,
        addressWithTokens: AddressWithTokens,
        amount: BigInteger,
        commission: BigInteger,
        tokenName: String,
        tokenEntity: TokenWithPendingTransactions?
    ): TransferResult {
        if (commission.toTokenAmount() <= BigDecimal.ZERO) {
            return TransferResult.Failure(IllegalArgumentException("Комиссия должна быть больше 0"))
        }

        val addressEntity = addressRepo.getAddressEntityByAddress(addressWithTokens.addressEntity.address)
        val centralAddressRepo = centralAddressRepo.getCentralAddress() ?: return TransferResult.Failure(IllegalStateException("Центральный адрес не найден."))
        val userId = profileRepo.getProfileUserId()

        val commissionAddressEntity: HasTronCredentials? = if (tokenName == "TRX") {
            addressEntity
        } else {
            centralAddressRepo
        }

        if (addressEntity == null)
            return TransferResult.Failure(IllegalStateException("Адрес отправителя не найден"))

        if (commissionAddressEntity == null)
            return TransferResult.Failure(IllegalStateException("Комиссионный адрес не найден"))

        val feeBalance = if (tokenName == "TRX") {
            tron.addressUtilities.getTrxBalance(addressEntity.address).toTokenAmount()
        } else {
            tron.addressUtilities.getTrxBalance(centralAddressRepo.address).toTokenAmount()
        }

        if (!tron.addressUtilities.isAddressActivated(addressWithTokens.addressEntity.address))
            return TransferResult.Failure(IllegalStateException("Для активации необходимо нажать кнопку «Системный TRX»"))

        if (feeBalance < commission.toTokenAmount()) {
            val source = if (tokenName == "TRX") "соте" else "центральном адресе"
            return TransferResult.Failure(IllegalStateException("Недостаточно средств на $source для оплаты комиссии."))
        }

        if (tokenEntity!!.balanceWithoutFrozen.toTokenAmount() < amount.toTokenAmount()) {
            return TransferResult.Failure(IllegalStateException("Указанная сумма больше, чем сумма транзакции"))
        }

        val netAmount =
            (tokenEntity.balanceWithoutFrozen.toTokenAmount() - amount.toTokenAmount()) - commission.toTokenAmount()
        if (netAmount < BigDecimal.ZERO && tokenName == "TRX") {
            return TransferResult.Failure(IllegalStateException("Перевод невозможен, так как суммы недостаточно с учетом комиссии"))
        }

        val amountSending = if (!tron.addressUtilities.isAddressActivated(toAddress) && tokenName == "TRX") {
            amount - tron.addressUtilities.getCreateNewAccountFeeInSystemContract() - commission
        } else if (tron.addressUtilities.isAddressActivated(toAddress) && tokenName == "TRX") {
            amount - commission
        } else amount

        val trxFeeAddress = profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = {
                it.trxFeeAddress
            },
            onFailure = {
                Sentry.captureException(it)
                return TransferResult.Failure(it)
            }
        )

        val token: TransferToken =
            if (tokenName == "TRX") TransferToken.TRX else TransferToken.USDT_TRC20

        val signedTxnBytesCommission = tron.transactions.getSignedTrxTransaction(
            fromAddress = commissionAddressEntity.address,
            toAddress = trxFeeAddress,
            privateKey = commissionAddressEntity.privateKey,
            amount = commission
        )
        val estimateCommissionBandwidth = tron.transactions.estimateBandwidthTrxTransaction(
            fromAddress = commissionAddressEntity.address,
            toAddress = trxFeeAddress,
            privateKey = commissionAddressEntity.privateKey,
            amount = commission
        )

        var estimateEnergy = EstimateEnergyData(0, BigInteger.ZERO)
        var estimateBandwidth = EstimateBandwidthData(300, 0.0)
        if (token == TransferToken.USDT_TRC20) {
            estimateEnergy = tron.transactions.estimateEnergy(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = toAddress,
                privateKey = addressEntity.privateKey,
                amount = amountSending
            )

            estimateBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = toAddress,
                privateKey = addressEntity.privateKey,
                amount = amountSending
            )
        } else if (token == TransferToken.TRX) {
            estimateBandwidth = tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = toAddress,
                privateKey = addressEntity.privateKey,
                amount = amountSending
            )
        }

        val signedTxnBytes: SignedTransactionData = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedUsdtTransaction(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = toAddress,
                    privateKey = addressEntity.privateKey,
                    amount = amountSending
                )
            }

            TransferToken.TRX -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = toAddress,
                    privateKey = addressEntity.privateKey,
                    amount = amountSending
                )
            }

            TransferToken.UNRECOGNIZED -> return TransferResult.Failure(IllegalArgumentException("Токен не был определен системой."))
        }

        val hasEnoughTransactionBandwidth = tron.accounts.hasEnoughBandwidth(
            addressWithTokens.addressEntity.address,
            estimateBandwidth.bandwidth
        )
        val hasEnoughCommissionBandwidth = tron.accounts.hasEnoughBandwidth(
            commissionAddressEntity.address,
            estimateCommissionBandwidth.bandwidth
        )

        return withContext(Dispatchers.IO) {
            try {
                walletAddressRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transaction = TransactionData.newBuilder()
                        .setAddress(addressWithTokens.addressEntity.address)
                        .setReceiverAddress(toAddress)
                        .setAmount(amountSending.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(if (hasEnoughTransactionBandwidth) 0 else estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes.signedTxn)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(commissionAddressEntity.address)
                        .setBandwidthRequired(if (hasEnoughCommissionBandwidth) 0 else estimateCommissionBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytesCommission.signedTxn)
                        .setAmount(commission.toByteString())
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = "null"
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                val tokenId = tokenRepo.getTokenIdByAddressIdAndTokenName(addressEntity.addressId!!, tokenType)

                pendingTransactionRepo.insert(
                    PendingTransactionEntity(
                        tokenId = tokenId,
                        txid = signedTxnBytes.txid,
                        amount = amountSending
                    )
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

    suspend fun acceptTransaction(
        addressWithTokens: AddressWithTokens,
        commission: BigInteger,
        walletId: Long,
        tokenName: String,
        tokenEntity: TokenWithPendingTransactions?
    ): TransferResult {
        val userId = profileRepo.getProfileUserId()

        val addressEntity = addressRepo.getAddressEntityByAddress(addressWithTokens.addressEntity.address)
        val centralAddressRepo = centralAddressRepo.getCentralAddress() ?: return TransferResult.Failure(IllegalStateException("Центральный адрес не найден."))
        val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)

        val commissionAddressEntity = if (tokenName == "TRX") {
            addressEntity
        } else {
            centralAddressRepo
        }

        if (addressEntity == null)
            return TransferResult.Failure(IllegalStateException("Адрес отправителя не найден"))

        if (commissionAddressEntity == null)
            return TransferResult.Failure(IllegalStateException("Комиссионный адрес не найден"))

        val feeBalance = if (tokenName == "TRX") {
            tron.addressUtilities.getTrxBalance(addressEntity.address).toTokenAmount()
        } else {
            tron.addressUtilities.getTrxBalance(centralAddressRepo.address).toTokenAmount()
        }

        if (!tron.addressUtilities.isAddressActivated(addressWithTokens.addressEntity.address))
            return TransferResult.Failure(IllegalStateException("Для активации необходимо нажать кнопку «Системный TRX»"))

        if (feeBalance < commission.toTokenAmount()) {
            val source = if (tokenName == "TRX") "соте" else "главном адресе"
            return TransferResult.Failure(IllegalStateException("Недостаточно средств на $source для оплаты комиссии."))
        }

        if (commission.toTokenAmount() <= BigDecimal.ZERO) {
            return TransferResult.Failure(IllegalArgumentException("Комиссия должна быть больше 0"))
        }

        val token: TransferToken =
            if (tokenName == "TRX") TransferToken.TRX else TransferToken.USDT_TRC20

        val amountSending = if (!tron.addressUtilities.isAddressActivated(generalAddress) && tokenName == "TRX") {
            (tokenEntity!!.balanceWithoutFrozen - commission) - tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
        } else if (tron.addressUtilities.isAddressActivated(generalAddress) && tokenName == "TRX") {
            tokenEntity!!.balanceWithoutFrozen - commission
        } else tokenEntity!!.balanceWithoutFrozen

        val trxFeeAddress = profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = {
                it.trxFeeAddress
            },
            onFailure = {
                Sentry.captureException(it)
                return TransferResult.Failure(it)
            }
        )

        val signedTxnBytesCommission = tron.transactions.getSignedTrxTransaction(
            fromAddress = commissionAddressEntity.address,
            toAddress = trxFeeAddress,
            privateKey = commissionAddressEntity.privateKey,
            amount = commission
        )
        val estimateCommissionBandwidth = tron.transactions.estimateBandwidthTrxTransaction(
            fromAddress = commissionAddressEntity.address,
            toAddress = trxFeeAddress,
            privateKey = commissionAddressEntity.privateKey,
            amount = commission
        )

        var estimateEnergy = EstimateEnergyData(0, BigInteger.ZERO)
        var estimateBandwidth = EstimateBandwidthData(300, 0.0)
        if (token == TransferToken.USDT_TRC20) {
            estimateEnergy = tron.transactions.estimateEnergy(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = generalAddress,
                privateKey = addressEntity.privateKey,
                amount = tokenEntity!!.balanceWithoutFrozen
            )

            estimateBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = generalAddress,
                privateKey = addressEntity.privateKey,
                amount = tokenEntity!!.balanceWithoutFrozen
            )
        } else if (token == TransferToken.TRX) {
            estimateBandwidth = tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = generalAddress,
                privateKey = addressEntity.privateKey,
                amount = amountSending
            )
        }

        val signedTxnBytes: SignedTransactionData = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedUsdtTransaction(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = generalAddress,
                    privateKey = addressEntity.privateKey,
                    amount = tokenEntity!!.balanceWithoutFrozen
                )
            }

            TransferToken.TRX -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = generalAddress,
                    privateKey = addressEntity.privateKey,
                    amount = amountSending
                )
            }

            TransferToken.UNRECOGNIZED -> return TransferResult.Failure(IllegalArgumentException("Токен не был определен системой."))
        }

        val hasEnoughTransactionBandwidth = tron.accounts.hasEnoughBandwidth(
            addressWithTokens.addressEntity.address,
            estimateBandwidth.bandwidth
        )
        val hasEnoughCommissionBandwidth = tron.accounts.hasEnoughBandwidth(
            commissionAddressEntity.address,
            estimateCommissionBandwidth.bandwidth
        )

        return withContext(Dispatchers.IO) {
            try {
                walletAddressRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transaction = TransactionData.newBuilder()
                        .setAddress(addressWithTokens.addressEntity.address)
                        .setReceiverAddress(generalAddress)
                        .setAmount(amountSending.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(if (hasEnoughTransactionBandwidth) 0 else estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes.signedTxn)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(commissionAddressEntity.address)
                        .setBandwidthRequired(if (hasEnoughCommissionBandwidth) 0 else estimateCommissionBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytesCommission.signedTxn)
                        .setAmount(commission.toByteString())
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = "null"
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                val tokenId = tokenRepo.getTokenIdByAddressIdAndTokenName(addressEntity.addressId!!, tokenType)

                pendingTransactionRepo.insert(
                    PendingTransactionEntity(
                        tokenId = tokenId,
                        txid = signedTxnBytes.txid,
                        amount = amountSending
                    )
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

}