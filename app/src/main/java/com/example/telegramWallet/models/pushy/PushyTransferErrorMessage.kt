package com.example.telegramWallet.models.pushy

import kotlinx.serialization.Serializable

@Serializable
data class PushyTransferErrorMessage(
    val senderAddress: String,
    val amount: Long,
    val transactionType: String,
    val transactionId: String
)
