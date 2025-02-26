package com.jolotan.unraidapp.di

import androidx.compose.runtime.Composable
import com.jolotan.unraidapp.viewmodels.login.LoginScreenViewModel
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

@Composable
fun KoinUnraidApplication(content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(viewModelsModule, platformModule)
    }, content)
}

val viewModelsModule = module { viewModelOf(::LoginScreenViewModel) }