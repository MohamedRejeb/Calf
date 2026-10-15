package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UINavigationBar

/**
 * iOS implementation of [AdaptiveTopBar].
 *
 * This implementation uses UIKit's UINavigationBar to create a native iOS navigation bar. Inside an
 * [AdaptiveBarHostUIViewController] the bar is the host container's own navigation bar instead.
 * The Material3 parameters ([title], [navigationIcon], [actions], [contentPadding]...) are ignored
 * on iOS — the bar is built from [iosTitle], [iosLeadingItems], and [iosTrailingItems].
 */
@OptIn(
    ExperimentalForeignApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
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
    contentPadding: PaddingValues,
    iosTitle: String,
    iosLeadingItems: List<UIKitUIBarButtonItem>,
    iosTrailingItems: List<UIKitUIBarButtonItem>,
    iosConfiguration: UIKitNavigationBarConfiguration,
) {
    val hostedScaffold = currentHostedScaffold()
    if (hostedScaffold != null) {
        PublishTopBar(
            scaffold = hostedScaffold,
            bar = HostedTopBar(
                title = iosTitle,
                leadingItems = iosLeadingItems,
                trailingItems = iosTrailingItems,
                configuration = iosConfiguration,
            ),
        )
        return
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val navBarView = remember {
        UINavigationBar().apply {
            translatesAutoresizingMaskIntoConstraints = false
        }
    }
    val isLiquidGlassEnabled = remember { isIOS26OrAbove() }

    val topBarManager = remember(navBarView) {
        TopBarManager(
            navBar = navBarView,
        )
    }

    LaunchedEffect(layoutDirection) {
        navBarView.applyLayoutDirection(layoutDirection)
    }

    LaunchedEffect(iosTitle, iosLeadingItems, iosTrailingItems, iosConfiguration) {
        topBarManager.update(
            title = iosTitle,
            leadingItems = iosLeadingItems,
            trailingItems = iosTrailingItems,
            configuration = iosConfiguration,
            isLiquidGlassEnabled = isLiquidGlassEnabled,
            density = density.density,
        )
    }

    // Pins the bar under the status bar and re-measures it whenever UIKit lays it out again
    // (rotation, large title, safe-area change). What it claims is published to
    // LocalIosNativeBarInsets, so AdaptiveScaffold keeps the content below the bar.
    val navBarLayout = rememberIosBarLayout(
        barView = navBarView,
        edge = IosBarEdge.Top,
        kind = if (iosConfiguration.prefersLargeTitles)
            IosBarKind.LargeTitleNavigationBar
        else
            IosBarKind.NavigationBar,
        barCoversSafeArea = false,
        // With no status bar above it (iPhone landscape) a Liquid Glass bar floats below the
        // screen edge, like UINavigationController's does.
        minimumEdgeOffset = iosTopBarMinimumOffset(isLiquidGlassEnabled),
    )

    IosBarPlaceholder(layout = { navBarLayout.value })
}

/** Inside an [AdaptiveBarHostUIViewController] the container's navigation bar shows [bar]. */
@Composable
private fun PublishTopBar(scaffold: HostedScaffold, bar: HostedTopBar) {
    PublishHostedBar(
        scaffold = scaffold,
        publish = { it.copy(topBar = bar) },
        withdraw = { it.copy(topBar = null) },
    )
}
