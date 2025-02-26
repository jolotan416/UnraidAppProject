package com.jolotan.unraidapp.data

class JVMPlatformConfig: PlatformConfig {
    override val name: String = "Java ${System.getProperty("java.version")}"
}