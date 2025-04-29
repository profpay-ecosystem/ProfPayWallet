package com.example.telegramWallet.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.bridge.view_model.settings.ThemeViewModel
import com.example.telegramWallet.ui.features.settings.row_button.RowSettingsButtonHidden
import com.example.telegramWallet.ui.features.settings.row_button.RowSettingsButtonHiddenWithoutDivider
import com.example.telegramWallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChoiceScreen(goToBack: () -> Unit, themeVM: ThemeViewModel = hiltViewModel()) {
    val shared = sharedPref()
    val themeVar: MutableIntState = remember {
        mutableIntStateOf(shared.getInt("valueTheme", 2))
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Тема",
                    style = TextStyle(color = Color.White, fontSize = 22.sp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            navigationIcon = {
                run {
                    IconButton(onClick = { goToBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
        )
    }) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RowSettingsButtonHidden(
                    textContent = "Светлая", modifier = Modifier,
                    funcRight = {
                        RadioButton(
                            selected = (themeVar.intValue == 0),
                            onClick = {
                                shared.edit().putInt("valueTheme", 0).apply()
                                themeVM.getThemeApp(shared)
                                themeVar.intValue = 0
                            })
                    })

                RowSettingsButtonHidden(textContent = "Тёмная", modifier = Modifier,
                    funcRight = {
                        RadioButton(
                            selected = (themeVar.intValue == 1),
                            onClick = {
                                shared.edit().putInt("valueTheme", 1).apply()
                                themeVM.getThemeApp(shared)
                                themeVar.intValue = 1
                            })
                    })

                RowSettingsButtonHiddenWithoutDivider(
                    textContent = "Системное значение", modifier = Modifier,
                    funcRight = {
                        RadioButton(
                            selected = (themeVar.intValue == 2),
                            onClick = {
                                shared.edit().putInt("valueTheme", 2).apply()
                                themeVM.getThemeApp(shared)
                                themeVar.intValue = 2
                            })
                    }
                )
            }
        }
    }
}