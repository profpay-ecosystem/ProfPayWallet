package com.example.telegramWallet.ui.app.new_navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.new_navigation.routes.Graph

fun NavGraphBuilder.onboardingNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "welcome",
        route = Graph.ONBOARDING
    ) {
//        composable("welcome") {
//            WelcomeScreen(onNext = {
//                navController.navigate("create_pin")
//            })
//        }
//
//        composable("create_pin") {
//            CreatePinScreen(onNext = {
//                navController.navigate("create_address")
//            })
//        }
//
//        composable("create_address") {
//            CreateAddressScreen(onFinished = {
//                navController.navigate(Graph.MAIN) {
//                    popUpTo(Graph.ROOT) { inclusive = true }
//                }
//            })
//        }
    }
}