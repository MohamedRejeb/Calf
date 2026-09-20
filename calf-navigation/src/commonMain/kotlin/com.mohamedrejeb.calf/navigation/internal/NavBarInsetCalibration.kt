package com.mohamedrejeb.calf.navigation.internal

/**
 * What an iOS entry host knows about its navigation bar's *resting* top inset,
 * the one with the large title expanded. All values are in points.
 *
 * UIKit shrinks the entry's safe area as the large title collapses. A native
 * scroll view hides that from its content: the content starts at the expanded
 * inset and simply scrolls under the shrinking bar. Compose content gets the
 * same behaviour by being padded with [contentTopInset], which does not follow
 * the collapse; padding it with the live inset moved it a second time, on top
 * of its own scroll, which showed as a jump when the title changed.
 *
 * @property width width the calibration was made for. Bar heights differ per
 *   orientation and size class, so another width starts over.
 * @property expandedTopInset largest top inset seen while the content was at
 *   its top, zero until sampled.
 */
internal data class NavBarInsetCalibration(
    val width: Double = 0.0,
    val expandedTopInset: Double = 0.0,
) {
    fun forWidth(width: Double): NavBarInsetCalibration =
        if (width == this.width) this else NavBarInsetCalibration(width = width)

    /** The inset only shrinks while scrolling, so at the top the largest value seen is the expanded one. */
    fun sampledAtTop(currentTopInset: Double): NavBarInsetCalibration =
        if (currentTopInset <= expandedTopInset) this else copy(expandedTopInset = currentTopInset)

    /** Top inset for the content: stable while the bar collapses, live when nothing was sampled yet. */
    fun contentTopInset(liveTopInset: Double): Double =
        maxOf(liveTopInset, expandedTopInset)

    /**
     * Offset to mirror into the host's scroll view. At the top it must sit
     * exactly on the current inset: UIKit expands the bar from there, and
     * anything past it counts as a pull-down (iOS 26 then moves the large title
     * with the content, out of the bar). Scrolled content counts from the
     * expanded inset, which is where UIKit starts collapsing.
     */
    fun mirroredOffsetY(atTop: Boolean, currentTopInset: Double, scrolledPoints: Double): Double =
        if (atTop) -currentTopInset else -expandedTopInset + scrolledPoints
}
