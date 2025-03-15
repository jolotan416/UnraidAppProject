package com.jolotan.unraidapp.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jolotan.unraidapp.ui.containers.UnraidAppScaffold
import com.jolotan.unraidapp.ui.screens.login.ConnectScreen
import com.jolotan.unraidapp.ui.screens.wake_on_lan.WakeOnLanScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Route.Connect.name) {
            composable(Route.Connect.name) {
                ConnectScreen {
                    navController.navigate(Route.WakeOnLan.name)
                }
            }
            composable(Route.WakeOnLan.name) {
                UnraidAppScaffold(navigateUp = { navController.navigateUp() }) {
                    WakeOnLanScreen()
                }
            }
        }
    }
}