package com.example.telegramWallet.ui.new_feature.wallet.tx_details

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.bridge.view_model.wallet.TXDetailsViewModel
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.TransactionStatusResult
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundContainerButtonLight
import com.example.telegramWallet.ui.app.theme.GreenColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetTransOnGeneralReceipt(
    viewModel: TXDetailsViewModel,
    snackbar: StackedSnakbarHostState,
    transactionEntity: TransactionEntity
): Pair<Boolean, (Boolean) -> Unit> {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }
    var isButtonEnabled by remember { mutableStateOf(false) }
    val commissionState by viewModel.stateCommission.collectAsStateWithLifecycle()
    val transactionStatus by viewModel.transactionStatus.collectAsStateWithLifecycle()

    val (commissionOnTransaction, setCommissionOnTransaction) = remember { mutableStateOf(BigDecimal.ZERO) }
    val (generalAddressActivatedCommission, setGeneralAddressActivatedCommission) = remember { mutableStateOf(BigInteger.ZERO) }
    val (isGeneralAddressNotActivatedVisible, setIsGeneralAddressNotActivatedVisible) = remember { mutableStateOf(false) }

    if (isOpenSheet) {
        LaunchedEffect(transactionStatus) {
            when (transactionStatus) {
                is TransactionStatusResult.Loading -> {}
                is TransactionStatusResult.Success -> {
                    val isProcessed = (transactionStatus as TransactionStatusResult.Success).response.isProcessed
                    val isError = (transactionStatus as TransactionStatusResult.Success).response.isError
                    if (isProcessed) {
                        withContext(Dispatchers.IO) {
                            viewModel.transactionsRepo.transactionSetProcessedUpdateTrueByTxId(transactionEntity.txId)
                        }

                        setIsOpenSheet(false)
                    }
                }
                is TransactionStatusResult.Error -> {}
                is TransactionStatusResult.Empty -> {}
            }
        }

        LaunchedEffect(transactionEntity.txId) {
            viewModel.getTransactionStatus(transactionEntity.txId)
        }

        LaunchedEffect(Unit) {
            val address = withContext(Dispatchers.IO) {
                viewModel.addressRepo.getAddressEntityByAddress(transactionEntity.receiverAddress)
            }
            val generalAddress = withContext(Dispatchers.IO) {
                viewModel.addressRepo.getGeneralAddressByWalletId(transactionEntity.walletId)
            }

            if (address == null) return@LaunchedEffect

            val requiredEnergy = withContext(Dispatchers.IO) {
                viewModel.tron.transactions.estimateEnergy(
                    fromAddress = address.address,
                    toAddress = generalAddress,
                    privateKey = address.privateKey,
                    amount = transactionEntity.amount
                )
            }
            val requiredBandwidth = withContext(Dispatchers.IO) {
                viewModel.tron.transactions.estimateBandwidth(
                    fromAddress = address.address,
                    toAddress = generalAddress,
                    privateKey = address.privateKey,
                    amount = transactionEntity.amount
                )
            }

            withContext(Dispatchers.IO) {
                if (!viewModel.tron.addressUtilities.isAddressActivated(generalAddress)) {
                    val commission = viewModel.tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
                    setIsGeneralAddressNotActivatedVisible(true)
                    setGeneralAddressActivatedCommission(commission)
                }

                viewModel.estimateCommission(
                    address = transactionEntity.receiverAddress,
                    bandwidth = requiredBandwidth.bandwidth,
                    energy = requiredEnergy.energy
                )
            }
        }

        LaunchedEffect(commissionState) {
            when (commissionState) {
                is EstimateCommissionResult.Loading -> {}
                is EstimateCommissionResult.Success -> {
                    val commission = (commissionState as EstimateCommissionResult.Success).response.commission
                    isButtonEnabled = true
                    setCommissionOnTransaction(commission.toBigDecimal())
                }
                is EstimateCommissionResult.Error -> {
                    val commission = (commissionState as EstimateCommissionResult.Error)
                    println(commission.throwable.message)
                }
                is EstimateCommissionResult.Empty -> {}
            }
        }

        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenSheet(false)
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Перевод на Главный",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .padding( vertical = 8.dp, horizontal = 16.dp,),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Комиссия:", fontWeight = FontWeight.SemiBold)
                        Row {
                            Text(text = "$commissionOnTransaction ")
                            Text(text = "TRX", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (isGeneralAddressNotActivatedVisible) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(18.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Активация адреса:", fontWeight = FontWeight.SemiBold, color = Color.Red)
                            Row {
                                Text(text = "${generalAddressActivatedCommission.toTokenAmount()} ")
                                Text(text = "TRX", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .padding( horizontal = 16.dp,),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 18.dp, horizontal = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = "Мы списываем комиссию в TRX, которая рассчитывается исходя из количества полученных AML отчетов и числа оплаченных услуг, " +
                                    "связанных с проверкой по AML. " +
                                    "Размер комиссии напрямую зависит от объема предоставленных отчетов и оплаченных проверок на соответствие требованиям по борьбе с отмыванием денег (AML).",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    enabled = isButtonEnabled,
                    onClick = {
                        isButtonEnabled = false // Отключаем кнопку
                        viewModel.viewModelScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                viewModel.acceptTransaction(
                                    transaction = transactionEntity,
                                    commission = commissionOnTransaction.toSunAmount()
                                )
                            }

                            when (result) {
                                is TransferResult.Success -> snackbar.showSuccessSnackbar(
                                    "Успешное действие",
                                    "Успешный перевод средств.",
                                    "Закрыть",
                                )
                                is TransferResult.Failure -> snackbar.showErrorSnackbar(
                                    "Перевод валюты невозможен",
                                    result.error.message,
                                    "Закрыть",
                                )
                            }

                            coroutineScope.launch {
                                sheetState.hide()
                                delay(400)
                                setIsOpenSheet(false)
                            }
                            isButtonEnabled = true
                        }

                    },
                    modifier = Modifier
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(50.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = GreenColor,
                        contentColor = BackgroundContainerButtonLight
                    ), shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Подтвердить",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}