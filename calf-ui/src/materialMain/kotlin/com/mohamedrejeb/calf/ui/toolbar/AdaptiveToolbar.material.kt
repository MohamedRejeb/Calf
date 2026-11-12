package com.mohamedrejeb.calf.ui.toolbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * Material3 Expressive implementation of [AdaptiveToolbar] using [HorizontalFloatingToolbar].
 *
 * The [iosItems] parameter is ignored on Material platforms.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    HorizontalFloatingToolbar(
        expanded = expanded,
        modifier = modifier,
        colors = colors,
        contentPadding = contentPadding,
        scrollBehavior = scrollBehavior,
        shape = shape,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        expandedShadowElevation = expandedShadowElevation,
        collapsedShadowElevation = collapsedShadowElevation,
        content = content,
    )
}
