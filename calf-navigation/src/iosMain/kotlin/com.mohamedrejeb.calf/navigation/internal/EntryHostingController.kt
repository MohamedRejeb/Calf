@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class, ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.ui.navigation.LargeTitleScrollMirror
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
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

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
 *  3. **Follow the view's appearance with the entry's lifecycle**: resumed in
 *     `viewDidAppear`, stopped in `viewDidDisappear`. The entry never tears
 *     itself down. Only its host knows that it left the back stack, and calls
 *     [disposeEntry]: `isMovingFromParentViewController()` cannot tell. It is
 *     false for a controller removed through `setViewControllers` (every pop
 *     made on the Compose back stack), and on iOS 26 it was true for a root
 *     that was only being covered, when its tab appeared and a push ran in the
 *     same turn.
 */
internal class EntryHostingController(
    private val entry: NavEntry<NavKey>,
    private val parentLocals: State<CompositionLocalContext>,
    private val density: Float,
) : UIViewController(nibName = null, bundle = null) {

    private val metadata = entry.adaptiveMetadata
    private val entryScope = NavEntryScope()

    /** Kept as one reference so it can be unsubscribed; see [disposeEntry]. */
    private val appStateListener: (AppForegroundState) -> Unit = entryScope::setAppState

    /** Read by the host's pop-gesture delegate to honour `iosGestureEnabled`. */
    val gestureEnabled: Boolean get() = metadata.iosGestureEnabled

    /** Read by a split view host to decide which column shows this entry. */
    val paneRole: AdaptivePaneRole get() = metadata.paneRole

    private var isDisposed: Boolean = false

    /**
     * UIKit-measured safe-area insets of this controller's view, exposed to
     * the child composition via [LocalNavEntrySafeArea]. The child Compose
     * runtime's own `WindowInsets` does not see the nav-bar extension that
     * `UINavigationController` applies to its children, so the bar inset has
     * to be piped through from UIKit.
     */
    private val safeAreaPadding = mutableStateOf(PaddingValues(0.dp))

    /**
     * Whether the entry's content is part of the composition yet. Compose renders a surface's
     * first frame synchronously, in the same main-thread turn in which UIKit sets up the push,
     * and UIKit can only hand the animation to the render server once that turn ends. With the
     * content in that first frame, a tap was followed by nothing for as long as the screen took
     * to compose and draw, and only then by the transition. An animated push therefore starts
     * with an empty surface and attaches the content one turn later: the animation is already
     * running (in the render server, so a busy main thread does not stall it) while the screen
     * loads.
     */
    private val contentAttached = mutableStateOf(false)

    /**
     * Mirrors the Compose content's scroll into an invisible scroll view registered as this
     * controller's content scroll view: UIKit collapses the large title and switches the bar's
     * scroll-edge appearance from *its* offset. See [LargeTitleScrollMirror].
     */
    private val scrollMirror = LargeTitleScrollMirror(
        density = density,
        onSafeAreaInsetsChange = ::updateSafeAreaPadding,
    )

    /** Retained so UIKit's weak bar-button targets stay alive and clicks keep working. */
    @Suppress("unused")
    private val itemBindings: NavBarItemBindings =
        applyEntryMetadataToItem(this.navigationItem, metadata, density)

    init {
        // Read by UIKit when this controller is pushed inside a UITabBarController.
        hidesBottomBarWhenPushed = !metadata.tabBarShown
        // UIKit sends no appearance callback when the app leaves the foreground; the entry's
        // lifecycle follows the app through this instead.
        IosAppForegroundState.subscribe(appStateListener)
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
                        LocalNavEntryScrollReporter provides scrollMirror::reportScrollOffset,
                    ) {
                        // The theme's background under the content, and in its place during the
                        // turn an animated push runs before the content attaches: the surface
                        // would otherwise show the system background sliding in.
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                            if (contentAttached.value) {
                                entry.Content()
                            }
                        }
                    }
                }
            }
        }

        // addChildViewController sends willMoveToParentViewController itself; only didMove is ours.
        addChildViewController(composeChild)
        composeChild.view.setFrame(view.bounds)
        composeChild.view.autoresizingMask =
            UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        view.addSubview(composeChild.view)
        composeChild.didMoveToParentViewController(this)
        // After the Compose view: the mirror must stay above it (see LargeTitleScrollMirror).
        scrollMirror.install(this)
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        attachContent(deferred = animated)
        navigationController?.let { nav ->
            // setNavigationBarHidden(animated:) coordinates with the push/pop
            // transition, so per-entry barShown animates cleanly.
            nav.setNavigationBarHidden(!metadata.barShown, animated)
            nav.navigationBar.translucent = metadata.iosConfiguration.isTranslucent
        }
    }

    private fun attachContent(deferred: Boolean) {
        if (contentAttached.value) return
        if (deferred) {
            dispatch_async(dispatch_get_main_queue()) { contentAttached.value = true }
        } else {
            // Nothing animates (root entry, tab switch): the first frame should be complete.
            contentAttached.value = true
        }
    }

    // Safe-area changes (bar shown/hidden, large-title collapse, rotation) normally come
    // with a layout pass. The exception is a change made from inside one, like the mirror
    // expanding a collapsed bar; the mirror reports those.
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        // Keeps the rest position on the expanded bar as insets settle.
        scrollMirror.layout()
        // After the offset: that is what (re)calibrates the inset the padding is built from.
        updateSafeAreaPadding()
    }

    private fun updateSafeAreaPadding() {
        safeAreaPadding.value = view.safeAreaInsets.useContents {
            PaddingValues.Absolute(
                left = left.dp,
                // Not the live inset: it shrinks with the large title, see NavBarInsetCalibration.
                top = scrollMirror.contentTopInset(liveTopInset = top).dp,
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
        entryScope.markStopped()
    }

    /**
     * Sets the lifecycle from whether the view is on screen. A split view moves controllers between
     * its columns when it collapses or expands, and UIKit's appearance callbacks are not balanced
     * around such a move (it logs "Unbalanced calls to begin/end appearance transitions"), so an
     * entry that is visible afterwards can be left stopped. The host calls this once the move has
     * settled.
     */
    fun refreshLifecycle() {
        if (isDisposed) return
        val isOnScreen = isViewLoaded() && view.window != null
        if (isOnScreen) entryScope.markStarted() else entryScope.markStopped()
    }

    /**
     * Tears the entry down: clears its `ViewModelStore`, destroys its lifecycle
     * and removes the Compose surface. Idempotent. Called by the host for every
     * entry that left the back stack: popped natively, popped or replaced from
     * Compose, dropped while covered, or the whole navigator leaving the
     * composition.
     */
    fun disposeEntry() {
        isDisposed = true
        IosAppForegroundState.unsubscribe(appStateListener)
        entryScope.markDestroyed()
        if (isViewLoaded()) {
            cleanupChildren()
        }
    }

    private fun cleanupChildren() {
        // removeFromParentViewController does not send willMove(null); the caller has to.
        childViewControllers.forEach { (it as UIViewController).willMoveToParentViewController(null) }
        view.subviews.forEach { (it as UIView).removeFromSuperview() }
        childViewControllers.forEach { (it as UIViewController).removeFromParentViewController() }
    }
}
