package com.mohamedrejeb.calf.ui.dropdown

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * Displays an adaptive drop-down menu controlled by the [expanded] state.
 *
 * On iOS, this uses a native pull-down menu via `UIButton.menu` with
 * `showsMenuAsPrimaryAction`, providing the system drop-down experience.
 *
 * On non-iOS platforms (Android, Desktop, Web), this delegates directly to Material3
 * [DropdownMenu].
 *
 * @param expanded Whether the drop-down menu is currently shown.
 * @param onDismissRequest Called when the menu should be dismissed (e.g. user taps outside).
 * @param iosItems The list of [AdaptiveDropDownItem]s to display in the menu.
 * @param modifier The modifier applied to the menu.
 * @param iosSections Optional grouped sections of items. On iOS, these are rendered as inline
 *   sub-menus with visual separators. On Material platforms, sections are separated by dividers.
 * @param materialContent Optional composable to fully customize how the menu content is rendered
 *   on non-iOS platforms. When null, items are rendered as default [DropdownMenuItem]s.
 */
@ExperimentalCalfUiApi
@Composable
expect fun BoxScope.AdaptiveDropDown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    iosItems: List<AdaptiveDropDownItem>,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = DefaultMenuProperties,
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    iosSections: List<AdaptiveDropDownSection> = emptyList(),
    materialContent: @Composable ColumnScope.() -> Unit,
)

internal expect val DefaultMenuProperties: PopupProperties
