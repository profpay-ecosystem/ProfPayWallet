package com.example.telegramWallet.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WalletCheckBoxFeature(funcButton: () -> Unit, textButton: String) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clip(shape = RoundedCornerShape(6.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.weight(0.4f)) {
                Checkbox(checked = check1, onCheckedChange = { check1 = it })

            }
            Box(modifier = Modifier.weight(2f)) {
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(6.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f))
                        .padding(vertical = 16.dp, horizontal = 10.dp)
                ) {
                    Text(
                        text = "Мои средства хранятся и находятся под контролем на данном устройстве.\n",
                        style = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Left
                        )
                    )
                    Text(
                        text = "Команда разработки не имеет доступа, к моим средствам, " +
                                "не контроллирует их и не распоряжается ими.",
                        style = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Left
                        ), modifier = Modifier
                            .alpha(0.7f)
                    )
                }
            }

        }
        Row(
            modifier = Modifier
                .padding()
                .clip(shape = RoundedCornerShape(6.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.weight(0.4f)) {
                Checkbox(checked = check2, onCheckedChange = { check2 = it })

            }
            Box(modifier = Modifier.weight(2f)) {
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(6.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f))
                        .padding(vertical = 16.dp, horizontal = 10.dp)
                ) {
                    Text(
                        text = "Команда разработки никогда не сможет вернуть мои средства за меня.\n",
                        style = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Left
                        )
                    )
                    Text(
                        text = "Я несу ответственность за запоминание сид-фразы из 12 слов.\n" +
                                "Фраза из 12 слов — единственная возможность вернуть мои средства в " +
                                "случае удаления приложения или утери устройства. Если я забуду фразу " +
                                "восстановления, восстановить её будет невозможно.",
                        style = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Left
                        ), modifier = Modifier
                            .alpha(0.7f)
                    )
                }
            }

        }

        if (check1 && check2) {
            Button(
                onClick = {
                    funcButton()
                },
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(fraction = 1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(text = textButton)
            }
        }

    }

}

//