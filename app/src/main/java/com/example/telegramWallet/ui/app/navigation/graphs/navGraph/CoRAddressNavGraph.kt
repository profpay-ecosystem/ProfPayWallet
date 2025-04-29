package com.example.telegramWallet.ui.app.navigation.graphs.navGraph

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.navigation.graphs.Graph
import com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet.CreateNewWalletScreen
import com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet.CreateOrRecoverWalletScreen
import com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet.CreatedWalletAddingScreen
import com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet.RecoverWalletScreen
import com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet.RecoveringWalletAddingScreen
import com.example.telegramWallet.ui.new_screens.createOrRecoveryWallet.SeedPhraseConfirmationScreen

fun NavGraphBuilder.coRAddressNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.FIRST_START,
        startDestination = Graph.CREATE_OR_RECOVERY_ADDRESS_FS
    ) {
        composable(route = Graph.CREATE_OR_RECOVERY_ADDRESS_FS) {
            CreateOrRecoverWalletScreen(
                goToCreateNewWallet = {
                    navController.navigate(route = CreateOrRecoverWalletFS.CreateNewWalletFS.route)
                },
                goToRecoverWallet = {
                    navController.navigate(route = CreateOrRecoverWalletFS.RecoverWalletFS.route)
                },
                goToBack = {}
            )
            BackHandler {}
        }
        composable(route = CreateOrRecoverWalletFS.CreateNewWalletFS.route) {
            CreateNewWalletScreen(
                goToSeedPhraseConfirmation = {
                    navController.navigate(route = CreateNewWalletFS.SeedPhraseConfirmationFS.route)
                },
                goToBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(route = CreateOrRecoverWalletFS.RecoverWalletFS.route) {
            RecoverWalletScreen(
                goToRecoveringWalletAdding = {
                    navController.navigate(route = RecoverWalletFS.RecoveringWalletAddingFS.route)
                },
                goToBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(route = CreateNewWalletFS.SeedPhraseConfirmationFS.route) {
            SeedPhraseConfirmationScreen(
                goToWalletAdded = {
                    navController.navigate(route = SeedPhraseConfirmationFS.WalletAddedFS.route)
                },
                goToBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(route = SeedPhraseConfirmationFS.WalletAddedFS.route) {
            CreatedWalletAddingScreen(
                goToHome = {
                    navController.navigate(route = Graph.ROOT) {
                        popUpTo(route = Graph.ROOT)
                    }
                },
                goToBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(route = RecoverWalletFS.RecoveringWalletAddingFS.route) {
            RecoveringWalletAddingScreen(
                goToHome = {
                    navController.navigate(route = Graph.ROOT) {
                        popUpTo(route = Graph.ROOT)
                    }
                },
                goToBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}

sealed class CreateOrRecoverWalletFS(val route: String) {
    data object CreateNewWalletFS : CreateOrRecoverWalletFS(route = "create_new_wallet_fs")
    data object RecoverWalletFS : CreateOrRecoverWalletFS(route = "recover_new_wallet_fs")
}

sealed class RecoverWalletFS(val route: String) {
    data object RecoveringWalletAddingFS : RecoverWalletFS(route = "recovering_wallet_adding_fs")
}

sealed class CreateNewWalletFS(val route: String) {
    data object SeedPhraseConfirmationFS : CreateNewWalletFS(route = "seed_phrase_confirmation_fs")
}

sealed class SeedPhraseConfirmationFS(val route: String) {
    data object WalletAddedFS : SeedPhraseConfirmationFS(route = "wallet_added_fs")
}