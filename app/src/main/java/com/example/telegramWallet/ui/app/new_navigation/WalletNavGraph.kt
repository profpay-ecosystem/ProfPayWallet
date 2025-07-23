package com.example.telegramWallet.ui.app.new_navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.new_navigation.routes.AuthRoute
import com.example.telegramWallet.ui.app.new_navigation.routes.Graph
import com.example.telegramWallet.ui.app.new_navigation.routes.WelcomingRoute
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.WelcomingScreen
import com.example.telegramWallet.ui.screens.lockScreen.CreateLockScreen
import com.example.telegramWallet.ui.screens.lockScreen.LockScreen

fun NavGraphBuilder.walletNavGraph(navController: NavController) {
    navigation(route = Graph.AUTH, startDestination = AuthRoute.CreatePin.route) {
        composable(AuthRoute.CreatePin.route) {
            CreateLockScreen(toNavigate = { navController.navigate(AuthRoute.Welcoming.route) })
        }

        composable(AuthRoute.Welcoming.route) {
            WelcomingScreen(
                goToCOR = {
                    navController.navigate(route = WelcomingRoute.CreateOrRecoverWallet.route) {
                        popUpTo(Graph.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AuthRoute.PinLockScreen.route) {
            LockScreen(toNavigate = { navController.navigate(Graph.HOME) })
        }
    }
}