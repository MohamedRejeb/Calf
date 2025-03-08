# AdaptiveClickable

`.adaptiveClickable` is a clickable modifier that adapts to the platform it is running on. On Android, it behaves like the standard `.clickable` modifier with ripple indication, while on iOS, it replaces the indication with a scaling effect that matches iOS design patterns.

## Usage

Use `.adaptiveClickable` when you want to provide a platform-specific click interaction:
- On Android: Standard Material ripple effect
- On iOS: Subtle scaling animation that follows iOS design guidelines

## Example

```kotlin
Box(
    modifier = Modifier
        .size(100.dp)
        .background(Color.Red, RoundedCornerShape(8.dp))
        .adaptiveClickable(
            // Optional parameters
            shape = RoundedCornerShape(8.dp),
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(), // Used on Android only
            enabled = true,
        ) {
            // Handle click
            println("Clicked!")
        }
)
```

