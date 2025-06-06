package com.example.telegramWallet.data.flow_db.repo

import com.example.telegramWallet.backend.grpc.CryptoAddressGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.tron.AddressGenerateFromSeedPhr
import com.example.telegramWallet.tron.AddressGenerateResult
import com.example.telegramWallet.tron.Tron
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AddressAndMnemonicRepo {
    suspend fun generateNewAddressAndMnemonic()
    val addressAndMnemonic: Flow<AddressGenerateResult>
    val addressFromMnemonic: Flow<RecoveryResult>
    suspend fun generateAddressFromMnemonic(mnemonic: String)
    suspend fun recoveryWallet(address: String, mnemonic: String)
}

class AddressAndMnemonicRepoImpl @Inject constructor(
    val profileRepo: ProfileRepo,
    val addressRepo: AddressRepo,
    private val tron: Tron,
    grpcClientFactory: GrpcClientFactory
) : AddressAndMnemonicRepo {
    private val cryptoAddressGrpcClient: CryptoAddressGrpcClient = grpcClientFactory.getGrpcClient(
        CryptoAddressGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )
    private val _addressAndMnemonic = MutableSharedFlow<AddressGenerateResult>(replay = 1)

    // Получение данных нового кошелька
    override val addressAndMnemonic: Flow<AddressGenerateResult> =
        _addressAndMnemonic.asSharedFlow()

    // Триггер на обновление данных нового кошелька
    override suspend fun generateNewAddressAndMnemonic() {
        withContext(Dispatchers.IO) {
            val addressAndMnemonic = tron.addressUtilities.generateAddressAndMnemonic()
            _addressAndMnemonic.emit(addressAndMnemonic)
        }
    }

    private val _addressFromMnemonic = MutableSharedFlow<RecoveryResult>(replay = 1)

    // Получение данных восстановленного кошелька по мнемонике(сид-фразе)
    override val addressFromMnemonic: Flow<RecoveryResult> =
        _addressFromMnemonic.asSharedFlow()

    // Триггер на обновление данных восстановленного кошелька по мнемонике(сид-фразе)
    override suspend fun generateAddressFromMnemonic(mnemonic: String) {
        withContext(Dispatchers.IO) {
            try {
                val generalAddress = tron.addressUtilities.getGeneralAddressBySeedPhrase(mnemonic)

                val byAddressOrNull = addressRepo.getAddressEntityByAddress(generalAddress)
                if (byAddressOrNull == null) {
                    recoveryWallet(generalAddress, mnemonic)
                } else {
                    _addressFromMnemonic.emit(RecoveryResult.RepeatingMnemonic)
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                _addressFromMnemonic.emit(RecoveryResult.InvalidMnemonic)
            }
        }
    }

    override suspend fun recoveryWallet(gAddress: String, mnemonic: String) {
        try {
            val result = cryptoAddressGrpcClient.getWalletData(address = gAddress)

            result.fold(
                onSuccess = { walletData ->
                    val recoveryResult = try {
                        val addressGenerateFromSeedPhr = tron.addressUtilities.recoveryKeysAndAddressBySeedPhrase(
                            mnemonic,
                            walletData.derivedIndicesList
                        )
                        RecoveryResult.Success(address = addressGenerateFromSeedPhr, accountWasFound = true, userId = walletData.userId)
                    } catch (e: Exception) {
                        RecoveryResult.InvalidMnemonic
                    }

                    _addressFromMnemonic.emit(recoveryResult)
                },

                onFailure = { error ->
                    // TODO: Создать enum на стороне gPRC
                    if (error.message == "INTERNAL: Address not found in database") {
                        val address = try {
                            tron.addressUtilities.generateKeysAndAddressBySeedPhrase(mnemonic)
                        } catch (e: Exception) {
                            _addressFromMnemonic.emit(RecoveryResult.InvalidMnemonic)
                            return
                        }
                        _addressFromMnemonic.emit(RecoveryResult.Success(address = address, accountWasFound = false))
                    } else {
                        Sentry.captureException(error)
                        _addressFromMnemonic.emit(RecoveryResult.Error(RuntimeException(error)))
                    }
                }
            )

        } catch (e: Exception) {
            Sentry.captureException(e)
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }
}

sealed class RecoveryResult {
    data class Success(val address: AddressGenerateFromSeedPhr, val accountWasFound: Boolean, val userId: Long? = null) : RecoveryResult()
    data object InvalidMnemonic : RecoveryResult()
    data object RepeatingMnemonic : RecoveryResult()
    data object AddressNotFound : RecoveryResult()
    data class Error(val throwable: Throwable) : RecoveryResult()
}

