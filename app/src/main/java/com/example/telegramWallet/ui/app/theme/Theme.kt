package com.example.telegramWallet.ui.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val DarkColorPalette = darkColorScheme(
    primary = BackgroundDark,
    secondary = BackgroundLight,
    onPrimary = BackgroundLight,
    surface = BackgroundDark,
    onSurface = BackgroundLight,
    surfaceVariant = BackgroundDark,
    surfaceContainer = BackgroundContainerButtonDark,
    onSecondaryContainer = PubAddressDark,
    surfaceBright = BackgroundContainerButtonDark,
)

private val LightColorPalette = lightColorScheme(
    primary = BackgroundLight,
    secondary = BackgroundDark,
    onPrimary = BackgroundDark,
    surface = BackgroundLight,
    onSurface = BackgroundDark,
    surfaceVariant = BackgroundLight,
    surfaceContainer = BackgroundContainerButtonLight,
    onSecondaryContainer = PubAddressLight,
    surfaceBright = BackgroundIcon,
)

@Composable
fun WalletNavigationBottomBarTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    fun colors(isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = false
            )
            systemUiController.setNavigationBarColor(
                color = BackgroundDark,
                darkIcons = false
            )
            DarkColorPalette
        } else {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = true
            )
            systemUiController.setNavigationBarColor(
                color = BackgroundLight,
                darkIcons = true
            )
            LightColorPalette
        }
    }

    MaterialTheme(
        colorScheme = colors(isDarkTheme),
        typography = rememberTypography("Manrope"),
        shapes = Shapes,
        content = content
    )
}
