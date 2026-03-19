package com.mohamedrejeb.calf.ui.utils

import platform.UIKit.UIDevice

internal fun isIOS26OrAbove(): Boolean {
    val systemVersion = UIDevice.currentDevice.systemVersion
    val major = systemVersion.split(".").firstOrNull()?.toIntOrNull() ?: 0
    return major >= 26
}