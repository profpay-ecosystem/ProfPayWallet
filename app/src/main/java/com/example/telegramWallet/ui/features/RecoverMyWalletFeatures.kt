package com.example.telegramWallet.ui.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletViewModel
import com.example.telegramWallet.ui.app.theme.BackgroundDark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RecoverMyWalletTextField(
    viewModel: RecoverWalletViewModel = hiltViewModel(),
    goToRecoveringWalletAdding: () -> Unit
) {
    var seedPhrase by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val openDialog = remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(vertical = 10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier,
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            TextField(
                value = seedPhrase,
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                label = {
                    Text(text = "Введите Сид-фразу")
                },
                shape = MaterialTheme.shapes.small.copy(),
                onValueChange = { seedPhrase = it },
                trailingIcon = {},
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = BackgroundDark,
                    selectionColors = TextSelectionColors(
                        handleColor = BackgroundDark,
                        backgroundColor = Color.Transparent
                    )
                )
            )
        }
//        when (state) {
//            is RecoverWalletState.Loading -> {}
//            is RecoverWalletState.Success -> {
//                address =
//                    (state as RecoverWalletState.Success).addressRecoverResult.addresses[0].address
//                if (openDialog.value) {
//                    if (address != "Uncorrect value") {
//                        AlertDialogWidget(
//                            onDismissRequest = { openDialog.value = !openDialog.value },
//                            onConfirmation = {
//                                openDialog.value = !openDialog.value
//                                goToRecoveringWalletAdding()
//                            },
//                            dialogTitle = "Восстановить кошелёк",
//                            dialogText = "Публичный ключ: ${address}\n\n" +
//                                    "Если ключ не совпал с вашим, перепроверьте правильность написание сид-фразы.\n" +
//                                    "Нажмите «Подтвердить», чтобы продолжить.\n",
//                            icon = Icons.Default.Create,
//                            textConfirmButton = "Подтвердить",
//                            textDismissButton = "Отменить"
//                        )
//                    } else {
//                        AlertDialogConfButtonWidget(
//                            onDismissRequest = { openDialog.value = !openDialog.value },
//                            onConfirmation = { openDialog.value = !openDialog.value },
//                            dialogTitle = "Восстановить кошелёк",
//                            dialogText = "Введена некорректная сид-фраза.\n\n" +
//                                    "Пожалуйста перепроверьте правильность написания сид-фразы, " +
//                                    "и попробуйте снова.",
//                            icon = Icons.Default.Create,
//                            textButton = "Вернуться"
//                        )
//                    }
//                }
//            }
//        }
        Button(
            onClick = {
                viewModel.recoverWallet(seedPhrase)
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500L)
                    openDialog.value = true
                }
            },
            modifier = Modifier
                .padding(vertical = 11.dp)
                .fillMaxWidth(fraction = 1f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(text = "Продолжить")
        }
    }
}