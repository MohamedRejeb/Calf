package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog

private const val NAVIGATION_BAR_USAGE = """
var selected by remember { mutableIntStateOf(0) }

AdaptiveNavigationBar(
    iosItems = listOf(
        UIKitUITabBarItem("Home", image = UIKitImage.SystemName(SFSymbol.house)),
        UIKitUITabBarItem("Saved", image = UIKitImage.Vector(Icons.Rounded.Favorite)),
    ),
    iosSelectedIndex = selected,
    iosOnItemSelected = { selected = it },
) {
    // Material: your NavigationBarItems, one per tab
    NavigationBarItem(
        selected = selected == 0,
        onClick = { selected = 0 },
        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
        label = { Text("Home") },
    )
}
"""

/** A past trip of the Home tab's history list. */
private data class PastTrip(
    val city: String,
    val detail: String,
    val nights: Int,
)

// Long enough to scroll: the list must slide behind the tab bar and its last row stop above it.
private val PastTrips = listOf(
    PastTrip(city = "Kyoto", detail = "Japan · April 2026", nights = 6),
    PastTrip(city = "Reykjavík", detail = "Iceland · February 2026", nights = 4),
    PastTrip(city = "Marrakesh", detail = "Morocco · December 2025", nights = 5),
    PastTrip(city = "Copenhagen", detail = "Denmark · October 2025", nights = 3),
    PastTrip(city = "Mexico City", detail = "Mexico · August 2025", nights = 7),
    PastTrip(city = "Edinburgh", detail = "Scotland · June 2025", nights = 2),
    PastTrip(city = "Seoul", detail = "South Korea · April 2025", nights = 8),
    PastTrip(city = "Tbilisi", detail = "Georgia · February 2025", nights = 4),
    PastTrip(city = "Porto", detail = "Portugal · November 2024", nights = 3),
    PastTrip(city = "Cape Town", detail = "South Africa · September 2024", nights = 9),
)

/** The Home tab of the navigation bar demo: the next trip, its bookings and the trip history. */
@Composable
internal fun TravelHomeTab(contentPadding: PaddingValues) {
    DemoPage(contentPadding = contentPadding) {
        DemoHeader(Screen.NavigationBar)
        NextTripSection()
        ManageTripSection()
        PastTripsSection()
        DemoSection(title = "Usage") {
            CodeSnippet(NAVIGATION_BAR_USAGE.trimIndent())
        }
    }
}

@Composable
private fun NextTripSection() {
    DemoSection(title = "Lisbon · Oct 12 – 16") {
        DemoListCard {
            DemoRow(
                title = "Flight TP 1351",
                subtitle = "Oct 12 · 08:40 from London Heathrow",
                leading = { IconTile(icon = Icons.Outlined.Flight, color = TileBlue) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = "Casa do Príncipe",
                subtitle = "4 nights · check-in from 15:00",
                leading = { IconTile(icon = Icons.Outlined.Hotel, color = TileTeal) },
            )
        }
    }
}

/**
 * Two dialogs opened from inside a tab. On iOS the tab bar is a UIKit view above the Compose
 * canvas, so this checks that both kinds of dialog still cover it.
 */
@Composable
private fun ManageTripSection() {
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showNotesDialog by rememberSaveable { mutableStateOf(false) }

    DemoSection(
        title = "Manage",
        footer = "Both dialogs draw above the tab bar. On iOS the first is a UIAlertController, " +
            "the second a Compose dialog.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Cancel booking",
                subtitle = "AdaptiveAlertDialog",
                leading = { IconTile(icon = Icons.Outlined.EventBusy, color = TileCoral) },
                onClick = { showCancelDialog = true },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = "Trip notes",
                subtitle = "Material 3 AlertDialog",
                leading = { IconTile(icon = Icons.Outlined.EditNote, color = TileAmber) },
                onClick = { showNotesDialog = true },
            )
        }
    }

    if (showCancelDialog) {
        AdaptiveAlertDialog(
            onConfirm = { showCancelDialog = false },
            onDismiss = { showCancelDialog = false },
            confirmText = "Cancel booking",
            dismissText = "Keep",
            title = "Cancel booking?",
            text = "Your stay at Casa do Príncipe is free to cancel until Oct 10.",
        )
    }

    if (showNotesDialog) {
        TripNotesDialog(onDismiss = { showNotesDialog = false })
    }
}

@Composable
private fun TripNotesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Trip notes") },
        text = {
            Text(
                text = "Buy a Viva Viagem card at the airport. Tram 28 leaves from Martim Moniz, " +
                    "go before 9:00 to get a seat.",
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}

@Composable
private fun PastTripsSection() {
    DemoSection(
        title = "Past trips",
        footer = "Scroll to the end: the list slides behind the tab bar and its last row stops " +
            "right above it.",
    ) {
        DemoListCard {
            PastTrips.forEachIndexed { index, trip ->
                if (index > 0) DemoDivider()
                DemoRow(
                    title = trip.city,
                    subtitle = trip.detail,
                    trailing = {
                        Text(
                            text = "${trip.nights} nights",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }
}
