package com.jolotan.unraidapp.viewmodels.login

import androidx.lifecycle.ViewModel
import com.jolotan.unraidapp.data.PlatformConfig

class LoginScreenViewModel(private val platformConfig: PlatformConfig): ViewModel() {
    fun startServer() {
        println("Hello, I'm starting in ${platformConfig.name}!!!")
    }
}