package com.example.telegramWallet.data.pusher.smart.model

import com.google.gson.annotations.SerializedName

data class SmartContractStatusChangedResponsePair (
    @SerializedName("appId") val appId: String,
    @SerializedName("eventName") val eventName: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
)