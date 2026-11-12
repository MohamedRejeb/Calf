package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

/**
 * Simple in-memory cache for the last measured iOS native bar heights.
 *
 * When [AdaptiveTopBar] or [AdaptiveNavigationBar] measures the actual native bar height,
 * it writes the value here. On subsequent compositions (e.g. navigating to a new screen),
 * the cached value is used as the initial estimate instead of the hardcoded default,
 * reducing the chance of a visible layout jump.
 *
 * Heights are keyed by [IosBarHeightKey], so a height measured in portrait is never
 * served to a screen that opens in landscape. The rules live in [IosBarHeights].
 */
internal object IosBarHeightCache {
    private var heights = IosBarHeights(isLiquidGlassEnabled = isIOS26OrAbove())

    fun heightFor(key: IosBarHeightKey): Dp =
        heights.heightFor(key)

    fun update(key: IosBarHeightKey, height: Dp) {
        heights = heights.withMeasuredHeight(key, height)
    }
}
