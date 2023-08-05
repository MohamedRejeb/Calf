package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun AdaptiveDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier = Modifier,
    dateFormatter: DatePickerFormatter = remember { DatePickerFormatter() },
    title: (@Composable () -> Unit)? = null,
    headline: (@Composable () -> Unit)? = null,
    showModeToggle: Boolean = true,
    colors: DatePickerColors = DatePickerDefaults.colors(),
)

internal val DatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
internal val DatePickerHeadlinePadding = PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)