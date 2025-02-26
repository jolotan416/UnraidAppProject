package com.jolotan.unraidapp.di

import com.jolotan.unraidapp.data.AndroidPlatformConfig
import com.jolotan.unraidapp.data.PlatformConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module { singleOf<PlatformConfig>(::AndroidPlatformConfig) }