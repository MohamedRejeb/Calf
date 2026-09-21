package com.mohamedrejeb.calf.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.calf.navigation.internal.materialListDetail
import com.mohamedrejeb.calf.navigation.internal.MaterialTabScope
import com.mohamedrejeb.calf.navigation.internal.adaptiveMetadata
import com.mohamedrejeb.calf.navigation.internal.rememberMaterialTabScopes
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration

private const val TAB_ENTER_MILLIS = 220
private const val TAB_EXIT_MILLIS = 90

/**
 * Material actual: a `NavigationSuiteScaffold` (bottom bar, rail or drawer
 * chosen from the window size) around one `NavDisplay` per tab.
 *
 * Only the selected tab's `NavDisplay` is composed, inside a
 * `SaveableStateProvider` keyed by the tab, and every tab's entry decorators
 * are backed by a [MaterialTabScope] that outlives the switch. Leaving a tab
 * therefore keeps its stack, `rememberSaveable` state and ViewModels, and
 * returning restores them, matching UIKit's per-tab navigation controllers.
 *
 * Android back at a non-start tab's root returns to the start tab, per
 * platform convention; elsewhere the tab's own `NavDisplay` handles back.
 */
@ExperimentalCalfNavigationApi
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun <T : NavKey> AdaptiveTabDisplay(
    state: AdaptiveTabState<T>,
    tabs: List<AdaptiveTab<T>>,
    modifier: Modifier,
    entryDecorators: List<NavEntryDecorator<NavKey>>?,
    materialLayoutType: NavigationSuiteType?,
    materialColors: NavigationSuiteColors?,
    iosConfiguration: UIKitTabBarConfiguration,
    iosSidebarAdaptable: Boolean,
    listDetail: AdaptiveListDetailSettings?,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    state.requireMatching(tabs)

    val tabBarShown = state.backStack.lastOrNull()
        ?.let { entryProvider(it).adaptiveMetadata.tabBarShown }
        ?: true
    val scaffoldState = rememberNavigationSuiteScaffoldState(
        initialValue = if (tabBarShown) NavigationSuiteScaffoldValue.Visible else NavigationSuiteScaffoldValue.Hidden,
    )
    LaunchedEffect(scaffoldState, tabBarShown) {
        if (tabBarShown) scaffoldState.show() else scaffoldState.hide()
    }

    BackHandler(enabled = state.isAtNonStartTabRoot) { state.select(state.startTab) }

    val tabStateHolder = rememberSaveableStateHolder()
    val tabScopes = rememberMaterialTabScopes(state.tabs)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            tabs.forEach { tab ->
                item(
                    selected = tab.key == state.selectedTab,
                    onClick = { state.select(tab.key) },
                    icon = tab.icon,
                    label = tab.label,
                    alwaysShowLabel = tab.alwaysShowLabel,
                    badge = tab.badge?.let { badge -> { Badge { Text(badge) } } },
                )
            }
        },
        modifier = modifier,
        layoutType = materialLayoutType
            ?: NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
        navigationSuiteColors = materialColors ?: NavigationSuiteDefaults.colors(),
        state = scaffoldState,
    ) {
        AnimatedContent(
            targetState = state.selectedTab,
            transitionSpec = {
                fadeIn(tween(TAB_ENTER_MILLIS, delayMillis = TAB_EXIT_MILLIS)) togetherWith
                    fadeOut(tween(TAB_EXIT_MILLIS))
            },
            label = "AdaptiveTabDisplay",
        ) { tab ->
            tabStateHolder.SaveableStateProvider(tab) {
                TabContent(
                    scope = tabScopes.getValue(tab),
                    state = state,
                    tab = tab,
                    entryDecorators = entryDecorators,
                    listDetail = listDetail != null,
                    entryProvider = entryProvider,
                )
            }
        }
    }
}

@ExperimentalCalfNavigationApi
@Composable
private fun <T : NavKey> TabContent(
    scope: MaterialTabScope,
    state: AdaptiveTabState<T>,
    tab: T,
    entryDecorators: List<NavEntryDecorator<NavKey>>?,
    listDetail: Boolean,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    val backStack = state.backStackOf(tab)
    // A tab without a list entry makes the list-detail scene decline, which leaves a plain stack.
    val material = if (listDetail) materialListDetail(backStack, entryProvider) else null
    CompositionLocalProvider(LocalViewModelStoreOwner provides scope.viewModelStoreOwner) {
        val decorators = entryDecorators ?: listOf(
            rememberSaveableStateHolderNavEntryDecorator(scope.entryStateHolder),
            rememberViewModelStoreNavEntryDecorator(),
        )
        if (material == null) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        } else {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = decorators,
                sceneStrategies = material.sceneStrategies,
                entryProvider = material.entryProvider,
            )
        }
    }
}
