package com.example.telegramWallet.backend.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.user.UserProto
import org.example.protobuf.user.UserServiceGrpc

class UserGrpcClient(private val channel: ManagedChannel) {
    private val stub: UserServiceGrpc.UserServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel)

    suspend fun setUserLegalConsentsTrue(appId: String): Result<UserProto.UserLegalConsentsResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = UserProto.UserLegalConsentsRequest.newBuilder()
                .setAppId(appId)
                .build()
            val response = stub.setUserLegalConsentsTrue(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(appId: String, deviceToken: String): Result<UserProto.RegisterUserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UserProto.RegisterUserRequest.newBuilder()
                .setAppId(appId)
                .setDeviceToken(deviceToken)
                .build()
            val response = stub.registerUser(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserDeviceToken(appId: String, deviceToken: String): Result<UserProto.UserTelegramDataResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UserProto.UpdateUserDeviceTokenRequest.newBuilder()
                .setAppId(appId)
                .setDeviceToken(deviceToken)
                .build()
            val response = stub.updateUserDeviceToken(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserTelegramData(appId: String): Result<UserProto.UserTelegramDataResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UserProto.UserTelegramDataRequest.newBuilder()
                .setAppId(appId)
                .build()
            val response = stub.getUserTelegramData(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUserDevice(userId: Long, appId: String, deviceToken: String): Result<UserProto.RegisterUserDeviceResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = UserProto.RegisterUserDeviceRequest.newBuilder()
                .setUserId(userId)
                .setAppId(appId)
                .setDeviceToken(deviceToken)
                .build()
            val response = stub.registerUserDevice(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserExists(userId: Long): Result<UserProto.IsUserExistsResponse> = withContext(
        Dispatchers.IO) {
        try {
            val request = UserProto.IsUserExistsRequest.newBuilder()
                .setUserId(userId)
                .build()
            val response = stub.isUserExists(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        fun create(address: String, port: Int): UserGrpcClient {
            val channel = ManagedChannelBuilder.forAddress(address, port)
                .useTransportSecurity()
                .build()
            return UserGrpcClient(channel)
        }
    }
}