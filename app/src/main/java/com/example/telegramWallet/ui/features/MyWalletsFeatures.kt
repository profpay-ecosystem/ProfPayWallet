package com.example.telegramWallet.ui.features

//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun MyWallets(
//    showcaseScope: ShowcaseScope,
//    viewModel: WalletSotViewModel = hiltViewModel()
//) {
//    val openDialog = remember { mutableStateOf(false) }
//    val clipboardManager: ClipboardManager = LocalClipboardManager.current
//
//    Column(
//        modifier = Modifier,
//    ) {
//        Row(
//            modifier = Modifier
//                .padding(horizontal = 18.dp, vertical = 6.dp)
//                .fillMaxWidth(fraction = 1f),
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            verticalAlignment = Alignment.Bottom
//
//        ) {
//            Box(
//                modifier = Modifier.weight(0.8f),
//                contentAlignment = Alignment.BottomStart
//            ) {
//                Text(
//                    text = "Address", style = TextStyle(
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                    )
//                )
//            }
//            Box(
//                modifier = Modifier.weight(1.1f),
//                contentAlignment = Alignment.BottomEnd
//            ) {
//                Text(
//                    text = "USDT",
//                    style = TextStyle(fontWeight = FontWeight.Bold),
//                )
//            }
//            Box(
//                modifier = Modifier.weight(0.5f),
//                contentAlignment = Alignment.BottomEnd
//            ) {
//                Text(
//                    text = "TRX",
//                    style = TextStyle(fontWeight = FontWeight.Bold),
//                )
//            }
//        }
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(1),
//            modifier = Modifier
//                .alpha(0.8f)
//        ) {
////            itemsIndexed(items = ) { _, address ->
////                val expanded = remember { mutableStateOf(false) }
//
//                showcaseScope.Showcase(k = 1, message = ShowcaseMsgs(MaterialTheme.colorScheme).DeleteAddress) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .combinedClickable(onLongClick = { expanded.value = true }) {},
//                        horizontalArrangement = Arrangement.spacedBy(16.dp),
//                    ) {
//                        Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
////                            scope.Showcase(k = 2, message = ShowcaseMsg("")) {
//                            Box(modifier = Modifier.weight(0.8f)) {
//                                Text(
//                                    text = "${address.address.take(5)}..." +
//                                            "${address.address.takeLast(5)} ",
//                                )
//                            }
////                            }
//                            Box(
//                                modifier = Modifier.weight(1f),
//                                contentAlignment = Alignment.CenterEnd
//                            ) {
//                                Text(
//                                    text = "$${hiderBalances(address.usdtBalance!!)}",
////                                    text = "$1234567.891113156",
//                                )
//                            }
//                            Box(modifier = Modifier.weight(0.05f))
//                            Box(
//                                modifier = Modifier.weight(0.5f),
//                                contentAlignment = Alignment.CenterEnd
//                            ) {
//                                Text(
////                                    text = "12.345678",
//                                    text = hiderBalances(address.trxBalance!!),
//                                )
//                            }
//
//                            DropdownMenu(
//                                expanded = expanded.value,
//                                onDismissRequest = { expanded.value = false }
//                            ) {
//                                DropdownMenuItem(
//                                    onClick = {
//                                        clipboardManager.setText(AnnotatedString(address.address))
//                                        expanded.value = false
//                                    },
//                                    leadingIcon = {
//                                        Icon(
//                                            Icons.Filled.Edit,
//                                            contentDescription = ""
//                                        )
//                                    },
//                                    text = { Text(text = "Копировать") }
//
//                                )
//                                Divider()
//                                DropdownMenuItem(
//                                    onClick = {
//                                        openDialog.value = true
//                                        expanded.value = false
//                                    },
//                                    leadingIcon = {
//                                        Icon(
//                                            Icons.Filled.Delete,
//                                            contentDescription = "Удалить адрес",
//                                            tint = Color.Red
//                                        )
//                                    },
//                                    text = {
//                                        Text(
//                                            text = "Удалить", style = TextStyle(
//                                                fontWeight = FontWeight.Bold
//                                            )
//                                        )
//                                    }
//                                )
//                            }
//                            if (openDialog.value) {
//                                AlertDialogWidget(
//                                    onDismissRequest = { openDialog.value = false },
//                                    onConfirmation = {
//                                        viewModel.deleteAddress(address.address)
//                                        openDialog.value = false
//                                    },
//                                    dialogTitle = "Удаление кошелька",
//                                    dialogText = "Кошелёк с адресом:\n" +
//                                            "${address.address}\n\n" +
//                                            "Нажмите \"Подтвердить\", для удаления кошелька",
//                                    icon = Icons.Filled.Delete,
//                                    colorIcon = Color.Red,
//                                    textDismissButton = "Назад",
//                                    textConfirmButton = "Подтвердить"
//                                )
//
//                            }
//                        }
//                    }
//                    Divider(color = Color.Black, thickness = 1.dp)
//                }
//            }
//        }
//    }
//}

