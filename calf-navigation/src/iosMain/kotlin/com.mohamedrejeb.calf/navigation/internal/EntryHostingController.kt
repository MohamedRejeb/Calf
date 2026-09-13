@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
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
    density: Float,
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
            CompositionLocalProvider(parentLocals.value) {
                CompositionLocalProvider(
                    LocalLifecycleOwner provides entryScope,
                    LocalViewModelStoreOwner provides entryScope,
                    LocalSavedStateRegistryOwner provides entryScope,
                    LocalNavEntrySafeArea provides safeAreaPadding.value,
                ) {
                    entry.Content()
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
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        navigationController?.let { nav ->
            // setNavigationBarHidden(animated:) coordinates with the push/pop
            // transition, so per-entry barShown animates cleanly.
            nav.setNavigationBarHidden(!metadata.barShown, animated)
            nav.navigationBar.applyEntryConfiguration(metadata.iosConfiguration)
        }
    }

    // Safe-area changes (bar shown/hidden, large-title collapse, rotation)
    // always trigger a layout pass, so this keeps the padding current.
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        updateSafeAreaPadding()
    }

    private fun updateSafeAreaPadding() {
        safeAreaPadding.value = view.safeAreaInsets.useContents {
            PaddingValues.Absolute(
                left = left.dp,
                top = top.dp,
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
    }
}
