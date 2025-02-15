package com.jolotan.unraidapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform