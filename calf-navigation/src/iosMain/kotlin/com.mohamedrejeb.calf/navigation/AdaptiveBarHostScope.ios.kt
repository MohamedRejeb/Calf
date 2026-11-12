package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.navigation.internal.LocalNavEntrySafeArea
import com.mohamedrejeb.calf.navigation.internal.LocalNavEntryScrollReporter
import com.mohamedrejeb.calf.ui.navigation.LargeTitleScrollTracker

/**
 * iOS actual. The bar itself is owned by the parent `UINavigationController`,
 * but UIKit lays the entry's view out *edge-to-edge underneath* the
 * (translucent) bar, so the content still needs to be inset. The insets are
 * measured from the hosting `UIViewController`'s `safeAreaInsets` (UIKit
 * ground truth — includes the nav-bar extension, and updates when the bar
 * hides or changes height) and passed as the content [PaddingValues],
 * matching Material's `Scaffold` padding contract: apply it as
 * `contentPadding` on a `LazyColumn` and the list scrolls under the
 * translucent bar while its content starts below it.
 *
 * Material composable parameters (`title`, `navigationIcon`, `actions`, …)
 * are ignored on iOS — the iOS bar is configured via the
 * `iosTitle` / `iosLeadingItems` / `iosTrailingItems` / `iosConfiguration`
 * parameters of [adaptiveNavEntry].
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
    // When the bar is hidden the hosting controller's safe area shrinks to
    // the status bar automatically, so the same insets are correct in both
    // cases. The safeDrawing fallback covers entries composed outside an
    // EntryHostingController (e.g. tests).
    val padding = LocalNavEntrySafeArea.current ?: WindowInsets.safeDrawing.asPaddingValues()
    val scrollReporter = LocalNavEntryScrollReporter.current
    if (scrollReporter == null) {
        content(padding)
        return
    }
    // Mirrors the content's scroll into the hosting controller's scroll view,
    // which is what drives the native large-title collapse and scroll-edge
    // appearance. Any nested-scrolling child (LazyColumn, verticalScroll)
    // participates without extra setup.
    val scrollTracker = remember(scrollReporter) { LargeTitleScrollTracker(scrollReporter) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollTracker),
    ) {
        content(padding)
    }
}
