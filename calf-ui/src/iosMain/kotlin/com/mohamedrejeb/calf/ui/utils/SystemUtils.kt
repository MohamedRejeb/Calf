package com.mohamedrejeb.calf.ui.utils

import platform.UIKit.UIDevice

/** True when the device runs iOS [major] or newer. */
internal fun isIOSVersionAtLeast(major: Int): Boolean {
    val systemVersion = UIDevice.currentDevice.systemVersion
    val current = systemVersion.split(".").firstOrNull()?.toIntOrNull() ?: 0
    return current >= major
}

internal fun isIOS26OrAbove(): Boolean = isIOSVersionAtLeast(26)
