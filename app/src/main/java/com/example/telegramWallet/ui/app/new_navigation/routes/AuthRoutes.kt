package com.example.telegramWallet.ui.app.new_navigation.routes

sealed class AuthRoute(val route: String) {
    object CreatePin : HomeRoute("create-pin-lock-screen")
    object PinLockScreen : HomeRoute("pin-lock-screen")
}