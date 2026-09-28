# Bottom Sheet

`AdaptiveBottomSheet` is a bottom sheet that adapts to the platform it is running on. It is a wrapper around `ModalBottomSheet` on Android and `UIModalPresentationPopover` on iOS, providing a native bottom sheet experience on each platform.

| Material (Android, Desktop, Web)                                       | Cupertino (iOS < 26)                                                        | Liquid Glass (iOS 26+)                                                                        |
|------------------------------------------------------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| ![Bottom Sheet Android](../images/gif/AdaptiveBottomSheet-material.gif)     | ![Bottom Sheet iOS](../images/gif/AdaptiveBottomSheet-ios.gif)              | ![Bottom Sheet Liquid Glass](../images/gif/AdaptiveBottomSheet-ios-liquid-glass.gif)          |

## Usage

The `AdaptiveBottomSheet` is typically used to display additional content or actions without navigating away from the current screen. It slides up from the bottom on Android and appears as a popover on iOS.

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

## Placement on iPad and iPhone Duo

On iOS 27 and later the native sheet opens on the side of the window where the screen that presents it sits. A sheet presented from the detail column of a split view (calf-navigation's list-detail on an iPad or an open iPhone Duo) therefore opens over that column: on a half-open iPhone Duo it stays in the column's pane instead of iOS putting it in the leading pane, over the list. A screen that spans the whole window keeps the system placement. The sheet keeps UIKit's width, so on a flat, open iPhone Duo it can still reach across the fold.

Placement follows the presenting UIKit view controller. Two Compose panes in one view, as with Navigation 3's list-detail scene, share one controller, so their sheets keep the system placement.
