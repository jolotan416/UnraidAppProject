package com.jolotan.unraidapp.data

import platform.UIKit.UIDevice

class IOSPlatformConfig: PlatformConfig {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}