package com.example.telegramWallet.backend.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrpcClientFactory @Inject constructor() {
    private val channels = mutableMapOf<String, ManagedChannel>()
    private val clients = mutableMapOf<String, Any>()

    private fun getOrCreateChannel(host: String, port: Int): ManagedChannel {
        return channels.getOrPut("$host:$port") {
            ManagedChannelBuilder.forAddress(host, port)
                .useTransportSecurity()
                .build()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getGrpcClient(clientClass: Class<T>, host: String, port: Int): T {
        val key = "${clientClass.simpleName}@$host:$port"

        return clients.getOrPut(key) {
            val channel = getOrCreateChannel(host, port)
            when (clientClass) {
                TransferGrpcClient::class.java -> TransferGrpcClient(channel)
                AmlGrpcClient::class.java -> AmlGrpcClient(channel)
                CryptoAddressGrpcClient::class.java -> CryptoAddressGrpcClient(channel)
                SmartContractGrpcClient::class.java -> SmartContractGrpcClient(channel)
                UserGrpcClient::class.java -> UserGrpcClient(channel)
                else -> throw IllegalArgumentException("Неизвестный gRPC-клиент: ${clientClass.simpleName}")
            }
        } as T
    }

    fun shutdownAll() {
        clients.clear()
        channels.values.forEach { it.shutdown() }
        channels.clear()
    }
}