package com.example.telegramWallet.backend.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractServerGrpc

class SmartContractGrpcClient(private val channel: ManagedChannel) {
    private val stub: SmartContractServerGrpc.SmartContractServerBlockingStub = SmartContractServerGrpc.newBlockingStub(channel)

    suspend fun getMyContractDeals(userId: Long): Result<SmartContractProto.GetMyContractDealsResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SmartContractProto.GetMyContractDealsRequest.newBuilder()
                .setUserId(userId)
                .build()
            val response = stub.getMyContractDeals(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun contractDealStatusChanged(request: SmartContractProto.ContractDealUpdateRequest): Result<SmartContractProto.ContractDealUpdateResponse>
    = withContext(Dispatchers.IO) {
        try {
            val response = stub.contractDealStatusChanged(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun contractDealStatusExpertChanged(request: SmartContractProto.ContractDealUpdateRequest): Result<SmartContractProto.ContractDealUpdateResponse>
    = withContext(Dispatchers.IO) {
        try {
            val response = stub.contractDealStatusExpertChanged(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deploySmartContract(request: SmartContractProto.DeploySmartContractRequest): Result<SmartContractProto.DeploySmartContractResponse>
    = withContext(Dispatchers.IO) {
        try {
            val response = stub.deploySmartContract(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getResourceQuote(request: SmartContractProto.ResourceQuoteRequest): Result<SmartContractProto.ResourceQuoteResponse>
    = withContext(Dispatchers.IO) {
        try {
            val response = stub.getResourceQuote(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun callContract(request: SmartContractProto.CallContractRequest): Result<SmartContractProto.CallContractResponse>
    = withContext(Dispatchers.IO) {
        try {
            val response = stub.callContract(request)
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