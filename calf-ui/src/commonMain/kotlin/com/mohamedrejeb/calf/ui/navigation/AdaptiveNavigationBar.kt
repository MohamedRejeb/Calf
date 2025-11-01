package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * An adaptive navigation bar that uses UIKit bottom bar for iOS and Material3 bottom bar for other platforms.
 *
 * @param modifier The modifier to be applied to the navigation bar.
 * @param containerColor The color used for the background of this navigation bar.
 * @param contentColor The preferred color for content inside this navigation bar.
 * @param content The content of the navigation bar, typically [AdaptiveNavigationBarItem]s.
 */
@ExperimentalCalfUiApi
@Composable
expect fun AdaptiveNavigationBar(
    onItemChanged: (String) -> Unit,
    iosPaddingValues: (PaddingValues) -> Unit,
    iosItems: List<UIKitUITabBarItem>,
    iosSelectedIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit
)
