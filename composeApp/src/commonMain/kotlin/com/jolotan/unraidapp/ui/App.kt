package com.jolotan.unraidapp.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jolotan.unraidapp.di.KoinUnraidApplication
import com.jolotan.unraidapp.ui.containers.UnraidAppScaffold
import com.jolotan.unraidapp.ui.screens.login.LoginScreen
import com.jolotan.unraidapp.ui.screens.wake_on_lan.WakeOnLanScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinUnraidApplication {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Route.Login.name) {
                composable(Route.Login.name) {
                    LoginScreen {
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
}