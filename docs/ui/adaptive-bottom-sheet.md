# Bottom Sheet

`AdaptiveBottomSheet` is a bottom sheet that adapts to the platform it is running on. It is a wrapper around `ModalBottomSheet` on Android, Desktop and Web, and a native sheet (`UISheetPresentationController`, with the medium and large detents) on iOS.

| Material (Android, Desktop, Web) | Cupertino (iOS < 26) | Liquid Glass (iOS 26+) |
| --- | --- | --- |
| ![AdaptiveBottomSheet material](../images/gif/AdaptiveBottomSheet-material.gif) | ![AdaptiveBottomSheet ios](../images/gif/AdaptiveBottomSheet-ios.gif) | ![AdaptiveBottomSheet ios-liquid-glass](../images/gif/AdaptiveBottomSheet-ios-liquid-glass.gif) |

## Usage

The `AdaptiveBottomSheet` is typically used to display additional content or actions without navigating away from the current screen. It slides up from the bottom on every platform; on iOS it is the system sheet.

```kotlin
// Create necessary state and scope
val scope = rememberCoroutineScope()
val sheetState = rememberAdaptiveSheetState()
var openBottomSheet by remember { mutableStateOf(false) }

Box(
    modifier = Modifier.fillMaxSize()
) {
    // Button to trigger the bottom sheet
    Button(
        onClick = { openBottomSheet = true },
        modifier = Modifier.align(Alignment.Center)
    ) {
        Text("Show Bottom Sheet")
    }

    // Show the bottom sheet when state is true
    if (openBottomSheet) {
        AdaptiveBottomSheet(
            // Called when the user dismisses the sheet
            onDismissRequest = { openBottomSheet = false },
            // State object to control the sheet
            adaptiveSheetState = sheetState,
            // Optional: Customize the sheet appearance
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            // Optional: Set the sheet content color
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            // Content of the bottom sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Bottom Sheet Content",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button to close the sheet programmatically
                Button(
                    onClick = {
                        // Hide the sheet with animation
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            // Update the state after animation completes
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
}
```

## The grabber and the sheet's colour on iOS

`dragHandle` decides whether the sheet shows the system grabber (`prefersGrabberVisible`); the composable itself is not drawn, UIKit draws its own. UIKit draws the grabber over the content without insetting it, so when it is shown Calf keeps the content 24dp down from the top, the iOS counterpart of the room Material's drag handle slot takes. Pass `dragHandle = null` for no grabber and no inset.

`containerColor` paints the whole sheet, from the grabber to the bottom edge at every detent, not only the height of your content. The content itself is a `Column`: give it its own padding and, for more than fits the medium detent, its own vertical scroll.

## Placement on iPad and iPhone Duo

The native sheet goes where iOS puts it, like the sheets of Apple's own apps: centred on the window, on an iPhone Duo opened flat across the fold and clear of the vertical bar, and in the leading pane when the Duo is half open (book pose), wherever it was presented from. Moving it to the presenting column's side would put part of it under the Duo's vertical bar and camera.

When the sheet does reach under an unsafe part of the window (the vertical bar of a closed iPhone Duo, the Dynamic Island in iPhone landscape), its content is kept out of it on the sides: Calf pads it by the sheet's own safe area, so no control ends up under the bar. The bottom is left to your content, as in any UIKit sheet. Compose's `WindowInsets` inside the sheet describe the window, not the sheet, so don't pad the sheet's content with them.

<!-- MEDIA: a closed iPhone Duo with AdaptiveBottomSheet at the large detent, content clear of the vertical bar -->
