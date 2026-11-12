package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.components.MoreOptionsButton
import com.mohamedrejeb.calf.sample.components.OverflowMenuItem
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
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
        MoreOptionsButton(onClick = { expanded = true })
        AdaptiveDropDown(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            iosItems = entries.map { it.item },
            iosSections = sections.map { it.section },
        ) {
            entries.forEach { entry ->
                OverflowMenuItem(item = entry.item, icon = entry.materialIcon, onDismiss = { expanded = false })
            }
            sections.forEach { section ->
                HorizontalDivider()
                section.entries.forEach { entry ->
                    OverflowMenuItem(item = entry.item, icon = entry.materialIcon, onDismiss = { expanded = false })
                }
            }
        }
    }
}
