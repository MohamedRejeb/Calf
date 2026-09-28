@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.NSDirectionalRectEdgeTop
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import kotlin.math.abs

/**
 * Invisible, non-interactive scroll view registered as a view controller's content scroll view,
 * so a navigation bar above Compose content collapses its large title and switches its
 * scroll-edge appearance as if the content were a native scroll view. Shared by every iOS host
 * of Compose content inside a `UINavigationController`: calf-navigation's entries and calf-ui's
 * bar host.
 *
 * UIKit expands the bar while the offset is at or past `-adjustedContentInset.top` measured with
 * the bar expanded, and collapses it as the offset rises from there. The inset itself shrinks
 * with the bar, so it is sampled as the largest value seen while the content is at its top;
 * parking the rest offset past that position (a bounce at the top, which UIKit renders fully
 * expanded) is what expands the bar on the very first layout, before the expanded inset is known.
 *
 * The scroll view must stay *above* the Compose view: iOS 26 renders the large title (and the
 * scroll-edge effect) inside the content scroll view's space, so a scroll view behind the Compose
 * surface would draw them out of sight.
 *
 * @param density points to pixels, for the offsets reported by [reportScrollOffset].
 * @param onSafeAreaInsetsChange called when the scroll view's safe area changes outside a layout
 *   pass of its view controller (it fills that view, so it has the same safe area).
 */
@InternalCalfApi
public class LargeTitleScrollMirror(
    private val density: Float,
    onSafeAreaInsetsChange: () -> Unit,
) {
    /** The mirrored scroll view. Content added to its superview goes below it. */
    public val scrollView: UIScrollView = MirrorScrollView(onSafeAreaInsetsChange).apply {
        // Transparent and touch-transparent: it only exists to be observed.
        userInteractionEnabled = false
        opaque = false
        showsVerticalScrollIndicator = false
        showsHorizontalScrollIndicator = false
        autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
    }

    private var trackedScrollPx: Float = 0f
    private var calibration = NavBarInsetCalibration()
    private val titleMovesWithContent = isIOSVersionAtLeast(LIQUID_GLASS_MIN_IOS)

    /**
     * Puts the scroll view on top of [viewController]'s view and registers it there. Called again
     * with another controller, it moves over (the bar host moves its content between tabs).
     */
    public fun install(viewController: UIViewController) {
        val view = viewController.view
        scrollView.removeFromSuperview()
        scrollView.setFrame(view.bounds)
        view.addSubview(scrollView)
        if (isIOSVersionAtLeast(CONTENT_SCROLL_VIEW_MIN_IOS)) {
            viewController.setContentScrollView(scrollView, forEdge = NSDirectionalRectEdgeTop)
        }
    }

    public fun remove() {
        scrollView.removeFromSuperview()
    }

    /** The content's scroll distance from its top, in pixels, as a [LargeTitleScrollTracker] reports it. */
    public fun reportScrollOffset(offsetPx: Float) {
        trackedScrollPx = offsetPx
        applyScrollOffset()
    }

    /** Call from the view controller's `viewDidLayoutSubviews`: keeps the rest position on the expanded bar. */
    public fun layout() {
        // Tall enough that any mirrored offset is a valid position.
        scrollView.setContentSize(
            scrollView.bounds.useContents { CGSizeMake(size.width, size.height + CONTENT_SCROLL_EXTENT) },
        )
        applyScrollOffset()
    }

    /** Top inset for the content: stable while the large title collapses. See [NavBarInsetCalibration]. */
    public fun contentTopInset(liveTopInset: Double): Double =
        calibration.contentTopInset(liveTopInset)

    private fun applyScrollOffset() {
        if (scrollView.superview == null) return
        val width = scrollView.bounds.useContents { size.width }
        // Bar heights differ per orientation; recalibrate after a size change.
        val recalibrated = calibration.forWidth(width)
        val isRecalibrating = recalibrated !== calibration
        calibration = recalibrated

        val atTop = trackedScrollPx == 0f
        val topInsetBefore = scrollView.adjustedContentInset.useContents { top }
        if ((atTop && isRecalibrating) || calibration.isCollapsedAtTop(atTop, topInsetBefore)) {
            expandCollapsedBar()
        }
        // Changing the offset can change the bar height, and UIKit then shifts
        // the offset by the inset delta to keep content still. Re-applying once
        // puts the title where the scroll says instead of one frame later.
        repeat(OFFSET_APPLY_PASSES) {
            val currentTopInset = scrollView.adjustedContentInset.useContents { top }
            if (atTop) {
                calibration = calibration.sampledAtTop(currentTopInset)
            }
            val offsetY = calibration.mirroredOffsetY(
                atTop = atTop,
                currentTopInset = currentTopInset,
                scrolledPoints = mirroredScrollPoints(
                    scrolledPoints = (trackedScrollPx / density).toDouble(),
                    titleMovesWithContent = titleMovesWithContent,
                ),
            )
            scrollView.setContentOffset(CGPointMake(0.0, offsetY), animated = false)
            val applied = scrollView.contentOffset.useContents { y }
            if (abs(applied - offsetY) < OFFSET_EPSILON) return
        }
    }

    /**
     * The bar can be collapsed although the content is at its top: after a size change (iPhone
     * landscape shows the title inline, and rotating back to portrait keeps it inline) and
     * after a one-step collapse on systems whose title only snaps. The rest offset sits exactly
     * on the *current* inset, which is then the collapsed one, and that is a stable state for
     * UIKit, so nothing would ever expand the bar again.
     * Pulling the offset past the inset once makes UIKit expand it (synchronously, the inset
     * grows with it), and the regular passes then settle the offset on the expanded inset.
     */
    private fun expandCollapsedBar() {
        val collapsedTopInset = scrollView.adjustedContentInset.useContents { top }
        scrollView.setContentOffset(
            CGPointMake(0.0, -(collapsedTopInset + EXPANSION_PULL_DOWN)),
            animated = false,
        )
    }
}

/**
 * `viewSafeAreaInsetsDidChange` cannot be overridden from Kotlin, this can. The scroll view fills
 * its view controller's view, so it has the same safe-area insets.
 */
private class MirrorScrollView(
    private val onSafeAreaInsetsChange: () -> Unit,
) : UIScrollView(frame = CGRectZero.readValue()) {
    override fun safeAreaInsetsDidChange() {
        super.safeAreaInsetsDidChange()
        onSafeAreaInsetsChange()
    }
}

private const val CONTENT_SCROLL_VIEW_MIN_IOS = 15
private const val LIQUID_GLASS_MIN_IOS = 26
private const val CONTENT_SCROLL_EXTENT = 100_000.0
private const val OFFSET_APPLY_PASSES = 2
private const val OFFSET_EPSILON = 0.5

/** Points past the inset; anything taller than a large title's extra height works. */
private const val EXPANSION_PULL_DOWN = 200.0
