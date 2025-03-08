# Adaptive UI

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%22calf-ui%22)

| Kotlin version | Compose version | Calf version |
|----------------|-----------------|--------------|
| 2.1.10         | 1.7.3           | 0.7.1        |
| 2.1.0          | 1.7.3           | 0.7.0        |
| 2.0.21         | 1.7.0           | 0.6.1        |
| 2.0.10         | 1.6.11          | 0.5.5        |
| 1.9.22         | 1.6.0           | 0.4.1        |
| 1.9.21         | 1.5.11          | 0.3.1        |
| 1.9.20         | 1.5.10          | 0.2.0        |
| 1.9.0          | 1.5.0           | 0.1.1        |

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
api("com.mohamedrejeb.calf:calf-ui:0.7.1")
```

If you are using `calf-ui` artifact, make sure to export it to binaries:

#### Regular Framework
```kotlin
kotlin {
    targets
        .filterIsInstance<KotlinNativeTarget>()
        .filter { it.konanTarget.family == Family.IOS }
        .forEach {
            it.binaries.framework {
                export("com.mohamedrejeb.calf:calf-ui:0.7.1")
            }
        }
}
```

#### CocoaPods
```kotlin
kotlin {
    cocoapods {
        framework {
            export("com.mohamedrejeb.calf:calf-ui:0.7.1")
        }
    }
}
```

> **Important:** Exporting `calf-ui` to binaries is required to make it work on iOS.

## Components

Calf UI provides a set of adaptive UI components that adapt to the platform they are running on. Here's a list of available components:

- [AdaptiveAlertDialog](ui/adaptive-alert-dialog.md) - A dialog that adapts to the platform it is running on
- [AdaptiveBottomSheet](ui/adaptive-bottom-sheet.md) - A bottom sheet that adapts to the platform it is running on
- [AdaptiveCircularProgressIndicator](ui/adaptive-circular-progress-indicator.md) - A circular progress indicator that adapts to the platform it is running on
- [AdaptiveClickable](ui/adaptive-clickable.md) - A clickable modifier that replaces indication on iOS with scaling effect
- [AdaptiveDatePicker](ui/adaptive-date-picker.md) - A date picker that adapts to the platform it is running on
- [AdaptiveTimePicker](ui/adaptive-time-picker.md) - A time picker that adapts to the platform it is running on
