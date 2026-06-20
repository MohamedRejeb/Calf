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
import platform.darwin.NSObject

/**
 * Hosts a real iOS [UINavigationController] whose `viewControllers` array is
 * synchronized to the Compose [NavBackStack].
 *
 * **Stack synchronization is one-way per call:**
 *
 *  - **Compose → UIKit**: when [NavBackStack] mutates (push/pop programmatically),
 *    [syncToUiStack] reconciles the wrapper VCs against the new snapshot via
 *    [reconcileNavStack] (duplicate keys get distinct wrappers), then calls
 *    `setViewControllers(_, animated = true)`. UIKit performs the push/pop
 *    animation natively.
 *
 *  - **UIKit → Compose**: when the user swipes back or taps the system back
 *    button, the popped wrapper's `viewDidDisappear(animated:)` fires with
 *    `isMovingFromParentViewController() == true`, which calls
 *    [handleUikitPop]. That flips [isApplyingFromUikit] and pops the Compose
 *    back-stack; the resulting snapshot no-ops in [syncToUiStack] because it
 *    already matches [lastSyncedSnapshot].
 */
internal class IosNavController(
    private val backStack: NavBackStack<NavKey>,
    private val onBack: () -> Unit,
    private val parentLocals: State<CompositionLocalContext>,
    private val entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) {
    private val gestureDelegate = PopGestureDelegate {
        uiNavigationController.viewControllers.size > 1 &&
            (uiNavigationController.topViewController as? EntryHostingController)
                ?.gestureEnabled != false
    }

    val uiNavigationController: UINavigationController = object : UINavigationController(
        nibName = null,
        bundle = null,
    ) {
        override fun viewDidLoad() {
            super.viewDidLoad()
            // Bar-wide flag stays on; per-entry visibility is controlled via
            // each wrapper's navigationItem.largeTitleDisplayMode.
            navigationBar.prefersLargeTitles = true
            refreshPopGesture()
        }
    }

    /**
     * Re-asserts swipe-back configuration. Called after every transition
     * settles (each wrapper's `viewDidAppear`), because UIKit re-claims the
     * recognizer's delegate around transitions — a one-time assignment ends
     * up ignored, which both lets "gesture disabled" entries swipe back and
     * kills the gesture on hidden-bar entries (UIKit's default delegate
     * refuses it when the bar is hidden; ours allows it).
     */
    private fun refreshPopGesture() {
        val recognizer = uiNavigationController.interactivePopGestureRecognizer ?: return
        recognizer.delegate = gestureDelegate
        recognizer.enabled = uiNavigationController.viewControllers.size > 1 &&
            (uiNavigationController.topViewController as? EntryHostingController)
                ?.gestureEnabled != false
    }

    /** Wrapper VCs in stack order. A list (not a map) because the same [NavKey] may appear twice. */
    private var entryControllers: List<Pair<NavKey, EntryHostingController>> = emptyList()
    private var lastSyncedSnapshot: List<NavKey> = emptyList()
    private var isApplyingFromUikit: Boolean = false

    /**
     * Reconcile the UIKit nav stack with [snapshot] (the current Compose
     * back-stack). Reuses existing wrapper VCs for keys still in the stack.
     */
    fun syncToUiStack(snapshot: List<NavKey>) {
        if (isApplyingFromUikit) return
        if (snapshot == lastSyncedSnapshot) return

        val diff = reconcileNavStack(entryControllers, snapshot, ::createController)
        entryControllers = diff.stack

        // Wrappers dropped while covered never receive viewDidDisappear, so
        // their entry scopes must be torn down here. The visible top wrapper
        // is skipped: UIKit animates it out and its own viewDidDisappear
        // (isMovingFromParentViewController == true) destroys the scope after
        // the transition completes.
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

    private fun createController(key: NavKey): EntryHostingController {
        val entry = entryProvider(key)
        val barData = entry.metadata[IosNavBarDataKey] as? IosNavBarData ?: IosNavBarData.Default
        return EntryHostingController(
            entry = entry,
            barData = barData,
            parentLocals = parentLocals,
            density = density,
            onPermanentPop = ::handleUikitPop,
            onAppeared = ::refreshPopGesture,
        )
    }

    /**
     * Called when UIKit removed [controller] from the stack on its own
     * (swipe-back or system back button) and the wrapper finished
     * disappearing. Mirrors the pop into the Compose back-stack unless a
     * Compose-driven sync already accounted for it.
     */
    private fun handleUikitPop(controller: EntryHostingController) {
        val index = entryControllers.indexOfLast { it.second === controller }
        // Already reconciled away by a Compose-driven syncToUiStack: the pop
        // originated in Compose, nothing left to mirror.
        if (index == -1) return

        val key = entryControllers[index].first
        entryControllers = entryControllers.filterIndexed { i, _ -> i != index }

        // Only mirror top-of-stack pops whose key still matches the Compose
        // back-stack; anything else will be reconciled by the next sync.
        if (index != lastSyncedSnapshot.lastIndex || backStack.lastOrNull() != key) return

        isApplyingFromUikit = true
        try {
            onBack()
            // Anticipate the resulting Compose snapshot so the upcoming
            // syncToUiStack(_) call sees a no-op match.
            lastSyncedSnapshot = lastSyncedSnapshot.dropLast(1)
        } finally {
            isApplyingFromUikit = false
        }
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
    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean =
        canPop()
}
