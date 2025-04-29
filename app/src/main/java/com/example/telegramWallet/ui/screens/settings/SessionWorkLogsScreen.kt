package com.example.telegramWallet.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.bridge.view_model.SessionWorkLogsViewModel

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionWorkLogsScreen(
    goToBack: () -> Unit,
    viewModel: SessionWorkLogsViewModel = hiltViewModel()
) {
    var openDropMenu by remember { mutableStateOf(false) }
    var copyAction by remember { mutableStateOf(false) }

    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val logs = viewModel.logCatOutput(internal = sliderPosition.toInt())
        .observeAsState("Nothing none")

    var listLogsError: List<String> = emptyList()
    var listLogsWarn: List<String> = emptyList()
    var listLogsDebug: List<String> = emptyList()
    var listLogsIntern: List<String> = emptyList()

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Сессия работы",
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
            },
            actions = {
                IconButton(onClick = { openDropMenu = !openDropMenu }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "icon", tint = Color.White)
                }
                DropdownMenu(
                    expanded = openDropMenu,
                    onDismissRequest = { openDropMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            openDropMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = ""
                            )
                        },
                        text = { Text(text = "Копировать") }

                    )
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            openDropMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Удалить адрес",
                                tint = Color.Red
                            )
                        },
                        text = {
                            Text(text = "Удалить", style = TextStyle(fontWeight = FontWeight.Bold))
                        }
                    )
                }
            }
        )
    },
        bottomBar = {
            BottomAppBar {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { sliderPosition = 0f }, Modifier.weight(1f)) {
                            Text(text = "Error", fontSize = 18.sp)
                        }
                        TextButton(onClick = { sliderPosition = 1f }, Modifier.weight(1f)) {
                            Text(text = "Warn", fontSize = 18.sp)
                        }
                        TextButton(onClick = { sliderPosition = 2f }, Modifier.weight(1f)) {
                            Text(text = "Debug", fontSize = 18.sp)
                        }
                        TextButton(onClick = { sliderPosition = 3f }, Modifier.weight(1f)) {
                            Text(text = "Intern", fontSize = 18.sp)
                        }
                    }
                    Slider(
                        modifier = Modifier.padding(horizontal = 26.dp),
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..3f,
                        steps = 2
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                when (sliderPosition) {
                    0f -> {
                        listLogsError += logs.value
                        LazyColumnForLogs(listLogsError)
                        if (openDropMenu){

                        }
                    }

                    1f -> {
                        listLogsWarn += logs.value
                        LazyColumnForLogs(listLogsWarn)
                    }

                    2f -> {
                        listLogsDebug += logs.value
                        LazyColumnForLogs(listLogsDebug)
                    }

                    3f -> {
                        listLogsIntern += logs.value
                        LazyColumnForLogs(listLogsIntern)
                    }
                }

            }
        }
    }
}


@Composable
fun LazyColumnForLogs(listLogs: List<String>) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        items(listLogs) {
            Text(text = it + "\n")
            LaunchedEffect(Unit) {
                listState.animateScrollToItem(listLogs.lastIndex)
            }
        }
    }
}