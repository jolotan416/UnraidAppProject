package com.jolotan.unraidapp.di

import androidx.compose.runtime.Composable
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSourceImpl
import com.jolotan.unraidapp.ui.viewmodels.login.LoginScreenViewModel
import com.jolotan.unraidapp.ui.viewmodels.wakeonlan.WakeOnLanViewModel
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

@Composable
fun KoinUnraidApplication(content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(viewModelsModule, dataSourcesModule, platformModule)
    }, content)
}

val viewModelsModule = module {
    viewModelOf(::LoginScreenViewModel)
    viewModelOf(::WakeOnLanViewModel)
}

val dataSourcesModule = module {
    singleOf<UdpSocketDataSource>(::UdpSocketDataSourceImpl)
}