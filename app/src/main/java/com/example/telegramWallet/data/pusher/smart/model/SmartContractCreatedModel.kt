package com.example.telegramWallet.data.pusher.smart.model

import com.google.gson.annotations.SerializedName

data class SmartContractCreatedModel (
    @SerializedName("contractId") val contractId: Long,
    @SerializedName("contractAddress") val contractAddress: String,
    @SerializedName("ownerUsername") val ownerUsername: String,
    @SerializedName("receiverUsername") val receiverUsername: String,
    @SerializedName("ownerUserId") val ownerUserId: Long,
    @SerializedName("receiverUserId") val receiverUserId: Long,
)