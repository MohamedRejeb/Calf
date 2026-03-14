package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * An adaptive top bar that uses UIKit's UINavigationBar on iOS and Material3's TopAppBar
 * on other platforms.
 *
 * On iOS, a native UINavigationBar is created using the [iosTitle], [iosLeadingItems],
 * [iosTrailingItems], and [iosConfiguration] parameters. The Material3 parameters
 * ([title], [navigationIcon], [actions]) are ignored on iOS.
 *
 * On Material platforms, the Material3 parameters are used to build a standard TopAppBar.
 * The `ios*` parameters are ignored on Material platforms.
 *
 * @param modifier The modifier to be applied to the top bar.
 * @param title The title composable for Material3 TopAppBar.
 * @param navigationIcon The navigation icon composable for Material3 TopAppBar.
 * @param actions The action icons composable for Material3 TopAppBar.
 * @param expandedHeight The expanded height of the Material3 TopAppBar.
 * @param windowInsets The window insets for the Material3 TopAppBar.
 * @param colors The colors for the Material3 TopAppBar.
 * @param scrollBehavior The scroll behavior for the Material3 TopAppBar.
 * @param iosTitle The title string for the iOS UINavigationBar.
 * @param iosLeadingItems The list of bar button items on the leading side of the iOS navigation bar.
 * @param iosTrailingItems The list of bar button items on the trailing side of the iOS navigation bar.
 * @param iosConfiguration Configuration for the iOS UINavigationBar appearance.
 */
@ExperimentalMaterial3Api
@ExperimentalCalfUiApi
@Composable
expect fun AdaptiveTopBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    iosTitle: String = "",
    iosLeadingItems: List<UIKitUIBarButtonItem> = emptyList(),
    iosTrailingItems: List<UIKitUIBarButtonItem> = emptyList(),
    iosConfiguration: UIKitNavigationBarConfiguration = UIKitNavigationBarConfiguration(),
)
