package com.mohamedrejeb.calf.ui.toolbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    leadingContent: @Composable (RowScope.() -> Unit)?,
    trailingContent: @Composable (RowScope.() -> Unit)?,
    content: @Composable RowScope.() -> Unit,
    iosItems: List<UIKitUIBarButtonItem>,
) {
    HorizontalFloatingToolbar(
        expanded = expanded,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        content = content,
    )
}
