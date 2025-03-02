package com.jolotan.unraidapp.data

import android.os.Build
import com.jolotan.unraidapp.data.models.PlatformConfig

class AndroidPlatformConfig : PlatformConfig {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}