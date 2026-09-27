package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet

/**
 * The third tab: who is coming, and the actions that open the overlays. Inviting opens the
 * bottom sheet; exporting, leaving and the two "About" rows open alert dialogs.
 */
@Composable
internal fun PeopleTab(
    padding: PaddingValues,
    onInvite: () -> Unit,
    onShowDialog: (ShowcaseDialog) -> Unit,
) {
    DemoPage(contentPadding = padding) {
        TravellersSection(onInvite = onInvite)
        DemoSection(title = "Trip") {
            DemoListCard {
                DemoRow(
                    title = "Export itinerary",
                    subtitle = "PDF with the plan, times and budget",
                    leading = { IconTile(icon = Icons.Outlined.Description, color = TileTeal) },
                    onClick = { onShowDialog(ShowcaseDialog.Export) },
                )
                DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = "Leave trip",
                    subtitle = "Asks before it removes you",
                    leading = { IconTile(icon = Icons.AutoMirrored.Outlined.Logout, color = TileCoral) },
                    onClick = { onShowDialog(ShowcaseDialog.Leave) },
                )
            }
        }
        DemoSection(
            title = "About this demo",
            footer = "The tab bar, the top bar and its menu, and every control above are Calf adaptive " +
                "components: UIKit on iOS, Material 3 elsewhere.",
        ) {
            DemoListCard {
                DemoRow(
                    title = "How it is built",
                    leading = { IconTile(icon = Icons.Outlined.Info, color = TileViolet) },
                    onClick = { onShowDialog(ShowcaseDialog.About) },
                )
                DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = "View source",
                    subtitle = CALF_SOURCE_URL,
                    leading = { IconTile(icon = Icons.Outlined.Code, color = TileIndigo) },
                    onClick = { onShowDialog(ShowcaseDialog.Source) },
                )
            }
        }
    }
}

@Composable
private fun TravellersSection(onInvite: () -> Unit) {
    DemoSection(title = "Travellers") {
        DemoListCard {
            TripTravellers.forEach { traveller ->
                DemoRow(
                    title = traveller.name,
                    subtitle = traveller.status,
                    leading = { TravellerAvatar(traveller) },
                    trailing = if (traveller.isPending) {
                        { PendingLabel() }
                    } else {
                        null
                    },
                )
                DemoDivider(startIndent = DividerIndentWithIcon)
            }
            DemoRow(
                title = "Invite travellers",
                leading = { IconTile(icon = Icons.Outlined.PersonAdd, color = TileBlue) },
                onClick = onInvite,
            )
        }
    }
}

@Composable
private fun PendingLabel() {
    Text(
        text = "Pending",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.tertiary,
    )
}
