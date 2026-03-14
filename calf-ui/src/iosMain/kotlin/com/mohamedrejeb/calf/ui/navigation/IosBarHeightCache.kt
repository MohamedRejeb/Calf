package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIDevice

/**
 * Simple in-memory cache for the last measured iOS native bar heights.
 *
 * When [AdaptiveTopBar] or [AdaptiveNavigationBar] measures the actual native bar height,
 * it writes the value here. On subsequent compositions (e.g. navigating to a new screen),
 * the cached value is used as the initial estimate instead of the hardcoded default,
 * reducing the chance of a visible layout jump.
 */
internal object IosBarHeightCache {
    private val isLiquidGlassEnabled = UIDevice.currentDevice.systemVersion.toDouble() >= 26.0
    private val DEFAULT_NAV_BAR_HEIGHT =
        if (isLiquidGlassEnabled)
            54.dp
        else
            44.dp
    private val DEFAULT_TAB_BAR_HEIGHT =
        if (isLiquidGlassEnabled)
            83.dp
        else
            49.dp

    var lastNavBarHeight: Dp = DEFAULT_NAV_BAR_HEIGHT
        private set

    var lastTabBarHeight: Dp = DEFAULT_TAB_BAR_HEIGHT
        private set

    fun updateNavBarHeight(height: Dp) {
        if (height.value > 0f) {
            lastNavBarHeight = height
        }
    }

    fun updateTabBarHeight(height: Dp) {
        if (height.value > 0f) {
            lastTabBarHeight = height
        }
    }
}
