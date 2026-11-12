package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Internal expect function that hosts a single navigation entry's content.
 *
 * Material implementation: wraps [content] in a `Scaffold(topBar = TopAppBar(...))`
 * (or `LargeTopAppBar` when [largeTitle] is true) using the composable slots,
 * or omits the bar entirely when [barShown] is false.
 *
 * iOS implementation: the bar is owned by the parent `UINavigationController`,
 * which lays content out edge-to-edge underneath it, so this scope passes the
 * safe-drawing insets (status bar + nav bar + home indicator) as the content
 * [PaddingValues].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal expect fun AdaptiveBarHostScope(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    expandedHeight: Dp,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    scrollBehavior: TopAppBarScrollBehavior?,
    largeTitle: Boolean,
    barShown: Boolean,
    content: @Composable (PaddingValues) -> Unit,
)
