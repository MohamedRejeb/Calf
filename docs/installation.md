# Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

Add the following dependencies to your module `build.gradle.kts` file:

```kotlin
// For Adaptive UI components
api("com.mohamedrejeb.calf:calf-ui:0.3.0")

// For Adaptive FilePicker
implementation("com.mohamedrejeb.calf:calf-file-picker:0.3.0")

// For Permissions
implementation("com.mohamedrejeb.calf:calf-permissions:0.3.0")
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
                export("com.mohamedrejeb.calf:calf-ui:0.3.0")
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
            export("com.mohamedrejeb.calf:calf-ui:0.3.0")
        }
    }
    ...
}
...
```

> **Important:** Exporting `calf-ui` to binaries is required to make it work on iOS.