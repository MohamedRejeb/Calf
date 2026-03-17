package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
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

    SampleScreenScaffold(
        title = "Adaptive Time Picker",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Time selection using native iOS UIDatePicker and Material3 TimePicker.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val formattedMinute = state.minute.toString().padStart(2, '0')
            Text(
                text = "Selected time: ${state.hour}:$formattedMinute",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdaptiveTimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            )
        }
    }
}