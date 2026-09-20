# Adaptive Navigation

!!! warning "Experimental"
    The `AdaptiveNavDisplay` and `AdaptiveTabDisplay` APIs are experimental (`@ExperimentalCalfNavigationApi`) and may change in a future release.

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-navigation)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-navigation%22)

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.calf:calf-navigation:{{ calf_version }}")
```

## Overview

`calf-navigation` wraps [Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) with one API surface and native rendering per platform:

| | Android / Desktop / Web | iOS |
|---|---|---|
| `AdaptiveNavDisplay` | Nav3 `NavDisplay` with predictive back; each entry's top bar is a Material 3 `TopAppBar` or `LargeTopAppBar` | A real `UINavigationController`: native push/pop, edge swipe-back, large-title shrink, title cross-fade |
| `AdaptiveTabDisplay` | Material 3 `NavigationSuiteScaffold`: bottom bar, rail or drawer chosen from the window size | A real `UITabBarController` with one `UINavigationController` per tab; adapts to iPad and iPhone Duo poses, optional sidebar |

The Compose `NavBackStack` is the single source of truth on every platform: mutate it (`backStack.add(...)`, `backStack.removeLastOrNull()`) and the platform follows. When the user pops natively on iOS, the back stack is trimmed to match.

!!! tip "Give the navigator the whole screen"
    Both displays paint their own bars. Mount them as the root of the screen (typically `Modifier.fillMaxSize()`) rather than inside another `Scaffold` with its own bars.

## Quick start: a stack

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

## Quick start: tabs

Each tab is a navigation stack whose root entry is the tab's own key. One `entryProvider` serves every tab.

```kotlin
@Serializable data object Home : NavKey
@Serializable data object Search : NavKey
@Serializable data object Profile : NavKey
@Serializable data class Detail(val itemId: Int) : NavKey

@OptIn(ExperimentalCalfNavigationApi::class)
@Composable
fun App() {
    val tabs = rememberAdaptiveTabState(Home, Search, Profile)

    AdaptiveTabDisplay(
        state = tabs,
        tabs = listOf(
            adaptiveTab(
                key = Home,
                icon = { Icon(Icons.Outlined.Home, null) },
                label = { Text("Home") },
                iosTitle = "Home",
                iosImage = UIKitImage.SystemName("house"),
                iosSelectedImage = UIKitImage.SystemName("house.fill"),
            ),
            adaptiveTab(
                key = Search,
                icon = { Icon(Icons.Outlined.Search, null) },
                label = { Text("Search") },
                badge = "3",
                iosTitle = "Search",
                iosImage = UIKitImage.SystemName("magnifyingglass"),
            ),
            adaptiveTab(
                key = Profile,
                icon = { Icon(Icons.Outlined.Person, null) },
                label = { Text("Profile") },
                iosTitle = "Profile",
                iosImage = UIKitImage.SystemName("person"),
            ),
        ),
        iosSidebarAdaptable = true, // sidebar on iPad and the iPhone Duo inner display
    ) { key ->
        when (key) {
            is Home -> adaptiveNavEntry(key, title = { Text("Home") }, iosTitle = "Home", largeTitle = true) { padding ->
                HomeList(padding) { id -> tabs.backStack.add(Detail(id)) }
            }
            is Detail -> adaptiveNavEntry(
                key = key,
                title = { Text("Item ${key.itemId}") },
                navigationIcon = { BackButton { tabs.backStack.removeLastOrNull() } },
                iosTitle = "Item ${key.itemId}",
                tabBarShown = false,             // tab bar leaves with the push
            ) { padding -> DetailScreen(key.itemId, padding) }
            is Search -> adaptiveNavEntry(key, title = { Text("Search") }, iosTitle = "Search") { padding -> SearchScreen(padding) }
            is Profile -> adaptiveNavEntry(key, title = { Text("Profile") }, iosTitle = "Profile") { padding -> ProfileScreen(padding) }
            else -> error("Unknown key: $key")
        }
    }
}
```

`AdaptiveTabState` exposes the selected tab, `backStack` (the selected tab's stack), `backStackOf(tab)`, `select(tab)` and `popToRoot(tab)`. Every tab keeps its stack, scroll positions and ViewModels while another tab is shown, on every platform. Selecting the already selected tab pops it to its root, as a native tab bar does.

**Back behavior**

- **iOS**: the system back button and edge swipe pop the current tab's stack. There is no back between tabs.
- **Android**: back pops the current tab's stack; at the root of a tab other than the start tab it returns to the start tab, then exits the app. This is the platform convention.
- **Desktop / Web**: back is only what your UI offers (a back button in the bar).

**Rapid taps and in-flight transitions**

A back-stack change that arrives while a transition is still animating is dropped and the stack reverted to what is on screen, the way Flutter ignores navigation during an animation. A double tap on a list row therefore pushes once. On iOS any change made mid-transition is dropped; on Android only pushes are dropped, so a predictive-back pop still completes. Desktop and web have no transitions and are unaffected.

**Screens above the tabs**

Put the tab display inside an entry of an outer `AdaptiveNavDisplay` (with `barShown = false`) to push a screen over the whole tab UI, for example a sign-out screen. On iOS the tab bar rides the push transition and returns with the back swipe, because it belongs to the view controller being covered.

## `adaptiveNavEntry`

Each route is declared with `adaptiveNavEntry`, which mirrors `AdaptiveTopBar`'s split between Material composable slots and iOS structured data:

| Parameter group | Parameters | Used on |
|---|---|---|
| Material slots | `title`, `navigationIcon`, `actions`, `expandedHeight`, `windowInsets`, `colors`, `scrollBehavior` | Android / Desktop / Web |
| iOS structured data | `iosTitle`, `iosBackTitle`, `iosLeadingItems`, `iosTrailingItems`, `iosConfiguration`, `iosGestureEnabled` | iOS |
| Cross-platform behavior | `largeTitle`, `barShown`, `tabBarShown` | All |

iOS bar options are structured data (not composables) because they are applied to `UINavigationItem` *before* the push animation starts. That is what makes the native title cross-fade animate with the right content.

- `largeTitle = true` renders a collapsing `LargeTopAppBar` on Material (an exit-until-collapsed scroll behavior is wired automatically) and a native large title on iOS that collapses as the content scrolls. Both work through nested scrolling, so a `LazyColumn` or `verticalScroll` inside the entry needs no extra setup. It is the only large-title switch.
- `barShown = false` hides the top bar for that entry on every platform.
- `tabBarShown = false` hides the tab bar of a hosting `AdaptiveTabDisplay` while the entry is on top: `hidesBottomBarWhenPushed` on iOS, an animated hide of the Material navigation bar elsewhere.
- `iosGestureEnabled = false` disables swipe-back for that entry: the edge swipe, and on iOS 26+ the swipe from anywhere in the content. The back button still works.
- The screen `content` receives `PaddingValues`: the Material `Scaffold` padding, or the safe-area insets on iOS where content is laid out edge-to-edge under the translucent bars. Apply it as `contentPadding` on a `LazyColumn` so the list scrolls under the bars but starts below them. On iOS the top value is the bar's resting height (large title expanded) and stays the same while the title collapses, exactly like the content inset of a native scroll view, so the content moves only with its own scroll. For a `verticalScroll` column put the padding inside the scroll (`Modifier.verticalScroll(state).padding(padding)`), not around it, so the content can scroll under the collapsed bar.

Bar items reuse the `UIKitUIBarButtonItem` / `UIKitNavigationBarConfiguration` types of [`AdaptiveTopBar`](ui/adaptive-top-bar.md), including SF Symbols, Compose vectors and bitmaps, dropdown menus and `titleColor`:

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

## `adaptiveTab`

| Parameter | Used on |
|---|---|
| `icon`, `label`, `alwaysShowLabel` | Material navigation item slots |
| `badge` | Both: a Material badge, the native `badgeValue` on iOS |
| `iosTitle`, `iosImage`, `iosSelectedImage` | Native `UITabBarItem` |

`AdaptiveTabDisplay` takes `iosConfiguration` (`UIKitTabBarConfiguration`: item tint colors, translucency), `iosSidebarAdaptable`, and on Material `materialLayoutType` (force a bottom bar, rail or drawer) and `materialColors`.

## State, ViewModels and persistence

- Each entry gets its own `ViewModelStoreOwner`: `viewModel<MyVm>()` inside an entry is scoped to that entry and cleared exactly when the entry leaves the back stack (including swipe-back, but not a cancelled swipe). On Material the default decorators do this; on iOS the per-entry view controller does.
- Inside tabs, each tab also gets its own ViewModel scope, so two tabs showing the same key never share a ViewModel.
- `rememberAdaptiveNavBackStack` and `rememberAdaptiveTabState` survive recomposition. Pass a `SavedStateConfiguration` whose serializers module registers your `NavKey` types to also survive Android configuration changes and process death:

```kotlin
val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Home::class)
            subclass(Detail::class)
        }
    }
}
val tabs = rememberAdaptiveTabState(Home, Search, Profile, configuration = config)
```

## Platform notes

- **iOS**: `entryDecorators` are not applied; the native per-entry scopes provide lifecycle, ViewModel and saved-state owners. `onBack` is not used; UIKit performs pops itself. A custom `iosConfiguration.titleColor` is applied per `UINavigationItem`, so the bar animates with the push and keeps a transparent scroll edge for large titles. UIKit drives large-title collapse and the bar's scroll-edge appearance from a scroll view, so each entry host keeps an invisible one fed by the content's nested scroll. Tab switches are instant on iOS, as in every native app; pushes inside a tab animate. An animated push starts with an empty entry and attaches the screen's content one main-thread turn later: Compose renders a surface's first frame synchronously, and UIKit cannot start the transition until that turn ends, so a heavy screen used to delay the animation itself. Now the slide starts right after the tap and the screen composes while it runs. The empty entry shows the system background, so an app that forces a theme different from the system appearance should keep its first frame cheap.
- **iPhone Duo / iPad**: the inner display is a regular-width, regular-height size class. `UITabBarController` adapts on its own; set `iosSidebarAdaptable = true` to allow the sidebar placement Apple recommends for richer navigation. Bars and toolbars are the system's, so they pick up iOS 27.1's vertical button layout without changes.
- **Android foldables and tablets**: the Material side uses `NavigationSuiteScaffold`, so a bottom bar becomes a rail on wide windows.

## Current limitations

- Modal presentation styles (sheets, full-screen modals) are not available yet; every entry is pushed.
- iOS 26's tab bar minimize-on-scroll needs a `UIScrollView` and does not trigger with Compose content.
- Deep links are not handled by the displays; drive the back stacks yourself.

## Relationship to `AdaptiveNavHost`

The older string-route `AdaptiveNavHost` / `AdaptiveNavHostController` API is still available and unchanged. It will be deprecated in favor of `AdaptiveNavDisplay` once the new API stabilizes.
