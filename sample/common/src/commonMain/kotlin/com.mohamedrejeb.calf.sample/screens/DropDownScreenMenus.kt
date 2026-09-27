package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDown
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * One menu item of the demo: the [AdaptiveDropDownItem] iOS draws with an SF Symbol, and the
 * Material icon the Material menu shows next to the same title.
 */
internal class MenuEntry(
    title: String,
    sfSymbol: String,
    val materialIcon: ImageVector,
    isDestructive: Boolean = false,
    isDisabled: Boolean = false,
    onClick: () -> Unit,
) {
    val item = AdaptiveDropDownItem(
        title = title,
        iosIcon = UIKitImage.SystemName(sfSymbol),
        isDestructive = isDestructive,
        isDisabled = isDisabled,
        onClick = onClick,
    )
}

/** A titled group of [MenuEntry]s: an inline UIMenu on iOS, a divided group on Material. */
internal class MenuSection(title: String, val entries: List<MenuEntry>) {
    val section = AdaptiveDropDownSection(title = title, items = entries.map { it.item })
}

/**
 * The more button of a file row and its [AdaptiveDropDown]: a UIMenu on iOS, a Material menu
 * with the same items, icons and dividers elsewhere.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal fun FileMenuButton(
    entries: List<MenuEntry>,
    sections: List<MenuSection> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        MoreButton(onClick = { expanded = true })
        AdaptiveDropDown(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            iosItems = entries.map { it.item },
            iosSections = sections.map { it.section },
        ) {
            entries.forEach { entry ->
                MaterialMenuItem(entry = entry, onDismiss = { expanded = false })
            }
            sections.forEach { section ->
                HorizontalDivider()
                section.entries.forEach { entry ->
                    MaterialMenuItem(entry = entry, onDismiss = { expanded = false })
                }
            }
        }
    }
}

/**
 * The Material side of the top bar menu. On iOS the bar is a UINavigationBar and the same
 * entries become the bar button's UIMenu, see `UIKitUIBarButtonItem.withMenu`.
 */
@Composable
internal fun BarMenuButton(entries: List<MenuEntry>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        MoreButton(onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            entries.forEach { entry ->
                MaterialMenuItem(entry = entry, onDismiss = { expanded = false })
            }
        }
    }
}

@Composable
private fun MoreButton(onClick: () -> Unit) {
    AdaptiveIconButton(onClick = onClick) {
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MaterialMenuItem(entry: MenuEntry, onDismiss: () -> Unit) {
    val item = entry.item
    DropdownMenuItem(
        text = { Text(item.title) },
        leadingIcon = { Icon(entry.materialIcon, contentDescription = null) },
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
