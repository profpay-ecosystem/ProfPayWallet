package com.example.telegramWallet.data.pusher.smart

import android.util.Log
import com.example.telegramWallet.data.pusher.PusherDI
import com.example.telegramWallet.data.pusher.PusherEventProcessor
import com.example.telegramWallet.data.pusher.smart.model.SmartContractCreatedModel
import com.google.gson.Gson
import com.pusher.client.channel.PusherEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartContractDealConfirmationProcessor : PusherEventProcessor {
    override fun processEvent(
        eventData: PusherEvent,
        notificationFunction: (String, String) -> Unit,
        di: PusherDI
    ) {
        try {
            val gson = Gson()
            val contractData: SmartContractCreatedModel =
                gson.fromJson(eventData.data, SmartContractCreatedModel::class.java)

            CoroutineScope(Dispatchers.Default).launch {
                if (di.profileStorage.getProfileUserId() == contractData.ownerUserId) {
                    notificationFunction(
                        "Подтверждение условий сделки",
                        "Созданный Вами контракт был подтвержден пользователем @${contractData.receiverUsername}\n" +
                                "Адрес контракта: ${contractData.contractAddress}"
                    );
                } else {
                    notificationFunction(
                        "Внесен депозит на смарт-контракт",
                        "Вы успешно подтвердили контракт пользователя @${contractData.ownerUsername}\n" +
                                "Адрес контракта: ${contractData.contractAddress}"
                    );
                }
            }
        } catch (e: Exception) {
            Log.e("Pusher Error", e.toString())
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            di.smartContractStorage.getMyContractDeals()
        }
    }
}