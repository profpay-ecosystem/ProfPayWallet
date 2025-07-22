package com.example.telegramWallet.ui.app.new_navigation.routes

sealed class HomeRoute(val route: String) {
    object Dashboard : HomeRoute("dashboard")
    object Notifications : HomeRoute("notifications")
}