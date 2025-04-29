package com.example.telegramWallet.ui.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.telegramWallet.bridge.view_model.BlockingAppState
import com.example.telegramWallet.bridge.view_model.BlockingAppViewModel
import com.example.telegramWallet.ui.app.navigation.graphs.RootNavigationGraph

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MyApp(
    navController: NavHostController,
    viewModel: BlockingAppViewModel = hiltViewModel()
) {
    val blockAppState by viewModel.state.collectAsStateWithLifecycle()
    when (blockAppState) {
        is BlockingAppState.Loading -> viewModel.getBlockedAppState()
        is BlockingAppState.Success -> {
            viewModel.getBlockedAppState()
            val resultState = (blockAppState as BlockingAppState.Success).value
            RootNavigationGraph(
                navController = navController,
                isBlockedApp = resultState.isBlockedApp,
            )
        }
    }

}