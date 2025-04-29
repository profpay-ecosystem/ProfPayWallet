package com.example.telegramWallet.tron.http

import com.example.telegramWallet.BuildConfig
import com.example.telegramWallet.tron.http.models.Trc20TransactionsDataResponse
import com.example.telegramWallet.tron.http.models.Trc20TransactionsResponse
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


interface Trc20TransactionsRequestCallback {
    fun onSuccess(data: List<Trc20TransactionsDataResponse>)
    fun onFailure(error: String)
}

// API запрос для получения USDT TRC20 транзакций.
class Trc20TransactionsService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = true }

    fun makeRequest(callback: Trc20TransactionsRequestCallback, address: String) {
        val tronHost: String
        val contractAddress: String

        if (BuildConfig.DEBUG) {
            tronHost = "nile.trongrid.io"
            contractAddress = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"
        } else {
            tronHost = "api.trongrid.io"
            contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        }
        val http = HttpUrl.Builder()
            .scheme("https")
            .host(tronHost)
            .addPathSegments("v1/accounts/${address}/transactions/trc20")
//            .addQueryParameter("only_confirmed", "true")
            .addQueryParameter("contract_address", contractAddress)
            .addQueryParameter("limit", "200")
            .build()

        val request = Request.Builder()
            .url(http)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onFailure("Error receiving TRC20 transactions")
                        throw IOException("Error receiving TRC20 transactions")
                    }

                    try {
                        val obj = localJson
                            .decodeFromString<Trc20TransactionsResponse>(response.body!!.string())
                        callback.onSuccess(obj.data)
                    } catch (e: Exception) {
                        callback.onFailure(e.toString())
                    }
                }
            }
        })
    }
}

object Trc20TransactionsApi {
    val trc20TransactionsService : Trc20TransactionsService by lazy {
        Trc20TransactionsService()
    }
}