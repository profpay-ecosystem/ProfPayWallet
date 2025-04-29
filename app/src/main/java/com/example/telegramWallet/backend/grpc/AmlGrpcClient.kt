package com.example.telegramWallet.backend.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.aml.AmlProto
import org.server.protobuf.aml.AmlServiceGrpc

class AmlGrpcClient(private val channel: ManagedChannel) {
    private val stub: AmlServiceGrpc.AmlServiceBlockingStub = AmlServiceGrpc.newBlockingStub(channel)

    suspend fun getAmlFromTransactionId(userId: Long, address: String, tx: String, tokenName: String): Result<AmlProto.GetAmlByTxIdResponse> = withContext(
        Dispatchers.IO
    ) {
        try {
            val request = AmlProto.GetAmlByTxIdRequest.newBuilder()
                .setUserId(userId)
                .setAddress(address)
                .setTx(tx)
                .setTokenName(tokenName)
                .build()

            val response = stub.getAmlFromTransactionId(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renewAmlFromTransactionId(userId: Long, address: String, tx: String, tokenName: String): Result<AmlProto.GetAmlByTxIdResponse> = withContext(
        Dispatchers.IO
    ) {
        try {
            val request = AmlProto.GetAmlByTxIdRequest.newBuilder()
                .setUserId(userId)
                .setAddress(address)
                .setTx(tx)
                .setTokenName(tokenName)
                .build()

            val response = stub.renewAmlFromTransactionId(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processAmlPayment(request: AmlProto.AmlPaymentRequest): Result<AmlProto.AmlPaymentResponse> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = stub.processAmlPayment(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        fun create(address: String, port: Int): AmlGrpcClient {
            val channel = ManagedChannelBuilder.forAddress(address, port)
                .useTransportSecurity()
                .build()
            return AmlGrpcClient(channel)
        }
    }
}