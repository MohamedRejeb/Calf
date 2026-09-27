package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem

/**
 * The Material side of a top bar's more menu: a more button that opens a `DropdownMenu` of
 * [items]. On iOS the bar is a UINavigationBar and the same items become the bar button's UIMenu
 * through `UIKitUIBarButtonItem.withMenu`, which the caller passes as an iOS trailing item.
 *
 * @param icons the Material icon of each item, by index; items past its end have none.
 */
@Composable
fun BarOverflowMenu(
    items: List<AdaptiveDropDownItem>,
    modifier: Modifier = Modifier,
    icons: List<ImageVector> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        MoreOptionsButton(onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEachIndexed { index, item ->
                OverflowMenuItem(
                    item = item,
                    icon = icons.getOrNull(index),
                    onDismiss = { expanded = false },
                )
            }
        }
    }
}

/** The ⋮ button that opens a more menu, in a bar or at the end of a row. */
@Composable
fun MoreOptionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * An [AdaptiveDropDownItem] drawn as a Material menu item: disabled and destructive (in the error
 * colour) like its UIMenu twin. [onDismiss] closes the menu before the item's action runs.
 */
@Composable
fun OverflowMenuItem(
    item: AdaptiveDropDownItem,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
) {
    DropdownMenuItem(
        text = { Text(item.title) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null) } },
        enabled = !item.isDisabled,
        colors = if (item.isDestructive) destructiveItemColors() else MenuDefaults.itemColors(),
        onClick = {
            onDismiss()
            item.onClick()
        },
    )
}

@Composable
private fun destructiveItemColors(): MenuItemColors =
    MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.error,
        leadingIconColor = MaterialTheme.colorScheme.error,
    )
