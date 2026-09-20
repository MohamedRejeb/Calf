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
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // Write the measured tab bar height to the CompositionLocal so AdaptiveScaffold
    // can automatically adjust content padding on iOS.
    val iosTabBarPaddingState = LocalIosTabBarPadding.current

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

    LaunchedEffect(iosItems, iosSelectedIndex) {
        tabBarManager.setItems(iosItems, iosSelectedIndex, density.density)
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
        contentPaddingState = iosTabBarPaddingState,
    )

    IosBarPlaceholder(layout = { tabBarLayout.value })
}
