package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

val LocalTestString = compositionLocalOf { "" }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    initialDate: LocalDate,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit,
) {
    val currentInitialDate by rememberUpdatedState(initialDate)
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var skipPartiallyExpanded by remember { mutableStateOf(false) }
    if (showDatePicker) {
        val current by remember {
            derivedStateOf {
                currentInitialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds().also {
                    println("Initial millis: $it")
                }
            }
        }
        var currentSelection by remember { mutableLongStateOf(current) }
        val sheetState = rememberAdaptiveSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
        var hideSheet by remember { mutableStateOf(false) }
        // TODO track min/max dates available and limit to those
        val datePickerState = rememberAdaptiveDatePickerState(current)
        AdaptiveBottomSheet(
            onDismissRequest = { showDatePicker = false },
            adaptiveSheetState = sheetState,
        ) {
//            Surface(
//                color = MaterialTheme.colorScheme.surface,
//                contentColor = MaterialTheme.colorScheme.onSurface,
//            ) {
                AdaptiveDatePicker(
                    datePickerState,
                    modifier = Modifier.fillMaxWidth(),
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = Color.Red,
                        dayContentColor = Color.DarkGray,
                        containerColor = Color.LightGray
                    ),
                    headline = { Text("Select a date", Modifier.padding(start = 16.dp)) },
                )

                Row(Modifier.padding(bottom = 16.dp)) {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { hideSheet = true }) { Text("Cancel") }
                    Spacer(Modifier.width(4.dp))
                    val confirmEnabled by remember {
                        derivedStateOf { datePickerState.selectedDateMillis != current }
                    }
                    TextButton(
                        onClick = {
                            currentSelection = datePickerState.selectedDateMillis ?: current
                            hideSheet = true
                        },
                        enabled = confirmEnabled,
                    ) {
                        Text("Confirm")
                    }
                }
//            }
        }
        if (hideSheet) {
            LaunchedEffect(Unit) {
                try {
                    sheetState.hide()
                } catch (e: Exception) {
                    println("But why?!") // because now it's animating back up?!?
                }
                hideSheet = false
                showDatePicker = false
                val selected =
                    currentSelection
                        .takeIf { it != current }
                        ?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date }
                        ?: currentInitialDate
                onDateSelected(selected)
            }
        }
    }
    NavigationBar(modifier = modifier) {
        IconButton(
            onClick = { onDateSelected(currentInitialDate.minus(DatePeriod(days = 1))) },
            modifier = Modifier.weight(0.2f),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Previous Day")
        }
        val scope = rememberCoroutineScope()
        TextButton(onClick = {
            showDatePicker = true
//            scope.launch {
//                delay(2000)
//                skipPartiallyExpanded = !skipPartiallyExpanded
//            }
                             }, modifier = Modifier.weight(0.6f)) {
            Row(verticalAlignment = CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "Select date")
                Spacer(Modifier.width(4.dp))
                val today = remember { Clock.System.now().toLocalDateTime(TimeZone.UTC).date }
                val text =
                    if (currentInitialDate == today) {
                        "$currentInitialDate (Today)"
                    } else {
                        currentInitialDate.toString()
                    }
                Text(text, modifier = Modifier.animateContentSize())
            }
        }
        IconButton(
            onClick = { onDateSelected(currentInitialDate.plus(DatePeriod(days = 1))) },
            modifier = Modifier.weight(0.2f),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Next Day")
        }
    }
}