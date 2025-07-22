package com.example.telegramWallet.ui.app.new_navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.new_navigation.routes.Graph

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "wallet",
        route = Graph.MAIN
    ) {
//        composable("wallet") { WalletScreen(...) }
//        composable("smart") { SmartScreen(...) }
//        composable("settings") { SettingsScreen(...) }
    }
}