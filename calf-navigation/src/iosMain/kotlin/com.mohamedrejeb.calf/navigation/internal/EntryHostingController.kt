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
import platform.UIKit.navigationController
import platform.UIKit.navigationItem
import platform.UIKit.removeFromParentViewController
import platform.UIKit.willMoveToParentViewController

/**
 * Per-entry child UIViewController. Three responsibilities:
 *
 *  1. **Populate `navigationItem` in `init`** — before the controller is added to
 *     the navigation stack. UIKit reads it during `setViewControllers(animated:)` to
 *     animate the title cross-fade and trailing-item transition. If we deferred to
 *     `viewDidLoad` or worse to a Compose effect inside the child runtime, the
 *     title would lag the push transition. Bar-wide state (hidden, translucency)
 *     is applied in `viewWillAppear` so it coordinates with the push transition.
 *
 *  2. **In `loadView`, instantiate a [ComposeUIViewController]** as a child VC
 *     whose content re-applies the parent composition's locals (theme, density,
 *     `LocalNavBackPress`, …) plus per-entry `LifecycleOwner` /
 *     `ViewModelStoreOwner` / `SavedStateRegistryOwner`.
 *
 *  3. **In `viewDidDisappear(animated:)`**, when `isMovingFromParentViewController()`
 *     is true (i.e. this VC was popped for real, not just covered by another
 *     push), tear down state: clear the per-entry `ViewModelStore`, mark
 *     lifecycle DESTROYED, and notify the navigator via [onPermanentPop] so
 *     it can update the Compose backstack.
 */
internal class EntryHostingController(
    private val entry: NavEntry<NavKey>,
    private val barData: IosNavBarData,
    private val parentLocals: State<CompositionLocalContext>,
    density: Float,
    private val onPermanentPop: (EntryHostingController) -> Unit,
    private val onAppeared: () -> Unit,
) : UIViewController(nibName = null, bundle = null) {

    private val entryScope = NavEntryScope()

    /** Read by the navigator's pop-gesture delegate to honour `iosGestureEnabled`. */
    val gestureEnabled: Boolean get() = barData.gestureEnabled

    /**
     * UIKit-measured safe-area insets of this controller's view, exposed to
     * the child composition via [LocalNavEntrySafeArea]. The child Compose
     * runtime's own `WindowInsets` does not see the nav-bar extension that
     * `UINavigationController` applies to its children, so the bar inset has
     * to be piped through from UIKit.
     */
    private val safeAreaPadding = mutableStateOf(PaddingValues(0.dp))

    /**
     * Strong references to the bar-button action handlers, retained so UIKit's
     * weak target reference doesn't get cleared and clicks become no-ops.
     */
    @Suppress("unused")
    private val itemHandlers: NavBarItemHandlers =
        applyNavBarDataToItem(this.navigationItem, barData, density)

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
            nav.setNavigationBarHidden(!barData.barShown, animated)
            nav.navigationBar.applyConfiguration(barData.configuration)
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
        onAppeared()
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        if (isMovingFromParentViewController()) {
            entryScope.markDestroyed()
            onPermanentPop(this)
            cleanupChildren()
        } else {
            entryScope.markStopped()
        }
    }

    /**
     * Tears down the entry when it leaves the stack without ever receiving
     * `viewDidDisappear` — dropped from the middle of the stack, or the whole
     * navigator left the composition. Without this, the entry's
     * `ViewModelStore` would never be cleared and `onCleared` never called.
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
