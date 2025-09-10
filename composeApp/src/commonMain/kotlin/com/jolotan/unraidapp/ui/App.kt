package com.jolotan.unraidapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jolotan.unraidapp.ui.containers.UnraidAppScaffold
import com.jolotan.unraidapp.ui.screens.dashboard.DashboardScreen
import com.jolotan.unraidapp.ui.screens.login.LoginScreen
import com.jolotan.unraidapp.ui.screens.wake_on_lan.WakeOnLanScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Route.Connect.name) {
            val navigateToDashboard = {
                navController.navigate(
                    Route.Dashboard.name,
                    navOptions = NavOptions.Builder()
                        .setPopUpTo(Route.Connect.name, inclusive = true).build()
                )
            }
            composable(Route.Connect.name) {
                LoginScreen(navigateToWakeOnLan = {
                    navController.navigate(Route.WakeOnLan.name)
                }, navigateToDashboard = navigateToDashboard)
            }
            composable(Route.WakeOnLan.name) {
                UnraidAppScaffold(navigateUp = { navController.navigateUp() }) {
                    WakeOnLanScreen(navigateToDashboard = navigateToDashboard)
                }
            }
            composable(Route.Dashboard.name) {
                DashboardScreen()
            }
        }
    }
}