package com.example.telegramWallet.backend.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionCommissionData
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferServiceGrpc

class TransferGrpcClient(private val channel: ManagedChannel) {
    private val stub: TransferServiceGrpc.TransferServiceBlockingStub = TransferServiceGrpc.newBlockingStub(channel)

    suspend fun sendTronTransactionRequest(userId: Long,
                                           transaction: TransactionData,
                                           commission: TransactionCommissionData? = null,
                                           network: TransferNetwork,
                                           token: TransferProto.TransferToken,
                                           txId: String? = null): Result<TransferProto.SendUsdtTransactionResponse> = withContext(
        Dispatchers.IO
    ) {
        try {
            val request = TransferProto.SendUsdtTransactionRequest.newBuilder()
                .setUserId(userId)
                .setTransactionData(transaction)
                .setCommissionData(commission)
                .setNetwork(network)
                .setToken(token)
                .setTxId(txId)
                .build()

            val response = stub.sendTronTransaction(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun estimateCommission(userId: Long, address: String, bandwidth: Long, energy: Long): Result<TransferProto.EstimateCommissionResponse> = withContext(
        Dispatchers.IO
    ) {
        try {
            val request = TransferProto.EstimateCommissionRequest.newBuilder()
                .setUserId(userId)
                .setAddress(address)
                .setBandwidthRequired(bandwidth)
                .setEnergyRequired(energy)
                .build()

            val response = stub.estimateCommission(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionStatus(txId: String): Result<TransferProto.TransactionStatusResponse> = withContext(
        Dispatchers.IO
    ) {
        try {
            val request = TransferProto.TransactionStatusRequest.newBuilder()
                .setTxId(txId)
                .build()

            val response = stub.getTransactionStatus(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        fun create(address: String, port: Int): TransferGrpcClient {
            val channel = ManagedChannelBuilder.forAddress(address, port)
                .useTransportSecurity()
                .build()
            return TransferGrpcClient(channel)
        }
    }
}