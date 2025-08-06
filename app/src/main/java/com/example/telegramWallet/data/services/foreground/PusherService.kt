package com.example.telegramWallet.data.services.foreground

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.telegramWallet.MainActivity
import com.example.telegramWallet.R
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.scheduler.transfer.tron.UsdtTransferScheduler
import com.example.telegramWallet.tron.Tron
import dagger.hilt.android.AndroidEntryPoint
import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class PusherService : Service(), CoroutineScope {
    private var job: Job = Job()
    @Inject lateinit var profileRepo: ProfileRepo
    @Inject lateinit var addressRepo: AddressRepo
    @Inject lateinit var transactionsRepo: TransactionsRepo
    @Inject lateinit var tokenRepo: TokenRepo
    @Inject lateinit var centralAddressRepo: CentralAddressRepo
    @Inject lateinit var tron: Tron
    @Inject lateinit var pendingTransactionRepo: PendingTransactionRepo

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        launch {
            scheduleAll()
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val uniqueNotificationID = System.currentTimeMillis().toInt() // Использование текущего времени для идентификатора
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Сервис уведомлений")
            .setContentText("Сервис уведомлений запущен! Данное уведомление невозможно скрыть.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(uniqueNotificationID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun showNotification(contentTitle: String, contentText: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = Notification.Builder(this, PusherService.CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setStyle(Notification.BigTextStyle().bigText(contentText))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Необходимая для сервиса функций уведомлений, Android по умолчанию выключает уведомления.
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            PusherService.CHANNEL_ID, "Pusher Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }

    private suspend fun scheduleAll() {
        val kronScheduler = buildSchedule {
            seconds {
                0 every 45
            }
        }
        kronScheduler.doInfinity {
            UsdtTransferScheduler(
                addressRepo = addressRepo,
                transactionsRepo = transactionsRepo,
                profileRepo = profileRepo,
                tokenRepo = tokenRepo,
                centralAddressRepo = centralAddressRepo,
                notificationFunction = ::showNotification,
                tron = tron,
                pendingTransactionRepo = pendingTransactionRepo
            ).scheduleAddresses()
        }
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        var isRunning = false
        private const val CHANNEL_ID = "PUSHER_SERVICE_CHANNEL"
    }
}