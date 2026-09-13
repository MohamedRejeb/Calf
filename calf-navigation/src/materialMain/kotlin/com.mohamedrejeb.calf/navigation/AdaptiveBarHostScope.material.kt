package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mohamedrejeb.calf.navigation.internal.ReportNavTransition
import androidx.compose.ui.unit.Dp

/**
 * Material actual for the per-entry bar host scope.
 *
 * Renders a `Scaffold` with `TopAppBar` (or `LargeTopAppBar` when [largeTitle]
 * is true) populated from the composable slots, or a plain `Box` with
 * safe-drawing padding when [barShown] is false.
 *
 * When [largeTitle] is true and no [scrollBehavior] is supplied, an
 * `exitUntilCollapsedScrollBehavior` is created so the large title collapses
 * on scroll — mirroring iOS's native large-title shrink. The behavior's
 * nested-scroll connection is wired to the `Scaffold`, so scrollable content
 * inside drives the collapse with no extra setup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun AdaptiveBarHostScope(
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
) {
    ReportNavTransition()
    if (!barShown) {
        Box(Modifier.fillMaxSize()) {
            content(WindowInsets.safeDrawing.asPaddingValues())
        }
        return
    }

    val effectiveScrollBehavior = scrollBehavior
        ?: if (largeTitle) TopAppBarDefaults.exitUntilCollapsedScrollBehavior() else null

    Scaffold(
        modifier = if (effectiveScrollBehavior != null) {
            Modifier.nestedScroll(effectiveScrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        },
        topBar = {
            if (largeTitle) {
                LargeTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    expandedHeight = expandedHeight,
                    windowInsets = windowInsets,
                    colors = colors,
                    scrollBehavior = effectiveScrollBehavior,
                )
            } else {
                TopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    expandedHeight = expandedHeight,
                    windowInsets = windowInsets,
                    colors = colors,
                    scrollBehavior = effectiveScrollBehavior,
                )
            }
        },
        content = content,
    )
}
