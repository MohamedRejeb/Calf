# AdaptiveTimePicker

`AdaptiveTimePicker` is a time picker that adapts to the platform it is running on. It is a wrapper around `TimePicker` on Android and `UIDatePicker` (in time mode) on iOS, providing a native time selection experience on each platform.

| Material (Android, Desktop, Web)                              | Cupertino (iOS)                                       |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![Time Picker Android](../images/AdaptiveTimePicker-android.png) | ![Time Picker iOS](../images/AdaptiveTimePicker-ios.png) |

## Usage

The `AdaptiveTimePicker` uses a state object to manage and track the selected time. You can observe changes to the hour and minute through the state.

```kotlin
// Create and remember the time picker state
val state = rememberAdaptiveTimePickerState()

// Optional: Set initial time (default is current time)
LaunchedEffect(Unit) {
    state.setHour(14)    // 2 PM
    state.setMinute(30)  // 30 minutes
}

// React to time changes
LaunchedEffect(state.hour, state.minute) {
    // Format the time
    val hour = state.hour
    val minute = state.minute
    val formattedTime = String.format("%02d:%02d", hour, minute)

    // Do something with the selected time
    println("Selected time: $formattedTime")
}

// Display the time picker
AdaptiveTimePicker(
    state = state,
    modifier = Modifier.fillMaxWidth(),
    // Optional: Configure time picker options
    is24Hour = true  // Use 24-hour format instead of AM/PM
)
```

