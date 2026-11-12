package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration

/**
 * State of an [AdaptiveTabDisplay]: the ordered tabs, the selected one, and
 * one [NavBackStack] per tab whose first entry is the tab's own key.
 *
 * Every tab keeps its own stack alive while another tab is selected, on every
 * platform, so scroll positions, ViewModels and pushed screens are still there
 * when the user comes back. Mutate a tab's stack through [backStackOf] (or
 * [backStack] for the selected tab); the platform host follows.
 */
@ExperimentalCalfNavigationApi
@Stable
class AdaptiveTabState<T : NavKey> internal constructor(
    /** Tabs in bar order. */
    val tabs: List<T>,
    /** The tab shown first, and the one Android's back returns to from another tab's root. */
    val startTab: T,
    private val selectedIndexState: MutableIntState,
    private val backStacks: Map<T, NavBackStack<NavKey>>,
) {
    init {
        require(tabs.isNotEmpty()) { "AdaptiveTabState needs at least one tab." }
        require(tabs.distinct().size == tabs.size) { "Tabs must be distinct, got $tabs." }
        require(startTab in tabs) { "startTab $startTab is not one of $tabs." }
        require(selectedIndexState.intValue in tabs.indices) {
            "Selected index ${selectedIndexState.intValue} is out of range for $tabs."
        }
        require(backStacks.keys == tabs.toSet()) { "Every tab needs a back stack; got ${backStacks.keys} for $tabs." }
    }

    /** The currently selected tab. */
    val selectedTab: T
        get() = tabs[selectedIndexState.intValue]

    /** The selected tab's back stack. */
    val backStack: NavBackStack<NavKey>
        get() = backStackOf(selectedTab)

    /** The back stack of [tab]; its first entry is always [tab] itself. */
    fun backStackOf(tab: T): NavBackStack<NavKey> =
        requireNotNull(backStacks[tab]) { "$tab is not one of $tabs." }

    /**
     * Selects [tab]. Selecting the tab that is already selected pops its stack
     * back to the root, which is what a native iOS tab bar does.
     */
    fun select(tab: T) {
        require(tab in tabs) { "$tab is not one of $tabs." }
        if (tab == selectedTab) {
            popToRoot(tab)
        } else {
            selectedIndexState.intValue = tabs.indexOf(tab)
        }
    }

    /** Trims [tab]'s stack down to its root entry. */
    fun popToRoot(tab: T = selectedTab) {
        val stack = backStackOf(tab)
        while (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
        }
    }

    /** Mirrors a selection the platform already performed (native tab tap); never pops. */
    internal fun selectFromPlatform(tab: T) {
        require(tab in tabs) { "$tab is not one of $tabs." }
        selectedIndexState.intValue = tabs.indexOf(tab)
    }

    /**
     * True when the selected tab is not [startTab] and its stack is at the
     * root — the state in which Android's back returns to the start tab.
     */
    internal val isAtNonStartTabRoot: Boolean
        get() = selectedTab != startTab && backStack.size <= 1
}

/**
 * Remembers an [AdaptiveTabState] for [tabs], starting on [startTab].
 *
 * The selected tab is always saved across configuration changes. With a
 * [configuration], every tab's back stack is persisted as well, under the same
 * contract as [rememberAdaptiveNavBackStack]: register each [NavKey] subtype
 * in its serializers module. Pass the same value on every call.
 */
@ExperimentalCalfNavigationApi
@Composable
fun <T : NavKey> rememberAdaptiveTabState(
    vararg tabs: T,
    startTab: T = tabs.first(),
    configuration: SavedStateConfiguration? = null,
): AdaptiveTabState<T> {
    val tabList = tabs.toList()
    val startIndex = tabList.indexOf(startTab)
    // Tied to the tab list: an index saved for another list (a tab removed at runtime, or before
    // the process was restored) may be out of range, so the selection restarts from startTab.
    val selectedIndex = rememberSaveable(
        tabList,
        saver = Saver(
            save = { it.intValue },
            restore = { saved -> mutableIntStateOf(if (saved in tabList.indices) saved else startIndex) },
        ),
    ) { mutableIntStateOf(startIndex) }
    val backStacks = tabList.associateWith { tab ->
        key(tab) { rememberAdaptiveNavBackStack(tab, configuration = configuration) }
    }
    return remember(tabList, startTab, configuration) {
        AdaptiveTabState(
            tabs = tabList,
            startTab = startTab,
            selectedIndexState = selectedIndex,
            backStacks = backStacks,
        )
    }
}

/** Fails fast when the bar items passed to a display do not line up with the state's tabs. */
@ExperimentalCalfNavigationApi
internal fun <T : NavKey> AdaptiveTabState<T>.requireMatching(tabs: List<AdaptiveTab<T>>) {
    val keys = tabs.map { it.key }
    require(keys == this.tabs) {
        "AdaptiveTabDisplay tabs $keys must match the state's tabs ${this.tabs}, in the same order."
    }
}
