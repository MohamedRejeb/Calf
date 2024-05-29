# File Picker

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22)

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.calf:calf-file-picker:0.4.1")
```

## Usage

Calf File Picker allows you to pick files from the device storage.


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

#### Extensions

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

#### Platform-specific APIs

KmpFile is a wrapper around platform-specific APIs, so you can use the platform-specific APIs to read the file:

##### Android
```kotlin
val uri: Uri = kmpFile.uri
```

##### iOS
```kotlin
val nsUrl: NSURL = kmpFile.nsUrl
```

##### Desktop
```kotlin
val file: java.io.File = kmpFile.file
```

##### Web
```kotlin
val file: org.w3c.files.File = kmpFile.file
```
