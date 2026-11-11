package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UITabBar

/**
 * iOS implementation of [AdaptiveNavigationBar].
 *
 * This implementation uses UIKit's UITabBar to create a native iOS tab bar.
 * The [content] lambda is ignored on iOS — items are built from [iosItems].
 *
 * Inside an [AdaptiveBarHostUIViewController] the tab bar is the host's `UITabBarController`,
 * for up to [MAX_HOSTED_TABS] items; with more, UIKit would move the rest behind its own "More"
 * tab, so the bar stays standalone.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveNavigationBar(
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    windowInsets: WindowInsets,
    iosItems: List<UIKitUITabBarItem>,
    iosSelectedIndex: Int,
    iosOnItemSelected: (Int) -> Unit,
    iosConfiguration: UIKitTabBarConfiguration,
    content: @Composable RowScope.() -> Unit
) {
    val hostedScaffold = currentHostedScaffold()
    if (hostedScaffold != null && iosItems.size in 1..MAX_HOSTED_TABS) {
        PublishTabBar(
            scaffold = hostedScaffold,
            bar = HostedTabBar(
                items = iosItems,
                selectedIndex = iosSelectedIndex,
                configuration = iosConfiguration,
            ),
            onItemSelected = iosOnItemSelected,
        )
        return
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val tabBarView = remember {
        UITabBar().apply {
            translatesAutoresizingMaskIntoConstraints = false
        }
    }
    val isLiquidGlassEnabled = remember { isIOS26OrAbove() }

    val onItemSelectedState by rememberUpdatedState(iosOnItemSelected)

    val tabBarManager = remember(tabBarView) {
        TabBarManager(
            tabBar = tabBarView,
            onItemSelected = { onItemSelectedState(it) },
        )
    }

    LaunchedEffect(layoutDirection) {
        tabBarView.applyLayoutDirection(layoutDirection)
    }

    LaunchedEffect(iosItems, iosSelectedIndex, density, layoutDirection) {
        tabBarManager.setItems(iosItems, iosSelectedIndex, density.density, layoutDirection)
    }

    SideEffect {
        tabBarManager.updateConfiguration(
            configuration = iosConfiguration,
        )
    }

    // Pins the bar to the bottom and re-measures it whenever UIKit lays it out again
    // (rotation, safe-area change). The Liquid Glass tab bar extends under the home
    // indicator, so its height already includes the bottom inset.
    val tabBarLayout = rememberIosBarLayout(
        barView = tabBarView,
        edge = IosBarEdge.Bottom,
        kind = IosBarKind.TabBar,
        barCoversSafeArea = isLiquidGlassEnabled,
    )

    IosBarPlaceholder(layout = { tabBarLayout.value })
}

/** Inside an [AdaptiveBarHostUIViewController] the container's tab bar shows [bar]. */
@Composable
private fun PublishTabBar(scaffold: HostedScaffold, bar: HostedTabBar, onItemSelected: (Int) -> Unit) {
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    SideEffect { scaffold.host.setTabSelection(scaffold) { currentOnItemSelected(it) } }
    PublishHostedBar(
        scaffold = scaffold,
        publish = { it.copy(tabBar = bar) },
        withdraw = { it.copy(tabBar = null) },
    )
}

/** The most tabs a `UITabBarController` shows on an iPhone without its "More" tab. */
private const val MAX_HOSTED_TABS = 5
