package com.example.telegramWallet.data.pusher.smart

import android.util.Log
import com.example.telegramWallet.data.pusher.PusherDI
import com.example.telegramWallet.data.pusher.PusherEventProcessor
import com.example.telegramWallet.data.pusher.smart.model.SmartContractStatusChangedResponsePair
import com.google.gson.Gson
import com.pusher.client.channel.PusherEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartContractStatusChangedProcessor : PusherEventProcessor {
    override fun processEvent(
        eventData: PusherEvent,
        notificationFunction: (String, String) -> Unit,
        di: PusherDI
    ) {
        try {
            val gson = Gson()
            val contractData: SmartContractStatusChangedResponsePair =
                gson.fromJson(eventData.data, SmartContractStatusChangedResponsePair::class.java)

            notificationFunction(contractData.title, contractData.message);
        } catch (e: Exception) {
            Log.e("Pusher Error", e.toString())
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            di.smartContractStorage.getMyContractDeals()
        }
    }
}