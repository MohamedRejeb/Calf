@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UINavigationController
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.transitionCoordinator
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
 * flight (an interactive swipe, or a push still animating), so a Compose
 * change arriving mid-transition is deferred and applied from `didShow`.
 */
internal class IosNavController(
    private val backStack: NavBackStack<NavKey>,
    private val parentLocals: State<CompositionLocalContext>,
    private val entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) {
    private val gestureDelegate = PopGestureDelegate(::canPopInteractively)
    private val navigationDelegate = NavigationDelegate(::onUiKitDidShow)

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

    /** A Compose change arrived during a transition; re-sync from the next `didShow`. */
    private var syncPending: Boolean = false

    init {
        uiNavigationController.delegate = navigationDelegate
    }

    private fun canPopInteractively(): Boolean =
        uiNavigationController.viewControllers.size > 1 &&
            (uiNavigationController.topViewController as? EntryHostingController)?.gestureEnabled != false

    /**
     * Re-asserts swipe-back configuration. UIKit re-claims the recognizer's
     * delegate around transitions, so a one-time assignment ends up ignored:
     * "gesture disabled" entries would swipe back, and hidden-bar entries
     * would lose the gesture (UIKit's default delegate refuses it when the
     * bar is hidden; ours allows it).
     */
    private fun refreshPopGesture() {
        val recognizer = uiNavigationController.interactivePopGestureRecognizer ?: return
        recognizer.delegate = gestureDelegate
        recognizer.enabled = canPopInteractively()
    }

    /** Makes the UIKit stack match [snapshot], the current Compose back stack. */
    fun syncToUiStack(snapshot: List<NavKey>) {
        if (snapshot == lastSyncedSnapshot) return
        if (uiNavigationController.transitionCoordinator != null) {
            syncPending = true
            return
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
        refreshPopGesture()
        mirrorNativePops()
        if (syncPending) {
            syncPending = false
            syncToUiStack(backStack.toList())
        }
    }

    /**
     * Trims the Compose back stack to what UIKit kept. If Compose changed the
     * stack during the transition, the native pop still wins for the entries
     * UIKit removed; the deferred sync then pushes whatever Compose added on
     * top of the kept prefix.
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
    private val onDidShow: () -> Unit,
) : NSObject(), UINavigationControllerDelegateProtocol {
    override fun navigationController(
        navigationController: UINavigationController,
        didShowViewController: UIViewController,
        animated: Boolean,
    ) {
        onDidShow()
    }
}
