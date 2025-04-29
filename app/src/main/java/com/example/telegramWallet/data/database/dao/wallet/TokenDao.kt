package com.example.telegramWallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import java.math.BigInteger

@Dao
interface TokenDao {
    @Insert(entity = TokenEntity::class)
    fun insertNewTokenEntity(tokenEntity: TokenEntity): Long

    @Query("UPDATE tokens SET balance = balance + :amount WHERE address_id = :addressId AND token_name = :tokenName")
    fun increaseTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)

    @Query("UPDATE tokens SET balance = balance - :amount WHERE address_id = :addressId AND token_name = :tokenName")
    fun decreaseTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)

    @Query("UPDATE tokens SET balance = :amount WHERE address_id = :addressId AND token_name = :tokenName")
    fun updateTronBalanceViaId(amount: BigInteger, addressId: Long, tokenName: String)

    @Query("UPDATE tokens SET frozen_balance = frozen_balance + :frozenBalance WHERE address_id = :addressId AND token_name = :tokenName")
    fun increaseTronFrozenBalanceViaId(frozenBalance: BigInteger, addressId: Long, tokenName: String)

    @Query("UPDATE tokens SET frozen_balance = frozen_balance - :frozenBalance WHERE address_id = :addressId AND token_name = :tokenName")
    fun decreaseTronFrozenBalanceViaId(
        frozenBalance: BigInteger,
        addressId: Long,
        tokenName: String,
    )

    @Query("SELECT frozen_balance FROM tokens WHERE address_id = :addressId AND token_name = :tokenName")
    fun getTronFrozenBalanceViaId(addressId: Long, tokenName: String): BigInteger

    @Query("UPDATE tokens SET frozen_balance = 0 WHERE address_id = :addressId AND token_name = :tokenName")
    fun updateTronFrozenBalanceViaId(
        addressId: Long,
        tokenName: String,
    )
}