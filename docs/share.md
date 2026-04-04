# Content Sharing

`calf-share` provides cross-platform content sharing using the platform's native share mechanism.

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-share)](https://search.maven.org/search?q=g:com.mohamedrejeb.calf)

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.mohamedrejeb.calf:calf-share:<version>")
}
```

## Quick Start

```kotlin
@OptIn(ExperimentalCalfApi::class)
@Composable
fun ShareButton() {
    val shareLauncher = rememberShareLauncher { result ->
        when (result) {
            ShareResult.Success -> println("Shared successfully")
            ShareResult.Dismissed -> println("User dismissed")
            ShareResult.Unavailable -> println("Sharing unavailable")
        }
    }

    Button(onClick = {
        shareLauncher.launch(ShareContent.Text(text = "Hello from Calf!"))
    }) {
        Text("Share")
    }
}
```

## ShareContent Types

### Text

Share plain text with an optional email subject:

```kotlin
shareLauncher.launch(
    ShareContent.Text(
        text = "Check out this library!",
        subject = "Calf Library",
    )
)
```

### URL

Share a URL. On iOS, this may show a rich link preview:

```kotlin
shareLauncher.launch(
    ShareContent.Url(
        url = "https://github.com/MohamedRejeb/Calf",
    )
)
```

### File

Share a single file with its MIME type:

```kotlin
shareLauncher.launch(
    ShareContent.File(
        file = kmpFile,
        mimeType = "image/png",
    )
)
```

### Multiple Files

Share multiple files:

```kotlin
shareLauncher.launch(
    ShareContent.Files(
        files = listOf(file1, file2),
        mimeType = "image/*",
    )
)
```

## Platform Behavior

| Platform | Mechanism | Text/URL | Files |
|----------|-----------|----------|-------|
| Android  | `Intent.createChooser()` | Native share sheet | Via content URIs |
| iOS      | `UIActivityViewController` | Native share sheet | Via file URLs |
| Desktop (macOS) | `NSSharingServicePicker` | Native share sheet | Native share sheet |
| Desktop (Windows/Linux) | Clipboard / `Desktop.browse()` | Clipboard copy (URLs open in browser) | Opens with default app |
| Web      | `navigator.share()` API | Native share or clipboard fallback | Via Web Share API (if browser supports it) |

## ShareResult

The `onResult` callback receives one of:

| Result | Meaning |
|--------|---------|
| `ShareResult.Success` | Content was shared (or action completed on desktop) |
| `ShareResult.Dismissed` | User dismissed the share sheet without sharing |
| `ShareResult.Unavailable` | Sharing is not available; a fallback (e.g., clipboard) may have been used |

!!! note "iPad"
    On iPad, the share sheet is presented as a popover anchored to the center of the screen.
