package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * An adaptive navigation bar that uses UIKit's UITabBar on iOS and Material3's NavigationBar
 * on other platforms.
 *
 * On iOS, the native UITabBar is created using the [iosItems], [iosSelectedIndex], and
 * [iosOnItemSelected] parameters. The [content] lambda is ignored on iOS.
 *
 * On Material platforms, the [content] lambda is used to provide full customization of the
 * NavigationBar content (typically [NavigationBarItem]s). The `ios*` parameters are ignored
 * on Material platforms.
 *
 * @param modifier The modifier to be applied to the navigation bar.
 * @param containerColor The color used for the background of this navigation bar.
 * @param contentColor The preferred color for content inside this navigation bar.
 * @param tonalElevation The tonal elevation of this navigation bar.
 * @param windowInsets The window insets to be applied to the navigation bar.
 * @param iosItems The list of tab bar items for the iOS UITabBar.
 * @param iosSelectedIndex The index of the currently selected item on iOS.
 * @param iosOnItemSelected Callback invoked when an item is selected on iOS, with the item index.
 * @param content The content of the navigation bar on Material platforms, typically
 *   [NavigationBarItem]s within a [RowScope].
 */
@ExperimentalCalfUiApi
@Composable
expect fun AdaptiveNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    iosItems: List<UIKitUITabBarItem> = emptyList(),
    iosSelectedIndex: Int = 0,
    iosOnItemSelected: (Int) -> Unit = {},
    content: @Composable RowScope.() -> Unit = {}
)
