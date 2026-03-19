package com.mohamedrejeb.calf.sample

import platform.UIKit.UIDevice

actual val currentPlatform: Platform = Platform.IOS

actual val isLiquidGlassAvailable: Boolean = isIOS26OrAbove()

private fun isIOS26OrAbove(): Boolean {
    val systemVersion = UIDevice.currentDevice.systemVersion
    val major = systemVersion.split(".").firstOrNull()?.toIntOrNull() ?: 0
    return major >= 26
}