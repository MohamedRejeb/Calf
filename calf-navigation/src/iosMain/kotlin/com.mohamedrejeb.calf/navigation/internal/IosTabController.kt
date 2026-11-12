@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.AdaptiveTab
import com.mohamedrejeb.calf.navigation.IosTabItem
import com.mohamedrejeb.calf.navigation.AdaptiveTabState
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.IosSplitViewConfiguration
import com.mohamedrejeb.calf.ui.navigation.TabBarConfigurationApplier
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.navigation.toUITabBarItem
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.UITabBarControllerModeAutomatic
import platform.UIKit.UITabBarControllerModeTabSidebar
import platform.UIKit.UITabBarItem
import platform.UIKit.UIViewController
import platform.UIKit.tabBarItem
import platform.darwin.NSObject

private const val SIDEBAR_MIN_IOS_VERSION = 18
private const val LIQUID_GLASS_MIN_IOS_VERSION = 26

/**
 * Hosts a real [UITabBarController] with one [IosNavController] per tab — the
 * hierarchy UIKit expects, so `hidesBottomBarWhenPushed`, tab-bar safe areas,
 * pop-to-root on re-select and pose adaptation on iPad and iPhone Duo are all
 * the system's.
 *
 * Selection flows both ways: Compose sets `selectedIndex` through [select],
 * and a native tab tap reaches the state through the tab controller delegate.
 * Each tab's stack is mirrored by its own [IosStackHost].
 *
 * With [listDetail], a tab whose root entry is a list gets an [IosSplitController] instead: the
 * split view goes *inside* the tab. That is the nesting UIKit adapts: on iPhone Duo the tab bar
 * joins the detail's bar items in the vertical strip, open and closed. Tabs inside a split view's
 * primary column keep a horizontal bar over the list and lose it altogether when the split view
 * collapses. The other tabs stay plain stacks, which would otherwise be squeezed into the narrow
 * list column on a wide screen.
 */
@OptIn(ExperimentalCalfNavigationApi::class)
internal class IosTabController<T : NavKey>(
    private val state: AdaptiveTabState<T>,
    tabs: List<AdaptiveTab<T>>,
    sidebarAdaptable: Boolean,
    listDetail: Boolean,
    parentLocals: State<CompositionLocalContext>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) {
    val uiTabBarController = UITabBarController()

    private val delegate = TabBarControllerDelegate(::onUiKitSelected)
    private val hosts: Map<T, IosStackHost> = state.tabs.associateWith { tab ->
        val isListTab = listDetail && entryProvider(tab).adaptiveMetadata.paneRole is AdaptivePaneRole.List
        if (isListTab) {
            IosSplitController(
                backStack = state.backStackOf(tab),
                parentLocals = parentLocals,
                entryProvider = entryProvider,
                density = density,
            )
        } else {
            IosNavController(
                backStack = state.backStackOf(tab),
                parentLocals = parentLocals,
                entryProvider = entryProvider,
                density = density,
            )
        }
    }
    private val tabBarConfigurationApplier: TabBarConfigurationApplier

    init {
        updateTabs(tabs.map { it.iosItem })
        uiTabBarController.setViewControllers(
            state.tabs.map { hosts.getValue(it).rootViewController },
            animated = false,
        )
        uiTabBarController.delegate = delegate
        keepClassicBarBackground()
        // After the classic background is in place: it is part of what a configuration going
        // back to its defaults restores.
        tabBarConfigurationApplier = TabBarConfigurationApplier(uiTabBarController.tabBar)
        setSidebarAdaptable(sidebarAdaptable)
        select(state.selectedTab)
    }

    /** Rebuilds the native tab bar items (titles, images, badges), in [AdaptiveTabState.tabs] order. */
    fun updateTabs(items: List<IosTabItem>) {
        items.forEachIndexed { index, item ->
            hosts.getValue(state.tabs[index]).rootViewController.tabBarItem = item.toUITabBarItem(index, density)
        }
    }

    /**
     * A classic (pre Liquid Glass) tab bar drops its background whenever UIKit believes the
     * content ends above it, which it decides from a scroll view reaching the bottom edge.
     * Compose content has no such scroll view, so the bar stayed transparent with the content
     * showing through the items. Using the standard appearance at the scroll edge too keeps
     * the background. iOS 26 draws its own glass bar and needs nothing.
     */
    private fun keepClassicBarBackground() {
        if (isIOSVersionAtLeast(LIQUID_GLASS_MIN_IOS_VERSION)) return
        val tabBar = uiTabBarController.tabBar
        tabBar.scrollEdgeAppearance = tabBar.standardAppearance
    }

    /** iOS 18+: lets the tab bar become a sidebar on wide displays. Older systems ignore it. */
    fun setSidebarAdaptable(enabled: Boolean) {
        if (!isIOSVersionAtLeast(SIDEBAR_MIN_IOS_VERSION)) return
        uiTabBarController.mode = if (enabled) UITabBarControllerModeTabSidebar else UITabBarControllerModeAutomatic
    }

    fun applyConfiguration(configuration: UIKitTabBarConfiguration) {
        tabBarConfigurationApplier.apply(configuration)
    }

    /** Compose → UIKit selection. */
    fun select(tab: T) {
        val index = state.tabs.indexOf(tab)
        if (index >= 0 && uiTabBarController.selectedIndex.toInt() != index) {
            uiTabBarController.selectedIndex = index.toULong()
        }
    }

    /** Compose → UIKit stack of one tab. */
    fun syncStack(tab: T, snapshot: List<NavKey>) {
        hosts.getValue(tab).syncToUiStack(snapshot)
    }

    /** The display composed again: every tab's entries are built anew. */
    fun refreshEntries(entryProvider: (NavKey) -> NavEntry<NavKey>) {
        hosts.values.forEach { it.refreshEntries(entryProvider) }
    }

    /** Applies the split view configuration to every list-detail tab. */
    fun applySplitViewConfiguration(configuration: IosSplitViewConfiguration) {
        hosts.values.filterIsInstance<IosSplitController>().forEach { it.applyConfiguration(configuration) }
    }

    /**
     * UIKit → Compose: the user tapped a tab. Re-selecting a navigation controller pops it natively
     * and the nav host mirrors that. UIKit has no such rule for a split view, so that pop is ours.
     */
    private fun onUiKitSelected(selected: UIViewController) {
        val (tab, host) = hosts.entries.firstOrNull { it.value.rootViewController === selected } ?: return
        when {
            tab != state.selectedTab -> state.selectFromPlatform(tab)
            host is IosSplitController -> state.popToRoot(tab)
        }
    }

    fun release() {
        hosts.values.forEach { it.release() }
    }
}

private fun IosTabItem.toUITabBarItem(index: Int, density: Float): UITabBarItem =
    UIKitUITabBarItem(title = title, image = image, selectedImage = selectedImage)
        .toUITabBarItem(tag = index.toLong(), density = density)
        .also { it.badgeValue = badge }

private class TabBarControllerDelegate(
    private val onSelected: (UIViewController) -> Unit,
) : NSObject(), UITabBarControllerDelegateProtocol {
    override fun tabBarController(tabBarController: UITabBarController, didSelectViewController: UIViewController) {
        onSelected(didSelectViewController)
    }
}
