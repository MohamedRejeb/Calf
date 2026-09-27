package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.ui.theme.IconTileSize
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
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
 * A share sheet for the album: a Done button that hides the sheet through its state, the swipe
 * option, an alert shown on top of the sheet, and a long list of people to send the album to.
 */
@Composable
internal fun ShareSheetContent(
    allowHide: Boolean,
    onAllowHideChange: (Boolean) -> Unit,
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
        ShareSheetHeader(onDone = onDone)
        DemoListCard {
            DemoRow(
                title = "Copy link",
                leading = { IconTile(icon = Icons.Outlined.Link, color = TileBlue) },
                onClick = { onAction("Link copied") },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwipeToCloseRow(allowHide = allowHide, onAllowHideChange = onAllowHideChange)
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
private fun ShareSheetHeader(onDone: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = Spacing.xs),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Share album",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$ALBUM_NAME · 42 photos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AdaptiveButton(onClick = onDone) {
            Text("Done")
        }
    }
}

@Composable
private fun ContactsList(onSend: (String) -> Unit) {
    DemoSection(title = "Send to") {
        DemoListCard {
            Contacts.forEachIndexed { index, name ->
                if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = name,
                    leading = { Avatar(name = name, color = AvatarColors[index % AvatarColors.size]) },
                    onClick = { onSend(name) },
                )
            }
        }
    }
}

/** The initials of a contact on a coloured circle, the size of an icon tile so dividers line up. */
@Composable
private fun Avatar(name: String, color: Color) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(IconTileSize)
            .background(color, CircleShape),
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}
