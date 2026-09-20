@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
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
import platform.UIKit.addChildViewController
import platform.UIKit.childViewControllers
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.hidesBottomBarWhenPushed
import platform.UIKit.navigationController
import platform.UIKit.navigationItem
import platform.UIKit.removeFromParentViewController
import platform.UIKit.willMoveToParentViewController

/**
 * Per-entry child UIViewController. Three responsibilities:
 *
 *  1. **Populate `navigationItem` and `hidesBottomBarWhenPushed` in `init`**,
 *     before the controller joins the navigation stack: UIKit reads both
 *     during `setViewControllers(animated:)` to animate the title cross-fade
 *     and the tab bar. Bar-wide state (hidden, appearance) is applied in
 *     `viewWillAppear` so it changes together with the transition.
 *
 *  2. **In `loadView`, host a [ComposeUIViewController]** whose content
 *     re-applies the parent composition's locals (theme, density, …) plus the
 *     per-entry `LifecycleOwner` / `ViewModelStoreOwner` / `SavedStateRegistryOwner`.
 *
 *  3. **In `viewDidDisappear(animated:)`**, when `isMovingFromParentViewController()`
 *     is true (popped for real, not merely covered by a push), tear the entry
 *     down: clear its `ViewModelStore` and mark its lifecycle destroyed. The
 *     navigation host mirrors the pop into the Compose back stack from its
 *     delegate, so no callback is needed here.
 */
internal class EntryHostingController(
    private val entry: NavEntry<NavKey>,
    private val parentLocals: State<CompositionLocalContext>,
    private val density: Float,
) : UIViewController(nibName = null, bundle = null) {

    private val metadata = entry.adaptiveMetadata
    private val entryScope = NavEntryScope()

    /** Read by the host's pop-gesture delegate to honour `iosGestureEnabled`. */
    val gestureEnabled: Boolean get() = metadata.iosGestureEnabled

    /**
     * UIKit-measured safe-area insets of this controller's view, exposed to
     * the child composition via [LocalNavEntrySafeArea]. The child Compose
     * runtime's own `WindowInsets` does not see the nav-bar extension that
     * `UINavigationController` applies to its children, so the bar inset has
     * to be piped through from UIKit.
     */
    private val safeAreaPadding = mutableStateOf(PaddingValues(0.dp))

    /**
     * Invisible, non-interactive scroll view registered as this controller's
     * content scroll view. UIKit collapses the large title and switches the
     * bar's scroll-edge appearance from *its* content offset, so the Compose
     * content's scroll is mirrored into it through [reportScrollOffset].
     *
     * UIKit expands the bar while the offset is at or past
     * `-adjustedContentInset.top` measured with the bar expanded, and collapses
     * it as the offset rises from there. The inset itself shrinks with the bar,
     * so it is sampled as the largest value seen while the content is at its
     * top; parking the rest offset past that position (a bounce at the top,
     * which UIKit renders fully expanded) is what expands the bar on the very
     * first layout, before the expanded inset is known.
     */
    private var contentScrollView: UIScrollView? = null
    private var trackedScrollPx: Float = 0f
    private var calibration = NavBarInsetCalibration()
    private val titleMovesWithContent = isIOSVersionAtLeast(LIQUID_GLASS_MIN_IOS)

    /** Retained so UIKit's weak bar-button targets stay alive and clicks keep working. */
    @Suppress("unused")
    private val itemBindings: NavBarItemBindings =
        applyEntryMetadataToItem(this.navigationItem, metadata, density)

    init {
        // Read by UIKit when this controller is pushed inside a UITabBarController.
        hidesBottomBarWhenPushed = !metadata.tabBarShown
    }

    override fun loadView() {
        super.loadView()
        val composeChild = ComposeUIViewController(
            configure = {
                onFocusBehavior = OnFocusBehavior.DoNothing
            },
        ) {
            // Locals this surface provides itself (its UIViewController, interop
            // container, window info, …) must win over the parent's captured
            // context: otherwise UIKit interop inside the entry (a nested
            // AdaptiveTabDisplay, a WebView, …) attaches to the parent surface,
            // whose Compose layer has no node for it, and never gets touches.
            val surfaceLocals = currentCompositionLocalContext
            CompositionLocalProvider(parentLocals.value) {
                CompositionLocalProvider(surfaceLocals) {
                    CompositionLocalProvider(
                        LocalLifecycleOwner provides entryScope,
                        LocalViewModelStoreOwner provides entryScope,
                        LocalSavedStateRegistryOwner provides entryScope,
                        LocalNavEntrySafeArea provides safeAreaPadding.value,
                        LocalNavEntryScrollReporter provides ::reportScrollOffset,
                    ) {
                        entry.Content()
                    }
                }
            }
        }

        composeChild.willMoveToParentViewController(this)
        composeChild.view.setFrame(view.bounds)
        composeChild.view.autoresizingMask =
            UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        view.addSubview(composeChild.view)
        addChildViewController(composeChild)
        composeChild.didMoveToParentViewController(this)
        installContentScrollView()
    }

    private fun installContentScrollView() {
        val scrollView = ContentScrollView(onSafeAreaInsetsChange = ::updateSafeAreaPadding).apply {
            // Transparent and touch-transparent: it only exists to be observed.
            userInteractionEnabled = false
            opaque = false
            showsVerticalScrollIndicator = false
            showsHorizontalScrollIndicator = false
            setFrame(view.bounds)
            autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        }
        // Above the Compose surface: iOS 26 renders the large title (and the
        // scroll-edge effect) inside the content scroll view's space, so a
        // scroll view behind the surface would draw them out of sight.
        view.addSubview(scrollView)
        if (isIOSVersionAtLeast(CONTENT_SCROLL_VIEW_MIN_IOS)) {
            setContentScrollView(scrollView, forEdge = NSDirectionalRectEdgeTop)
        }
        contentScrollView = scrollView
    }

    private fun reportScrollOffset(offsetPx: Float) {
        trackedScrollPx = offsetPx
        applyScrollOffset()
    }

    private fun applyScrollOffset() {
        val scrollView = contentScrollView ?: return
        val width = view.bounds.useContents { size.width }
        // Bar heights differ per orientation; recalibrate after a size change.
        val recalibrated = calibration.forWidth(width)
        val isRecalibrating = recalibrated !== calibration
        calibration = recalibrated

        val atTop = trackedScrollPx == 0f
        val topInsetBefore = scrollView.adjustedContentInset.useContents { top }
        if ((atTop && isRecalibrating) || calibration.isCollapsedAtTop(atTop, topInsetBefore)) {
            expandCollapsedBar(scrollView)
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
            if (kotlin.math.abs(applied - offsetY) < OFFSET_EPSILON) return
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
    private fun expandCollapsedBar(scrollView: UIScrollView) {
        val collapsedTopInset = scrollView.adjustedContentInset.useContents { top }
        scrollView.setContentOffset(
            CGPointMake(0.0, -(collapsedTopInset + EXPANSION_PULL_DOWN)),
            animated = false,
        )
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        navigationController?.let { nav ->
            // setNavigationBarHidden(animated:) coordinates with the push/pop
            // transition, so per-entry barShown animates cleanly.
            nav.setNavigationBarHidden(!metadata.barShown, animated)
            nav.navigationBar.translucent = metadata.iosConfiguration.isTranslucent
        }
    }

    // Safe-area changes (bar shown/hidden, large-title collapse, rotation) normally come
    // with a layout pass. The exception is a change made from inside one, like
    // expandCollapsedBar(); ContentScrollView reports those.
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        // Tall enough that any mirrored offset is a valid position.
        contentScrollView?.setContentSize(
            view.bounds.useContents { CGSizeMake(size.width, size.height + CONTENT_SCROLL_EXTENT) },
        )
        // Keeps the rest position on the expanded bar as insets settle.
        applyScrollOffset()
        // After the offset: that is what (re)calibrates the inset the padding is built from.
        updateSafeAreaPadding()
    }

    private fun updateSafeAreaPadding() {
        safeAreaPadding.value = view.safeAreaInsets.useContents {
            PaddingValues.Absolute(
                left = left.dp,
                // Not the live inset: it shrinks with the large title, see NavBarInsetCalibration.
                top = calibration.contentTopInset(liveTopInset = top).dp,
                right = right.dp,
                bottom = bottom.dp,
            )
        }
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        entryScope.markStarted()
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        if (isMovingFromParentViewController()) {
            disposeEntry()
        } else {
            entryScope.markStopped()
        }
    }

    /**
     * Tears the entry down. Idempotent: called from `viewDidDisappear` after a
     * native pop, and by the host for entries that leave the stack without
     * ever disappearing (dropped from the middle, popped while covered, or the
     * whole navigator leaving the composition). Without it the entry's
     * `ViewModelStore` would never be cleared.
     */
    fun disposeEntry() {
        entryScope.markDestroyed()
        if (isViewLoaded()) {
            cleanupChildren()
        }
    }

    private fun cleanupChildren() {
        view.subviews.forEach { (it as UIView).removeFromSuperview() }
        childViewControllers.forEach { (it as UIViewController).removeFromParentViewController() }
        contentScrollView = null
    }
}

/**
 * The mirrored scroll view fills the entry's view, so it has the same safe-area insets.
 * `viewSafeAreaInsetsDidChange` cannot be overridden from Kotlin, this can.
 */
private class ContentScrollView(
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

