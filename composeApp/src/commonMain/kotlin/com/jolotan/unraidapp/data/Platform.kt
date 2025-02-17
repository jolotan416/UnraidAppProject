package com.jolotan.unraidapp.data

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform