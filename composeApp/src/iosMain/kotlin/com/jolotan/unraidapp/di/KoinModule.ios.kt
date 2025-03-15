package com.jolotan.unraidapp.di

import com.jolotan.unraidapp.data.IOSPlatformConfig
import com.jolotan.unraidapp.data.datasource.UnraidAppDatabase
import com.jolotan.unraidapp.data.datasource.getUnraidAppDatabase
import com.jolotan.unraidapp.data.getDatabaseBuilder
import com.jolotan.unraidapp.data.models.PlatformConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformDatabaseModule = module {
    single<UnraidAppDatabase> { getDatabaseBuilder().getUnraidAppDatabase() }
}

actual val platformModule: Module = module {
    singleOf<PlatformConfig>(::IOSPlatformConfig)
    includes(platformDatabaseModule)
}