package com.example.telegramWallet.backend.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.address.CryptoAddressProto
import org.example.protobuf.address.CryptoAddressServiceGrpc

class CryptoAddressGrpcClient(private val channel: ManagedChannel) {
    private val stub: CryptoAddressServiceGrpc.CryptoAddressServiceBlockingStub = CryptoAddressServiceGrpc.newBlockingStub(channel)

    suspend fun addCryptoAddress(appId: String, address: String, pubKey: String, derivedIndices: Iterable<Int>): Result<CryptoAddressProto.AddCryptoAddressResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = CryptoAddressProto.AddCryptoAddressRequest.newBuilder()
                .setAppId(appId)
                .setAddress(address)
                .setPubKey(pubKey)
                .addAllDerivedIndices(derivedIndices)
                .build()
            val response = stub.addCryptoAddress(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDerivedIndex(appId: String, oldIndex: Long, newIndex: Long, generalAddress: String): Result<CryptoAddressProto.UpdateDerivedIndexResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = CryptoAddressProto.UpdateDerivedIndexRequest.newBuilder()
                .setAppId(appId)
                .setGeneralAddress(generalAddress)
                .setOldIndex(oldIndex)
                .setNewIndex(newIndex)
                .build()
            val response = stub.updateDerivedIndex(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDerivedIndex(userId: Long, generalAddress: String, derivedIndices: Iterable<Int>): Result<CryptoAddressProto.SetDerivedIndicesResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = CryptoAddressProto.SetDerivedIndicesRequest.newBuilder()
                .setUserId(userId)
                .setGeneralAddress(generalAddress)
                .addAllDerivedIndices(derivedIndices)
                .build()
            val response = stub.setDerivedIndices(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWalletData(address: String): Result<CryptoAddressProto.GetWalletDataResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = CryptoAddressProto.GetWalletDataRequest.newBuilder()
                .setAddress(address)
                .build()
            val response = stub.getWalletData(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        fun create(address: String, port: Int): CryptoAddressGrpcClient {
            val channel = ManagedChannelBuilder.forAddress(address, port)
                .useTransportSecurity()
                .build()
            return CryptoAddressGrpcClient(channel)
        }
    }
}
