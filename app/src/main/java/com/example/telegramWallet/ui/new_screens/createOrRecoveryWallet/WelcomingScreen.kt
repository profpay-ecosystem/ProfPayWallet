package com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.theme.BackgroundContainerButtonLight
import com.example.telegramWallet.ui.app.theme.BackgroundDark
import com.example.telegramWallet.ui.app.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomingScreen(goToCOR: () -> Unit) {

    val textBlocks: List<String> = listOf(
        "ProfPay — некастодиальное приложение. Все средства хранятся исключительно на стороне пользователя." +
                " Доступ к вашим ключам и транзакциям отсутствует со стороны разработчиков.",
        "\n" +
                "Приложение находится в стадии закрытого бета-тестирования. Настоятельно рекомендуем воздержаться от" +
                " проведения крупных транзакций до завершения тестового периода.",
        "\n" +
                "Перед установкой убедитесь, что вы скачали приложение из доверенного источника и проверили " +
                "контрольную хеш-сумму файла для подтверждения его подлинности.",
        "\n" +
                "Обращаем внимание: использование приложения на территории США строго не рекомендуется. Все действия " +
                "пользователей из США осуществляются исключительно под их личную ответственность.",
        "\n" +
                "Благодарим за участие в тестировании и оказанное доверие.",
    )
    Scaffold(topBar = {
        TopAppBar(
            title = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            navigationIcon = {
                run {

                }
            }
        )
    }
    ) { padding ->
        padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.create_recovery_bg),
                    contentScale = ContentScale.FillBounds
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start

            ) {
                Text(
                    text = "Добро пожаловать \n в ProfPay",
                    modifier = Modifier.padding(start = 16.dp)
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = BackgroundLight
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedFadingTextList(textBlocks) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                    contentColor = BackgroundDark
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth(0.5f)
                                    .height(IntrinsicSize.Min)
                                    .shadow(7.dp, RoundedCornerShape(10.dp))
                                    .clickable {
                                    goToCOR()
                                    },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Принять")
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedFadingTextList(textBlocks: List<String>, itemContent: @Composable () -> Unit = {}) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(textBlocks) { index, block ->
            val itemInfo = listState.layoutInfo.visibleItemsInfo
                .find { it.index == index }

            // Затухание начинается, когда элемент начинает уезжать вверх
            val fadeDistancePx = with(density) { 10.dp.toPx() }

            val alpha = remember {
                mutableFloatStateOf(1f)
            }

            LaunchedEffect(itemInfo?.offset) {
                if (itemInfo != null) {
                    val offset = itemInfo.offset.toFloat()
                    alpha.floatValue = when {
                        offset >= 0 -> 1f
                        offset <= -fadeDistancePx -> 0f
                        else -> 1f + (offset / fadeDistancePx)
                    }.coerceIn(0f, 1f)
                } else {
                    alpha.floatValue = 1f
                }
            }

            val animatedAlpha by animateFloatAsState(
                targetValue = alpha.floatValue,
                animationSpec = tween(durationMillis = 400),
                label = "fadeAlpha"
            )

            Text(
                text = block,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { this.alpha = animatedAlpha }
                    .padding(vertical = 8.dp),
//                style = MaterialTheme.typography.titleMedium,
                style = MaterialTheme.typography.bodyLarge,
                color = BackgroundDark
            )
        }
        item {
            itemContent()
        }
    }
}