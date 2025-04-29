package com.example.telegramWallet.ui.shared.utils

import kotlin.random.Random

fun getRandomIndexesFromList(listSize: Int, queryIndexes: Int): List<Int> {
    var counterIndex: Set<Int> = HashSet()
    while (counterIndex.size < queryIndexes) {
        counterIndex += Random.nextInt(listSize)
    }
    return counterIndex.toList()
}