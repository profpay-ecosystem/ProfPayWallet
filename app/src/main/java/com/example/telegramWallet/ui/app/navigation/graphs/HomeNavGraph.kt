package com.example.telegramWallet.ui.app.navigation.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.telegramWallet.ui.app.navigation.BottomBarScreen
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.WalletInfo
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.coRAddressFromWalletSystemNG
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.profileNavGraph
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.settingsNavGraph
import com.example.telegramWallet.ui.new_screens.SmartListScreen

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HomeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.HOME,
        startDestination = Graph.PROFILE
    ) {

        composable(route = BottomBarScreen.SmartContractList.route) {
           SmartListScreen(
               goToSystemTRX = { navController.navigate(route = WalletInfo.WalletSystemTRX.route) }
           )
//            SmartInDevelopment()
        }

        settingsNavGraph(navController)
        profileNavGraph(navController)
        coRAddressFromWalletSystemNG(navController)
    }
}

sealed class FAQScreen(val route: String) {
    data object AboutUs : FAQScreen(route = "about_us")
    data object Security : FAQScreen(route = "security")
}
