package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

private const val USAGE = """
val state = rememberAdaptiveTimePickerState(
    initialHour = 7,
    initialMinute = 30,
    is24Hour = false,
)

AdaptiveTimePicker(state = state)

// state.hour is 0..23, state.minute 0..59
"""

private const val MINUTES_PER_HOUR = 60
private const val HALF_DAY_HOURS = 12
private const val INITIAL_ALARM_MINUTES = 7 * MINUTES_PER_HOUR + 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerScreen(navigateBack: () -> Unit) {
    var use24Hour by rememberSaveable { mutableStateOf(false) }
    // The picked time, in minutes after midnight, carried over when the format changes.
    var alarmMinutes by rememberSaveable { mutableIntStateOf(INITIAL_ALARM_MINUTES) }
    // is24Hour is fixed when the state is created, so a new format needs a new state.
    val state = key(use24Hour) {
        rememberAdaptiveTimePickerState(
            initialHour = alarmMinutes / MINUTES_PER_HOUR,
            initialMinute = alarmMinutes % MINUTES_PER_HOUR,
            is24Hour = use24Hour,
        )
    }

    SampleScreenScaffold(title = Screen.TimePicker.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.TimePicker)
            AlarmSection(state = state)
            DemoSection(
                title = "Options",
                footer = "is24Hour applies on Material. The iOS wheels follow the device's 12 or 24-hour setting.",
            ) {
                DemoListCard {
                    DemoRow(
                        title = "24-hour time",
                        leading = { IconTile(icon = Icons.Outlined.Schedule, color = TileViolet) },
                        trailing = {
                            AdaptiveSwitch(
                                checked = use24Hour,
                                onCheckedChange = {
                                    alarmMinutes = state.hour * MINUTES_PER_HOUR + state.minute
                                    use24Hour = it
                                },
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

/** "7:30 AM" or "07:30", in the format the picker shows. */
private fun AdaptiveTimePickerState.timeLabel(): String {
    val minutes = minute.toString().padStart(2, '0')
    if (is24hour) return "${hour.toString().padStart(2, '0')}:$minutes"
    val period = if (hour < HALF_DAY_HOURS) "AM" else "PM"
    val displayHour = (hour % HALF_DAY_HOURS).takeIf { it != 0 } ?: HALF_DAY_HOURS
    return "$displayHour:$minutes $period"
}
