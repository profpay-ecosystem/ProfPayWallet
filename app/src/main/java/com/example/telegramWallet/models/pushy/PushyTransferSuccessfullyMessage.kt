package com.example.telegramWallet.models.pushy

import kotlinx.serialization.Serializable

@Serializable
data class PushyTransferSuccessfullyMessage (
    val senderAddress: String,
    val amount: Long,
    val transactionType: String
)