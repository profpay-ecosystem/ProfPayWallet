package com.example.telegramWallet.ui.app.new_navigation

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.new_navigation.routes.AuthRoute
import com.example.telegramWallet.ui.app.new_navigation.routes.Graph
import com.example.telegramWallet.ui.screens.lockScreen.CreateLockScreen
import com.example.telegramWallet.ui.screens.lockScreen.LockScreen

fun NavGraphBuilder.authNavGraph(navController: NavController, startDestination: String) {
    navigation(route = Graph.AUTH, startDestination = startDestination) {
        composable(AuthRoute.PinLockScreen.route) {
            LockScreen(
                toNavigate = {
                    navController.navigate(Graph.MAIN) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                }
            )
        }
        composable(route = AuthRoute.CreatePin.route) {
            CreateLockScreen(
                toNavigate = {
                    navController.navigate(route = Graph.ONBOARDING)
                }
            )
            BackHandler {}
        }
    }
}