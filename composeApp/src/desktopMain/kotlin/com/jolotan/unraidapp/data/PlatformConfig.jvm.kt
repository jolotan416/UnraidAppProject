package com.jolotan.unraidapp.data

import com.jolotan.unraidapp.data.models.PlatformConfig

class JVMPlatformConfig: PlatformConfig {
    override val name: String = "Java ${System.getProperty("java.version")}"
}