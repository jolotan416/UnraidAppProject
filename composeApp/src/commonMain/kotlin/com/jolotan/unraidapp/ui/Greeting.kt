package com.jolotan.unraidapp.ui

import com.jolotan.unraidapp.data.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}