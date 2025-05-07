package com.example.telegramWallet.backend.grpc

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.prof_pay_server.ProfPayServerGrpc
import org.server.protobuf.prof_pay_server.ProfPayServerProto

class ProfPayServerGrpcClient(private val channel: ManagedChannel) {
    private val stub: ProfPayServerGrpc.ProfPayServerBlockingStub = ProfPayServerGrpc.newBlockingStub(channel)

    suspend fun getServerStatus(): Result<ProfPayServerProto.GetServerStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = stub.getServerStatus(Empty.newBuilder().build())
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServerParameters(): Result<ProfPayServerProto.GetServerParametersResponse> = withContext(Dispatchers.IO) {
        try {
            val response = stub.getServerParameters(Empty.newBuilder().build())
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        fun create(address: String, port: Int): SmartContractGrpcClient {
            val channel = ManagedChannelBuilder.forAddress(address, port)
                .useTransportSecurity()
                .build()
            return SmartContractGrpcClient(channel)
        }
    }
}