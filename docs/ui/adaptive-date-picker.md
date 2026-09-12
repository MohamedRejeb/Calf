# Date Picker

Calf provides two date pickers that adapt to the platform they run on:

1. **AdaptiveDatePicker**: the full calendar, always visible. Material3 `DatePicker` on Android, Desktop and Web; `UICalendarView` (iOS 16+) or `UIDatePicker` on iOS.
2. **AdaptiveCompactDatePicker**: a field showing the selected date that opens the calendar on tap. A `DatePickerDialog` on Material platforms; the system compact `UIDatePicker` on iOS.

Both share `AdaptiveDatePickerState` and support the same [selectable rules](#restricting-selectable-dates).

## Usage

The `AdaptiveDatePicker` uses a state object to manage and track the selected date. You can observe changes to the selected date through the state. Timestamps are UTC milliseconds at the start of the selected day.

```kotlin
// Create and remember the date picker state, optionally with an initial selection
val state = rememberAdaptiveDatePickerState(
    initialSelectedDateMillis = LocalDate(2026, 1, 1)
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds(),
)

// React to date changes
LaunchedEffect(state.selectedDateMillis) {
    val selectedDate = state.selectedDateMillis?.let { millis ->
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
    }
    println("Selected date: $selectedDate")
}

// Display the date picker
AdaptiveDatePicker(
    state = state,
    modifier = Modifier.fillMaxWidth(),
)
```

The examples use [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) for date math. Any source of UTC epoch milliseconds works.

## Restricting selectable dates

Every picker takes a Material3 `SelectableDates` rule through `selectableDates`. Calf ships `DateBounds` for the common case of a minimum and maximum day, and an infix `and` to combine rules.

### Bounds

```kotlin
val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

val state = rememberAdaptiveDatePickerState(
    selectableDates = DateBounds(
        minDateMillis = today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        maxDateMillis = today.plus(30, DateTimeUnit.DAY)
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds(),
    ),
)
```

Both bounds are inclusive and compared per UTC day, so any timestamp inside the first or last day keeps that whole day selectable. A `null` bound is open on that side. Besides greying out days, bounds are applied natively: the iOS wheels stop at the range and the calendars hide the months outside it, using the same calendar day in the device time zone. If the current selection falls outside a new range, it is moved to the nearest selectable day on every platform.

The rule is an observable property of the state, so it can depend on other state. A typical case is a date range where the end date cannot come before the start date:

```kotlin
val startState = rememberAdaptiveDatePickerState()
val endState = rememberAdaptiveDatePickerState(
    selectableDates = DateBounds(minDateMillis = startState.selectedDateMillis),
)
```

`DateBounds` is a data class, so passing an equal value on recomposition changes nothing.

### Rules for individual days

Rules that a min/max range cannot express, such as excluding weekends, are plain `SelectableDates` implementations. Combine them with bounds using `and`:

```kotlin
val weekdaysOnly = remember {
    object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val day = Instant.fromEpochMilliseconds(utcTimeMillis)
                .toLocalDateTime(TimeZone.UTC)
                .date
                .dayOfWeek
            return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
        }
    }
}

val state = rememberAdaptiveDatePickerState(
    selectableDates = DateBounds(minDateMillis = todayMillis) and weekdaysOnly,
)
```

Pass a stable instance, such as an `object`, a data class or a remembered value, so the picker is not re-evaluated on every recomposition.

| Platform | Behaviour |
|---|---|
| Material (Android, Desktop, Web) | Rejected days are disabled in the calendar grid. `isSelectableYear` disables years in the year picker. |
| iOS 16+ inline calendar (`UIKitDisplayMode.Picker`) | Backed by `UICalendarView`: rejected days are greyed out and cannot be tapped. Bounds coming from a `DateBounds` also hide the months outside the range. |
| iOS compact picker, wheels (`UIKitDisplayMode.Wheels`) and inline below iOS 16 | Backed by `UIDatePicker`, which cannot grey out days. Bounds coming from a `DateBounds` apply natively; picking any other rejected day snaps the control to the nearest selectable day. |

On every platform, a current selection that a new rule rejects is moved to the nearest selectable day. `isSelectableYear` has no effect on iOS.

## Compact picker

`AdaptiveCompactDatePicker` takes the same state as `AdaptiveDatePicker`, so the bounds and rules apply unchanged. It is the right choice for forms and for anything that should not take the full height of a calendar.

```kotlin
val state = rememberAdaptiveDatePickerState()

AdaptiveCompactDatePicker(
    state = state,
    enabled = true,
    // Material only: the field placeholder and the dialog buttons.
    materialPlaceholder = "Select date",
    materialConfirmText = "OK",
    materialDismissText = "Cancel",
)
```

| Platform | Behaviour |
|---|---|
| Material (Android, Desktop, Web) | An outlined button with the formatted date opens a `DatePickerDialog`. The day picked in the dialog reaches the state only on confirm; dismiss, or tapping outside, discards it. |
| iOS | The system compact `UIDatePicker`, which pops its calendar over the content. Changes apply immediately, as they do everywhere in iOS. Bounds coming from a `DateBounds` apply natively; the control cannot grey out days any other rule rejects, so such a pick snaps to the nearest selectable day. |

> `AdaptiveDatePicker` has no `dateValidator` parameter. An earlier version of this page documented one that never existed. Use `selectableDates` with `DateBounds` as described above instead.
