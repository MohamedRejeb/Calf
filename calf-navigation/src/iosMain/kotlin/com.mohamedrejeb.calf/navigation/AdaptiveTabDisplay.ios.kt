@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class)

package com.mohamedrejeb.calf.navigation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.navigation.internal.IosTabController
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS actual: hosts a real `UITabBarController` with one
 * `UINavigationController` per tab. Selection and every tab's stack are
 * mirrored from Compose; native taps and pops flow back into [state].
 *
 * [entryDecorators], [materialLayoutType] and [materialColors] are Material
 * only and ignored here. With [listDetail], tabs whose root entry is a list
 * are hosted in a `UISplitViewController` inside the tab.
 */
@ExperimentalCalfNavigationApi
@OptIn(CalfIosOnlyApi::class)
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

    val updatedEntryProvider = rememberUpdatedState(entryProvider)
    val density = LocalDensity.current.density
    val parentLocals = rememberUpdatedState(currentCompositionLocalContext)

    val isListDetail = listDetail != null
    val host = remember(state, isListDetail) {
        IosTabController(
            state = state,
            tabs = tabs,
            sidebarAdaptable = iosSidebarAdaptable,
            listDetail = isListDetail,
            parentLocals = parentLocals,
            entryProvider = { updatedEntryProvider.value(it) },
            density = density,
        )
    }

    val iosItems = tabs.map { it.iosItem }
    LaunchedEffect(host, iosItems) { host.updateTabs(iosItems) }
    LaunchedEffect(host, iosSidebarAdaptable) { host.setSidebarAdaptable(iosSidebarAdaptable) }
    LaunchedEffect(host, iosConfiguration) { host.applyConfiguration(iosConfiguration) }
    val splitViewConfiguration = listDetail?.iosSplitView
    LaunchedEffect(host, splitViewConfiguration) {
        if (splitViewConfiguration != null) host.applySplitViewConfiguration(splitViewConfiguration)
    }

    LaunchedEffect(host) {
        snapshotFlow { state.selectedTab }.collect { tab -> host.select(tab) }
    }
    state.tabs.forEach { tab ->
        key(tab) {
            LaunchedEffect(host) {
                snapshotFlow { state.backStackOf(tab).toList() }.collect { snapshot -> host.syncStack(tab, snapshot) }
            }
        }
    }

    UIKitViewController(
        factory = { host.uiTabBarController },
        modifier = modifier,
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )

    DisposableEffect(host) { onDispose { host.release() } }
}
