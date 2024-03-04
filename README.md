# Calf - Compose Adaptive Look & Feel

Calf is a library that allows you to easily create adaptive UIs for your Compose Multiplatform apps.


[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![MohamedRejeb](https://raw.githubusercontent.com/MohamedRejeb/MohamedRejeb/main/badges/mohamedrejeb.svg)](https://github.com/MohamedRejeb)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![BuildPassing](https://shields.io/badge/build-passing-brightgreen)](https://github.com/MohamedRejeb/ksoup/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

![Calf thumbnail](docs/images/thumbnail.png)

Calf stands for **C**ompose **A**daptive **L**ook & **F**eel

## Artifacts

| Artifact              | Description                               | Platforms                            | Version                                                                                                                                                                                           |
|-----------------------|-------------------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **calf-ui**           | Adaptive UI components                    | Android, iOS, Desktop, Web(Js, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)                   |
| **calf-file-picker**  | Native File Picker wrapper                | Android, iOS, Desktop, Web(Js, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22) |
| **calf-permissions**  | API that allows you to handle permissions | Android, iOS                         | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22) |                                                                                                                                                                        
| **calf-geo**          | API that allows you to access geolocation | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-navigation**   | Native navigation wrapper                 | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-map**          | Native Maps wrapper                       | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-media**        | Video/Audio player                        | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-notification** | Notification manager                      | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-sf-symbols**   | Apple SF Symbols icons                    | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |

> The main focus for now is Android and iOS, but more Desktop components are coming that allows you to create adaptive UIs for Desktop as well (Windows, macOS, Linux)

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

| Kotlin version | Compose version | Calf version |
|----------------|-----------------|--------------|
| 1.9.22         | 1.6.0           | 0.4.0        |
| 1.9.21         | 1.5.11          | 0.3.1        |
| 1.9.20         | 1.5.10          | 0.2.0        |
| 1.9.0          | 1.5.0           | 0.1.1        |

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
// For Adaptive UI components
api("com.mohamedrejeb.calf:calf-ui:0.4.0")

// For Adaptive FilePicker
implementation("com.mohamedrejeb.calf:calf-file-picker:0.4.0")
```

If you are using `calf-ui` artifact, make sure to export it to binaries:

#### Regular Framewoek
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
                export("com.mohamedrejeb.calf:calf-ui:0.4.0")
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
            export("com.mohamedrejeb.calf:calf-ui:0.4.0")
        }
    }
    ...
}
...
```

> **Important:** Exporting `calf-ui` to binaries is required to make it work on iOS.

## Usage

### Calf UI

#### AdaptiveAlertDialog

`AdaptiveAlertDialog` is a dialog that adapts to the platform it is running on. It is a wrapper around `AlertDialog` on Android and `UIAlertController` on iOS.

| Android                                                         | iOS                                                     |
|-----------------------------------------------------------------|---------------------------------------------------------|
| ![Alert Dialog Android](docs/images/AdaptiveAlertDialog-android.png) | ![Alert Dialog iOS](docs/images/AdaptiveAlertDialog-ios.png) |

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

| Android                                                         | iOS                                                     |
|-----------------------------------------------------------------|---------------------------------------------------------|
| ![Bottom Sheet Android](docs/images/AdaptiveBottomSheet-android.png) | ![Bottom Sheet iOS](docs/images/AdaptiveBottomSheet-ios.png) |

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

| Android                                                                                      | iOS                                                                                  |
|----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| ![Circular Progress Indicator Android](docs/images/AdaptiveCircularProgressIndicator-android.png) | ![Circular Progress Indicator iOS](docs/images/AdaptiveCircularProgressIndicator-ios.png) |

```kotlin
AdaptiveCircularProgressIndicator(
    modifier = Modifier.size(50.dp),
    color = Color.Red,
)
```

#### AdaptiveDatePicker

`AdaptiveDatePicker` is a date picker that adapts to the platform it is running on. It is a wrapper around `DatePicker` on Android and `UIDatePicker` on iOS.

| Android                                                       | iOS                                                   |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![Date Picker Android](docs/images/AdaptiveDatePicker-android.png) | ![Date Picker iOS](docs/images/AdaptiveDatePicker-ios.png) |

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

| Android                                                       | iOS                                                   |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![Time Picker Android](docs/images/AdaptiveTimePicker-android.png) | ![Time Picker iOS](docs/images/AdaptiveTimePicker-ios.png) |

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

#### WebView

`WebView` is a view that adapts to the platform it is running on. It is a wrapper around `WebView` on Android, `WKWebView` on iOS and JavaFX `WebView` on Desktop.

| Android                                         | iOS                                     |
|-------------------------------------------------|-----------------------------------------|
| ![Web View Android](docs/images/WebView-android.png) | ![Web View iOS](docs/images/WebView-ios.png) |

```kotlin
val state = rememberWebViewState(
    url = "https://github.com/MohamedRejeb"
)

LaunchedEffect(state.isLoading) {
    // Get the current loading state
}

WebView(
    state = state,
    modifier = Modifier
        .fillMaxSize()
)
```

#### Web View Settings

You can customize the web view settings by changing the `WebSettings` object in the `WebViewState`:

```kotlin
val state = rememberWebViewState(
    url = "https://github.com/MohamedRejeb"
)

LaunchedEffect(Unit) {
    // Enable JavaScript
    state.settings.javaScriptEnabled = true

    // Enable Zoom in Android
    state.settings.androidSettings.supportZoom = true
}
```

#### Call JavaScript

You can call JavaScript functions from the web view by using the `evaluateJavaScript` function:

```kotlin
val state = rememberWebViewState(
    url = "https://github.com/MohamedRejeb"
)

LaunchedEffect(Unit) {
    val jsCode = """
        document.body.style.backgroundColor = "red";
        document.title
    """.trimIndent()

    // Evaluate the JavaScript code
    state.evaluateJavaScript(jsCode) {
        // Do something with the result
        println("JS Response: $it")
    }
}
```

> **Note:** The `evaluateJavaScript` method only works when you enable JavaScript in the web view settings.

### Calf File Picker

Calf File Picker allows you to pick files from the device storage.

| Android                                                            | iOS                                                        |
|--------------------------------------------------------------------|------------------------------------------------------------|
| ![File Picker Android](docs/images/AdaptiveFilePicker-android.png) | ![File Picker iOS](docs/images/AdaptiveFilePicker-ios.png) |

| Desktop                                                            | Web                                                        |
|--------------------------------------------------------------------|------------------------------------------------------------|
| ![File Picker Desktop](docs/images/AdaptiveFilePicker-desktop.png) | ![File Picker Web](docs/images/AdaptiveFilePicker-web.png) |

```kotlin
val scope = rememberCoroutineScope()
val context = LocalPlatformContext.current

val pickerLauncher = rememberFilePickerLauncher(
    type = FilePickerFileType.Image,
    selectionMode = FilePickerSelectionMode.Single,
    onResult = { files ->
        scope.launch {
            files.firstOrNull()?.let { file ->
                // Do something with the selected file
                // You can get the ByteArray of the file
                file.readByteArray(context)
            }
        }
    }
)

Button(
    onClick = {
        pickerLauncher.launch()
    },
    modifier = Modifier.padding(16.dp)
) {
    Text("Open File Picker")
}
```

#### FilePickerFileType

`FilePickerFileType` allows you to specify the type of files you want to pick:

* `FilePickerFileType.Image` - Allows you to pick images only
* `FilePickerFileType.Video` - Allows you to pick videos only
* `FilePickerFileType.ImageView` - Allows you to pick images and videos only
* `FilePickerFileType.Audio` - Allows you to pick audio files only
* `FilePickerFileType.Document` - Allows you to pick documents only
* `FilePickerFileType.Text` - Allows you to pick text files only
* `FilePickerFileType.Pdf` - Allows you to pick PDF files only
* `FilePickerFileType.Presentation` - Allows you to pick presentation files only
* `FilePickerFileType.Spreadsheet` - Allows you to pick spreadsheet files only
* `FilePickerFileType.Word` - Allows you to pick compressed word only
* `FilePickerFileType.All` - Allows you to pick all types of files
* `FilePickerFileType.Folder` - Allows you to pick folders

You can also specify the file types you want to pick by using the `FilePickerFileType.Custom` type:

```kotlin
val type = FilePickerFileType.Custom(
    "text/plain"
)
```

#### FilePickerSelectionMode

`FilePickerSelectionMode` allows you to specify the selection mode of the file picker:

* `FilePickerSelectionMode.Single` - Allows you to pick a single file
* `FilePickerSelectionMode.Multiple` - Allows you to pick multiple files

Read the full file picker documentation [here](docs/filepicker.md).

## Contribution
If you've found an error in this sample, please file an issue. <br>
Feel free to help out by sending a pull request :heart:.

[Code of Conduct](https://github.com/MohamedRejeb/Calf/blob/main/CODE_OF_CONDUCT.md)

## Find this library useful? :heart:
Support it by joining __[stargazers](https://github.com/MohamedRejeb/Calf/stargazers)__ for this repository. :star: <br>
Also, __[follow me](https://github.com/MohamedRejeb)__ on GitHub for more libraries! 🤩

You can always <a href="https://www.buymeacoffee.com/MohamedRejeb"><img src="https://img.buymeacoffee.com/button-api/?text=Buy me a coffee&emoji=&slug=MohamedRejeb&button_colour=FFDD00&font_colour=000000&font_family=Cookie&outline_colour=000000&coffee_colour=ffffff"></a>

# License
```
Copyright 2023 Mohamed Rejeb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
