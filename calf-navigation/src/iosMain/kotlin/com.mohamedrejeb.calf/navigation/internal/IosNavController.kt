@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

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
 *
 * The navigation controller itself, its swipe-back policy and its transition
 * tracking live in [IosNavColumn].
 */
internal class IosNavController(
    private val backStack: NavBackStack<NavKey>,
    private val parentLocals: State<CompositionLocalContext>,
    private val entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) : IosStackHost {
    private val column = IosNavColumn(onDidShow = ::onTransitionFinished)

    val uiNavigationController: UINavigationController get() = column.uiNavigationController

    override val rootViewController: UIViewController get() = uiNavigationController

    /** Wrapper controllers in stack order. A list, not a map: the same key may appear twice. */
    private var entryControllers: List<Pair<NavKey, EntryHostingController>> = emptyList()
    private var lastSyncedSnapshot: List<NavKey> = emptyList()

    /** Controllers UIKit has reported at the end of a transition; see [entriesKeptAfterNativePop]. */
    private var acknowledged: Set<EntryHostingController> = emptySet()

    private val deferredDisposal = DeferredEntryDisposal()

    /** Makes the UIKit stack match [snapshot], the current Compose back stack. */
    override fun syncToUiStack(snapshot: List<NavKey>) {
        when (val decision = decideNavChange(lastSyncedSnapshot, snapshot, column.isTransitioning)) {
            is NavChangeDecision.Revert -> {
                revertBackStack(decision.stack)
                return
            }
            NavChangeDecision.Accept -> if (snapshot == lastSyncedSnapshot) return
        }

        val diff = reconcileNavStack(entryControllers, snapshot, ::createController)
        entryControllers = diff.stack

        // Covered wrappers can be torn down at once. The visible top is
        // animated out by UIKit first and torn down when that transition has
        // finished.
        val visibleTop = column.topController
        val (onScreen, covered) = diff.dropped.partition { it === visibleTop }
        covered.forEach { it.disposeEntry() }
        deferredDisposal.defer(onScreen)

        val animated = lastSyncedSnapshot.isNotEmpty()
        lastSyncedSnapshot = snapshot
        column.show(diff.stack.map { it.second }, animated = animated)
        if (!animated) deferredDisposal.disposeReleased()
    }

    private fun createController(key: NavKey): EntryHostingController =
        EntryHostingController(
            entry = entryProvider(key),
            parentLocals = parentLocals,
            density = density,
        )

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

    private fun onTransitionFinished() {
        deferredDisposal.disposeReleased()
        mirrorNativePops()
    }

    /**
     * A transition finished. UIKit only ever removes controllers from the top,
     * so whatever it kept is a prefix of [entryControllers]; anything after
     * that prefix was popped natively (back button, edge swipe, pop to root)
     * and is trimmed from the Compose back stack.
     */
    private fun mirrorNativePops() {
        val shown = column.controllers.filterIsInstance<EntryHostingController>().toSet()
        val kept = entriesKeptAfterNativePop(entryControllers.map { it.second }, shown, acknowledged)
        val remaining = entryControllers.take(kept.size)
        acknowledged = (acknowledged + shown).intersect(kept.toSet())
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

    override fun release() {
        entryControllers.forEach { (_, controller) -> controller.disposeEntry() }
        deferredDisposal.disposeAll()
        entryControllers = emptyList()
        acknowledged = emptySet()
        lastSyncedSnapshot = emptyList()
    }
}
