package com.example.telegramWallet.ui.app.new_navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.new_navigation.routes.AuthRoute
import com.example.telegramWallet.ui.app.new_navigation.routes.Graph
import com.example.telegramWallet.ui.screens.lockScreen.LockScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(route = Graph.AUTH, startDestination = AuthRoute.CreatePin.route) {
        composable(AuthRoute.LockScreen.route) {
            LockScreen(
                toNavigate = {
                    navController.navigate(Graph.MAIN) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                }
            )
        }
    }
}