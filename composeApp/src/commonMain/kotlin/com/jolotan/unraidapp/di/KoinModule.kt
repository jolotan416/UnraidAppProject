package com.jolotan.unraidapp.di

import com.jolotan.unraidapp.data.datasource.NasConnectionDao
import com.jolotan.unraidapp.data.datasource.NasConnectionDataSource
import com.jolotan.unraidapp.data.datasource.NasConnectionDataSourceImpl
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSourceImpl
import com.jolotan.unraidapp.data.datasource.UnraidAppDatabase
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import com.jolotan.unraidapp.data.repositories.NasDataRepositoryImpl
import com.jolotan.unraidapp.ui.viewmodels.login.ConnectScreenViewModel
import com.jolotan.unraidapp.ui.viewmodels.wakeonlan.WakeOnLanViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

fun initializeKoin() {
    startKoin {
        applicationModules()
    }
}

fun KoinApplication.applicationModules() {
    modules(
        viewModelsModule,
        repositoriesModule,
        dataSourcesModule,
        databaseModule,
        platformModule
    )
}

val viewModelsModule = module {
    viewModelOf(::ConnectScreenViewModel)
    viewModelOf(::WakeOnLanViewModel)
}

val repositoriesModule = module {
    singleOf(::NasDataRepositoryImpl) bind NasDataRepository::class
}

val dataSourcesModule = module {
    singleOf<UdpSocketDataSource>(::UdpSocketDataSourceImpl)
    singleOf(::NasConnectionDataSourceImpl) bind NasConnectionDataSource::class
}

val databaseModule = module {
    single<NasConnectionDao> { get<UnraidAppDatabase>().getNasConnectionDao() }
}