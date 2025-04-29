package com.example.telegramWallet.bridge.view_model.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.flow_db.repo.SettingsAccountRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsAccountViewModel @Inject constructor(
    private val profileRepo: ProfileRepo,
    private val settingsAccountRepo: SettingsAccountRepo
) : ViewModel() {
    suspend fun getProfileUserId(): Long {
        return withContext(Dispatchers.IO) {
            profileRepo.getProfileUserId()
        }
    }

    suspend fun getProfileAppId(): String {
        return withContext(Dispatchers.IO) {
            return@withContext profileRepo.getProfileAppId()
        }
    }

    suspend fun getProfileTelegramId(): LiveData<Long> {
        return withContext(Dispatchers.IO) {
            return@withContext profileRepo.getProfileTelegramId()
        }
    }

    suspend fun getProfileTgUsername(): LiveData<String> {
        return withContext(Dispatchers.IO) {
            return@withContext profileRepo.getProfileTgUsername()
        }
    }

    suspend fun getUserTelegramData() {
        settingsAccountRepo.getUserTelegramData()
    }
}