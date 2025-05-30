package com.example.telegramWallet.data.database.repositories

import androidx.lifecycle.LiveData
import com.example.telegramWallet.bridge.view_model.dto.ProfileDto
import com.example.telegramWallet.data.database.dao.ProfileDao
import com.example.telegramWallet.data.database.entities.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface ProfileRepo {
    suspend fun getProfileUserId(): Long
    suspend fun getProfileAppId(): String
    // Создание профиля аккаунта
    suspend fun insertNewProfile(profileEntity: ProfileEntity)
    // Получение TgId подтверждённого профиля
    suspend fun getProfileTelegramId(): LiveData<Long>
    suspend fun getProfileTgUsername(): LiveData<String>
    // Получение кол-ва профилей в бд
    suspend fun isProfileExists(): Boolean
    // Получение данных неподтверждённого профиля для UI
    suspend fun getInactiveProfileFromVM(): Flow<ProfileDto>
    // Получение данных подтверждённого профиля для UI
    suspend fun getActiveProfileFromVM(): Flow<ProfileDto>
    // Удаление профиля по Tg-Id
    suspend fun deleteProfileByTgId(tgId: Long)
    // Получение данных подтверждённого профиля
    suspend fun getActiveProfile(): ProfileDto
    // Обновление активности профиля и access-token с временем его жизни
    suspend fun updateActiveTgId(
        valActive: Boolean,
        tgId: Long,
        accessToken: String,
        expiresAt: Long
    )
    suspend fun updateProfileTelegramIdAndUsername(telegramId: Long, username: String)
    suspend fun getDeviceToken(): String?
    suspend fun updateDeviceToken(deviceToken: String)
    suspend fun updateUserId(userId: Long)
}

@Singleton
class ProfileRepoImpl @Inject constructor(private val profileDao: ProfileDao): ProfileRepo {
    override suspend fun getProfileAppId(): String {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getProfileAppId()
        }
    }
    override suspend fun getProfileUserId(): Long {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getProfileUserId()
        }
    }
    override suspend fun insertNewProfile(profileEntity: ProfileEntity) {
        withContext(Dispatchers.IO) {
            profileDao.insertNewProfile(profileEntity)
        }
    }

    override suspend fun getProfileTelegramId(): LiveData<Long> {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getProfileTelegramId()
        }
    }

    override suspend fun getProfileTgUsername(): LiveData<String> {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getProfileTgUsername()
        }
    }

    override suspend fun isProfileExists(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.isProfileExists()
        }
    }

    override suspend fun updateActiveTgId(valActive: Boolean, tgId: Long, accessToken: String, expiresAt: Long) {
        withContext(Dispatchers.IO) {
            profileDao.updateActiveTgId(valActive, tgId, accessToken, expiresAt)
        }
    }

    override suspend fun updateProfileTelegramIdAndUsername(telegramId: Long, username: String) {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.updateProfileTelegramIdAndUsername(telegramId, username)
        }
    }

    override suspend fun getDeviceToken(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getDeviceToken()
        }
    }

    override suspend fun updateDeviceToken(deviceToken: String) {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.updateDeviceToken(deviceToken)
        }
    }

    override suspend fun updateUserId(userId: Long) {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.updateUserId(userId)
        }
    }

    override suspend fun getInactiveProfileFromVM(): Flow<ProfileDto> {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getInactiveProfileFromVM()
        }
    }

    override suspend fun getActiveProfileFromVM(): Flow<ProfileDto> {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getActiveProfileFromVM()
        }
    }

    override suspend fun deleteProfileByTgId(tgId: Long) {
        withContext(Dispatchers.IO) {
            profileDao.deleteProfileByTgId(tgId)
        }
    }

    override suspend fun getActiveProfile(): ProfileDto {
        return withContext(Dispatchers.IO) {
            return@withContext profileDao.getActiveProfile()
        }
    }
}