package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.InitialsAvatar
import com.mohamedrejeb.calf.sample.components.SheetHeader
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle

private const val ALBUM_NAME = "Lisbon, May 2026"

/** Long enough to scroll, so the sheet shows how it expands and scrolls its content. */
private val Contacts = listOf(
    "Sara Haddad", "Yuki Tanaka", "Omar Benali", "Lena Fischer", "Diego Alvarez",
    "Amara Okafor", "Noah Martin", "Inês Costa", "Priya Nair", "Tom Becker",
    "Chloé Dubois", "Mateo Rossi", "Hana Kim", "Jonas Berg", "Leila Mansour",
    "Arjun Mehta", "Clara Novak", "Samir Toumi", "Elif Yilmaz", "Ben Carter",
)

private val AvatarColors = listOf(TileIndigo, TileTeal, TileCoral, TileAmber, TileViolet, TileBlue)

/**
 * The row that moves the sheet between its two heights through `expand()` and `partialExpand()`.
 * Null when the sheet has no half height to go back to (`skipPartiallyExpanded`).
 */
internal data class SheetHeightToggle(
    val isExpanded: Boolean,
    val onToggle: () -> Unit,
)

internal fun sheetHeightToggle(
    isExpanded: Boolean,
    canToggle: Boolean,
    onToggle: () -> Unit,
): SheetHeightToggle? = if (canToggle) SheetHeightToggle(isExpanded, onToggle) else null

/**
 * A share sheet for the album: a Done button that hides the sheet through its state, the swipe
 * option, the height toggle, an alert shown on top of the sheet, and a long list of people to send
 * the album to.
 */
@Composable
internal fun ShareSheetContent(
    allowHide: Boolean,
    onAllowHideChange: (Boolean) -> Unit,
    heightToggle: SheetHeightToggle?,
    onAction: (String) -> Unit,
    onDone: () -> Unit,
) {
    var confirmStopSharing by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.l),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.pageMargin)
            .padding(bottom = Spacing.xxl),
    ) {
        SheetHeader(
            title = "Share album",
            subtitle = "$ALBUM_NAME · 42 photos",
            onDone = onDone,
        )
        DemoListCard {
            DemoRow(
                title = "Copy link",
                leading = { IconTile(icon = Icons.Outlined.Link, color = TileBlue) },
                onClick = { onAction("Link copied") },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwipeToCloseRow(allowHide = allowHide, onAllowHideChange = onAllowHideChange)
            if (heightToggle != null) {
                DemoDivider(startIndent = DividerIndentWithIcon)
                SheetHeightRow(heightToggle)
            }
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = "Stop sharing",
                subtitle = "Opens an alert above the sheet",
                leading = { IconTile(icon = Icons.Outlined.LinkOff, color = TileCoral) },
                onClick = { confirmStopSharing = true },
            )
        }
        ContactsList(onSend = { name -> onAction("Sent to $name") })
    }

    if (confirmStopSharing) {
        AdaptiveAlertDialog(
            onConfirm = {
                confirmStopSharing = false
                onAction("Stopped sharing")
            },
            onDismiss = { confirmStopSharing = false },
            confirmText = "Stop Sharing",
            dismissText = "Cancel",
            title = "Stop sharing “$ALBUM_NAME”?",
            text = "People you shared it with lose access to the photos.",
            iosConfirmButtonStyle = AlertDialogIosActionStyle.Destructive,
            iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
        )
    }
}

@Composable
private fun SheetHeightRow(toggle: SheetHeightToggle) {
    DemoRow(
        title = if (toggle.isExpanded) "Half height" else "Full height",
        subtitle = if (toggle.isExpanded) "partialExpand()" else "expand()",
        leading = {
            IconTile(
                icon = if (toggle.isExpanded) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore,
                color = TileViolet,
            )
        },
        onClick = toggle.onToggle,
    )
}

@Composable
private fun ContactsList(onSend: (String) -> Unit) {
    DemoSection(title = "Send to") {
        DemoListCard {
            Contacts.forEachIndexed { index, name ->
                if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = name,
                    leading = { InitialsAvatar(name = name, color = AvatarColors[index % AvatarColors.size]) },
                    onClick = { onSend(name) },
                )
            }
        }
    }
}

