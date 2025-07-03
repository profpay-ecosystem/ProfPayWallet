package com.example.telegramWallet.ui.new_screens.lockScreen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.GetContextViewModel
import com.example.telegramWallet.data.utils.hashPasswordWithBCrypt
import com.example.telegramWallet.ui.new_feature.lockScreen.InputDots
import com.example.telegramWallet.ui.new_feature.lockScreen.NumberBoard
import com.example.telegramWallet.ui.shared.sharedPref


@Composable
fun LockScreen(
    toNavigate: () -> Unit,
    viewModel: GetContextViewModel = hiltViewModel(),
    goToBack: () -> Unit = {},
    goingBack: Boolean = false
) {

    val inputPinCode = remember { mutableStateListOf<Int>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val sharedPref = sharedPref()
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFFff2a00),
                        contentColor = Color.White,
                    )
                }
            )
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.fillMaxWidth(0.1f))

                Icon(
                    painter = painterResource(id = R.drawable.icon_smart), contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(0.5f)
                        .clip(CircleShape)
                        .weight(1f)
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                Text(text = "Hii, User!")
                Text(text = "Verify 4-digit security PIN")

                Spacer(modifier = Modifier.fillMaxHeight(0.02f))

                InputDots(inputPinCode)

                NumberBoard(
                    inputPinCode = inputPinCode,
                    goingBack = goingBack,
                    onNumberClick = { enterNumber ->
                        when (enterNumber) {
                            "" -> {}
                            "<" -> {
                                goToBack()
                            }
                            "-1" -> {}
                            "X" -> {
                                if (inputPinCode.isNotEmpty()) inputPinCode.removeAt(inputPinCode.lastIndex)
                            }

                            else -> {
                                if (inputPinCode.size < 4)
                                    inputPinCode.add(enterNumber.toInt())
                            }
                        }
                    },
                    onClickBiom = {
                        sharedPref.edit() { putBoolean("session_activity", true) }
                        toNavigate()
                    }
                )

                Spacer(modifier = Modifier.weight(0.1f))


                if (inputPinCode.size == 4) {
                    val sharedPref = viewModel.getAppContext().getSharedPreferences(
                        ContextCompat.getString(
                            viewModel.getAppContext(),
                            R.string.preference_file_key
                        ),
                        Context.MODE_PRIVATE
                    )

                    val pinCode = sharedPref.getString("pin_code", "startInit")
                    val inputPinCodeInt = inputPinCode.joinToString(separator = "").toInt()
                    val hashPin = hashPasswordWithBCrypt(sharedPref, inputPinCodeInt)
                    if (pinCode.equals(hashPin)) {
                        sharedPref.edit() { putBoolean("session_activity", true) }

                        LaunchedEffect(Unit) {
                            toNavigate()
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            snackbarHostState
                                .showSnackbar(
                                    message = "Введен неверный пин-код",
                                    duration = SnackbarDuration.Short
                                )
                            inputPinCode.clear()
                        }
                    }
                }
            }
        }
    }
}