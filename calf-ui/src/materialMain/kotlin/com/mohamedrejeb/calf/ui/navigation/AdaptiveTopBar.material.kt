package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * Material3 implementation of [AdaptiveTopBar] using Material3 TopAppBar.
 *
 * The `ios*` parameters are ignored on Material platforms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveTopBar(
    modifier: Modifier,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    expandedHeight: Dp,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    scrollBehavior: TopAppBarScrollBehavior?,
    iosTitle: String,
    iosLeadingItems: List<UIKitUIBarButtonItem>,
    iosTrailingItems: List<UIKitUIBarButtonItem>,
    iosConfiguration: UIKitNavigationBarConfiguration,
) {
    TopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        expandedHeight = expandedHeight,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}
