package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

private const val USAGE = """
var expanded by remember { mutableStateOf(false) }
val items = listOf(
    AdaptiveDropDownItem(
        title = "Share",
        iosIcon = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
        onClick = { share(file) },
    ),
    AdaptiveDropDownItem(title = "Delete", isDestructive = true, onClick = { delete(file) }),
)

Box {
    AdaptiveIconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "More")
    }
    AdaptiveDropDown(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        iosItems = items,
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.title) },
                onClick = { expanded = false; item.onClick() },
            )
        }
    }
}
"""

private const val NO_ACTION = "None yet"
private const val REPORT = "Q3 report.pdf"
private const val ITINERARY = "Lisbon itinerary"

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun DropDownScreen(navigateBack: () -> Unit) {
    var lastAction by rememberSaveable { mutableStateOf(NO_ACTION) }
    val barMenu = remember { barMenuEntries(onAction = { lastAction = it }) }

    // The demo is the bar: it keeps its own top bar (SampleHost.DetailOwnBar) so the menu lives in it.
    SampleScreenScaffold(
        title = Screen.DropDown.title,
        navigateBack = navigateBack,
        actions = { BarMenuButton(entries = barMenu) },
        iosTrailingItems = listOf(
            UIKitUIBarButtonItem.withMenu(
                image = UIKitImage.SystemName(SFSymbol.ellipsisCircle),
                menuItems = barMenu.map { it.item },
                label = "More",
            ),
        ),
    ) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.DropDown)
            FilesSection(onAction = { lastAction = it })
            DemoSection(title = "Result") {
                DemoCard {
                    ValueRow(label = "Last action", value = lastAction)
                }
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun FilesSection(onAction: (String) -> Unit) {
    val reportMenu = remember(onAction) { reportMenuEntries(onAction) }
    val (items, sections) = remember(onAction) { itineraryMenu(onAction) }
    DemoSection(
        title = "Recent files",
        footer = "The report menu has icons, a disabled item and a destructive one; the itinerary menu adds a " +
            "titled section. The more button in the top bar has a menu too: on iOS, the bar button's own UIMenu.",
    ) {
        DemoListCard {
            DemoRow(
                title = REPORT,
                subtitle = "2.4 MB · Edited today",
                leading = { IconTile(icon = Icons.Outlined.Description, color = TileCoral) },
                trailing = { FileMenuButton(entries = reportMenu) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = ITINERARY,
                subtitle = "Shared with Sara",
                leading = { IconTile(icon = Icons.AutoMirrored.Outlined.Article, color = TileBlue) },
                trailing = { FileMenuButton(entries = items, sections = sections) },
            )
        }
    }
}

private fun barMenuEntries(onAction: (String) -> Unit): List<MenuEntry> =
    listOf(
        MenuEntry("Edit", SFSymbol.pencil, Icons.Outlined.Edit) { onAction("Editing $ITINERARY") },
        MenuEntry("Duplicate", SFSymbol.documentOnDocument, Icons.Outlined.ContentCopy) {
            onAction("Duplicated $ITINERARY")
        },
        MenuEntry("Pin", SFSymbol.pin, Icons.Outlined.PushPin) { onAction("Pinned $ITINERARY") },
        MenuEntry("Delete", SFSymbol.trash, Icons.Outlined.Delete, isDestructive = true) {
            onAction("Deleted $ITINERARY")
        },
    )

private fun reportMenuEntries(onAction: (String) -> Unit): List<MenuEntry> =
    listOf(
        MenuEntry("Copy", SFSymbol.documentOnDocument, Icons.Outlined.ContentCopy) { onAction("Copied $REPORT") },
        MenuEntry("Share", SFSymbol.squareAndArrowUp, Icons.Outlined.Share) { onAction("Shared $REPORT") },
        // No printer nearby: shown, but greyed out on both platforms.
        MenuEntry("Print", SFSymbol.printer, Icons.Outlined.Print, isDisabled = true) { onAction("Printed $REPORT") },
        MenuEntry("Delete", SFSymbol.trash, Icons.Outlined.Delete, isDestructive = true) {
            onAction("Deleted $REPORT")
        },
    )

private fun itineraryMenu(onAction: (String) -> Unit): Pair<List<MenuEntry>, List<MenuSection>> {
    val items = listOf(
        MenuEntry("Open", SFSymbol.folder, Icons.Outlined.FolderOpen) { onAction("Opened $ITINERARY") },
        MenuEntry("Rename", SFSymbol.pencil, Icons.Outlined.DriveFileRenameOutline) {
            onAction("Renaming $ITINERARY")
        },
    )
    val manage = MenuSection(
        title = "Manage",
        entries = listOf(
            MenuEntry("Archive", SFSymbol.archivebox, Icons.Outlined.Archive) { onAction("Archived $ITINERARY") },
            MenuEntry("Delete", SFSymbol.trash, Icons.Outlined.Delete, isDestructive = true) {
                onAction("Deleted $ITINERARY")
            },
        ),
    )
    return items to listOf(manage)
}
