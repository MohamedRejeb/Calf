package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Pure layout rules of the native iOS bars (`UINavigationBar`, `UITabBar`) that
 * AdaptiveTopBar / AdaptiveNavigationBar add on top of Compose. Kept free of
 * UIKit so they can be unit tested; the UIKit side only feeds measurements in.
 */

/** The native bars whose height is cached between screens. */
internal enum class IosBarKind {
    NavigationBar,
    LargeTitleNavigationBar,
    TabBar,
}

/**
 * What a cached bar height is valid for. UIKit sizes bars from the vertical
 * size class (iPhone landscape is compact), so a height measured in one size
 * class must never be served in the other.
 */
internal data class IosBarHeightKey(
    val kind: IosBarKind,
    val isCompactHeight: Boolean,
)

/**
 * Last measured bar heights, used as the first-frame estimate of a new screen
 * so its content does not jump once the real bar is measured.
 */
@Immutable
internal class IosBarHeights private constructor(
    private val isLiquidGlassEnabled: Boolean,
    private val measuredHeights: Map<IosBarHeightKey, Dp>,
) {
    constructor(isLiquidGlassEnabled: Boolean) : this(isLiquidGlassEnabled, emptyMap())

    fun heightFor(key: IosBarHeightKey): Dp =
        measuredHeights[key] ?: defaultIosBarHeight(key, isLiquidGlassEnabled)

    /** A bar that is not laid out yet reports a zero height, which is not worth remembering. */
    fun withMeasuredHeight(key: IosBarHeightKey, height: Dp): IosBarHeights =
        if (height <= 0.dp || measuredHeights[key] == height)
            this
        else
            IosBarHeights(isLiquidGlassEnabled, measuredHeights + (key to height))
}

private val CLASSIC_NAV_BAR_HEIGHT = 44.dp
private val CLASSIC_LARGE_TITLE_NAV_BAR_HEIGHT = 96.dp
private val CLASSIC_TAB_BAR_HEIGHT = 49.dp

/** Classic bars all shrink to this in a compact-height container; large titles collapse there. */
private val CLASSIC_COMPACT_BAR_HEIGHT = 32.dp

private val LIQUID_GLASS_NAV_BAR_HEIGHT = 54.dp
private val LIQUID_GLASS_LARGE_TITLE_NAV_BAR_HEIGHT = 106.dp
private val LIQUID_GLASS_TAB_BAR_HEIGHT = 83.dp

/**
 * Estimate for a bar that was never measured. Liquid Glass has no separate
 * compact values yet: they were never measured on a device, and the first real
 * measurement replaces the estimate anyway.
 */
private fun defaultIosBarHeight(key: IosBarHeightKey, isLiquidGlassEnabled: Boolean): Dp =
    when {
        isLiquidGlassEnabled -> when (key.kind) {
            IosBarKind.NavigationBar -> LIQUID_GLASS_NAV_BAR_HEIGHT
            IosBarKind.LargeTitleNavigationBar -> LIQUID_GLASS_LARGE_TITLE_NAV_BAR_HEIGHT
            IosBarKind.TabBar -> LIQUID_GLASS_TAB_BAR_HEIGHT
        }

        key.isCompactHeight -> CLASSIC_COMPACT_BAR_HEIGHT

        else -> when (key.kind) {
            IosBarKind.NavigationBar -> CLASSIC_NAV_BAR_HEIGHT
            IosBarKind.LargeTitleNavigationBar -> CLASSIC_LARGE_TITLE_NAV_BAR_HEIGHT
            IosBarKind.TabBar -> CLASSIC_TAB_BAR_HEIGHT
        }
    }

/**
 * Space the content has to leave for a bar: the bar itself plus the safe-area
 * inset it sits against. A bar that already extends under that inset
 * ([barCoversSafeArea], the Liquid Glass tab bar) includes it in its height.
 */
internal fun iosBarContentPadding(
    barHeight: Dp,
    safeAreaInset: Dp,
    barCoversSafeArea: Boolean,
): Dp =
    if (barCoversSafeArea) barHeight else barHeight + safeAreaInset

/** One reading of the native bar's frame (in its host view) and the inset on the bar's edge. */
internal data class IosBarMeasurement(
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val barHeight: Dp,
    val safeAreaInset: Dp,
)

/** Where the native bar sits and how much room the Compose content leaves for it. */
@Immutable
internal data class IosBarLayout(
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val contentPadding: Dp,
) {
    /**
     * Layout after a new [measurement]. A bar that UIKit has not laid out yet
     * measures zero; applying that would drop the estimate and make the
     * content jump twice, so it is ignored.
     */
    fun measured(measurement: IosBarMeasurement, barCoversSafeArea: Boolean): IosBarLayout =
        if (measurement.barHeight <= 0.dp)
            this
        else
            IosBarLayout(
                x = measurement.x,
                y = measurement.y,
                width = measurement.width,
                contentPadding = iosBarContentPadding(
                    barHeight = measurement.barHeight,
                    safeAreaInset = measurement.safeAreaInset,
                    barCoversSafeArea = barCoversSafeArea,
                ),
            )

    companion object {
        /** First-frame layout, before UIKit measured anything. */
        fun estimated(
            cachedBarHeight: Dp,
            safeAreaInset: Dp,
            barCoversSafeArea: Boolean,
        ): IosBarLayout =
            IosBarLayout(
                x = 0.dp,
                y = 0.dp,
                width = 0.dp,
                contentPadding = iosBarContentPadding(cachedBarHeight, safeAreaInset, barCoversSafeArea),
            )
    }
}
