# Navigation Bar

`AdaptiveNavigationBar` is an adaptive navigation bar that uses UIKit's `UITabBar` on iOS and Material3's `NavigationBar` on other platforms (Android, Desktop, Web).

On iOS, the native UITabBar is created using the `iosItems`, `iosSelectedIndex`, and `iosOnItemSelected` parameters. On Material platforms, the `content` lambda is used to provide full customization of the NavigationBar content (typically `NavigationBarItem`s).

| Material (Android, Desktop, Web)                                                       | Cupertino (iOS < 26)                                                            | Liquid Glass (iOS 26+)                                                                            |
|----------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| ![AdaptiveNavigationBar Material](../images/gif/AdaptiveNavigationBar-material.gif)    | ![AdaptiveNavigationBar iOS](../images/gif/AdaptiveNavigationBar-ios.gif)       | ![AdaptiveNavigationBar Liquid Glass](../images/gif/AdaptiveNavigationBar-ios-liquid-glass.gif)   |

## Usage

```kotlin
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun MyNavigationBar() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Search", "Profile")

    AdaptiveNavigationBar(
        // Material content
        content = {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = { Icon(Icons.Default.Home, contentDescription = item) },
                    label = { Text(item) },
                )
            }
        },
        // iOS-specific parameters
        iosItems = items.map { title ->
            UIKitUITabBarItem(title = title)
        },
        iosSelectedIndex = selectedIndex,
        iosOnItemSelected = { selectedIndex = it },
    )
}
```

## Parameters

| Parameter            | Description                                                                                                  |
|----------------------|--------------------------------------------------------------------------------------------------------------|
| `modifier`           | The modifier to be applied to the navigation bar.                                                            |
| `containerColor`     | The color used for the background of this navigation bar.                                                    |
| `contentColor`       | The preferred color for content inside this navigation bar.                                                  |
| `tonalElevation`     | The tonal elevation of this navigation bar.                                                                  |
| `windowInsets`       | The window insets to be applied to the navigation bar.                                                       |
| `iosItems`           | The list of tab bar items for the iOS UITabBar. See `UIKitUITabBarItem`.                                     |
| `iosSelectedIndex`   | The index of the currently selected item on iOS.                                                             |
| `iosOnItemSelected`  | Callback invoked when an item is selected on iOS, with the item index.                                       |
| `iosConfiguration`   | Configuration for the iOS UITabBar appearance (item tint colors, translucency). See `UIKitTabBarConfiguration`. |
| `content`            | The content of the navigation bar on Material platforms, typically `NavigationBarItem`s within a `RowScope`.  |

## iOS Configuration

You can customize the iOS tab bar appearance using `UIKitTabBarConfiguration`:

```kotlin
AdaptiveNavigationBar(
    iosItems = items,
    iosSelectedIndex = selectedIndex,
    iosOnItemSelected = { selectedIndex = it },
    iosConfiguration = UIKitTabBarConfiguration(
        selectedItemColor = Color.Red,
        unselectedItemColor = Color.Gray,
        isTranslucent = true,
    ),
    content = { /* Material content */ },
)
```

| Property              | Description                                                                                                |
|-----------------------|------------------------------------------------------------------------------------------------------------|
| `selectedItemColor`   | Tint color for selected tab bar items (icon + label). Maps to `UITabBar.tintColor`. Default: iOS system blue. |
| `unselectedItemColor` | Color for unselected tab bar items (icon + label). Maps to `UITabBar.unselectedItemTintColor`. Note: Liquid Glass (iOS 26+) ignores this property; use the per-state colors below instead. |
| `isTranslucent`       | Whether the tab bar is translucent. Default: `true`.                                                       |

### Per-state colors, title font and background

For finer control, set the per-state colors, the title font and the background color. They are
applied through `UITabBarAppearance` and take precedence over the tint colors for the state they
cover:

```kotlin
UIKitTabBarConfiguration(
    backgroundColor = Color.White,
    selectedIconColor = Color.Red,
    selectedTitleColor = Color.Red,
    unselectedIconColor = Color.Gray,
    unselectedTitleColor = Color.DarkGray,
    titleFont = UIKitFont.Named(name = "Rubik-Medium", size = 12f),
)
```

| Property               | Description                                                                           |
|------------------------|---------------------------------------------------------------------------------------|
| `backgroundColor`      | Background color of the tab bar, drawn over the translucent material when `isTranslucent` is `true`. |
| `selectedIconColor`    | Icon color of the selected item.                                                      |
| `selectedTitleColor`   | Title color of the selected item.                                                     |
| `unselectedIconColor`  | Icon color of unselected items.                                                       |
| `unselectedTitleColor` | Title color of unselected items.                                                      |
| `titleFont`            | Font of the item titles: `UIKitFont.System(size, weight)` or `UIKitFont.Named(postScriptName, size)`. `null` keeps the system font. |

!!! note
    `UIKitFont.Named` needs a font UIKit knows: bundle it through the `UIAppFonts` Info.plist key
    or register it at runtime (`CTFontManagerRegisterGraphicsFont`). Fonts loaded only through
    Compose resources are not visible to UIKit; an unknown name falls back to the system font.

## iOS Tab Bar Items

Use `UIKitUITabBarItem` to define items for the iOS UITabBar:

```kotlin
UIKitUITabBarItem(
    title = "Home",
    image = UIKitImage.SystemName(SFSymbol.house),
    selectedImage = UIKitImage.SystemName(SFSymbol.houseFill),
)
```

!!! tip
    Use type-safe `SFSymbol` constants from `calf-ui` instead of raw strings. See [SF Symbols & Cupertino Icons](../sf-symbols.md) for details.


### Images

`UIKitImage` supports several sources:

| Type                     | Description                                                                                  |
|--------------------------|----------------------------------------------------------------------------------------------|
| `UIKitImage.SystemName`  | An SF Symbol.                                                                                |
| `UIKitImage.Painter`     | Any Compose `Painter`, e.g. `painterResource(Res.drawable.home)` or `rememberVectorPainter(...)`. Drawn offscreen at the screen density, so even-odd fills, strokes, transforms and bitmaps are preserved. Set `renderingMode = UIKitImageRenderingMode.Original` for photos that must keep their colors. |
| `UIKitImage.Vector`      | An `ImageVector`, rasterized as a single filled shape (no fill rules, strokes or transforms). Prefer `Painter` for anything but simple icons. |
| `UIKitImage.Bitmap`      | An `ImageBitmap`. Pass `scale` so a bitmap rendered at the screen density shows at its intended point size. |

```kotlin
val homeIcon = painterResource(Res.drawable.ic_home)
val avatar = rememberAsyncImagePainter(user.avatarUrl)

UIKitUITabBarItem(title = "Home", image = UIKitImage.Painter(homeIcon))
UIKitUITabBarItem(
    title = "Profile",
    image = UIKitImage.Painter(avatar, renderingMode = UIKitImageRenderingMode.Original),
)
```
