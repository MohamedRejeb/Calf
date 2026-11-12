package com.mohamedrejeb.calf.ui.toolbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * An adaptive toolbar that uses a native UIToolbar with Liquid Glass on iOS 26+
 * and a Material 3 Expressive [HorizontalFloatingToolbar] on other platforms.
 *
 * This toolbar is intended for contextual action buttons (not navigation), similar
 * to a web browser's bottom toolbar with back, forward, share, and bookmark buttons.
 *
 * On iOS 26+, a native [UIToolbar] is displayed with the Liquid Glass appearance,
 * using the [iosItems] parameter. On older iOS versions and other platforms, the
 * Material 3 Expressive [HorizontalFloatingToolbar] is used with the composable
 * [content] parameter.
 *
 * @param expanded Whether the toolbar is in its expanded state.
 * @param modifier The modifier to be applied to the toolbar.
 * @param colors The container and content colors of the Material 3 Expressive floating toolbar.
 *   Material only; ignored on iOS.
 * @param contentPadding The padding around the content of the floating toolbar. Material only;
 *   ignored on iOS.
 * @param scrollBehavior Hides and shows the floating toolbar as the content under it scrolls; see
 *   [FloatingToolbarDefaults.exitAlwaysScrollBehavior]. Material only; ignored on iOS.
 * @param shape The shape of the floating toolbar's container. Material only; ignored on iOS.
 * @param leadingContent Optional leading content for the Material 3 Expressive floating toolbar.
 * @param trailingContent Optional trailing content for the Material 3 Expressive floating toolbar.
 * @param expandedShadowElevation The shadow elevation of the floating toolbar while expanded.
 *   Material only; ignored on iOS.
 * @param collapsedShadowElevation The shadow elevation of the floating toolbar while collapsed.
 *   Material only; ignored on iOS.
 * @param content The main content composable for the Material 3 Expressive floating toolbar.
 *   Typically a row of [IconButton] composables.
 * @param iosItems The list of bar button items for the iOS native toolbar. See [UIKitUIBarButtonItem].
 */
@ExperimentalCalfUiApi
@Composable
expect fun AdaptiveToolbar(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    colors: FloatingToolbarColors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
    contentPadding: PaddingValues = FloatingToolbarDefaults.ContentPadding,
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    expandedShadowElevation: Dp = FloatingToolbarDefaults.ContainerExpandedElevation,
    collapsedShadowElevation: Dp = FloatingToolbarDefaults.ContainerCollapsedElevation,
    content: @Composable RowScope.() -> Unit = {},
    iosItems: List<UIKitUIBarButtonItem> = emptyList(),
)
