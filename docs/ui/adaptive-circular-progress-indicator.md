# Circular Progress Indicator

`AdaptiveCircularProgressIndicator` is a circular progress indicator that adapts to the platform it is running on. It is a wrapper around `CircularProgressIndicator` on Android, and it implements similar look to `UIActivityIndicatorView` on iOS.

| Material (Android, Desktop, Web) | Cupertino (iOS < 26) | Liquid Glass (iOS 26+) |
| --- | --- | --- |
| ![AdaptiveCircularProgressIndicator material](../images/AdaptiveCircularProgressIndicator-material.png) | ![AdaptiveCircularProgressIndicator ios](../images/AdaptiveCircularProgressIndicator-ios.png) | ![AdaptiveCircularProgressIndicator ios-liquid-glass](../images/AdaptiveCircularProgressIndicator-ios-liquid-glass.png) |

```kotlin
AdaptiveCircularProgressIndicator(
    modifier = Modifier.size(50.dp),
    color = Color.Red,
)
```