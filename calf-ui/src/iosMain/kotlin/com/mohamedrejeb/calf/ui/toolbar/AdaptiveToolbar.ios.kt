package com.mohamedrejeb.calf.ui.toolbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveBarHostUIViewController
import com.mohamedrejeb.calf.ui.navigation.PublishHostedBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.currentHostedScaffold
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection

/**
 * iOS implementation of [AdaptiveToolbar].
 *
 * Shows a native UIToolbar (Liquid Glass on iOS 26+) built from [iosItems], and nothing when
 * [iosItems] is empty. Inside an [AdaptiveBarHostUIViewController] the items go to the host
 * container's own toolbar. The Material floating toolbar parameters ([colors], [contentPadding],
 * [scrollBehavior], [shape], [leadingContent], [trailingContent], [expandedShadowElevation],
 * [collapsedShadowElevation] and [content]) are ignored here.
 */
@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveToolbar(
    expanded: Boolean,
    modifier: Modifier,
    colors: FloatingToolbarColors,
    contentPadding: PaddingValues,
    scrollBehavior: FloatingToolbarScrollBehavior?,
    shape: Shape,
    leadingContent: @Composable (RowScope.() -> Unit)?,
    trailingContent: @Composable (RowScope.() -> Unit)?,
    expandedShadowElevation: Dp,
    collapsedShadowElevation: Dp,
    content: @Composable RowScope.() -> Unit,
    iosItems: List<UIKitUIBarButtonItem>,
) {
    if (iosItems.isEmpty())
        return

    val hostedScaffold = currentHostedScaffold()
    if (hostedScaffold != null) {
        // Inside an AdaptiveBarHostUIViewController the container's toolbar shows the items.
        PublishHostedBar(
            scaffold = hostedScaffold,
            publish = { it.copy(toolbarItems = iosItems) },
            withdraw = { it.copy(toolbarItems = null) },
        )
        return
    }

    val viewController = LocalUIViewController.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val toolbarManager = remember {
        ToolbarManager()
    }

    DisposableEffect(viewController) {
        toolbarManager.attachTo(viewController.view)
        onDispose { toolbarManager.detach() }
    }

    LaunchedEffect(layoutDirection) {
        toolbarManager.applyLayoutDirection(layoutDirection)
    }

    LaunchedEffect(iosItems) {
        toolbarManager.update(items = iosItems, density = density.density)
    }
}
