package com.jolotan.unraidapp.data

import com.jolotan.unraidapp.data.models.PlatformConfig
import platform.UIKit.UIDevice

class IOSPlatformConfig: PlatformConfig {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}