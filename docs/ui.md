# Adaptive UI

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

| Kotlin version | Compose version | Calf version |
|----------------|-----------------|--------------|
| 2.0.0          | 1.6.11          | 0.5.2        |
| 1.9.22         | 1.6.0           | 0.4.1        |
| 1.9.21         | 1.5.11          | 0.3.1        |
| 1.9.20         | 1.5.10          | 0.2.0        |
| 1.9.0          | 1.5.0           | 0.1.1        |

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
api("com.mohamedrejeb.calf:calf-ui:0.5.2")
```

If you are using `calf-ui` artifact, make sure to export it to binaries:

#### Regular Framework
```kotlin
...
kotlin {
    ...
    targets
        .filterIsInstance<KotlinNativeTarget>()
        .filter { it.konanTarget.family == Family.IOS }
        .forEach {
            it.binaries.framework {
                ...
                export("com.mohamedrejeb.calf:calf-ui:0.5.2")
            }
        }
    ...
}
...
```

#### CocoaPods
```kotlin
...
kotlin {
    ...
    cocoapods {
        ...
        framework {
            ...
            export("com.mohamedrejeb.calf:calf-ui:0.5.2")
        }
    }
    ...
}
...
```

> **Important:** Exporting `calf-ui` to binaries is required to make it work on iOS.

## Usage

#### AdaptiveAlertDialog

`AdaptiveAlertDialog` is a dialog that adapts to the platform it is running on. It is a wrapper around `AlertDialog` on Android and `UIAlertController` on iOS.

| Material (Android, Desktop, Web)                                | Cupertino (iOS)                                         |
|-----------------------------------------------------------------|---------------------------------------------------------|
| ![Alert Dialog Android](images/AdaptiveAlertDialog-android.png) | ![Alert Dialog iOS](images/AdaptiveAlertDialog-ios.png) |

```kotlin
var showDialog by remember { mutableStateOf(false) }

Button(
    onClick = { showDialog = true },
) {
    Text("Show Alert Dialog")
}

if (showDialog) {
    AdaptiveAlertDialog(
        onConfirm = { showDialog = false },
        onDismiss = { showDialog = false },
        confirmText = "Ok",
        dismissText = "Cancel",
        title = "Alert Dialog",
        text = "This is a native alert dialog from Calf",
    )
}
```

#### AdaptiveBottomSheet

`AdaptiveBottomSheet` is a bottom sheet that adapts to the platform it is running on. It is a wrapper around `ModalBottomSheet` on Android and `UIModalPresentationPopover` on iOS.

| Material (Android, Desktop, Web)                                | Cupertino (iOS)                                         |
|-----------------------------------------------------------------|---------------------------------------------------------|
| ![Bottom Sheet Android](images/AdaptiveBottomSheet-android.png) | ![Bottom Sheet iOS](images/AdaptiveBottomSheet-ios.png) |

```kotlin
val scope = rememberCoroutineScope()
val sheetState = rememberAdaptiveSheetState()
var openBottomSheet by remember { mutableStateOf(false) }

Box(
    modifier = Modifier.fillMaxSize()
) {
    Button(
        onClick = { openBottomSheet = true },
    ) {
        Text("Show Bottom Sheet")
    }

    if (openBottomSheet) {
        AdaptiveBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            adaptiveSheetState = sheetState,
        ) {
            Button(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            openBottomSheet = false
                        }
                    }
                }
            ) {
                Text("Close")
            }
        }
    }
}
```

#### AdaptiveCircularProgressIndicator

`AdaptiveCircularProgressIndicator` is a circular progress indicator that adapts to the platform it is running on. It is a wrapper around `CircularProgressIndicator` on Android, and it implements similar look to `UIActivityIndicatorView` on iOS.

| Material (Android, Desktop, Web)                                                             | Cupertino (iOS)                                                                      |
|----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| ![Circular Progress Indicator Android](images/AdaptiveCircularProgressIndicator-android.png) | ![Circular Progress Indicator iOS](images/AdaptiveCircularProgressIndicator-ios.png) |

```kotlin
AdaptiveCircularProgressIndicator(
    modifier = Modifier.size(50.dp),
    color = Color.Red,
)
```

#### AdaptiveClickable

`.adaptiveClickable` is a clickable modifier that replaces indication on iOS with scaling effect.

```kotlin
Box(
    modifier = Modifier
        .size(50.dp)
        .background(Color.Red)
        .adaptiveClickable(
            shape = RoundedCornerShape(8.dp),
        ) {
            // Handle click
        }
)
```

#### AdaptiveDatePicker

`AdaptiveDatePicker` is a date picker that adapts to the platform it is running on. It is a wrapper around `DatePicker` on Android and `UIDatePicker` on iOS.

| Material (Android, Desktop, Web)                              | Cupertino (iOS)                                       |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![Date Picker Android](images/AdaptiveDatePicker-android.png) | ![Date Picker iOS](images/AdaptiveDatePicker-ios.png) |

```kotlin
val state = rememberAdaptiveDatePickerState()

LaunchedEffect(state.selectedDateMillis) {
    // Do something with the selected date
}

AdaptiveDatePicker(
    state = state,
)
```

#### AdaptiveTimePicker

`AdaptiveTimePicker` is a time picker that adapts to the platform it is running on. It is a wrapper around `TimePicker` on Android and `UIDatePicker` on iOS.

| Material (Android, Desktop, Web)                              | Cupertino (iOS)                                       |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![Time Picker Android](images/AdaptiveTimePicker-android.png) | ![Time Picker iOS](images/AdaptiveTimePicker-ios.png) |

```kotlin
val state = rememberAdaptiveTimePickerState()

LaunchedEffect(state.hour, state.minute) {
    // Do something with the selected time
}

AdaptiveTimePicker(
    state = state,
    modifier = Modifier
)
```
