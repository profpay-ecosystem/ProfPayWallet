package com.example.telegramWallet.data.flow_db

import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GrpcModule {
//    @Provides
//    @Singleton
//    fun provideGrpcClientFactory(): GrpcClientFactory {
//        return GrpcClientFactory()
//    }
}