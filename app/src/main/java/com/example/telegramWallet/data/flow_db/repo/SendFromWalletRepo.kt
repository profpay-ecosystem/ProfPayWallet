package com.example.telegramWallet.data.flow_db.repo

import android.util.Log
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.TransferGrpcClient
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionCommissionData
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import javax.inject.Inject

interface SendFromWalletRepo {
    val estimateCommission: Flow<EstimateCommissionResult>
    suspend fun sendTronTransactionRequestGrpc(userId: Long,
                                                        transaction: TransactionData,
                                                        commission: TransactionCommissionData?,
                                                        network: TransferNetwork,
                                                        token: TransferProto.TransferToken,
                                                        txId: String?
    )
    suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long)
}

class SendFromWalletRepoImpl @Inject constructor(private val profileRepo: ProfileRepo,
                                                 grpcClientFactory: GrpcClientFactory
) : SendFromWalletRepo {
    private val _estimateCommission = MutableSharedFlow<EstimateCommissionResult>(replay = 1)
    override val estimateCommission: Flow<EstimateCommissionResult> = _estimateCommission.asSharedFlow()

    private val transferClient: TransferGrpcClient = grpcClientFactory.getGrpcClient(
        TransferGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    override suspend fun sendTronTransactionRequestGrpc(userId: Long,
                                                        transaction: TransactionData,
                                                        commission: TransactionCommissionData?,
                                                        network: TransferNetwork,
                                                        token: TransferProto.TransferToken,
                                                        txId: String?
    ) {
        try {
            val result = transferClient.sendTronTransactionRequest(
                userId = userId,
                transaction = transaction,
                commission = commission,
                network = network,
                token = token,
                txId = txId
            )
            result.fold(
                onSuccess = {
                    Log.d("sendTronTransactionRequestGrpc", it.timestamp.toString())
                },
                onFailure = {
                    val message = it.message ?: "Unknown gRPC error"
                    val cause = it.cause ?: Throwable("No cause provided")
                    val exception = GrpcServerErrorSendTransactionExcpetion(message, cause)

                    Sentry.captureException(exception)
                    throw exception
                }
            )
        } catch (e: Exception) {
            val message = e.message ?: "Unknown client error"
            val cause = e.cause ?: Throwable("No cause provided")
            val exception = GrpcClientErrorSendTransactionExcpetion(message, cause)

            Sentry.captureException(exception)
            throw exception
        }
    }

    override suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long) {
        val userId = profileRepo.getProfileUserId()

        try {
            val result = transferClient.estimateCommission(userId, address, bandwidth = bandwidth, energy = energy)
            result.fold(
                onSuccess = {
                    _estimateCommission.emit(EstimateCommissionResult.Success(it))
                },
                onFailure = {
                    _estimateCommission.emit(EstimateCommissionResult.Error(it))
                }
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}