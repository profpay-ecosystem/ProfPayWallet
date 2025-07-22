package com.example.telegramWallet.ui.app.new_navigation.routes

sealed class WelcomingRoute(val route: String) {
    object CreateOrRecoverWallet : HomeRoute("create-or-recovery-wallet")
}