package com.example.telegramWallet.ui.features

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telegramWallet.backend.http.models.AuthUserRequest
import com.example.telegramWallet.backend.http.models.AuthUserResponse
import com.example.telegramWallet.backend.http.models.RegisterUserRequest
import com.example.telegramWallet.backend.http.models.RegisterUserResponse
import com.example.telegramWallet.backend.http.models.UpdateUserModel
import com.example.telegramWallet.backend.http.models.UpdateUserRequest
import com.example.telegramWallet.backend.http.models.UpdateUserResponse
import com.example.telegramWallet.backend.http.models.UserErrorResponse
import com.example.telegramWallet.backend.http.user.AuthApi
import com.example.telegramWallet.backend.http.user.AuthRequestCallback
import com.example.telegramWallet.backend.http.user.RegisterApi
import com.example.telegramWallet.backend.http.user.RegisterRequestCallback
import com.example.telegramWallet.backend.http.user.UpdateApi
import com.example.telegramWallet.backend.http.user.UpdateRequestCallback
import com.example.telegramWallet.bridge.view_model.RequestNewTelegramAcViewModel
import com.example.telegramWallet.ui.widgets.dialog.AlertDialogConfButtonWidget
import com.example.telegramWallet.ui.widgets.dialog.AlertDialogWidget
import dev.inmo.micro_utils.coroutines.launchSynchronously
import java.util.UUID

@Composable
fun RequestNewTelegramAc(
    viewModel: RequestNewTelegramAcViewModel = hiltViewModel(),
) {
    val openDialog = remember { mutableStateOf(false) }
    val openDialogError = remember { mutableStateOf(false) }
    var textError by remember { mutableStateOf("") }

    val inactiveProfile by remember { launchSynchronously { viewModel.inactiveProfile() } }
        .collectAsStateWithLifecycle(initialValue = null)
    val activeProfile by remember { launchSynchronously { viewModel.activeProfile() } }
        .collectAsStateWithLifecycle(initialValue = null)

    if (inactiveProfile != null && activeProfile == null) {
        AlertDialogWidget(
            onDismissRequest = {
                viewModel.deleteProfileById(inactiveProfile!!.telegram_id)
                openDialog.value = !openDialog.value
            },
            onConfirmation = {
                RegisterApi.registerService.makeRequest(
                    object : RegisterRequestCallback {
                        override fun onSuccess(data: RegisterUserResponse) {
                            viewModel.updateActiveBot(
                                isActive = true, tgId = inactiveProfile!!.telegram_id,
                                accessToken = data.access_token, expiresAt = data.expires_at
                            )
                        }

                        override fun onFailure(error: UserErrorResponse) {
                            openDialogError.value = true
                            textError = error.message
                        }
                    }, RegisterUserRequest(
                        inactiveProfile!!.telegram_id,
                        inactiveProfile!!.username!!,
                        UUID.randomUUID().toString()
                    )
                )
                openDialog.value = !openDialog.value
            },
            dialogTitle = "Новый Telegram-аккаунт",
            dialogText =
            "Поступил запрос на добавление аккаунта.\n\n" +
                    "Данные аккаунта: \n" +
                    "Username: @${inactiveProfile!!.username} \n" +
                    "Telegram ID: ${inactiveProfile!!.telegram_id} \n\n" +
                    "Нажмите \"Подтвердить\", чтобы добавить аккаунт.",
            icon = Icons.Default.AccountCircle,
            textDismissButton = "Отклонить",
            textConfirmButton = "Подтвердить"
        )
    } else if (inactiveProfile != null) {
        if (!inactiveProfile!!.active_tg_id && activeProfile!!.active_tg_id) {
            AlertDialogWidget(
                onDismissRequest = {
                    viewModel.deleteProfileById(inactiveProfile!!.telegram_id)
                    openDialog.value = !openDialog.value
                },
                onConfirmation = {
                    val accessToken: String = activeProfile!!.access_token!!
                    val expiresAt: Long = activeProfile!!.expires_at!!

                    UpdateApi.updateService.makeRequest(
                        object : UpdateRequestCallback {
                            override fun onSuccess(data: UpdateUserResponse) {
                                viewModel.deleteProfileById(activeProfile!!.telegram_id)
                                viewModel.updateActiveBot(
                                    isActive = true, tgId = inactiveProfile!!.telegram_id,
                                    accessToken = accessToken, expiresAt = expiresAt
                                )
                            }
                            override fun onFailure(error: UserErrorResponse, code: Int?) {
                                if (code != null && code == 401) {
                                    AuthApi.authService.makeRequest(
                                        object : AuthRequestCallback {
                                            override fun onFailure(error: UserErrorResponse) {
                                                // TODO: Обрабатывать
                                                Log.e("EEEEE2", error.message)
                                            }

                                            override fun onSuccess(data: AuthUserResponse) {
                                                viewModel.updateActiveBot(
                                                    true, activeProfile!!.telegram_id, data.access_token, data.expires_at
                                                )
                                            }
                                        }, AuthUserRequest(activeProfile!!.telegram_id, accessToken, "android_unique")
                                    )
                                }
                                openDialogError.value = true
                                textError = error.message
                            }
                        }, UpdateUserRequest(
                            old_data = UpdateUserModel(
                                activeProfile!!.telegram_id,
                                activeProfile!!.username!!
                            ),
                            new_data = UpdateUserModel(
                                inactiveProfile!!.telegram_id,
                                inactiveProfile!!.username!!
                            )
                        ), accessToken
                    )
                    openDialog.value = !openDialog.value
                },
                dialogTitle = "Смена Telegram-аккаунта",
                dialogText = "Поступил запрос о смене аккаунта.\n\n" +
                        "Данные текущего аккаунта:\n" +
                        "Username: @${activeProfile!!.username} \n" +
                        "Telegram ID: ${activeProfile!!.telegram_id} \n\n" +
                        "Данные нового аккаунта: \n" +
                        "Username: @${inactiveProfile!!.username} \n" +
                        "Telegram ID: ${inactiveProfile!!.telegram_id} \n\n" +
                        "Нажмите \"Подтвердить\", для смены аккаунта.",
                icon = Icons.Default.AccountCircle,
                textDismissButton = "Отклонить",
                textConfirmButton = "Подтвердить"
            )
        }
    }
    if (openDialogError.value) {
        ErrorChangeProfile(
            openDialogError = { openDialogError.value = false },
            error = textError
        )
    }
}

@Composable
fun ErrorChangeProfile(openDialogError: () -> Unit, error: String) {
    AlertDialogConfButtonWidget(
        onDismissRequest = { openDialogError() },
        onConfirmation = { openDialogError() },
        dialogTitle = "Произошла ошибка",
        dialogText = "Произошла ошибка: \n$error \n\nОбратитесь к команде разработки.",
        icon = Icons.Default.Warning,
        textButton = "Ок"
    )
}
