package com.mohamedrejeb.calf.ui.utils

import com.mohamedrejeb.calf.core.InternalCalfApi
import platform.UIKit.UIDevice

/** True when the device runs iOS [major] or newer. */
@InternalCalfApi
public fun isIOSVersionAtLeast(major: Int): Boolean {
    val systemVersion = UIDevice.currentDevice.systemVersion
    val current = systemVersion.split(".").firstOrNull()?.toIntOrNull() ?: 0
    return current >= major
}

/** True on iOS 26+, where the system paints Liquid Glass bars. */
@InternalCalfApi
public fun isIOS26OrAbove(): Boolean = isIOSVersionAtLeast(26)
