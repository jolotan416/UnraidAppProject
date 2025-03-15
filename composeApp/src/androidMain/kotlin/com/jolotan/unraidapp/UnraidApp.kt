package com.jolotan.unraidapp

import android.app.Application
import com.jolotan.unraidapp.di.applicationModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class UnraidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@UnraidApp)
            applicationModules()
        }
    }
}