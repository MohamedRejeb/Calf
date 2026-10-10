@file:OptIn(ExperimentalForeignApi::class, ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.IosSplitViewConfiguration
import com.mohamedrejeb.calf.navigation.kind
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.UIKit.UISplitViewController
import platform.UIKit.UISplitViewControllerColumn
import platform.UIKit.UISplitViewControllerDelegateProtocol
import platform.UIKit.UISplitViewControllerDisplayMode
import platform.UIKit.UISplitViewControllerDisplayModeButtonVisibility
import platform.UIKit.UISplitViewControllerStyle
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Hosts a real two-column [UISplitViewController] whose columns show one Compose [NavBackStack]:
 * the list side in the primary column, the detail side in the secondary one (see
 * [arrangeListDetail]). iOS decides when there is room for two columns and adapts them to the
 * device, which on iPhone Duo includes lining the primary column up with the fold.
 *
 * **Collapsing uses the compact column.** The split view owns three [IosNavColumn]s: primary,
 * secondary and compact. Just before it collapses, every entry controller is moved into the
 * compact one; just before it expands they are moved back. UIKit then never edits a stack of
 * ours, so the back stack stays the single source of truth in both layouts. The alternatives do
 * not hold up: without a compact column UIKit pushes the secondary `UINavigationController`
 * itself onto the primary stack, or resets the primary stack to its root.
 *
 * What a move has to respect, each learnt from a failing run:
 *  - it happens inside the two delegate callbacks, where UIKit has not flipped `isCollapsed` yet,
 *    hence the own [isCollapsed];
 *  - it is never animated (an animated `setViewControllers` in there froze the main thread);
 *  - a controller leaves its old column before it joins the new one;
 *  - native pops are not mirrored while controllers move, and entry lifecycles are set from what is
 *    on screen afterwards, because UIKit's appearance callbacks are not balanced around a move.
 *
 * Entries keep their [EntryHostingController] across moves, and with it the composition,
 * `rememberSaveable` state and ViewModels.
 */
internal class IosSplitController(
    private val backStack: NavBackStack<NavKey>,
    private val parentLocals: State<CompositionLocalContext>,
    private val entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) : IosStackHost {
    val uiSplitViewController = UISplitViewController(style = UISplitViewControllerStyle.UISplitViewControllerStyleDoubleColumn)

    override val rootViewController: UIViewController get() = uiSplitViewController

    private val primary = IosNavColumn(onDidShow = ::onColumnDidShow)
    private val secondary = IosNavColumn(onDidShow = ::onColumnDidShow)
    private val compact = IosNavColumn(onDidShow = ::onColumnDidShow)
    private val columns = listOf(primary, secondary, compact)

    /** What a column that is off screen, or has nothing to show, holds: UIKit wants a root. */
    private val emptyPrimary = UIViewController(nibName = null, bundle = null)
    private val emptySecondary = UIViewController(nibName = null, bundle = null)

    private val splitDelegate = SplitDelegate(
        onWillCollapse = { relocate(collapsed = true) },
        onWillExpand = { relocate(collapsed = false) },
        onDidChange = ::refreshLifecyclesOnceSettled,
    )

    /** Wrapper controllers in stack order. A list, not a map: the same key may appear twice. */
    private var entryControllers: List<Pair<NavKey, EntryHostingController>> = emptyList()
    private var lastSyncedSnapshot: List<NavKey> = emptyList()

    /** The layout UIKit is in, or is about to be in: the delegate tells us before `isCollapsed` does. */
    private var isCollapsed: Boolean = false

    /** True while controllers are being handed to the columns, when UIKit's view of them is partial. */
    private var isArranging: Boolean = false

    /** The placeholder shown for [Pair.first], the list entry it belongs to. */
    private var placeholder: Pair<EntryHostingController, EntryHostingController>? = null

    /** Controllers UIKit has reported at the end of a transition; see [entriesKeptAfterNativePop]. */
    private var acknowledged: Set<EntryHostingController> = emptySet()

    private val deferredDisposal = DeferredEntryDisposal()

    /**
     * True while the list column is hidden by us because the stack has no list entry. Tracked so that
     * showing it again only undoes our own hiding, never the user's (the sidebar button, a swipe).
     */
    private var isListColumnHiddenForMissingList: Boolean = false

    init {
        uiSplitViewController.delegate = splitDelegate
        uiSplitViewController.setViewController(primary.uiNavigationController, forColumn = UISplitViewControllerColumn.UISplitViewControllerColumnPrimary)
        uiSplitViewController.setViewController(secondary.uiNavigationController, forColumn = UISplitViewControllerColumn.UISplitViewControllerColumnSecondary)
        uiSplitViewController.setViewController(compact.uiNavigationController, forColumn = UISplitViewControllerColumn.UISplitViewControllerColumnCompact)
    }

    fun applyConfiguration(configuration: IosSplitViewConfiguration) {
        uiSplitViewController.applySplitViewConfiguration(configuration)
    }

    /** Makes the columns match [snapshot], the current Compose back stack. */
    override fun syncToUiStack(snapshot: List<NavKey>) {
        val isTransitioning = isArranging || columns.any { it.isTransitioning }
        when (val decision = decideNavChange(lastSyncedSnapshot, snapshot, isTransitioning)) {
            is NavChangeDecision.Revert -> {
                revertBackStack(decision.stack)
                return
            }
            NavChangeDecision.Accept -> if (snapshot == lastSyncedSnapshot) return
        }

        val diff = reconcileNavStack(entryControllers, snapshot, ::createController)
        entryControllers = diff.stack

        // A dropped controller that is on screen is animated out by UIKit first. The others never
        // get a callback for leaving, so they are torn down here.
        val visibleTops = visibleColumns().mapNotNull { it.topController }
        val (onScreen, covered) = diff.dropped.partition { it in visibleTops }
        covered.forEach { it.disposeEntry() }
        deferredDisposal.defer(onScreen)

        val animated = lastSyncedSnapshot.isNotEmpty()
        lastSyncedSnapshot = snapshot
        arrange(animated = animated)
    }

    private fun createController(key: NavKey): EntryHostingController =
        EntryHostingController(
            entry = entryProvider(key),
            parentLocals = parentLocals,
            density = density,
        )

    private fun visibleColumns(): List<IosNavColumn> =
        if (isCollapsed) listOf(compact) else listOf(primary, secondary)

    /** Hands every column what [arrangeListDetail] gives it for the current layout. */
    private fun arrange(animated: Boolean) {
        val arrangement = arrangeListDetail(
            stack = entryControllers.map { it.second },
            roleOf = { controller -> controller.paneRole.kind },
            paneCount = if (isCollapsed) ONE_PANE else TWO_PANES,
        )
        if (isCollapsed) releasePlaceholder() else showListColumnOnlyWhenSplit(arrangement.isSplit)
        val targets: List<Pair<IosNavColumn, List<UIViewController>>> =
            if (isCollapsed) {
                listOf(
                    primary to listOf(emptyPrimary),
                    secondary to listOf(emptySecondary),
                    compact to arrangement.compact,
                )
            } else {
                listOf(
                    compact to emptyList(),
                    primary to arrangement.primary.ifEmpty { listOf(emptyPrimary) },
                    secondary to arrangement.secondary.ifEmpty { listOf(placeholderFor(arrangement.placeholderList)) },
                )
            }

        isArranging = true
        releaseMovingControllers(targets)
        val animatedColumns = targets.count { (column, wanted) -> column.showIfChanged(wanted, animated) }
        isArranging = false
        // Nothing animates: no didShow is coming for what was just replaced.
        if (animatedColumns == 0) disposeControllersThatLeft()
    }

    /** The placeholder has no column while collapsed; it is rebuilt when two panes come back. */
    private fun releasePlaceholder() {
        placeholder?.second?.let { deferredDisposal.defer(listOf(it)) }
        placeholder = null
    }

    /**
     * Without a list entry the stack is not split: the wide column shows it, the list column hides,
     * and so does the bar button that would bring an empty list column back.
     */
    private fun showListColumnOnlyWhenSplit(isSplit: Boolean) {
        val listColumn = UISplitViewControllerColumn.UISplitViewControllerColumnPrimary
        if (!isSplit && !isListColumnHiddenForMissingList) {
            uiSplitViewController.hideColumn(listColumn)
            uiSplitViewController.displayModeButtonVisibility = UISplitViewControllerDisplayModeButtonVisibility.UISplitViewControllerDisplayModeButtonVisibilityNever
            isListColumnHiddenForMissingList = true
        } else if (isSplit && isListColumnHiddenForMissingList) {
            uiSplitViewController.displayModeButtonVisibility = UISplitViewControllerDisplayModeButtonVisibility.UISplitViewControllerDisplayModeButtonVisibilityAutomatic
            uiSplitViewController.showColumn(listColumn)
            isListColumnHiddenForMissingList = false
        }
    }

    /**
     * A controller can have one parent only: a column that still holds a controller another
     * column is about to show lets go of it first.
     */
    private fun releaseMovingControllers(targets: List<Pair<IosNavColumn, List<UIViewController>>>) {
        targets.forEach { (column, wanted) ->
            val current = column.controllers
            val losesToAnotherColumn = current.any { controller ->
                controller !in wanted && targets.any { (other, otherWanted) -> other !== column && controller in otherWanted }
            }
            if (losesToAnotherColumn) column.show(current.filter { it in wanted }, animated = false)
        }
    }

    /**
     * Returns whether it animates. Animates only a push or pop inside the column. Replacing a column's root (another item was
     * selected, the placeholder gives way to a detail) is not a navigation step and would animate
     * as a push that cannot be popped.
     */
    private fun IosNavColumn.showIfChanged(wanted: List<UIViewController>, animated: Boolean): Boolean {
        val current = controllers
        if (current == wanted) return false
        val keepsRoot = current.isNotEmpty() && wanted.isNotEmpty() && current.first() === wanted.first()
        val animates = animated && keepsRoot
        show(wanted, animated = animates)
        return animates
    }

    /**
     * The controller that fills the detail column while no detail is open. It is rebuilt whenever
     * it is needed again: it holds no state, and it is torn down once it leaves the column.
     */
    private fun placeholderFor(list: EntryHostingController?): UIViewController {
        val role = list?.paneRole as? AdaptivePaneRole.List ?: return emptySecondary
        placeholder
            ?.takeIf { (owner, controller) -> owner === list && controller in secondary.controllers }
            ?.let { return it.second }

        // Through adaptiveNavEntry so the placeholder gets the column's safe area like any entry:
        // on iOS 26 the list column floats over a full-width detail column, and a placeholder
        // centred on the whole column would sit under the list. The padding is handed to the
        // placeholder, not applied around it: applied around it, a background the placeholder
        // draws stops at the bars and the safe area, and the system background shows there.
        val controller = EntryHostingController(
            entry = adaptiveNavEntry(key = DetailPlaceholderKey) { padding -> role.detailPlaceholder(padding) },
            parentLocals = parentLocals,
            density = density,
        )
        placeholder?.second?.let { deferredDisposal.defer(listOf(it)) }
        placeholder = list to controller
        return controller
    }

    /** The split view is about to collapse or expand: move every entry to where it will be shown. */
    private fun relocate(collapsed: Boolean) {
        isCollapsed = collapsed
        arrange(animated = false)
        // A column that goes off screen mid-transition never gets its didShow.
        columns.forEach { it.resetTransition() }
    }

    /** One main-queue turn later UIKit's own, unbalanced appearance callbacks are through. */
    private fun refreshLifecyclesOnceSettled() {
        dispatch_async(dispatch_get_main_queue()) {
            entryControllers.forEach { (_, controller) -> controller.refreshLifecycle() }
            placeholder?.second?.refreshLifecycle()
        }
    }

    private fun onColumnDidShow() {
        if (isArranging) return
        disposeControllersThatLeft()
        mirrorNativePops()
    }

    private fun disposeControllersThatLeft() {
        val released = deferredDisposal.disposeReleased()
        if (placeholder?.second in released) placeholder = null
    }

    /**
     * A transition finished. If UIKit removed controllers on its own (back button, edge swipe), they
     * leave the back stack. The two columns pop on their own, so with two panes each side is cut by
     * itself ([entriesKeptAfterSplitPop]): popping a screen pushed over the list keeps an open
     * detail, popping the list takes its detail side with it.
     */
    private fun mirrorNativePops() {
        val shown = visibleColumns().flatMap { it.controllers }.filterIsInstance<EntryHostingController>().toSet()
        val controllers = entryControllers.map { it.second }
        val kept = if (isCollapsed) {
            entriesKeptAfterNativePop(controllers, shown, acknowledged)
        } else {
            entriesKeptAfterSplitPop(partitionListDetail(controllers) { it.paneRole.kind }, shown, acknowledged)
        }.toSet()
        acknowledged = (acknowledged + shown).intersect(kept)
        if (kept.size == entryControllers.size) return

        val previousKeys = entryControllers.map { it.first }
        val droppedIndices = entryControllers.indices.filter { entryControllers[it].second !in kept }
        droppedIndices.forEach { entryControllers[it].second.disposeEntry() }
        entryControllers = entryControllers.filter { (_, controller) -> controller in kept }

        // Only if Compose still holds what was on screen; otherwise it already
        // moved on and the next sync reconciles.
        if (backStack.toList() == previousKeys) {
            droppedIndices.asReversed().forEach { backStack.removeAt(it) }
        }
        val keys = entryControllers.map { it.first }
        // What is left is what UIKit shows, so its snapshot must no-op.
        lastSyncedSnapshot = keys
        // The other column may still hold what was cut, or now needs the placeholder.
        arrange(animated = false)
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

    override fun release() {
        entryControllers.forEach { (_, controller) -> controller.disposeEntry() }
        deferredDisposal.disposeAll()
        placeholder?.second?.disposeEntry()
        entryControllers = emptyList()
        acknowledged = emptySet()
        isListColumnHiddenForMissingList = false
        placeholder = null
        lastSyncedSnapshot = emptyList()
    }
}

/** Key of the synthetic entry that hosts a list's detail placeholder. It is never on a back stack. */
private data object DetailPlaceholderKey : NavKey

private class SplitDelegate(
    private val onWillCollapse: () -> Unit,
    private val onWillExpand: () -> Unit,
    private val onDidChange: () -> Unit,
) : NSObject(), UISplitViewControllerDelegateProtocol {
    // Both selectors map to the same Kotlin signature; the annotation keeps
    // them distinct for the Objective-C bridge.
    @ObjCSignatureOverride
    override fun splitViewController(
        svc: UISplitViewController,
        topColumnForCollapsingToProposedTopColumn: UISplitViewControllerColumn,
    ): UISplitViewControllerColumn {
        onWillCollapse()
        return UISplitViewControllerColumn.UISplitViewControllerColumnCompact
    }

    @ObjCSignatureOverride
    override fun splitViewController(
        svc: UISplitViewController,
        displayModeForExpandingToProposedDisplayMode: UISplitViewControllerDisplayMode,
    ): UISplitViewControllerDisplayMode {
        onWillExpand()
        return displayModeForExpandingToProposedDisplayMode
    }

    override fun splitViewControllerDidCollapse(svc: UISplitViewController) = onDidChange()

    override fun splitViewControllerDidExpand(svc: UISplitViewController) = onDidChange()
}

private const val ONE_PANE = 1
private const val TWO_PANES = 2
