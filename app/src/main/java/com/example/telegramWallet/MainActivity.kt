package com.example.telegramWallet

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.telegramWallet.bridge.view_model.settings.ThemeState
import com.example.telegramWallet.bridge.view_model.settings.ThemeViewModel
import com.example.telegramWallet.ui.app.MyApp
import com.example.telegramWallet.ui.app.navigation.graphs.Graph
import com.example.telegramWallet.ui.app.theme.WalletNavigationBottomBarTheme
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.launch
import me.pushy.sdk.Pushy
import javax.inject.Inject
import androidx.core.content.edit

@AndroidEntryPoint
class MainActivity : FragmentActivity(), Application.ActivityLifecycleCallbacks {
    private var navController: NavHostController? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>
    @Inject lateinit var appInitializer: AppInitializer
    lateinit var viewModel: ThemeViewModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pushy.listen(this)

        registerActivityLifecycleCallbacks(this)
        enableEdgeToEdge()

        SentryAndroid.init(this) { options ->
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
        }

        val sharedPrefs = this.getSharedPreferences(
            ContextCompat.getString(this, R.string.preference_file_key),
            MODE_PRIVATE
        )

        lifecycleScope.launch { appInitializer.initialize(sharedPrefs, this@MainActivity) }

        // Инициализация ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                setContent {
                    MyAppContent(sharedPrefs)
                }
            }
        }

        setContent {
            MyAppContent(sharedPrefs)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun MyAppContent(sharedPrefs: SharedPreferences) {
        viewModel = hiltViewModel()
        navController = rememberNavController()
        val isDarkTheme: Boolean

        val state by viewModel.state.collectAsStateWithLifecycle()
        val isSystemInDarkTheme = isSystemInDarkTheme()
        when (state) {
            is ThemeState.Loading -> viewModel.getThemeApp(sharedPrefs)
            is ThemeState.Success -> {
                viewModel.getThemeApp(sharedPrefs)
                isDarkTheme = viewModel.isDarkTheme(
                    (state as ThemeState.Success).themeStateResult,
                    isSystemInDarkTheme
                )
                WalletNavigationBottomBarTheme(isDarkTheme = isDarkTheme) {
                    MyApp(navController!!)
                }
            }
        }
    }

    // Проверка пин-кода при открытии вкладки приложения, дефолт-значение запускает страницу создания пин-кода
    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResumed(activity: Activity) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled() || navController == null
            || navController!!.findDestination(Graph.HOME) == null
        ) return
        val sharedPref = activity.getSharedPreferences(
            ContextCompat.getString(activity, R.string.preference_file_key),
            MODE_PRIVATE
        )
        val pinCode = sharedPref.getString("pin_code", "startInit")
        val sessionActivity = sharedPref.getBoolean("session_activity", false)
        val isBlockApp = sharedPref.getBoolean("is_blocked_app", false)

        if (!isBlockApp) {
            if (pinCode.equals("startInit")) {
                if (navController?.currentBackStackEntry?.destination?.route != Graph.CREATE_LOCK_SCREEN ){
                    navController!!.navigate(route = Graph.CREATE_LOCK_SCREEN)
                }
            } else if (!sessionActivity) {
                if (navController?.currentBackStackEntry?.destination?.route != Graph.LOCK_SCREEN ){
                    navController!!.navigate(route = Graph.LOCK_SCREEN)
                }
            }
        }
    }

    // При сворачивании приложения вызывает страницу блокировки с вводом пин-кода
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityPaused(activity: Activity) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return

        val sharedPref = activity.getSharedPreferences(
            ContextCompat.getString(activity, R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        sharedPref.edit() { putBoolean("session_activity", false) }
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

}