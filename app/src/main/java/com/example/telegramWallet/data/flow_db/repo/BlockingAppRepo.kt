package com.example.telegramWallet.data.flow_db.repo

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.telegramWallet.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BlockingAppRepo {
    val isBlockedApp: Flow<BlockingAppRepoState>
    suspend fun getBlockedAppState()
}

class BlockingAppRepoImpl @Inject constructor(@ApplicationContext val appContext: Context) :
    BlockingAppRepo {
    private val _isBlockedApp = MutableSharedFlow<BlockingAppRepoState>(replay = 1)
    // Получение текущего состояния статуса блокировки приложения
    override val isBlockedApp: Flow<BlockingAppRepoState> =
        _isBlockedApp.asSharedFlow()
    // Триггер обновления статуса
    override suspend fun getBlockedAppState() {
        withContext(Dispatchers.IO) {
            val sharedPref = appContext.getSharedPreferences(
                ContextCompat.getString(appContext, R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val isBlockedApp = sharedPref.getBoolean("is_blocked_app", false)
            _isBlockedApp.emit(BlockingAppRepoState(isBlockedApp))
        }
    }
}
data class BlockingAppRepoState(
    val isBlockedApp: Boolean
)