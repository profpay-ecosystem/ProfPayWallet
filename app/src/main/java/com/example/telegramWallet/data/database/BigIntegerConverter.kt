package com.example.telegramWallet.data.database

import androidx.room.TypeConverter
import java.math.BigInteger

class BigIntegerConverter {
    @TypeConverter
    fun fromBigInteger(value: BigInteger?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigInteger(value: String?): BigInteger? {
        return value?.let { BigInteger(it) }
    }
}