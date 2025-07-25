package com.example.telegramWallet.bridge.view_model.smart_contract

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.ProcessSmartContractUseCase
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.estimate.ProcessContractEstimatorUseCase
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.estimate.TransactionEstimatorResult
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.getOppositeUserId
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isBuyerNotDeposited
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isBuyerRequestInitialized
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isContractAwaitingUserConfirmation
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isDisputeNotAgreed
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isDisputeNotDeclined
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isExpertNotDecision
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isSellerNotPayedExpertFee
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.flow_db.repo.EstimateResourcePriceResult
import com.example.telegramWallet.data.flow_db.repo.SmartContractButtonType
import com.example.telegramWallet.data.flow_db.repo.SmartContractModalData
import com.example.telegramWallet.data.flow_db.repo.SmartContractRepo
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.tron.SignedTransactionData
import com.example.telegramWallet.tron.Tron
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractProto.DeploySmartContractRequest
import org.example.protobuf.smart.SmartContractProto.DeployTransactionData
import org.example.protobuf.smart.SmartContractProto.ResourceQuoteRequest
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import com.example.telegramWallet.data.database.repositories.wallet.SmartContractRepo as SmartContractDatabaseRepo

@HiltViewModel
class GetSmartContractViewModel @Inject constructor(
    private val smartContractRepo: SmartContractRepo,
    val profileRepo: ProfileRepo,
    val processSmartContractUseCase: ProcessSmartContractUseCase,
    val processContractEstimatorUseCase: ProcessContractEstimatorUseCase,
    val addressRepo: AddressRepo,
    private val centralAddressRepo: CentralAddressRepo,
    val smartContractDatabaseRepo: SmartContractDatabaseRepo,
    val tron: Tron,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val _state = MutableStateFlow<List<SmartContractProto.ContractDealListResponse>>(emptyList())
    val state: StateFlow<List<SmartContractProto.ContractDealListResponse>> = _state.asStateFlow()

    private val _stateModal = MutableStateFlow(SmartContractModalData(isActive = false, text = ""))
    val stateModal: StateFlow<SmartContractModalData> = _stateModal.asStateFlow()

    private val _stateEstimateResourcePrice = MutableStateFlow<EstimateResourcePriceResult>(EstimateResourcePriceResult(commission = 0, feePolicy = SmartContractProto.FeePolicy.STANDARD))
    val stateEstimateResourcePrice: StateFlow<EstimateResourcePriceResult> = _stateEstimateResourcePrice.asStateFlow()

    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    private fun loadMyContractDeals() {
        viewModelScope.launch {
            smartContractRepo.getMyContractDeals()
            smartContractRepo.deals.collect { deals ->
                _state.value = deals
            }
        }
    }

    private fun loadSCModalData() {
        viewModelScope.launch {
            smartContractRepo.smartContractModalData.collect { modalData ->
                _stateModal.value = modalData
            }
        }
    }

    fun setSmartContractModalActive(isActive: Boolean, buttonType: SmartContractButtonType?, deal: SmartContractProto.ContractDealListResponse?) {
        viewModelScope.launch {
            smartContractRepo.initSmartContractModalAndStart(isActive, buttonType, deal)
        }
    }

    fun getResourceQuote(address: String, energy: Long, bandwidth: Long) {
        viewModelScope.launch {
            val appId = profileRepo.getProfileAppId()
            smartContractRepo.getResourceQuote(
                ResourceQuoteRequest.newBuilder()
                    .setAppId(appId)
                    .setAddress(address)
                    .setEnergyRequired(energy)
                    .setBandwidthRequired(bandwidth)
                    .build()
            )

            smartContractRepo.estimateResourcePrice.collect { value ->
                _stateEstimateResourcePrice.value = value
            }
        }
    }

    init {
        loadMyContractDeals()
        loadSCModalData()
    }

    private fun setSmartContractModalData(isActive: Boolean, text: String) {
        viewModelScope.launch {
            smartContractRepo.setSmartContractModalData(isActive, text)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun completeContract(commission: BigDecimal, deal: SmartContractProto.ContractDealListResponse): CompleteReturnData {
        return processSmartContractUseCase.processCompleteSmartContract(commission = commission, deal = deal)
    }

    suspend fun rejectContract(commission: BigDecimal, deal: SmartContractProto.ContractDealListResponse): CompleteReturnData {
        return processSmartContractUseCase.processRejectSmartContract(commission = commission, deal = deal)
    }

    suspend fun estimateCompleteContract(deal: SmartContractProto.ContractDealListResponse): TransactionEstimatorResult? {
        return processContractEstimatorUseCase.processCompleteSmartContract(deal = deal)
    }

    suspend fun estimateRejectContract(deal: SmartContractProto.ContractDealListResponse): TransactionEstimatorResult? {
        return processContractEstimatorUseCase.processRejectSmartContract(deal = deal)
    }

    suspend fun expertSetDecision(deal: SmartContractProto.ContractDealListResponse, sellerValue: BigInteger, buyerValue: BigInteger) {
        processSmartContractUseCase.expertSetDecision(
            deal = deal,
            sellerValue = sellerValue,
            buyerValue = buyerValue
        )
    }

    suspend fun isButtonVisible(deal: SmartContractProto.ContractDealListResponse): ContractButtonVisibleType {
        val userId = profileRepo.getProfileUserId()
        return when {
            isBuyerRequestInitialized(deal, userId) -> ContractButtonVisibleType(true, true)
            isBuyerNotDeposited(deal, userId) -> ContractButtonVisibleType(true, true)
            isContractAwaitingUserConfirmation(deal, userId) -> ContractButtonVisibleType(true, true)
            isSellerNotPayedExpertFee(deal, userId) -> ContractButtonVisibleType(true, true)
            isExpertNotDecision(deal, userId) -> ContractButtonVisibleType(true, false)
            (isDisputeNotAgreed(deal, userId) && isDisputeNotDeclined(deal, userId)) -> ContractButtonVisibleType(true, true)
            (!isDisputeNotAgreed(deal, userId) && isDisputeNotDeclined(deal, userId)) -> ContractButtonVisibleType(false, true)
            (isDisputeNotAgreed(deal, userId) && !isDisputeNotDeclined(deal, userId)) -> ContractButtonVisibleType(true, false)
            else -> ContractButtonVisibleType(false, false)
        }
    }

    suspend fun getOppositeTelegramId(deal: SmartContractProto.ContractDealListResponse): Long {
        val userId = profileRepo.getProfileUserId()
        // Логика получения идентификатора противоположной стороны
        return if (deal.buyer.userId == userId) {
            deal.seller.telegramId
        } else {
            deal.buyer.telegramId
        }
    }

    suspend fun getOppositeUsername(deal: SmartContractProto.ContractDealListResponse): String {
        val userId = profileRepo.getProfileUserId()
        // Логика получения идентификатора противоположной стороны
        return if (deal.buyer.userId == userId) {
            deal.seller.username
        } else {
            deal.buyer.username
        }
    }

    suspend fun smartContractStatus(deal: SmartContractProto.ContractDealListResponse): StatusData {
        val userId = profileRepo.getProfileUserId()
        return when {
            isBuyerRequestInitialized(deal, userId) ->
                StatusData(
                    "Ожидание создания сделки в контракте",
                    "Создать",
                    "Отменить")
            isBuyerNotDeposited(deal, userId) ->
                StatusData(
                    "Ожидание депозита на смарт-контракт",
                    "Пополнить",
                    "Отменить")
            isSellerNotPayedExpertFee(deal, userId) ->
                StatusData(
                    "Ожидание подтверждения от продавца",
                    "Оплатить",
                    "Отменить")
            isContractAwaitingUserConfirmation(deal, userId) ->
                StatusData(
                    "Выполните условия договора и нажмите «Отправить»",
                    "Отправить",
                    "Открыть спор")
            isExpertNotDecision(deal, userId) -> {
                StatusData(
                    "Решение над диспутом",
                    "Подтвердить",
                    "Отклонить")
            }
            isDisputeNotAgreed(deal, userId) || isDisputeNotDeclined(deal, userId) -> {
                StatusData(
                    "Примите решение над условиями диспута",
                    "Подтвердить",
                    "Отклонить")
            }
            else -> determineOppositeStatus(deal)
        }
    }

    private suspend fun determineOppositeStatus(deal: SmartContractProto.ContractDealListResponse): StatusData {
        val userId = profileRepo.getProfileUserId()
        return when {
            isBuyerRequestInitialized(deal, getOppositeUserId(deal, userId)) ->
                StatusData(
                    "Ожидание создания сделки в контракте",
                    "Создать",
                    "Отменить")
            isBuyerNotDeposited(deal, getOppositeUserId(deal, userId)) ->
                StatusData(
                    "Ожидание депозита на смарт-контракт",
                    "Пополнить",
                    "Отменить")
            isSellerNotPayedExpertFee(deal, getOppositeUserId(deal, userId)) ->
                StatusData(
                    "Ожидание подтверждения от продавца",
                    "Оплатить",
                    "Отменить")
            isContractAwaitingUserConfirmation(deal, getOppositeUserId(deal, userId)) ->
                StatusData(
                    "Выполните условия договора и нажмите «Отправить»",
                    "Отправить",
                    "Открыть спор")
            else -> StatusData(
                "Неизвестный статус контракта",
                "Нет действий",
                "Нет действий")
        }
    }

    suspend fun deploySmartContract(commission: BigDecimal, energy: Long, bandwidth: Long, context: Context) {
        val address = addressRepo.getGeneralAddressByWalletId(1L)
        val centralAddressEntity = centralAddressRepo.getCentralAddress()
        val addressEntity = addressRepo.getAddressEntityByAddress(address)
        val userId = profileRepo.getProfileUserId()

        if (centralAddressEntity == null || addressEntity == null) return

        val centralAddressBalance = tron.addressUtilities.getTrxBalance(centralAddressEntity.address)

        if (centralAddressBalance.toTokenAmount() < commission) return

        val trxFeeAddress = profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = {
                it.trxFeeAddress
            },
            onFailure = {
                Sentry.captureException(it)
                throw RuntimeException(it)
            }
        )

        val signedCommissionTransaction: SignedTransactionData = tron.transactions.getSignedTrxTransaction(
            fromAddress = centralAddressEntity.address,
            toAddress = trxFeeAddress,
            privateKey = centralAddressEntity.privateKey,
            amount = commission.toSunAmount()
        )
        val signedDeployContractTransaction: ByteString? = tron.smartContracts.getSignedDeployMultiSigContract(
            ownerAddress = addressEntity.address,
            privateKey = addressEntity.privateKey,
        )
        val estimateBandwidthCommission = tron.transactions.estimateBandwidthTrxTransaction(
            fromAddress = centralAddressEntity.address,
            toAddress = trxFeeAddress,
            privateKey = centralAddressEntity.privateKey,
            amount = commission.toSunAmount()
        )

        smartContractRepo.deploySmartContract(
            DeploySmartContractRequest.newBuilder()
                .setUserId(userId)
                .setContract(
                    DeployTransactionData.newBuilder()
                        .setAddress(addressEntity.address)
                        .setAmount(commission.toSunAmount().toByteString())
                        .setEstimateEnergy(energy)
                        .setBandwidthRequired(bandwidth)
                        .setTxnBytes(signedDeployContractTransaction)
                        .build()
                )
                .setCommission(
                    DeployTransactionData.newBuilder()
                        .setAddress(centralAddressEntity.address)
                        .setAmount(commission.toSunAmount().toByteString())
                        .setBandwidthRequired(estimateBandwidthCommission.bandwidth)
                        .setTxnBytes(signedCommissionTransaction.signedTxn)
                        .build()
                )
                .build()
        )
    }
}

data class StatusData(
    val status: String,
    val completeButtonName: String,
    val rejectButtonName: String,
)

data class ContractButtonVisibleType(
    val agreeVisible: Boolean,
    val cancelVisible: Boolean
)