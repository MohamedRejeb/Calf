# AdaptiveBottomSheet

`AdaptiveBottomSheet` is a bottom sheet that adapts to the platform it is running on. It is a wrapper around `ModalBottomSheet` on Android and `UIModalPresentationPopover` on iOS, providing a native bottom sheet experience on each platform.

| Material (Android, Desktop, Web)                                | Cupertino (iOS)                                         |
|-----------------------------------------------------------------|---------------------------------------------------------|
| ![Bottom Sheet Android](../images/AdaptiveBottomSheet-android.png) | ![Bottom Sheet iOS](../images/AdaptiveBottomSheet-ios.png) |

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

