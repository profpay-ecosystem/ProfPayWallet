package com.example.telegramWallet.bridge.view_model.wallet

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.backend.http.aml.DownloadAmlPdfApi
import com.example.telegramWallet.backend.http.aml.DownloadAmlPdfRequestCallback
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.data.flow_db.repo.AmlResult
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.TXDetailsRepo
import com.example.telegramWallet.data.flow_db.repo.TransactionStatusResult
import com.example.telegramWallet.data.utils.toBigInteger
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
import org.server.protobuf.aml.AmlProto.AmlPaymentRequest
import org.server.protobuf.aml.AmlProto.AmlTransactionDetails
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class TXDetailsViewModel @Inject constructor(
    private val txDetailsRepo: TXDetailsRepo,
    private val walletRepo: WalletProfileRepo,
    private val profileRepo: ProfileRepo,
    val transactionsRepo: TransactionsRepo,
    val addressRepo: AddressRepo,
    private val tokenRepo: TokenRepo,
    val exchangeRatesRepo: ExchangeRatesRepo,
    val tron: Tron,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val _state = MutableStateFlow<AmlResult>(AmlResult.Empty)
    val state: StateFlow<AmlResult> = _state.asStateFlow()

    private val _stateCommission =
        MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
    val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

    private val _transactionStatus =
        MutableStateFlow<TransactionStatusResult>(TransactionStatusResult.Empty)
    val transactionStatus: StateFlow<TransactionStatusResult> = _transactionStatus.asStateFlow()

    private val _isActivated = MutableStateFlow<Boolean>(false)
    val isActivated: StateFlow<Boolean> = _isActivated

    private val _amlFeeResult = MutableStateFlow<ByteString?>(null)
    val amlFeeResult: StateFlow<ByteString?> = _amlFeeResult.asStateFlow()

    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    fun getAmlFeeResult() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                profPayServerGrpcClient.getServerParameters()
            }

            result.fold(
                onSuccess = { _amlFeeResult.emit(it.amlFee) },
                onFailure = { Sentry.captureException(it) }
            )
        }
    }

    init {
        getAmlFeeResult()
    }

    fun checkActivation(address: String) {
        viewModelScope.launch {
            _isActivated.value = withContext(Dispatchers.IO) {
                tron.addressUtilities.isAddressActivated(address)
            }
        }
    }

    suspend fun processedAmlReport(
        walletId: Long,
        receiverAddress: String,
        txId: String
    ): Pair<Boolean, String> {
        val generalAddress = addressRepo.getGeneralAddressEntityByWalletId(walletId)
        val balance = tron.addressUtilities.getTrxBalance(generalAddress.address)
        val userId = profileRepo.getProfileUserId()

        val serverParameters = profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = { it },
            onFailure = {
                Sentry.captureException(it)
                return Pair(false, "Сервер недоступен")
            }
        )

        val amlFeeValue = serverParameters.amlFee
        val trxFeeAddress = serverParameters.trxFeeAddress

        if (balance.toTokenAmount() < amlFeeValue.toBigInteger().toTokenAmount()) {
            return Pair(false, "Недостаточно средств на балансе.")
        }

        val signedTxnBytes = tron.transactions.getSignedTrxTransaction(
            fromAddress = generalAddress.address,
            toAddress = trxFeeAddress,
            privateKey = generalAddress.privateKey,
            amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount()
        )

        val estimateBandwidth = tron.transactions.estimateBandwidth(
            fromAddress = generalAddress.address,
            toAddress = trxFeeAddress,
            privateKey = generalAddress.privateKey,
            amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount()
        )

        txDetailsRepo.processAmlPayment(
            AmlPaymentRequest.newBuilder()
                .setUserId(userId)
                .setTx(txId)
                .setAddress(receiverAddress)
                .setTransaction(
                    AmlTransactionDetails.newBuilder()
                        .setAddress(generalAddress.address)
                        .setBandwidthRequired(estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes)
                        .build()
                )
                .build()
        )
        return Pair(true, "Успешное действие, ожидайте уведомление.")
    }

    suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long) {
        txDetailsRepo.estimateCommission(address, bandwidth = bandwidth, energy = energy)
        txDetailsRepo.estimateCommission.collect { comission ->
            _stateCommission.value = comission
        }
    }

    suspend fun getAmlFromTransactionId(address: String, tx: String, tokenName: String) {
        val data =
            txDetailsRepo.getAmlFromTransactionId(address = address, tx = tx, tokenName = tokenName)
        txDetailsRepo.aml.collect { aml ->
            _state.value = aml
        }
        return data
    }

    fun getTransactionStatus(txId: String) {
        viewModelScope.launch {
            txDetailsRepo.getTransactionStatus(txId = txId)
            txDetailsRepo.transactionStatus.collect { status ->
                _transactionStatus.value = status
            }
        }
    }

    fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity> {
        return liveData(Dispatchers.IO) {
            emitSource(transactionsRepo.getTransactionLiveDataById(transactionId))
        }
    }

    suspend fun isGeneralAddress(address: String): Boolean {
        return addressRepo.isGeneralAddress(address)
    }

    suspend fun getWalletNameById(walletId: Long): String {
        return walletRepo.getWalletNameById(walletId)
    }

    suspend fun rejectTransaction(
        toAddress: String,
        transaction: TransactionEntity,
        amount: BigInteger,
        commission: BigInteger
    ): TransferResult {
        if (commission.toTokenAmount() <= BigDecimal.ZERO) {
            return TransferResult.Failure(IllegalArgumentException("Комиссия должна быть больше 0"))
        }

        val address = addressRepo.getAddressEntityByAddress(transaction.receiverAddress)
        val generalAddress = addressRepo.getGeneralAddressByWalletId(transaction.walletId)
        val userId = profileRepo.getProfileUserId()

        val commissionAddressEntity = if (transaction.tokenName == "TRX") {
            addressRepo.getAddressEntityByAddress(transaction.receiverAddress)
        } else {
            addressRepo.getAddressEntityByAddress(generalAddress)
        }

        if (address == null)
            return TransferResult.Failure(IllegalStateException("Адрес отправителя не найден"))

        if (commissionAddressEntity == null)
            return TransferResult.Failure(IllegalStateException("Комиссионный адрес не найден"))

        val feeBalance = if (transaction.tokenName == "TRX") {
            tron.addressUtilities.getTrxBalance(address.address).toTokenAmount()
        } else {
            tron.addressUtilities.getTrxBalance(generalAddress).toTokenAmount()
        }

        if (!tron.addressUtilities.isAddressActivated(transaction.receiverAddress))
            return TransferResult.Failure(IllegalStateException("Для активации необходимо нажать кнопку «Системный TRX»"))

        if (feeBalance < commission.toTokenAmount()) {
            val source = if (transaction.tokenName == "TRX") "соте" else "главном адресе"
            return TransferResult.Failure(IllegalStateException("Недостаточно средств на $source для оплаты комиссии."))
        }

        if (transaction.amount.toTokenAmount() < amount.toTokenAmount()) {
            return TransferResult.Failure(IllegalStateException("Указанная сумма больше, чем сумма транзакции"))
        }

        val netAmount =
            (transaction.amount.toTokenAmount() - amount.toTokenAmount()) - commission.toTokenAmount()
        if (netAmount < BigDecimal.ZERO && transaction.tokenName == "TRX") {
            return TransferResult.Failure(IllegalStateException("Перевод невозможен, так как суммы недостаточно с учетом комиссии"))
        }

        val amountSending = if (!tron.addressUtilities.isAddressActivated(toAddress) && transaction.tokenName == "TRX") {
            amount - tron.addressUtilities.getCreateNewAccountFeeInSystemContract() - commission
        } else if (tron.addressUtilities.isAddressActivated(toAddress) && transaction.tokenName == "TRX") {
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
            if (transaction.tokenName == "TRX") TransferToken.TRX else TransferToken.USDT_TRC20

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
                fromAddress = transaction.receiverAddress,
                toAddress = toAddress,
                privateKey = address.privateKey,
                amount = amountSending
            )

            estimateBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = transaction.receiverAddress,
                toAddress = toAddress,
                privateKey = address.privateKey,
                amount = amountSending
            )
        } else if (token == TransferToken.TRX) {
            estimateBandwidth = tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = transaction.receiverAddress,
                toAddress = toAddress,
                privateKey = address.privateKey,
                amount = amountSending
            )
        }

        val signedTxnBytes: ByteString? = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedUsdtTransaction(
                    fromAddress = transaction.receiverAddress,
                    toAddress = toAddress,
                    privateKey = address.privateKey,
                    amount = amountSending
                )
            }

            TransferToken.TRX -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = transaction.receiverAddress,
                    toAddress = toAddress,
                    privateKey = address.privateKey,
                    amount = amountSending
                )
            }

            TransferToken.UNRECOGNIZED -> return TransferResult.Failure(IllegalArgumentException("Токен не был определен системой."))
        }

        return withContext(Dispatchers.IO) {
            try {
                txDetailsRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transactionId = transaction.transactionId!!,
                    transaction = TransactionData.newBuilder()
                        .setAddress(transaction.receiverAddress)
                        .setReceiverAddress(toAddress)
                        .setAmount(amountSending.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes!!)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(commissionAddressEntity.address)
                        .setBandwidthRequired(estimateCommissionBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytesCommission)
                        .setAmount(commission.toByteString())
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = transaction.txId
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                transactionsRepo.transactionSetProcessedUpdateTrueById(transaction.transactionId)
                tokenRepo.increaseTronFrozenBalanceViaId(
                    amountSending,
                    address.addressId!!,
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

    suspend fun acceptTransaction(
        transaction: TransactionEntity,
        commission: BigInteger
    ): TransferResult {
        val userId = profileRepo.getProfileUserId()

        val address = addressRepo.getAddressEntityByAddress(transaction.receiverAddress)
        val generalAddress = addressRepo.getGeneralAddressByWalletId(transaction.walletId)

        val commissionAddressEntity = if (transaction.tokenName == "TRX") {
            addressRepo.getAddressEntityByAddress(transaction.receiverAddress)
        } else {
            addressRepo.getAddressEntityByAddress(generalAddress)
        }

        if (address == null)
            return TransferResult.Failure(IllegalStateException("Адрес отправителя не найден"))

        if (commissionAddressEntity == null)
            return TransferResult.Failure(IllegalStateException("Комиссионный адрес не найден"))

        val feeBalance = if (transaction.tokenName == "TRX") {
            tron.addressUtilities.getTrxBalance(address.address).toTokenAmount()
        } else {
            tron.addressUtilities.getTrxBalance(generalAddress).toTokenAmount()
        }

        if (!tron.addressUtilities.isAddressActivated(transaction.receiverAddress))
            return TransferResult.Failure(IllegalStateException("Для активации необходимо нажать кнопку «Системный TRX»"))

        if (feeBalance < commission.toTokenAmount()) {
            val source = if (transaction.tokenName == "TRX") "соте" else "главном адресе"
            return TransferResult.Failure(IllegalStateException("Недостаточно средств на $source для оплаты комиссии."))
        }

        if (commission.toTokenAmount() <= BigDecimal.ZERO) {
            return TransferResult.Failure(IllegalArgumentException("Комиссия должна быть больше 0"))
        }

        val token: TransferToken =
            if (transaction.tokenName == "TRX") TransferToken.TRX else TransferToken.USDT_TRC20

        val amountSending = if (!tron.addressUtilities.isAddressActivated(generalAddress) && transaction.tokenName == "TRX") {
            (transaction.amount - commission) - tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
        } else if (tron.addressUtilities.isAddressActivated(generalAddress) && transaction.tokenName == "TRX") {
            transaction.amount - commission
        } else transaction.amount

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
                fromAddress = transaction.receiverAddress,
                toAddress = generalAddress,
                privateKey = address.privateKey,
                amount = transaction.amount
            )

            estimateBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = transaction.receiverAddress,
                toAddress = generalAddress,
                privateKey = address.privateKey,
                amount = transaction.amount
            )
        } else if (token == TransferToken.TRX) {
            estimateBandwidth = tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = transaction.receiverAddress,
                toAddress = generalAddress,
                privateKey = address.privateKey,
                amount = amountSending
            )
        }

        val signedTxnBytes: ByteString? = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedUsdtTransaction(
                    fromAddress = transaction.receiverAddress,
                    toAddress = generalAddress,
                    privateKey = address.privateKey,
                    amount = transaction.amount
                )
            }

            TransferToken.TRX -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = transaction.receiverAddress,
                    toAddress = generalAddress,
                    privateKey = address.privateKey,
                    amount = amountSending
                )
            }

            TransferToken.UNRECOGNIZED -> return TransferResult.Failure(IllegalArgumentException("Токен не был определен системой."))
        }

        return withContext(Dispatchers.IO) {
            try {
                txDetailsRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transactionId = transaction.transactionId!!,
                    transaction = TransactionData.newBuilder()
                        .setAddress(transaction.receiverAddress)
                        .setReceiverAddress(generalAddress)
                        .setAmount(amountSending.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes!!)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(commissionAddressEntity.address)
                        .setBandwidthRequired(estimateCommissionBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytesCommission)
                        .setAmount(commission.toByteString())
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = transaction.txId
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                transactionsRepo.transactionSetProcessedUpdateTrueById(transaction.transactionId)
                tokenRepo.increaseTronFrozenBalanceViaId(
                    amountSending,
                    address.addressId!!,
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

    suspend fun downloadPdfFile(txId: String, destinationFile: File) {
        val userId = profileRepo.getProfileUserId()
        DownloadAmlPdfApi.downloadAmlPdfService.makeRequest(
            object : DownloadAmlPdfRequestCallback {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun onSuccess(inputStream: InputStream?) {
                    val outputStream = FileOutputStream(destinationFile)
                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d(
                        "FileDownload",
                        "File saved successfully: ${destinationFile.absolutePath}"
                    )
                }

                override fun onFailure(error: String) {
                    Sentry.captureException(Exception(error))
                }
            }, userId = userId, txId = txId
        )
    }
}