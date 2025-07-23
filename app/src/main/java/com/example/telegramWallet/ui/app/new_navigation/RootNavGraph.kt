package com.example.telegramWallet.ui.app.new_navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.telegramWallet.ui.app.new_navigation.routes.Graph

@Composable
fun RootNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = startDestination // onboarding_graph или main_graph
    ) {
        onboardingNavGraph(navController)
        mainNavGraph(navController)
        authNavGraph(navController, startDestination)
    }
}