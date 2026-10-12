# Toolbar

`AdaptiveToolbar` is an adaptive toolbar that uses a native `UIToolbar` with Liquid Glass on iOS 26+ and a Material 3 Expressive `HorizontalFloatingToolbar` on other platforms (Android, Desktop, Web, and older iOS).

This toolbar is intended for contextual action buttons (not navigation), similar to a web browser's bottom toolbar with back, forward, share, and bookmark buttons.

| Material (Android, Desktop, Web) | Cupertino (iOS < 26) | Liquid Glass (iOS 26+) |
| --- | --- | --- |
| ![AdaptiveToolbar material](../images/gif/AdaptiveToolbar-material.gif) | ![AdaptiveToolbar ios](../images/gif/AdaptiveToolbar-ios.gif) | ![AdaptiveToolbar ios-liquid-glass](../images/gif/AdaptiveToolbar-ios-liquid-glass.gif) |

## Usage

```kotlin
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun MyToolbar() {
    var expanded by remember { mutableStateOf(true) }

    AdaptiveToolbar(
        expanded = expanded,
        // Material content
        content = {
            IconButton(onClick = { /* back */ }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = { /* forward */ }) {
                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = { /* share */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        },
        // iOS-specific parameters
        iosItems = listOf(
            UIKitUIBarButtonItem.systemItem(
                systemItem = UIKitUIBarButtonSystemItem.Reply,
                onClick = { /* back */ }
            ),
            UIKitUIBarButtonItem.flexibleSpace(),
            UIKitUIBarButtonItem.systemItem(
                systemItem = UIKitUIBarButtonSystemItem.Action,
                onClick = { /* share */ }
            ),
        ),
    )
}
```

## Parameters

| Parameter            | Description                                                                                                      |
|----------------------|------------------------------------------------------------------------------------------------------------------|
| `expanded`           | Whether the toolbar is in its expanded state.                                                                    |
| `modifier`           | The modifier to be applied to the toolbar.                                                                       |
| `leadingContent`     | Optional leading content for the Material 3 Expressive floating toolbar.                                         |
| `trailingContent`    | Optional trailing content for the Material 3 Expressive floating toolbar.                                        |
| `content`            | The main content composable for the Material 3 Expressive floating toolbar. Typically a row of `IconButton`s.    |
| `iosItems`           | The list of bar button items for the iOS native toolbar. See `UIKitUIBarButtonItem`.                             |

## Inside the bar host (iOS)

With [`AdaptiveBarHostUIViewController`](../ui.md#installing-the-bar-host-ios) as the app's iOS entry point, `iosItems` become the `toolbarItems` of the host's `UINavigationController`: the system toolbar at the bottom of the screen, which appears and leaves with the screen that shows it. On an iPhone Duo its items join the vertical bar below the navigation bar's items; when the vertical bar is full they go to its overflow menu first.
