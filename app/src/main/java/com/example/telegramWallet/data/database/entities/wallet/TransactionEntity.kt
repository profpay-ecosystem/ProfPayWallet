package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = ["address_id"],
            childColumns = ["sender_address_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = ["address_id"],
            childColumns = ["receiver_address_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            name = "index_transactions_tx_id_wallet_id",
            value = ["tx_id", "wallet_id"],
            unique = true
        ),
        Index(value = ["sender_address_id"]),
        Index(value = ["receiver_address_id"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "transaction_id") val transactionId: Long? = null,
    @ColumnInfo(name = "tx_id") val txId: String,
    @ColumnInfo(name = "sender_address_id") val senderAddressId: Long?,
    @ColumnInfo(name = "receiver_address_id") val receiverAddressId: Long?,
    @ColumnInfo(name = "sender_address") val senderAddress: String,
    @ColumnInfo(name = "receiver_address") val receiverAddress: String,
    @ColumnInfo(name = "wallet_id") val walletId: Long,
    @ColumnInfo(name = "token_name") val tokenName: String,
    @ColumnInfo(name = "amount", defaultValue = "0") val amount: BigInteger,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "is_processed", defaultValue = "0") val isProcessed: Boolean = false,
    @ColumnInfo(name = "server_response_received", defaultValue = "0") val serverResponseReceived: Boolean = false,
    @ColumnInfo(name = "type", defaultValue = "0") val type: Int,
)

enum class TransactionType(val index: Int){
    RECEIVE(0),
    SEND(1),
    BETWEEN_YOURSELF(2),
    TRIGGER_SMART_CONTRACT(3)
}
fun assignTransactionType(idSend: Long?, idReceive: Long?): Int {
    return if (idSend == null && idReceive == null){
        -1
    } else if (idSend != null && idReceive == null){
        TransactionType.SEND.index
    } else if (idSend == null){
        TransactionType.RECEIVE.index
    } else {
        TransactionType.BETWEEN_YOURSELF.index
    }

}
