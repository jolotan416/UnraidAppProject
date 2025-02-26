package com.jolotan.unraidapp.data

import android.os.Build

class AndroidPlatformConfig : PlatformConfig {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}