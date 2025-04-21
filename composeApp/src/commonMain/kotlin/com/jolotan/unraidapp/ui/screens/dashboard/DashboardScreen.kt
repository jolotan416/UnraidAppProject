package com.jolotan.unraidapp.ui.screens.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DashboardScreen() {
    Text(modifier = Modifier.fillMaxSize(), text = "HELLO WORLD!!!", textAlign = TextAlign.Center)
}