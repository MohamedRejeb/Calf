package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

private val AvatarSize = 72.dp
private val ActionRowMinHeight = 52.dp

/** A place on the Saved tab. */
private data class SavedPlace(
    val name: String,
    val detail: String,
    val icon: ImageVector,
    val tint: Color,
)

private val SavedPlaces = listOf(
    SavedPlace("Pastéis de Belém", "Lisbon · bakery", Icons.Outlined.LocalCafe, TileAmber),
    SavedPlace("Time Out Market", "Lisbon · food hall", Icons.Outlined.Restaurant, TileCoral),
    SavedPlace("Gulbenkian Museum", "Lisbon · museum", Icons.Outlined.Museum, TileIndigo),
    SavedPlace("LX Factory", "Lisbon · shops and cafés", Icons.Outlined.Storefront, TileViolet),
    SavedPlace("Miradouro da Graça", "Lisbon · viewpoint", Icons.Outlined.Landscape, TileTeal),
    SavedPlace("Praia da Ursa", "Sintra · beach", Icons.Outlined.BeachAccess, TileBlue),
)

/** The Saved tab of the navigation bar demo: the places saved for the next trip. */
@Composable
internal fun TravelSavedTab(contentPadding: PaddingValues) {
    DemoPage(contentPadding = contentPadding) {
        DemoSection(
            title = "Lisbon, October",
            footer = "Tap a heart to remove a place from the list, tap it again to keep it.",
        ) {
            DemoListCard {
                SavedPlaces.forEachIndexed { index, place ->
                    if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                    SavedPlaceRow(place)
                }
            }
        }
    }
}

@Composable
private fun SavedPlaceRow(place: SavedPlace) {
    var saved by rememberSaveable(place.name) { mutableStateOf(true) }
    DemoRow(
        title = place.name,
        subtitle = place.detail,
        leading = { IconTile(icon = place.icon, color = place.tint) },
        trailing = {
            AdaptiveIconButton(onClick = { saved = !saved }) {
                Icon(
                    imageVector = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (saved) "Remove ${place.name}" else "Save ${place.name}",
                    tint = if (saved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                )
            }
        },
    )
}

/** The Profile tab of the navigation bar demo: the traveller, their settings and a log out. */
@Composable
internal fun TravelProfileTab(contentPadding: PaddingValues) {
    DemoPage(contentPadding = contentPadding) {
        ProfileCard()
        ProfileSettingsSection()
        LogOutSection()
    }
}

@Composable
private fun ProfileCard() {
    DemoCard(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(AvatarSize)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Text(
                text = "MC",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Maya Chen",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "maya.chen@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
        ) {
            ProfileStat(value = "12", label = "Trips")
            ProfileStat(value = "9", label = "Countries")
            ProfileStat(value = "51", label = "Nights")
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileSettingsSection() {
    var reminders by rememberSaveable { mutableStateOf(true) }
    var priceAlerts by rememberSaveable { mutableStateOf(false) }
    var offlineMaps by rememberSaveable { mutableStateOf(true) }

    DemoSection(title = "Settings") {
        DemoListCard {
            ProfileSwitchRow(
                title = "Trip reminders",
                icon = Icons.Outlined.Notifications,
                tint = TileCoral,
                checked = reminders,
                onCheckedChange = { reminders = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            ProfileSwitchRow(
                title = "Price alerts",
                icon = Icons.Outlined.LocalOffer,
                tint = TileAmber,
                checked = priceAlerts,
                onCheckedChange = { priceAlerts = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            ProfileSwitchRow(
                title = "Offline maps",
                icon = Icons.Outlined.Map,
                tint = TileTeal,
                checked = offlineMaps,
                onCheckedChange = { offlineMaps = it },
            )
        }
    }
}

@Composable
private fun ProfileSwitchRow(
    title: String,
    icon: ImageVector,
    tint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    DemoRow(
        title = title,
        leading = { IconTile(icon = icon, color = tint) },
        trailing = { AdaptiveSwitch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

@Composable
private fun LogOutSection() {
    var showLogOutDialog by rememberSaveable { mutableStateOf(false) }

    DemoListCard {
        // A destructive action reads as a centred red row, as in the iOS Settings app.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveClickable { showLogOutDialog = true }
                .heightIn(min = ActionRowMinHeight),
        ) {
            Text(
                text = "Log out",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (showLogOutDialog) {
        AdaptiveAlertDialog(
            onConfirm = { showLogOutDialog = false },
            onDismiss = { showLogOutDialog = false },
            confirmText = "Log out",
            dismissText = "Cancel",
            title = "Log out?",
            text = "Your saved places stay on this device.",
        )
    }
}
