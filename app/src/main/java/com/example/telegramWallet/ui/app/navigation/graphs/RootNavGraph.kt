package com.example.telegramWallet.ui.app.navigation.graphs

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.coRAddressNavGraph
import com.example.telegramWallet.ui.app.navigation.HomeScreen
import com.example.telegramWallet.ui.new_screens.lockScreen.BlockedAppScreen
import com.example.telegramWallet.ui.new_screens.lockScreen.CreateLockScreen
import com.example.telegramWallet.ui.new_screens.lockScreen.LockScreen


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun RootNavigationGraph(navController: NavHostController, isBlockedApp: Boolean) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.HOME
    ) {
        composable(route = Graph.HOME) {
            HomeScreen()
        }
        composable(route = Graph.CREATE_LOCK_SCREEN) {
            CreateLockScreen(
                toNavigate = {
                    navController.navigate(route = Graph.CREATE_OR_RECOVERY_ADDRESS_FS)
                }
            )
            BackHandler {}
        }
        composable(route = Graph.LOCK_SCREEN) {
            LockScreen(
                toNavigate = {
                    navController.navigateUp()
                }
            )
            BackHandler {}
        }

        coRAddressNavGraph(navController)

        composable(route = Graph.BLOCKED_APP_SCREEN) {
            BlockedAppScreen(toNavigate = {
                navController.navigateUp()
            })
            BackHandler {}
        }
    }

    // Мы можем делать такую проверку только после создания дерева навигатора.
    val sharedPref = LocalContext.current.getSharedPreferences(
        ContextCompat.getString(LocalContext.current, R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    val pinCode = sharedPref.getString("pin_code", "startInit")
    val sessionActivity = sharedPref.getBoolean("session_activity", false)
    val isFirstStart = sharedPref.getBoolean("FIRST_STARTED", true)

    if (isBlockedApp) {
        navController.navigate(route = Graph.BLOCKED_APP_SCREEN)
    } else {
        if (pinCode.equals("startInit")) navController.navigate(route = Graph.CREATE_LOCK_SCREEN)
        else if (!sessionActivity) {
            navController.navigate(route = Graph.LOCK_SCREEN)
        }

        if (isFirstStart && !pinCode.equals("startInit")) {
            navController.navigate(route = Graph.CREATE_OR_RECOVERY_ADDRESS_FS)
        }
    }

}

object Graph {
    const val ROOT = "root_graph"
    const val HOME = "home_graph"
    const val PROFILE = "profile_graph"
    const val SETTINGS = "settings_graph"
    const val CREATE_LOCK_SCREEN = "create_lock_screen"
    const val LOCK_SCREEN = "lock_screen"
    const val BLOCKED_APP_SCREEN = "blocked_app_screen"
    const val CREATE_OR_RECOVERY_ADDRESS_FS = "create_or_recover_address_fs"
    const val CREATE_OR_RECOVERY_ADDRESS = "create_or_recover_address"
    const val FIRST_START = "first_start"
}

