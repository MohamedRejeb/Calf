package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.mohamedrejeb.calf.ui.slider.AdaptiveSlider
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

/** A fake sync: long enough to see the spinner and the disabled button. */
private const val SYNC_MILLIS = 2_000L
private const val TRIP_EVENTS = 3
private val SyncIndicatorSize = 22.dp
private val SyncIndicatorStroke = 2.5.dp
private val ButtonIconSize = 18.dp

private enum class SyncStatus(val label: String) {
    Idle("$TRIP_EVENTS events · synced 2 hours ago"),
    Syncing("Adding $TRIP_EVENTS events to your calendar"),
    Done("$TRIP_EVENTS events · synced just now"),
}

/**
 * The first tab: the trip at a glance, the budget (a slider), switches for what happens while
 * travelling, and a calendar sync (a button and a progress indicator).
 */
@Composable
internal fun PlanTab(
    padding: PaddingValues,
    departure: LocalDate,
    travellerCount: Int,
) {
    var budgetPerPerson by rememberSaveable { mutableFloatStateOf(BUDGET_INITIAL) }
    // A continuous slider (tick marks every €10 would be noise), read in whole tens of euros.
    val perPerson = (budgetPerPerson / BUDGET_ROUNDING).roundToInt() * BUDGET_ROUNDING

    DemoPage(contentPadding = padding) {
        TripSummaryCard(
            departure = departure,
            travellerCount = travellerCount,
            groupBudget = perPerson * travellerCount,
        )
        BudgetSection(
            perPerson = perPerson,
            travellerCount = travellerCount,
            value = budgetPerPerson,
            onValueChange = { budgetPerPerson = it },
        )
        TravelSettingsSection()
        CalendarSection()
    }
}

@Composable
private fun TripSummaryCard(
    departure: LocalDate,
    travellerCount: Int,
    groupBudget: Int,
) {
    var saved by rememberSaveable { mutableStateOf(true) }

    DemoCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lisbon, Portugal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = TRIP_NAME,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${tripRangeLabel(departure)} · $TRIP_NIGHTS nights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AdaptiveIconButton(onClick = { saved = !saved }) {
                Icon(
                    imageVector = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (saved) "Remove from saved trips" else "Save trip",
                    tint = if (saved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                )
            }
        }
        DemoDivider(startIndent = 0.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            TripStat(value = "$travellerCount", label = "Travellers", modifier = Modifier.weight(1f))
            TripStat(value = departure.shortLabel(), label = "Departure", modifier = Modifier.weight(1f))
            TripStat(value = euros(groupBudget), label = "Budget", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TripStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BudgetSection(
    perPerson: Int,
    travellerCount: Int,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    DemoSection(
        title = "Budget",
        footer = "Split evenly between $travellerCount travellers. Flights are paid separately.",
    ) {
        DemoCard(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            ValueRow(label = "Per person", value = euros(perPerson))
            AdaptiveSlider(
                value = value,
                onValueChange = onValueChange,
                valueRange = BUDGET_MIN..BUDGET_MAX,
                modifier = Modifier.fillMaxWidth(),
            )
            ValueRow(label = "Group total", value = euros(perPerson * travellerCount))
        }
    }
}

@Composable
private fun TravelSettingsSection() {
    var offlineMaps by rememberSaveable { mutableStateOf(true) }
    var priceAlerts by rememberSaveable { mutableStateOf(true) }
    var shareLocation by rememberSaveable { mutableStateOf(false) }

    DemoSection(title = "While travelling") {
        DemoListCard {
            TripSwitchRow(
                title = "Offline maps",
                subtitle = if (offlineMaps) "Lisbon, 240 MB downloaded" else "Not downloaded",
                icon = Icons.Outlined.Map,
                tint = TileTeal,
                checked = offlineMaps,
                onCheckedChange = { offlineMaps = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            TripSwitchRow(
                title = "Price alerts",
                subtitle = "Stays and trains under budget",
                icon = Icons.Outlined.NotificationsActive,
                tint = TileAmber,
                checked = priceAlerts,
                onCheckedChange = { priceAlerts = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            TripSwitchRow(
                title = "Share location",
                subtitle = "With travellers, during the trip only",
                icon = Icons.Outlined.LocationOn,
                tint = TileBlue,
                checked = shareLocation,
                onCheckedChange = { shareLocation = it },
            )
        }
    }
}

@Composable
private fun TripSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    DemoRow(
        title = title,
        subtitle = subtitle,
        leading = { IconTile(icon = icon, color = tint) },
        trailing = { AdaptiveSwitch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

/** The button starts a sync; while it runs the button is disabled and the row shows a spinner. */
@Composable
private fun CalendarSection() {
    var status by rememberSaveable { mutableStateOf(SyncStatus.Idle) }
    val syncing = status == SyncStatus.Syncing

    LaunchedEffect(status) {
        if (status == SyncStatus.Syncing) {
            delay(SYNC_MILLIS)
            status = SyncStatus.Done
        }
    }

    DemoSection(title = "Calendar") {
        DemoListCard {
            DemoRow(
                title = "Trip calendar",
                subtitle = status.label,
                leading = { IconTile(icon = Icons.Outlined.Event, color = TileViolet) },
                trailing = {
                    if (syncing) {
                        AdaptiveCircularProgressIndicator(
                            strokeWidth = SyncIndicatorStroke,
                            modifier = Modifier.size(SyncIndicatorSize),
                        )
                    }
                },
            )
            AdaptiveButton(
                onClick = { status = SyncStatus.Syncing },
                enabled = !syncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.l),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonIconSize),
                )
                Spacer(Modifier.width(Spacing.s))
                Text(if (syncing) "Syncing" else "Sync now")
            }
        }
    }
}
