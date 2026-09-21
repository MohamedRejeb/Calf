@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UINavigationController
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * Hosts a real [UINavigationController] whose `viewControllers` mirror a
 * Compose [NavBackStack].
 *
 *  - **Compose → UIKit**: [syncToUiStack] reconciles the wrapper controllers
 *    against the new snapshot (duplicate keys get distinct wrappers) and calls
 *    `setViewControllers(_:animated:)`; UIKit animates the push or pop.
 *  - **UIKit → Compose**: after every transition UIKit calls the navigation
 *    delegate's `didShow`. If UIKit removed controllers on its own (back
 *    button, edge swipe, pop to root from a tab re-select), the Compose back
 *    stack is trimmed to what UIKit kept. Comparing whole stacks rather than
 *    listening for single pops handles multi-pops and cancelled swipes alike.
 *
 * UIKit does not allow `viewControllers` to change while a transition is in
 * flight (an interactive swipe, or a push still animating). A Compose change
 * arriving mid-transition is therefore *dropped*: the back stack is reverted
 * to what is on screen, the way Flutter ignores navigation while animating.
 * Transitions are tracked from this controller's own `willShow` / `didShow`
 * rather than `transitionCoordinator`, which also reports ancestors'
 * transitions and stays non-nil inside `didShow`.
 */
internal class IosNavController(
    private val backStack: NavBackStack<NavKey>,
    private val parentLocals: State<CompositionLocalContext>,
    private val entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) {
    /** Resolved once: the OS version cannot change while the app runs. */
    private val managedGestures: List<PopGestureKind> = managedPopGestures(::isIOSVersionAtLeast)
    private val gestureDelegate = PopGestureDelegate(::canPopTopEntry)
    private val navigationDelegate = NavigationDelegate(
        onWillShow = { isTransitioning = true },
        onDidShow = ::onUiKitDidShow,
    )

    val uiNavigationController: UINavigationController = object : UINavigationController(
        nibName = null,
        bundle = null,
    ) {
        override fun viewDidLoad() {
            super.viewDidLoad()
            // Bar-wide flag stays on; each wrapper's navigationItem decides
            // whether its own title is large.
            navigationBar.prefersLargeTitles = true
            refreshPopGesture()
        }
    }

    /** Wrapper controllers in stack order. A list, not a map: the same key may appear twice. */
    private var entryControllers: List<Pair<NavKey, EntryHostingController>> = emptyList()
    private var lastSyncedSnapshot: List<NavKey> = emptyList()

    /** True between this controller's `willShow` and `didShow`, including an interactive pop in flight. */
    private var isTransitioning: Boolean = false

    init {
        uiNavigationController.delegate = navigationDelegate
    }

    private fun canPopTopEntry(): Boolean =
        canPopInteractively(
            stackSize = uiNavigationController.viewControllers.size,
            topGestureEnabled = (uiNavigationController.topViewController as? EntryHostingController)?.gestureEnabled,
        )

    /**
     * Re-asserts swipe-back configuration on every recognizer the system has.
     * UIKit re-claims the edge recognizer's delegate around transitions, so a
     * one-time assignment ends up ignored: "gesture disabled" entries would
     * swipe back, and hidden-bar entries would lose the gesture (UIKit's
     * default delegate refuses it when the bar is hidden; ours allows it).
     *
     * The iOS 26 content recognizer is only switched on and off. It keeps
     * UIKit's delegate, which decides where in the content a swipe may start.
     */
    private fun refreshPopGesture() {
        val canPop = canPopTopEntry()
        managedGestures.forEach { kind ->
            when (kind) {
                PopGestureKind.Edge -> uiNavigationController.interactivePopGestureRecognizer?.let { recognizer ->
                    recognizer.delegate = gestureDelegate
                    recognizer.enabled = canPop
                }
                PopGestureKind.Content ->
                    uiNavigationController.interactiveContentPopGestureRecognizer?.enabled = canPop
            }
        }
    }

    /** Makes the UIKit stack match [snapshot], the current Compose back stack. */
    fun syncToUiStack(snapshot: List<NavKey>) {
        when (val decision = decideNavChange(lastSyncedSnapshot, snapshot, isTransitioning)) {
            is NavChangeDecision.Revert -> {
                revertBackStack(decision.stack)
                return
            }
            NavChangeDecision.Accept -> if (snapshot == lastSyncedSnapshot) return
        }

        val diff = reconcileNavStack(entryControllers, snapshot, ::createController)
        entryControllers = diff.stack

        // Wrappers dropped while covered never receive viewDidDisappear, so
        // their scopes are torn down here. The visible top is skipped: UIKit
        // animates it out and its own viewDidDisappear destroys the scope
        // once the transition completes.
        val visibleTop = uiNavigationController.topViewController
        diff.dropped.forEach { controller ->
            if (controller !== visibleTop) controller.disposeEntry()
        }

        val animated = lastSyncedSnapshot.isNotEmpty()
        lastSyncedSnapshot = snapshot
        uiNavigationController.setViewControllers(
            diff.stack.map { it.second },
            animated = animated,
        )
        refreshPopGesture()
    }

    private fun createController(key: NavKey): EntryHostingController =
        EntryHostingController(
            entry = entryProvider(key),
            parentLocals = parentLocals,
            density = density,
        )

    /**
     * A transition finished. UIKit only ever removes controllers from the top,
     * so whatever it kept is a prefix of [entryControllers]; anything after
     * that prefix was popped natively and is mirrored into the Compose stack.
     */
    private fun onUiKitDidShow() {
        isTransitioning = false
        refreshPopGesture()
        mirrorNativePops()
    }

    /**
     * Drops a change made while a transition runs by restoring the Compose
     * back stack to what UIKit shows. The resulting snapshot equals
     * [lastSyncedSnapshot], so the next sync is a no-op.
     */
    private fun revertBackStack(stack: List<NavKey>) {
        if (backStack.toList() == stack) return
        backStack.clear()
        backStack.addAll(stack)
    }

    /**
     * Trims the Compose back stack to what UIKit kept after a native pop
     * (back button, edge swipe, pop to root).
     */
    private fun mirrorNativePops() {
        val shown = uiNavigationController.viewControllers.filterIsInstance<UIViewController>().toSet()
        val remaining = entryControllers.takeWhile { (_, controller) -> controller in shown }
        if (remaining.size == entryControllers.size) return

        entryControllers.drop(remaining.size).forEach { (_, controller) -> controller.disposeEntry() }
        entryControllers = remaining

        val keys = remaining.map { it.first }
        // Trim only if Compose still holds what UIKit kept as a prefix;
        // otherwise Compose already moved on and the next sync reconciles.
        if (backStack.size > keys.size && backStack.take(keys.size) == keys) {
            while (backStack.size > keys.size) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
        // The trimmed stack is what UIKit shows, so its snapshot must no-op.
        lastSyncedSnapshot = keys
    }

    fun release() {
        entryControllers.forEach { (_, controller) -> controller.disposeEntry() }
        entryControllers = emptyList()
        lastSyncedSnapshot = emptyList()
    }
}

private class PopGestureDelegate(
    private val canPop: () -> Boolean,
) : NSObject(), UIGestureRecognizerDelegateProtocol {
    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean = canPop()
}

private class NavigationDelegate(
    private val onWillShow: () -> Unit,
    private val onDidShow: () -> Unit,
) : NSObject(), UINavigationControllerDelegateProtocol {
    // Both selectors map to the same Kotlin signature; the annotation keeps
    // them distinct for the Objective-C bridge.
    @ObjCSignatureOverride
    override fun navigationController(
        navigationController: UINavigationController,
        willShowViewController: UIViewController,
        animated: Boolean,
    ) {
        onWillShow()
    }

    @ObjCSignatureOverride
    override fun navigationController(
        navigationController: UINavigationController,
        didShowViewController: UIViewController,
        animated: Boolean,
    ) {
        onDidShow()
    }
}
