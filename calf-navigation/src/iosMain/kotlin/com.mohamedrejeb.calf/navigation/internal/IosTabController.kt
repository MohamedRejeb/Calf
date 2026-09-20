@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptiveTab
import com.mohamedrejeb.calf.navigation.IosTabItem
import com.mohamedrejeb.calf.navigation.AdaptiveTabState
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.navigation.applyTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.toUITabBarItem
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
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
 * Each tab's stack is mirrored by its own [IosNavController].
 */
@OptIn(ExperimentalCalfNavigationApi::class)
internal class IosTabController<T : NavKey>(
    private val state: AdaptiveTabState<T>,
    tabs: List<AdaptiveTab<T>>,
    sidebarAdaptable: Boolean,
    parentLocals: State<CompositionLocalContext>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    private val density: Float,
) {
    val uiTabBarController = UITabBarController()

    private val delegate = TabBarControllerDelegate(::onUiKitSelected)
    private val navControllers: Map<T, IosNavController> = state.tabs.associateWith { tab ->
        IosNavController(
            backStack = state.backStackOf(tab),
            parentLocals = parentLocals,
            entryProvider = entryProvider,
            density = density,
        )
    }
    private val defaultTintColor: UIColor = uiTabBarController.tabBar.tintColor
    private val defaultUnselectedItemTintColor: UIColor? = uiTabBarController.tabBar.unselectedItemTintColor

    init {
        updateTabs(tabs.map { it.iosItem })
        uiTabBarController.setViewControllers(
            state.tabs.map { navControllers.getValue(it).uiNavigationController },
            animated = false,
        )
        uiTabBarController.delegate = delegate
        keepClassicBarBackground()
        setSidebarAdaptable(sidebarAdaptable)
        select(state.selectedTab)
    }

    /** Rebuilds the native tab bar items (titles, images, badges), in [AdaptiveTabState.tabs] order. */
    fun updateTabs(items: List<IosTabItem>) {
        items.forEachIndexed { index, item ->
            navControllers.getValue(state.tabs[index]).uiNavigationController.tabBarItem = item.toUITabBarItem(index, density)
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
        uiTabBarController.tabBar.applyTabBarConfiguration(
            configuration = configuration,
            defaultTintColor = defaultTintColor,
            defaultUnselectedItemTintColor = defaultUnselectedItemTintColor,
        )
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
        navControllers.getValue(tab).syncToUiStack(snapshot)
    }

    /** UIKit → Compose: the user tapped a tab. Re-selecting pops natively; the nav host mirrors it. */
    private fun onUiKitSelected(selected: UIViewController) {
        val tab = navControllers.entries.firstOrNull { it.value.uiNavigationController === selected }?.key ?: return
        if (tab != state.selectedTab) state.selectFromPlatform(tab)
    }

    fun release() {
        navControllers.values.forEach { it.release() }
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
