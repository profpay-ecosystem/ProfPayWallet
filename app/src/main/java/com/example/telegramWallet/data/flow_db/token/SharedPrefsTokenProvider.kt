package com.example.telegramWallet.data.flow_db.token

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.telegramWallet.R
import com.example.telegramWallet.backend.grpc.AuthGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import javax.inject.Inject

class SharedPrefsTokenProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val profileRepo: ProfileRepo,
    private val grpcClientFactory: Lazy<GrpcClientFactory>
) : TokenProvider {
    private val authGrpcClient: AuthGrpcClient by lazy {
        grpcClientFactory.get().getGrpcClient(
            AuthGrpcClient::class.java,
            "grpc.wallet-services-srv.com",
            8443
        )
    }

    private val prefs = context.getSharedPreferences(
        ContextCompat.getString(context, R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    override fun getAccessToken(): String =
        prefs.getString("access_token", "") ?: ""
    override fun getRefreshToken(): String =
        prefs.getString("refresh_token", "") ?: ""

    override suspend fun refreshTokensIfNeeded() {
        val userId = profileRepo.getProfileUserId()
        val appId = profileRepo.getProfileAppId()
        val deviceToken = profileRepo.getDeviceToken()

        if (deviceToken == null) return

        if (getRefreshToken().isEmpty()) {
            val result = authGrpcClient.issueTokens(
                appId = appId,
                userId = userId,
                deviceToken = deviceToken
            )

            result.fold(
                onSuccess = {
                    saveTokens(
                        access = it.accessToken,
                        refresh = it.refreshToken
                    )
                },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )
        } else {
            val result = authGrpcClient.refreshTokenPair(
                refreshToken = getRefreshToken(),
                userId = userId,
                deviceToken = deviceToken
            )

            result.fold(
                onSuccess = {
                    saveTokens(
                        access = it.accessToken,
                        refresh = it.refreshToken
                    )
                },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )
        }
    }

    override fun saveTokens(access: String, refresh: String) {
        prefs.edit(commit = true) {
            putString("access_token", access)
            putString("refresh_token", refresh)
        }
    }

    override fun clearTokens() {
        prefs.edit { clear() }
    }
}
