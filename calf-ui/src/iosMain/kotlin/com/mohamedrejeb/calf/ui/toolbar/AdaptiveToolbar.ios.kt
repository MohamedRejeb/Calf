package com.mohamedrejeb.calf.ui.toolbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

/**
 * iOS implementation of [AdaptiveToolbar].
 *
 * On iOS 26+, uses a native UIToolbar with Liquid Glass effects when [iosItems] is provided.
 * On older iOS versions or when [iosItems] is empty, falls back to Material 3 Expressive
 * [HorizontalFloatingToolbar].
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveToolbar(
    expanded: Boolean,
    modifier: Modifier,
    leadingContent: @Composable (RowScope.() -> Unit)?,
    trailingContent: @Composable (RowScope.() -> Unit)?,
    content: @Composable RowScope.() -> Unit,
    iosItems: List<UIKitUIBarButtonItem>,
) {
    val isLiquidGlassAvailable = remember { isIOS26OrAbove() }

    if (isLiquidGlassAvailable && iosItems.isNotEmpty()) {
        NativeLiquidGlassToolbar(iosItems = iosItems)
    } else {
        HorizontalFloatingToolbar(
            expanded = expanded,
            modifier = modifier,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            content = content,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NativeLiquidGlassToolbar(iosItems: List<UIKitUIBarButtonItem>) {
    val viewController = LocalUIViewController.current

    val toolbarManager = remember {
        ToolbarManager()
    }

    DisposableEffect(viewController) {
        toolbarManager.attachTo(viewController.view)
        onDispose { toolbarManager.detach() }
    }

    LaunchedEffect(iosItems) {
        toolbarManager.update(items = iosItems)
    }
}
