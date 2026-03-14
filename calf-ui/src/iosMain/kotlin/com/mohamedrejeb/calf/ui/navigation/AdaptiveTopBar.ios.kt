package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIDevice
import platform.UIKit.UINavigationBar

/**
 * iOS implementation of [AdaptiveTopBar].
 *
 * This implementation uses UIKit's UINavigationBar to create a native iOS navigation bar.
 * The Material3 parameters ([title], [navigationIcon], [actions]) are ignored on iOS —
 * the bar is built from [iosTitle], [iosLeadingItems], and [iosTrailingItems].
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
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
    val density = LocalDensity.current
    val viewController = LocalUIViewController.current
    val navBarView = remember {
        UINavigationBar().apply {
            translatesAutoresizingMaskIntoConstraints = false
        }
    }
    val isLiquidGlassEnabled = remember {
        val systemVersion = UIDevice.currentDevice.systemVersion.toDouble()
        systemVersion >= 26.0
    }

    val topBarManager = remember(navBarView) {
        TopBarManager(
            navBar = navBarView,
        )
    }

    LaunchedEffect(iosTitle, iosLeadingItems, iosTrailingItems, iosConfiguration) {
        topBarManager.update(
            title = iosTitle,
            leadingItems = iosLeadingItems,
            trailingItems = iosTrailingItems,
            configuration = iosConfiguration,
            isLiquidGlassEnabled = isLiquidGlassEnabled,
        )
    }

    var topLeft by remember { mutableStateOf(DpOffset.Zero) }
    var positionInRoot by remember { mutableStateOf(DpOffset.Zero) }
    var navBarWidth by remember { mutableStateOf(0.dp) }
    var navBarHeight by remember { mutableStateOf(0.dp) }

    DisposableEffect(navBarView, viewController) {
        viewController.view.addSubview(navBarView)

        NSLayoutConstraint.activateConstraints(
            listOf(
                navBarView.leadingAnchor.constraintEqualToAnchor(viewController.view.leadingAnchor),
                navBarView.trailingAnchor.constraintEqualToAnchor(viewController.view.trailingAnchor),
                navBarView.topAnchor.constraintEqualToAnchor(
                    viewController.view.safeAreaLayoutGuide.topAnchor
                ),
            )
        )

        onDispose {
            navBarView.removeFromSuperview()
        }
    }

    // Write the measured navigation bar height to the CompositionLocal so AdaptiveScaffold
    // can automatically adjust content padding on iOS.
    val iosTopBarPaddingState = LocalIosTopBarPadding.current

    LaunchedEffect(Unit) {
        var navBarHeightConsistencyCounter = 0

        while (true) {
            withFrameMillis { }

            navBarView.frame.useContents {
                topLeft = DpOffset(
                    x = origin.x.dp,
                    y = origin.y.dp,
                )
                navBarWidth = size.width.dp
                val safeAreaTop = viewController.view.safeAreaInsets.useContents { top.dp }
                val newNavBarHeight = size.height.dp + safeAreaTop
                if (navBarHeight != newNavBarHeight) {
                    navBarHeight = newNavBarHeight
                } else {
                    navBarHeightConsistencyCounter++
                }
            }

            if (navBarHeight.value > 0f && navBarHeightConsistencyCounter > 10)
                break
        }
    }

    // Update the CompositionLocal whenever the navigation bar height changes
    LaunchedEffect(navBarHeight) {
        if (navBarHeight.value > 0f) {
            iosTopBarPaddingState.value = PaddingValues(top = navBarHeight)
        }
    }

    // Clean up the padding when this composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            iosTopBarPaddingState.value = PaddingValues()
        }
    }

    Box(
        modifier = Modifier
            .onPlaced {
                val positionInRootPx = it.positionInRoot()
                positionInRoot = with(density) {
                    DpOffset(
                        x = positionInRootPx.x.toDp(),
                        y = positionInRootPx.y.toDp(),
                    )
                }
            }
            .graphicsLayer {
                translationX = (topLeft.x - positionInRoot.x).toPx()
                translationY = (topLeft.y - positionInRoot.y).toPx()
            }
            .width(navBarWidth)
            .height(navBarHeight)
    )
}
