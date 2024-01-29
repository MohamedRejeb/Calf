# File Picker

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22)

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.calf:calf-file-picker:0.3.1")
```

## Usage

Calf File Picker allows you to pick files from the device storage.

| Android                                                    | iOS                                                |
|------------------------------------------------------------|----------------------------------------------------|
| ![Web View Android](images/AdaptiveFilePicker-android.png) | ![Web View iOS](images/AdaptiveFilePicker-ios.png) |

```kotlin
val pickerLauncher = rememberFilePickerLauncher(
    type = FilePickerFileType.Image,
    selectionMode = FilePickerSelectionMode.Single,
    onResult = { files ->
        files.firstOrNull()?.let { file ->
            // Do something with the selected file
            // You can get the ByteArray of the file
            file.readByteArray()
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