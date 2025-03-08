# AdaptiveDatePicker

`AdaptiveDatePicker` is a date picker that adapts to the platform it is running on. It is a wrapper around `DatePicker` on Android and `UIDatePicker` on iOS, providing a native date selection experience on each platform.

| Material (Android, Desktop, Web)                              | Cupertino (iOS)                                       |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![Date Picker Android](../images/AdaptiveDatePicker-android.png) | ![Date Picker iOS](../images/AdaptiveDatePicker-ios.png) |

## Usage

The `AdaptiveDatePicker` uses a state object to manage and track the selected date. You can observe changes to the selected date through the state.

```kotlin
// Create and remember the date picker state
val state = rememberAdaptiveDatePickerState()

// Optional: Set initial date (default is current date)
LaunchedEffect(Unit) {
    state.setSelection(Calendar.getInstance().apply {
        set(2023, 0, 1) // January 1, 2023
    }.timeInMillis)
}

// React to date changes
LaunchedEffect(state.selectedDateMillis) {
    val selectedDate = state.selectedDateMillis?.let { millis ->
        Calendar.getInstance().apply {
            timeInMillis = millis
        }
    }

    // Do something with the selected date
    selectedDate?.let {
        val year = it.get(Calendar.YEAR)
        val month = it.get(Calendar.MONTH) + 1 // Calendar months are 0-based
        val day = it.get(Calendar.DAY_OF_MONTH)
        println("Selected date: $year-$month-$day")
    }
}

// Display the date picker
AdaptiveDatePicker(
    state = state,
    modifier = Modifier.fillMaxWidth(),
    // Optional: Customize date constraints
    dateValidator = { timestamp ->
        // Example: Only allow dates from today forward
        timestamp >= Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
)
```

