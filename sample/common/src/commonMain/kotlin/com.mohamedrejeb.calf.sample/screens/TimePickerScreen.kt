package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerScreen(
    navigateBack: () -> Unit
) {
    val state = rememberAdaptiveTimePickerState(
        initialHour = 19,
        initialMinute = 20,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        IconButton(
            onClick = {
                navigateBack()
            },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            text = "Adaptive Time Picker",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(16.dp)
        )

        Text(
            text = "Selected time: ${state.hour}:${state.minute}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(16.dp)
        )

        AdaptiveTimePicker(
            state = state,
            colors = TimePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
        )
    }
}