package com.example.telegramWallet.tron.smart_contract

import org.tron.trident.abi.FunctionReturnDecoder
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.core.ApiWrapper
import org.tron.trident.utils.Numeric

class MultiSigRead {
    /**
     * Получаем текущий адрес USDT смарт-контракта.
     */
    fun getUsdt(ownerAddress: String, privateKey: String, contractAddress: String): String {
        val wrapper: ApiWrapper = ApiWrapper.ofNile(privateKey)

        val usdtFunc = Function("USDT", emptyList(), listOf(object : TypeReference<Address?>() {}))
        val extension = wrapper.triggerConstantContract(ownerAddress, contractAddress, usdtFunc)
        val result = Numeric.toHexString(extension.getConstantResult(0).toByteArray())

        val decodedResult = FunctionReturnDecoder.decode(result, usdtFunc.outputParameters)
        wrapper.close()
        return decodedResult[0].value.toString()
    }

    // returns openDeals, closedDeals 1
    fun getContractStats(ownerAddress: String, privateKey: String, contractAddress: String): Pair<String, String> {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val function = Function("getContractStats", emptyList(), listOf(object : TypeReference<Uint256?>() {}, object : TypeReference<Uint256?>() {}))
        val extension = wrapper.triggerConstantContract(ownerAddress, contractAddress, function)
        val result = Numeric.toHexString(extension.getConstantResult(0).toByteArray())

        val decodedResult = FunctionReturnDecoder.decode(result, function.outputParameters)
        wrapper.close()
        return Pair(decodedResult[0].value.toString(), decodedResult[1].value.toString())
    }
}