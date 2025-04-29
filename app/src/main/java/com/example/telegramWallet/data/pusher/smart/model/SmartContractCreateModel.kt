package com.example.telegramWallet.data.pusher.smart.model

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class SmartContractCreateModel(
    @SerializedName("id") val id: Int,
    @SerializedName("ownerAddress") val ownerAddress: String,
    @SerializedName("receiverAddress") val receiverAddress: String,
    @SerializedName("admin1Address") val admin1Address: String,
    @SerializedName("admin2Address") val admin2Address: String,
    @SerializedName("admin3Address") val admin3Address: String,
    @SerializedName("amount") val amount: BigInteger
)
