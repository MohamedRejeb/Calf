# Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

| Kotlin version | Compose version | Calf version |
|----------------|-----------------|--------------|
| 1.9.22         | 1.6.0           | 0.4.1        |
| 1.9.21         | 1.5.11          | 0.3.1        |
| 1.9.20         | 1.5.10          | 0.2.0        |
| 1.9.0          | 1.5.0           | 0.1.1        |

Add the following dependencies to your module `build.gradle.kts` file:

```kotlin
// For Adaptive UI components
api("com.mohamedrejeb.calf:calf-ui:0.4.1")

// For Adaptive FilePicker
implementation("com.mohamedrejeb.calf:calf-file-picker:0.4.1")

// For Permissions
implementation("com.mohamedrejeb.calf:calf-permissions:0.4.1")
```

If you are using `calf-ui` artifact, make sure to export it to binaries:

### Regular Framewoek
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
                export("com.mohamedrejeb.calf:calf-ui:0.4.1")
            }
        }
    ...
}
...
```

### CocoaPods
```kotlin
...
kotlin {
    ...
    cocoapods {
        ...
        framework {
            ...
            export("com.mohamedrejeb.calf:calf-ui:0.4.1")
        }
    }
    ...
}
...
```

> **Important:** Exporting `calf-ui` to binaries is required to make it work on iOS.