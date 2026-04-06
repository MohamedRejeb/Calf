# SF Symbols & Cupertino Icons

Calf provides two ways to work with Apple-style icons:

| API | Module | Purpose | Icons | Platforms |
|-----|--------|---------|-------|-----------|
| `SFSymbol` | `calf-ui` | Type-safe SF Symbol **name** constants | 6,984 | iOS only (via `UIImage.systemImageNamed`) |
| `CupertinoIcons` + `CupertinoIcon` | `calf-cupertino-icons` | Cross-platform icon **rendering** from bundled font | 1,322 | All (Android, iOS, Desktop, Web) |

## When to use which

- **Building iOS-native UI** (e.g., `UIKitImage.SystemName` for `AdaptiveTopBar`, `AdaptiveNavigationBar`) → use `SFSymbol` names from `calf-ui`
- **Showing icons in Compose UI across all platforms** → use `CupertinoIcon` + `CupertinoIcons` from `calf-cupertino-icons`

## SFSymbol (included in calf-ui)

Type-safe string constants for all 6,984 Apple SF Symbol names (SF Symbols 1.0 through 7.0).

These are **just strings** — they work with `UIImage.systemImageNamed()` on iOS. They do not render anything on non-Apple platforms.

`SFSymbol` is included automatically when you add `calf-ui` — no additional dependency needed.

### Usage

```kotlin
// Before — error-prone raw strings:
UIKitImage.SystemName("house.fill")

// After — type-safe with autocomplete:
UIKitImage.SystemName(SFSymbol.houseFill)
```

Use with adaptive navigation components:

```kotlin
AdaptiveNavigationBar(
    iosItems = listOf(
        UIKitUITabBarItem(
            title = "Home",
            image = UIKitImage.SystemName(SFSymbol.houseFill),
        ),
        UIKitUITabBarItem(
            title = "Search",
            image = UIKitImage.SystemName(SFSymbol.magnifyingglass),
        ),
    ),
    // ...
)
```

### Naming Convention

Follows [SFSafeSymbols](https://github.com/SFSafeSymbols/SFSafeSymbols): dots are removed and the following character is capitalized.

| SF Symbol name | Kotlin constant |
|---------------|----------------|
| `house.fill` | `SFSymbol.houseFill` |
| `chevron.left` | `SFSymbol.chevronLeft` |
| `square.and.arrow.up` | `SFSymbol.squareAndArrowUp` |
| `0.circle` | `SFSymbol._0Circle` |

Browse all symbols in the [SF Symbols app](https://developer.apple.com/sf-symbols/).

---

## calf-cupertino-icons

Cross-platform Cupertino icon rendering using a bundled open-source font (MIT licensed, from Flutter's [cupertino_icons](https://github.com/nicklama/cupertino_icons) package).

Renders 1,322 icons on **all platforms** — Android, iOS, Desktop, and Web.

### Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-cupertino-icons)](https://search.maven.org/search?q=g:com.mohamedrejeb.calf)

```kotlin
commonMain.dependencies {
    implementation("com.mohamedrejeb.calf:calf-cupertino-icons:<version>")
}
```

### Usage

```kotlin
// Basic usage
CupertinoIcon(CupertinoIcons.houseFill)

// With customization
CupertinoIcon(
    iconCode = CupertinoIcons.heartFill,
    size = 32.dp,
    tint = Color.Red,
)
```

### API Reference

#### CupertinoIcon

```kotlin
@Composable
fun CupertinoIcon(
    iconCode: Int,              // Codepoint from CupertinoIcons
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,           // Icon size
    tint: Color = LocalContentColor.current,  // Icon color
    contentDescription: String? = null,
)
```

#### CupertinoIcons

Object containing 1,322 icon codepoint constants:

```kotlin
CupertinoIcons.house          // Home icon
CupertinoIcons.houseFill      // Filled home icon
CupertinoIcons.heart          // Heart outline
CupertinoIcons.heartFill      // Filled heart
CupertinoIcons.star           // Star outline
CupertinoIcons.starFill       // Filled star
CupertinoIcons.gear           // Settings gear
CupertinoIcons.search         // Search magnifying glass
CupertinoIcons.person         // Person silhouette
CupertinoIcons.bell           // Notification bell
// ... 1,312 more
```

### Comparison

| Feature | `SFSymbol` (in calf-ui) | `CupertinoIcons` (calf-cupertino-icons) |
|---------|------------------------|----------------------------------------|
| Type | `String` (symbol name) | `Int` (font codepoint) |
| Count | 6,984 | 1,322 |
| iOS | Native system rendering | Font-based rendering |
| Android | Not available | Font-based rendering |
| Desktop | Not available | Font-based rendering |
| Web | Not available | Font-based rendering |
| Use with | `UIKitImage.SystemName()` | `CupertinoIcon()` composable |
