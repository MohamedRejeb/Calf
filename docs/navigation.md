# Adaptive Navigation

!!! warning "Experimental"
    The `AdaptiveNavDisplay` API is experimental (`@ExperimentalCalfNavigationApi`) and may change in a future release.

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-navigation)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-navigation%22)

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.calf:calf-navigation:{{ calf_version }}")
```

## Overview

`AdaptiveNavDisplay` wraps [Jetpack Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) with one API surface and per-platform rendering:

- **Android / Desktop / Web** — delegates to Nav3's `NavDisplay`, with each entry's top bar rendered as a Material 3 `TopAppBar` (or `LargeTopAppBar`).
- **iOS** — hosts a real `UINavigationController` inside the Compose surface. Every back-stack entry becomes a child `UIViewController`, so you get native push/pop animations, swipe-back from the edge, large-title shrink-on-scroll, and title cross-fade for free — without writing any Swift.

The Compose `NavBackStack` is the single source of truth on every platform: mutate it (`backStack.add(...)`, `backStack.removeLastOrNull()`) and the platform renderer follows.

!!! tip "Give the navigator the whole screen"
    `AdaptiveNavDisplay` draws its own top bar per entry — mount it as the root of the screen (typically with `Modifier.fillMaxSize()`) rather than inside another `Scaffold` that has its own top bar. Nesting two bars stacks them on top of each other, which hides the navigator's bar and back button.

## Quick start

```kotlin
@Serializable
data object Home : NavKey

@Serializable
data class Detail(val itemId: Int) : NavKey

@OptIn(ExperimentalCalfNavigationApi::class)
@Composable
fun App() {
    val backStack = rememberAdaptiveNavBackStack(Home)

    AdaptiveNavDisplay(backStack) { key ->
        when (key) {
            is Home -> adaptiveNavEntry(
                key = key,
                title = { Text("Home") },        // Material top bar
                iosTitle = "Home",               // native iOS nav bar
                largeTitle = true,
            ) { padding ->
                HomeScreen(
                    contentPadding = padding,
                    onItemClick = { id -> backStack.add(Detail(id)) },
                )
            }

            is Detail -> adaptiveNavEntry(
                key = key,
                title = { Text("Item ${key.itemId}") },
                navigationIcon = { BackButton { backStack.removeLastOrNull() } },
                iosTitle = "Item ${key.itemId}",
                iosBackTitle = "Home",
            ) { padding ->
                DetailScreen(itemId = key.itemId, padding = padding)
            }

            else -> error("Unknown key: $key")
        }
    }
}
```

## `adaptiveNavEntry`

Each route is declared with `adaptiveNavEntry`, which mirrors `AdaptiveTopBar`'s split between Material composable slots and iOS structured data:

| Parameter group | Parameters | Used on |
|---|---|---|
| Material slots | `title`, `navigationIcon`, `actions`, `expandedHeight`, `windowInsets`, `colors`, `scrollBehavior` | Android / Desktop / Web |
| iOS structured data | `iosTitle`, `iosBackTitle`, `iosLeadingItems`, `iosTrailingItems`, `iosConfiguration` | iOS |
| Cross-platform behavior | `largeTitle`, `barShown`, `iosGestureEnabled`, `iosPresentation` | All / iOS |

iOS bar options are structured data (not composables) because they are applied to `UINavigationItem` *before* the push animation starts — that's what makes the native title cross-fade animate with the correct content.

- `largeTitle = true` renders a collapsing `LargeTopAppBar` on Material (an exit-until-collapsed scroll behavior is wired automatically) and sets `largeTitleDisplayMode = .always` on iOS — the title shrinks on scroll on both.
- `barShown = false` hides the bar for that entry on every platform.
- `iosGestureEnabled = false` disables swipe-back from the edge for that entry.
- The screen `content` receives `PaddingValues` — the Material `Scaffold` padding, or the safe-drawing insets on iOS where content is laid out edge-to-edge under the translucent native bar. Apply it as `contentPadding` on a `LazyColumn` so the list scrolls under the bar but starts below it.

Bar items reuse the same `UIKitUIBarButtonItem` / `UIKitNavigationBarConfiguration` types as [`AdaptiveTopBar`](ui/adaptive-top-bar.md):

```kotlin
adaptiveNavEntry(
    key = key,
    actions = {
        IconButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Add") }
    },
    iosTrailingItems = listOf(
        UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Add, onClick = onAdd),
    ),
) { padding -> /* ... */ }
```

## Back navigation

- **iOS** renders the system back button automatically for every pushed entry (with `iosBackTitle` controlling the label shown on the *next* screen) and supports swipe-back from the edge out of the box.
- **Material platforms** show whatever you pass as `navigationIcon` — typically an arrow icon button that calls `backStack.removeLastOrNull()`. Android's system back is handled by Nav3's `NavDisplay` via the `onBack` parameter.

## State and ViewModels

- Each entry gets its own `ViewModelStoreOwner`: on Material platforms via the default `rememberViewModelStoreNavEntryDecorator()`, on iOS via the per-entry `UIViewController` scope. `viewModel<MyVm>()` inside an entry is scoped to that entry and cleared exactly when the entry leaves the back stack (including swipe-back, but not a cancelled swipe).
- `rememberAdaptiveNavBackStack` survives recomposition but **not** process death or Android configuration changes. For full state restoration, use upstream's `rememberNavBackStack(configuration, *entries)` with a serializers module that registers your `NavKey` types — its return type is the same `NavBackStack<NavKey>`.

## Current limitations

- `iosPresentation` values other than `NavPresentation.Card` (form sheet, page sheet, full-screen modal) fall back to `Card`. Modal presentation is a planned follow-up.
- Custom `entryDecorators` are not executed on iOS; the built-in lifecycle / ViewModel / saved-state behavior is provided instead.
- Bar item images on iOS support SF Symbols (`UIKitImage.SystemName`); `Vector` / `Bitmap` images are not rendered yet.
- `UIKitNavigationBarConfiguration.titleColor` is not applied by the navigator yet.
- Tabs (`UITabBarController`) and deep links are planned follow-ups.

## Relationship to `AdaptiveNavHost`

The older string-route `AdaptiveNavHost` / `AdaptiveNavHostController` API is still available and unchanged. It will be deprecated in favor of `AdaptiveNavDisplay` once the new API stabilizes.
