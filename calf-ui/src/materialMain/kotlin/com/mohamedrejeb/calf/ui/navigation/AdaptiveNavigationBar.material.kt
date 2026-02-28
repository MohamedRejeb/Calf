package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * Material3 implementation of [AdaptiveNavigationBar] using Material3 NavigationBar.
 */
@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveNavigationBar(
    onItemChanged: (String) -> Unit,
    iosPaddingValues: (PaddingValues) -> Unit,
    iosItems: List<UIKitUITabBarItem>,
    iosSelectedIndex: Int,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    windowInsets: WindowInsets,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets,
        content = content
    )
}
