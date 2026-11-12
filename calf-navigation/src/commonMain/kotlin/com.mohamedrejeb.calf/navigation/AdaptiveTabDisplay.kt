package com.mohamedrejeb.calf.navigation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration

/**
 * Tabbed navigation with one navigation stack per tab, rendered natively per
 * platform.
 *
 *  - **iOS** hosts a real `UITabBarController` whose children are one
 *    `UINavigationController` per tab, exactly the hierarchy UIKit expects.
 *    Pushed screens can hide the tab bar with the transition
 *    (`tabBarShown = false`), re-selecting a tab pops it to its root, the tab
 *    bar adapts to the device pose, and [iosSidebarAdaptable] opts into the
 *    sidebar on wide displays such as iPad and the inner display of
 *    iPhone Duo.
 *  - **Android / Desktop / Web** render a Material 3 `NavigationSuiteScaffold`
 *    that picks a bottom bar, rail or drawer from the window size, with one
 *    `NavDisplay` per tab.
 *
 * Each tab's stack stays alive while another tab is shown, so scroll
 * positions and ViewModels survive switching tabs on every platform.
 *
 * @param state the tab state, from [rememberAdaptiveTabState].
 * @param tabs bar items, one per tab of [state] in the same order (see [adaptiveTab]).
 * @param entryDecorators decorators applied to every tab's entries on Material
 *   platforms. `null` gives each tab its own saveable-state and ViewModel
 *   decorators, scoped so two tabs showing the same key do not share state.
 *   Not applied on iOS, where the native per-entry scopes provide the same.
 * @param materialLayoutType forces a `NavigationSuiteType` (bottom bar, rail,
 *   drawer) instead of choosing it from the window size.
 * @param materialColors colors of the Material navigation suite (`null` = defaults).
 * @param iosConfiguration appearance of the native iOS tab bar.
 * @param iosSidebarAdaptable lets the iOS tab bar become a sidebar on wide
 *   displays (iOS 18+). No effect on a compact phone screen.
 * @param listDetail lays out every tab whose root entry is an [AdaptivePaneRole.List] as
 *   list-detail, see [AdaptiveListDetailSettings]; the other tabs stay plain stacks. On iOS
 *   the split view sits inside the tab, so the tab bar stays reachable in both layouts.
 *   `null` keeps plain stacks everywhere.
 * @param entryProvider builds the entry for any key on any tab's stack; use
 *   [adaptiveNavEntry] so bars are painted natively.
 */
@ExperimentalCalfNavigationApi
@Composable
expect fun <T : NavKey> AdaptiveTabDisplay(
    state: AdaptiveTabState<T>,
    tabs: List<AdaptiveTab<T>>,
    modifier: Modifier = Modifier,
    entryDecorators: List<NavEntryDecorator<NavKey>>? = null,
    materialLayoutType: NavigationSuiteType? = null,
    materialColors: NavigationSuiteColors? = null,
    iosConfiguration: UIKitTabBarConfiguration = UIKitTabBarConfiguration(),
    iosSidebarAdaptable: Boolean = false,
    listDetail: AdaptiveListDetailSettings? = null,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
)
