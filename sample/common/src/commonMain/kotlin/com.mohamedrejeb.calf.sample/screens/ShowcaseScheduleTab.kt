package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDown
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

private val TrailingIconSize = 18.dp

/**
 * The second tab: when the trip starts. A summary whose reminder row opens a drop down, then
 * the date and time pickers that fill it in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleTab(
    padding: PaddingValues,
    datePickerState: AdaptiveDatePickerState,
    timePickerState: AdaptiveTimePickerState,
) {
    var reminder by rememberSaveable { mutableStateOf(ReminderOption.OneHourBefore) }
    val departure = departureDateOf(datePickerState.selectedDateMillis)

    DemoPage(contentPadding = padding) {
        DemoSection(
            title = "Departure",
            footer = "Back on ${departure.returnDate().shortLabel()}, after $TRIP_NIGHTS nights.",
        ) {
            DemoListCard {
                DemoRow(
                    title = "Date",
                    leading = { IconTile(icon = Icons.Outlined.CalendarMonth, color = TileCoral) },
                    trailing = { TrailingValue(departure.shortLabel()) },
                )
                DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = "Time",
                    leading = { IconTile(icon = Icons.Outlined.Schedule, color = TileBlue) },
                    trailing = { TrailingValue(timeLabel(timePickerState.hour, timePickerState.minute)) },
                )
                DemoDivider(startIndent = DividerIndentWithIcon)
                ReminderRow(reminder = reminder, onSelect = { reminder = it })
            }
        }
        DemoSection(title = "Date") {
            DemoCard(
                contentPadding = PaddingValues(vertical = Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // The rows above already show the date, so the Material header is left out.
                AdaptiveDatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                )
            }
        }
        DemoSection(title = "Time") {
            DemoCard(horizontalAlignment = Alignment.CenterHorizontally) {
                AdaptiveTimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                )
            }
        }
    }
}

/**
 * The reminder row. Tapping it opens an `AdaptiveDropDown` anchored to the row: a `UIMenu` on
 * iOS, a Material `DropdownMenu` elsewhere. "Off" is the destructive entry.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun ReminderRow(
    reminder: ReminderOption,
    onSelect: (ReminderOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val items = remember(reminder, onSelect) { reminderMenuItems(reminder, onSelect) }

    DemoRow(
        title = "Reminder",
        leading = { IconTile(icon = Icons.Outlined.Notifications, color = TileAmber) },
        trailing = {
            // The menu anchors to the value, so it opens under it on both platforms.
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TrailingValue(reminder.label)
                    Icon(
                        imageVector = Icons.Outlined.UnfoldMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(TrailingIconSize),
                    )
                }
                AdaptiveDropDown(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    iosItems = items,
                ) {
                    items.forEachIndexed { index, item ->
                        ReminderMenuItem(
                            item = item,
                            selected = ReminderOption.entries[index] == reminder,
                            onClick = {
                                expanded = false
                                item.onClick()
                            },
                        )
                    }
                }
            }
        },
        onClick = { expanded = true },
    )
}

@Composable
private fun ReminderMenuItem(
    item: AdaptiveDropDownItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = item.title,
                color = if (item.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingIcon = if (selected) {
            { Icon(Icons.Outlined.Check, contentDescription = "Selected") }
        } else {
            null
        },
        onClick = onClick,
    )
}

/** The iOS menu: a checkmark on the current choice, "Off" in red. */
private fun reminderMenuItems(
    selected: ReminderOption,
    onSelect: (ReminderOption) -> Unit,
): List<AdaptiveDropDownItem> = ReminderOption.entries.map { option ->
    AdaptiveDropDownItem(
        title = option.label,
        iosIcon = if (option == selected) UIKitImage.SystemName(SFSymbol.checkmark) else null,
        isDestructive = option == ReminderOption.Off,
        onClick = { onSelect(option) },
    )
}

@Composable
private fun TrailingValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
