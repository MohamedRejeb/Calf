# File Picker

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22)

```kotlin
implementation("com.mohamedrejeb.calf:calf-file-picker:{{ calf_version }}")
```

## Quick Start

| Android                                                       | iOS                                                   |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![File Picker Android](images/AdaptiveFilePicker-android.png) | ![File Picker iOS](images/AdaptiveFilePicker-ios.png) |

| Desktop                                                       | Web                                                   |
|---------------------------------------------------------------|-------------------------------------------------------|
| ![File Picker Desktop](images/AdaptiveFilePicker-desktop.png) | ![File Picker Web](images/AdaptiveFilePicker-web.png) |

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

## Settings

Use `FilePickerSettings` to customize the dialog behavior:

```kotlin
val pickerLauncher = rememberFilePickerLauncher(
    type = FilePickerFileType.Image,
    settings = FilePickerSettings(
        title = "Select an image",
        initialDirectory = "/home/user/Pictures",
    ),
    onResult = { files -> /* ... */ }
)
```

Common properties available on all platforms:

- `title` - Dialog window title
- `initialDirectory` - Directory to open the dialog in

Desktop adds an additional property:

- `parentWindow` - The `ComposeWindow` to attach the dialog to (see [Desktop Setup](#desktop-setup))

## File Types

`FilePickerFileType` controls which files the user can select:

- `FilePickerFileType.Image` - Images only
- `FilePickerFileType.Video` - Videos only
- `FilePickerFileType.ImageVideo` - Images and videos
- `FilePickerFileType.Audio` - Audio files only
- `FilePickerFileType.Document` - Documents only
- `FilePickerFileType.Text` - Text files only
- `FilePickerFileType.Pdf` - PDF files only
- `FilePickerFileType.All` - All file types
- `FilePickerFileType.Folder` - Folders only

Filter by MIME type:

```kotlin
val type = FilePickerFileType.Custom(
    listOf("text/plain")
)
```

Filter by extension:

```kotlin
val type = FilePickerFileType.Extension(
    listOf("txt")
)
```

## Selection Modes

- `FilePickerSelectionMode.Single` - Pick a single file
- `FilePickerSelectionMode.Multiple` - Pick multiple files

## Desktop Setup

To attach the file dialog to the current window, you have three options:

#### Option 1: ProvideFilePickerParentWindow (recommended)

Wrap your content with `ProvideFilePickerParentWindow` to automatically provide the parent window to all file pickers in the tree:

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        ProvideFilePickerParentWindow {
            App()
        }
    }
}
```

#### Option 2: FrameWindowScope extension

Use the `FrameWindowScope.rememberFilePickerLauncher` extension, which auto-captures the window:

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        val pickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Image,
            onResult = { files -> /* ... */ }
        )
    }
}
```

#### Option 3: Explicit settings

Pass the window directly through `FilePickerSettings`:

```kotlin
val window = LocalFilePickerParentWindow.current

val pickerLauncher = rememberFilePickerLauncher(
    type = FilePickerFileType.Image,
    settings = FilePickerSettings(
        parentWindow = window,
    ),
    onResult = { files -> /* ... */ }
)
```

## Extensions

* Read the `ByteArray` of the file using the `readByteArray` extension function:

```kotlin
val context = LocalPlatformContext.current

LaunchedEffect(file) {
    val byteArray = file.readByteArray(context)
}
```

> The `readByteArray` extension function is a suspending function, so you need to call it from a coroutine scope.

> It's not recommended to use `readByteArray` extension function on large files, as it reads the entire file into memory.
> For large files, it's recommended to use the platform-specific APIs to read the file.
> You can read more about accessing the platform-specific APIs below.

* Check if the file exists using the `exists` extension function:

```kotlin
val context = LocalPlatformContext.current

val exists = file.exists(context)
```

* Get the file name using the `getName` extension function:

```kotlin
val context = LocalPlatformContext.current

val name = file.getName(context)
```

* Get the file path using the `getPath` extension function:

```kotlin
val context = LocalPlatformContext.current

val path = file.getPath(context)
```

* Check if the file is a directory using the `isDirectory` extension function:

```kotlin
val context = LocalPlatformContext.current

val isDirectory = file.isDirectory(context)
```

## Platform-specific APIs

KmpFile is a wrapper around platform-specific APIs,
you can access the native APIs for each platform using the following properties:

##### Android
```kotlin
val uri: Uri = kmpFile.uri
```

##### iOS
```kotlin
val nsUrl: NSURL = kmpFile.url
```

##### Desktop
```kotlin
val file: java.io.File = kmpFile.file
```

##### Web
```kotlin
val file: org.w3c.files.File = kmpFile.file
```

## Coil Extensions

In case you're using [Coil](https://github.com/coil-kt/coil) in your project, Calf has a dedicated package that includes utilities to ease the integration between both libraries.

You can use it by adding the following dependency to your module `build.gradle.kts` file:

```kotlin  
implementation("com.mohamedrejeb.calf:calf-file-picker-coil:{{ calf_version }}")  
```  

Currently, this package contains a `KmpFileFetcher` that you can use to let Coil know how to load a KmpFile by adding it to Coil's  `ImageLoader`:

```kotlin  
ImageLoader.Builder(context)  
 .components { add(KmpFileFetcher.Factory()) }
 .build()  
```  

For more info regarding how to extend the Image Pipeline in Coil, you can read [here](https://coil-kt.github.io/coil/image_pipeline/).
