@file:OptIn(ExperimentalTime::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.timepicker.isPm
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val USAGE = """
// is24Hour defaults to the device setting.
val state = rememberAdaptiveTimePickerState(
    initialHour = 7,
    initialMinute = 30,
)

AdaptiveTimePicker(state = state)

// state.hour is 0..23, state.minute 0..59; both can be set.
state.hour = 9
state.minute = 30
val afternoon = state.isPm
"""

private const val HALF_DAY_HOURS = 12
private const val INITIAL_ALARM_HOUR = 7
private const val INITIAL_ALARM_MINUTE = 30
private const val PRESET_HOUR = 9
private const val PRESET_MINUTE = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerScreen(navigateBack: () -> Unit) {
    val state = rememberAdaptiveTimePickerState(
        initialHour = INITIAL_ALARM_HOUR,
        initialMinute = INITIAL_ALARM_MINUTE,
    )
    // The format the state started with: the device setting, kept across a rotation.
    val deviceUses24Hour = rememberSaveable { state.is24hour }

    SampleScreenScaffold(title = Screen.TimePicker.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.TimePicker)
            AlarmSection(state = state)
            QuickSetSection(state = state)
            DemoSection(
                title = "Options",
                footer = "is24hour starts at the device setting (${deviceUses24Hour.formatLabel()} here) and " +
                    "re-lays out the Material picker when switched. The iOS wheels always follow the device.",
            ) {
                DemoListCard {
                    DemoRow(
                        title = "24-hour time",
                        leading = { IconTile(icon = Icons.Outlined.Schedule, color = TileViolet) },
                        trailing = {
                            AdaptiveSwitch(
                                checked = state.is24hour,
                                onCheckedChange = { state.is24hour = it },
                            )
                        },
                    )
                }
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmSection(state: AdaptiveTimePickerState) {
    DemoSection(title = "Alarm") {
        DemoCard(
            contentPadding = PaddingValues(),
            verticalArrangement = Arrangement.Top,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.l),
            ) {
                ValueRow(label = "Wake up at", value = state.timeLabel())
                ValueRow(label = "Half of the day", value = if (state.isPm) "PM" else "AM")
            }
            DemoDivider()
            AdaptiveTimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = Spacing.l),
            )
        }
    }
}

/** Rows that assign `hour` and `minute`; the picker follows on every platform. */
@Composable
private fun QuickSetSection(state: AdaptiveTimePickerState) {
    DemoSection(
        title = "Quick set",
        footer = "Setting state.hour and state.minute moves the picker.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Set to 09:30",
                subtitle = "state.hour = 9; state.minute = 30",
                leading = { IconTile(icon = Icons.Outlined.Alarm, color = TileAmber) },
                onClick = {
                    state.hour = PRESET_HOUR
                    state.minute = PRESET_MINUTE
                },
            )
            DemoDivider(startIndent = Spacing.l)
            DemoRow(
                title = "Set to now",
                subtitle = "The current time in the device time zone",
                leading = { IconTile(icon = Icons.Outlined.Update, color = TileBlue) },
                onClick = {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    state.hour = now.hour
                    state.minute = now.minute
                },
            )
        }
    }
}

private fun Boolean.formatLabel(): String = if (this) "24-hour" else "12-hour"

/** "7:30 AM" or "07:30", in the format the picker shows. */
private fun AdaptiveTimePickerState.timeLabel(): String {
    val minutes = minute.toString().padStart(2, '0')
    if (is24hour) return "${hour.toString().padStart(2, '0')}:$minutes"
    val period = if (isPm) "PM" else "AM"
    val displayHour = (hour % HALF_DAY_HOURS).takeIf { it != 0 } ?: HALF_DAY_HOURS
    return "$displayHour:$minutes $period"
}
