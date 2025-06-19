package com.usagecontrol.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.usagecontrol.android.ui.screens.*

@Composable
fun UsageControlNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        
        composable(Screen.AppRestrictions.route) {
            AppRestrictionsScreen(navController)
        }
        
        composable(Screen.UsageStats.route) {
            UsageStatsScreen(navController)
        }
        
        composable(Screen.ProgressiveLevels.route) {
            ProgressiveLevelsScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        
        composable(Screen.Permissions.route) {
            PermissionsScreen(navController)
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AppRestrictions : Screen("app_restrictions")
    object UsageStats : Screen("usage_stats")
    object Settings : Screen("settings")
    object ProgressiveLevels : Screen("progressive_levels")
    object Permissions : Screen("permissions")
}
