package com.example.telegramWallet.ui.app.new_navigation.routes

sealed class AuthRoute(val route: String) {
    object CreatePin : HomeRoute("create-pin")
    object LockScreen : HomeRoute("lock-screen")
    object Welcoming : HomeRoute("welcoming")
}