package com.mohamedrejeb.calf.ui.navigation

import kotlin.math.round

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
 * @property sampledByWidth the expanded inset of the last few widths sampled,
 *   keyed by whole points. The inset can only be sampled with the content at
 *   its top, so a width change while scrolled (a rotation, an iPhone Duo pose
 *   change) would otherwise leave the content following the live inset until
 *   the next scroll to the top; a width seen before restores its inset at once.
 *   Only [REMEMBERED_WIDTHS] entries are kept, so a continuous resize (iPad
 *   Stage Manager) cannot grow it.
 */
internal data class NavBarInsetCalibration(
    val width: Double = 0.0,
    val expandedTopInset: Double = 0.0,
    private val sampledByWidth: Map<Double, Double> = emptyMap(),
) {
    fun forWidth(width: Double): NavBarInsetCalibration {
        val key = round(width)
        if (key == this.width) return this
        val remembered = when {
            expandedTopInset > 0.0 -> (sampledByWidth - this.width + (this.width to expandedTopInset)).takeLast(REMEMBERED_WIDTHS)
            else -> sampledByWidth
        }
        return NavBarInsetCalibration(
            width = key,
            expandedTopInset = remembered[key] ?: 0.0,
            sampledByWidth = remembered,
        )
    }

    /** The inset only shrinks while scrolling, so at the top the largest value seen is the expanded one. */
    fun sampledAtTop(currentTopInset: Double): NavBarInsetCalibration =
        if (currentTopInset <= expandedTopInset) this else copy(expandedTopInset = currentTopInset)

    /** Top inset for the content: stable while the bar collapses, live when nothing was sampled yet. */
    fun contentTopInset(liveTopInset: Double): Double =
        maxOf(liveTopInset, expandedTopInset)

    /**
     * The content is at its top but the bar is smaller than its expanded size. UIKit never leaves
     * that state on its own: the rest offset sits on the *current* inset, and resting on a
     * collapsed bar's inset is stable. It happens whenever the bar was collapsed in one step
     * instead of following the scroll back up (see [mirroredScrollPoints]).
     */
    fun isCollapsedAtTop(atTop: Boolean, currentTopInset: Double): Boolean =
        atTop && currentTopInset < expandedTopInset - INSET_EPSILON

    /**
     * Offset to mirror into the host's scroll view. At the top it must sit
     * exactly on the current inset: UIKit expands the bar from there, and
     * anything past it counts as a pull-down (iOS 26 then moves the large title
     * with the content, out of the bar). Scrolled content counts from the
     * expanded inset, which is where UIKit starts collapsing.
     */
    fun mirroredOffsetY(atTop: Boolean, currentTopInset: Double, scrolledPoints: Double): Double =
        if (atTop) -currentTopInset else -expandedTopInset + scrolledPoints

    companion object {
        /** Enough for the widths a phone or foldable cycles through; iPad multitasking widths are transient. */
        const val REMEMBERED_WIDTHS = 6
    }
}

private fun <K, V> Map<K, V>.takeLast(n: Int): Map<K, V> =
    if (size <= n) this else entries.drop(size - n).associate { (k, v) -> k to v }

private const val INSET_EPSILON = 0.5

/**
 * Scroll distance past which a classic large title is certainly collapsed. The title itself is
 * 52 points tall; the margin covers larger text sizes, and a bigger value costs nothing because
 * the mirrored scroll view is invisible.
 */
private const val SNAPPING_TITLE_COLLAPSE_POINTS = 100.0

/**
 * Scroll distance to mirror into the host's scroll view.
 *
 * iOS 26 draws the large title in the scroll view's space, so it travels with the content and
 * the real distance is right ([titleMovesWithContent]). Earlier systems only collapse the title
 * progressively for a finger-driven `UIScrollView`; from a mirrored offset the title stays fully
 * expanded until the scroll passes its height and then snaps, so the content slid over it. There
 * the collapse is requested as soon as scrolling starts, which gets the title out of the way
 * before the content reaches it.
 */
internal fun mirroredScrollPoints(scrolledPoints: Double, titleMovesWithContent: Boolean): Double =
    when {
        titleMovesWithContent || scrolledPoints <= 0.0 -> scrolledPoints
        else -> maxOf(scrolledPoints, SNAPPING_TITLE_COLLAPSE_POINTS)
    }
