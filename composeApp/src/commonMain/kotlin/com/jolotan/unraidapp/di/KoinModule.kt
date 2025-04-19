package com.jolotan.unraidapp.di

import com.jolotan.unraidapp.data.api.QueryApi
import com.jolotan.unraidapp.data.api.QueryApiImpl
import com.jolotan.unraidapp.data.datasource.NasConnectionDao
import com.jolotan.unraidapp.data.datasource.NasConnectionDataSource
import com.jolotan.unraidapp.data.datasource.NasConnectionDataSourceImpl
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSourceImpl
import com.jolotan.unraidapp.data.datasource.UnraidAppDatabase
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import com.jolotan.unraidapp.data.repositories.NasDataRepositoryImpl
import com.jolotan.unraidapp.ui.utils.InternalLog
import com.jolotan.unraidapp.ui.viewmodels.login.ConnectScreenViewModel
import com.jolotan.unraidapp.ui.viewmodels.wakeonlan.WakeOnLanViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
    factory {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    encodeDefaults = true
                    isLenient = true
                    coerceInputValues = true
                    explicitNulls = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        InternalLog.d(tag = "HttpClient", message = message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
    singleOf(::QueryApiImpl) bind QueryApi::class
    singleOf<UdpSocketDataSource>(::UdpSocketDataSourceImpl)
    singleOf(::NasConnectionDataSourceImpl) bind NasConnectionDataSource::class
}

val databaseModule = module {
    single<NasConnectionDao> { get<UnraidAppDatabase>().getNasConnectionDao() }
}