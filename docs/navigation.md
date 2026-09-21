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

On iOS, a back-stack change that arrives while a push or pop is still animating is dropped and the stack reverted to what is on screen. `UINavigationController` cannot start a transition inside another one, and applying the change late leaves the Compose stack and the UIKit stack out of step (stalled or doubled navigation). A double tap on a list row therefore pushes once on iOS.

On Android, Desktop and Web, Calf adds nothing on top of Navigation 3: changes made mid-transition behave as in any Nav3 app. If you want a double tap to push once there as well, guard it where you navigate (for example, ignore the push when the key is already on top of the stack).

**Screens above the tabs**

Put the tab display inside an entry of an outer `AdaptiveNavDisplay` (with `barShown = false`) to push a screen over the whole tab UI, for example a sign-out screen. On iOS the tab bar rides the push transition and returns with the back swipe, because it belongs to the view controller being covered.

## List-detail

A list and its detail are ordinary entries of **one** back stack. When there is room they are shown side by side; when there is not, the detail is a normal push over the list. The back stack has the same shape in both layouts, so opening or closing an iPhone Duo, rotating an iPad or resizing a desktop window re-arranges the screen and keeps the selected item, scroll positions, the screens the detail pushed, ViewModels and saved state.

<!-- MEDIA: iPhone Duo closed → open with a message and its attachment open: the stack becomes two columns, scroll position and counter unchanged -->

Turn it on with `listDetail` and mark the entries:

```kotlin
@Serializable data object Inbox : NavKey
@Serializable data class Message(val id: Int) : NavKey
@Serializable data class Attachment(val id: Int) : NavKey

val backStack = rememberAdaptiveNavBackStack(Inbox, configuration = config)

AdaptiveNavDisplay(
    backStack = backStack,
    listDetail = rememberAdaptiveListDetailSettings(),
) { key ->
    when (key) {
        is Inbox -> adaptiveNavEntry(
            key = key,
            iosTitle = "Inbox",
            paneRole = AdaptivePaneRole.List(
                detailPlaceholder = { Text("Select a message") },
            ),
        ) { padding ->
            InboxScreen(padding, onOpen = { id -> backStack.selectDetail(list = Inbox, detail = Message(id)) })
        }

        is Message -> adaptiveNavEntry(
            key = key,
            iosTitle = "Message",
            paneRole = AdaptivePaneRole.Detail,
        ) { padding ->
            MessageScreen(key.id, padding, onOpenAttachment = { backStack.add(Attachment(key.id)) })
        }

        // No role: it was pushed after the detail, so it stays in the detail pane.
        is Attachment -> adaptiveNavEntry(key, iosTitle = "Attachment") { padding -> AttachmentScreen(key.id, padding) }

        else -> error("Unknown key $key")
    }
}
```

- `AdaptivePaneRole.List` marks the list. Its `detailPlaceholder` fills the detail pane while two panes are shown and nothing is open. It is not an entry: it has no bar and is never on the back stack.
- `AdaptivePaneRole.Detail` marks the **first** entry of the detail side. Whatever is pushed after it stays on the detail side without being marked, so a detail can push its own screens. An entry without a role that is pushed over the list *before* any detail (a filter screen) stays on the list side.
- `backStack.selectDetail(list, detail)` replaces whatever is above `list` with `detail`. Use it instead of `add` for list taps: in two panes the list stays visible, and `add` would grow the stack by one detail per tap.
- **Start the stack at the list.** Entries below the list are shown full screen on Android, Desktop and Web, but inside the narrow list column on iOS (like Mailboxes → Inbox in Mail). Screens that come before the list belong in an outer `AdaptiveNavDisplay`, the way the tabs are nested in [Quick start: tabs](#quick-start-tabs). A stack without a list entry is never split, so a deep link to a detail has to put the list below it: `rememberAdaptiveNavBackStack(Inbox, Message(42))`.
- On Android **pass a `SavedStateConfiguration`** (see [State, ViewModels and persistence](#state-viewmodels-and-persistence)). Folding or unfolding recreates the Activity unless the app handles those configuration changes itself, and a back stack without a configuration starts over.

### With tabs

`AdaptiveTabDisplay` takes the same `listDetail` parameter. A tab whose **root entry** is an `AdaptivePaneRole.List` is laid out as list-detail; the other tabs stay plain stacks, so a settings tab is not squeezed into a list column on a wide screen. Re-selecting a list-detail tab pops it to the list.

On iOS the split view sits *inside* the tab. That is the nesting iOS adapts: on iPhone Duo the tab bar joins the detail's bar items in the vertical strip, open and closed.

<!-- MEDIA: open iPhone Duo, Mail tab of the sample: list column, detail column, tab bar and the detail's bar items in the vertical strip -->

### What happens when

| | One pane | Two panes |
|---|---|---|
| Visible | The top entry, pushed over the rest. | The list beside the top entry of the detail side, or beside the placeholder. |
| Going from two panes to one | | The detail side's top entry is on top if one is open, otherwise the list. |
| Back | System back and predictive back on Android; back button and swipe on iOS. One entry per step. | Android: one entry per step, the last step leaves the placeholder. iOS: the detail column swipes and pops its own screens; its first entry has no back button, another item is opened from the list. |
| Selecting another item (`selectDetail`) | The new detail is pushed. | The open detail and everything it pushed are replaced; the list keeps its scroll position. |
| Popping the last detail entry | Back to the list. | The placeholder. |
| Popping the list on iOS while a detail is open | | The detail goes with it: it belonged to that list. |
| Deep link to a detail | Seed the stack with the list below it. | Same stack, two panes. |
| Process death | The back stack is restored through its `SavedStateConfiguration`. The arrangement is derived from it and the window, never stored. | Same. |

What survives a layout change is everything an entry keeps while it is on the back stack: its ViewModels and its `rememberSaveable` state, lazy list positions included. Entries that stay on screen keep plain `remember` state too. As in any Navigation 3 app, an entry that is covered in one pane keeps its saved state and ViewModels but not plain `remember`.

### Which platform shows two panes when

| Platform | Two panes when | Decided by |
|---|---|---|
| iPhone | Never (always one pane). | `UISplitViewController`: compact width. |
| iPhone Duo | Open, flat or half open. Closed is one pane. | `UISplitViewController`: the inner display is regular width. Half open, iOS lines the list column up with the fold. |
| iPad | Whenever the window is regular width; one pane in a narrow Split View or Stage Manager window. With the default `Automatic` display mode iOS may show the list as an overlay opened from the bar button in portrait; `IosSplitViewDisplayMode.OneBesideSecondary` keeps both columns side by side. | `UISplitViewController`: the window's horizontal size class. |
| Android phone | Never. | Window width below 600dp. |
| Android foldable, open | From 600dp when a vertical hinge runs through the window, otherwise from 840dp. Half open, the panes sit on either side of the hinge. | Material 3 adaptive, with Calf raising the pane count for an open book-style foldable. |
| Android tablet, Desktop, Web | Window width of 840dp and up. | Material 3 adaptive's expanded width class. |

Calf does not debounce the pane count. On iPhone Duo the system container does not flicker while the device opens, and on Android a fold or unfold produces a single change.

<!-- MEDIA: desktop window dragged across 840dp with a detail open: two panes ↔ one pane -->
<!-- MEDIA: Pixel Fold folded ↔ unfolded on the Mail tab of the sample -->

### iOS only: the split view

`rememberAdaptiveListDetailSettings(iosSplitView = …)` configures the `UISplitViewController`. These values do nothing on other platforms, so creating the configuration asks for `@OptIn(CalfIosOnlyApi::class)`; taking the defaults does not.

```kotlin
@OptIn(CalfIosOnlyApi::class)
val listDetail = rememberAdaptiveListDetailSettings(
    iosSplitView = IosSplitViewConfiguration(
        preferredDisplayMode = IosSplitViewDisplayMode.OneBesideSecondary,
        splitBehavior = IosSplitViewBehavior.Tile,
    ),
)
```

| Value | `UISplitViewController` | Default |
|---|---|---|
| `preferredDisplayMode` | `preferredDisplayMode`: `Automatic`, `SecondaryOnly`, `OneBesideSecondary`, `OneOverSecondary` | `Automatic` |
| `splitBehavior` | `preferredSplitBehavior`: `Automatic`, `Tile`, `Overlay`, `Displace` | `Automatic` |
| `primaryColumnWidth`, `minPrimaryColumnWidth`, `maxPrimaryColumnWidth` | The matching column width properties, in dp (one dp is one point). | `null`: the system's width, which is what follows the fold on iPhone Duo. |
| `primaryEdge` | `primaryEdge`: `Leading`, `Trailing` | `Leading` |
| `presentsWithGesture` | `presentsWithGesture` | `true` |
| `showsSecondaryOnlyButton` | `showsSecondaryOnlyButton` | `false` |

For anything else, `iosMain` code can reach the controller: `IosSplitViewConfiguration(...).withSplitViewController { splitViewController -> … }`. Leave its delegate and its column controllers alone; they are how Calf keeps the back stack in charge of what each column shows.

**Android, Desktop and Web have no such knobs in Calf.** Pane widths, the drag handle, pane expansion and other scene options belong to Material 3 adaptive. If you need them, use Navigation 3's `NavDisplay` with `rememberListDetailSceneStrategy(...)` and `ListDetailSceneStrategy.listPane()` / `detailPane()` from `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` directly.

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
val tabs = rememberAdaptiveTabState<NavKey>(Home, Search, Profile, configuration = config)
```

Pass `<NavKey>` explicitly when the tabs are `@Serializable` objects: on iOS and Web the serialization plugin gives them an extra supertype, and the inferred tab type (`NavKey & SerializerFactory`) no longer matches an `AdaptiveTabState<NavKey>` parameter.

## Platform notes

- **iOS**: `entryDecorators` are not applied; the native per-entry scopes provide lifecycle, ViewModel and saved-state owners. `onBack` is not used; UIKit performs pops itself. A custom `iosConfiguration.titleColor` is applied per `UINavigationItem`, so the bar animates with the push and keeps a transparent scroll edge for large titles. UIKit drives large-title collapse and the bar's scroll-edge appearance from a scroll view, so each entry host keeps an invisible one fed by the content's nested scroll. Tab switches are instant on iOS, as in every native app; pushes inside a tab animate. An animated push starts with an empty entry and attaches the screen's content one main-thread turn later: Compose renders a surface's first frame synchronously, and UIKit cannot start the transition until that turn ends, so a heavy screen used to delay the animation itself. Now the slide starts right after the tap and the screen composes while it runs. The empty entry shows the system background, so an app that forces a theme different from the system appearance should keep its first frame cheap.
- **iPhone Duo / iPad**: the inner display is a regular-width, regular-height size class. `UITabBarController` adapts on its own; set `iosSidebarAdaptable = true` to allow the sidebar placement Apple recommends for richer navigation. Bars and toolbars are the system's, so they pick up iOS 27.1's vertical button layout without changes.
- **Android foldables and tablets**: the Material side uses `NavigationSuiteScaffold`, so a bottom bar becomes a rail on wide windows.

## Current limitations

- Modal presentation styles (sheets, full-screen modals) are not available yet; every entry is pushed.
- iOS 26's tab bar minimize-on-scroll needs a `UIScrollView` and does not trigger with Compose content.
- Deep links are not handled by the displays; drive the back stacks yourself. For a list-detail display, put the list below the detail.
- List-detail has two panes, never a third (supporting) one.

## API reference

Everything below needs `@OptIn(ExperimentalCalfNavigationApi::class)`.

### `rememberAdaptiveNavBackStack`

| Parameter | Description |
|---|---|
| `vararg startEntries` | The keys the stack starts with; the last one is the screen shown first. |
| `configuration` | `SavedStateConfiguration` used to persist the stack. `null` (default): the stack survives recomposition only. |

### `AdaptiveNavDisplay`

| Parameter | Description |
|---|---|
| `backStack` | The Nav3 back stack to render. Add and remove keys to navigate. |
| `modifier` | Modifier for the display. |
| `onBack` | Called for a back event on Material platforms. Default: removes the last key. Not used on iOS, where UIKit pops and the stack follows. |
| `entryDecorators` | Nav3 entry decorators. Default: saveable state + per-entry ViewModel store. Not applied on iOS. |
| `listDetail` | `AdaptiveListDetailSettings` to lay the stack out as [list-detail](#list-detail). `null` (default): a plain stack. |
| `entryProvider` | Maps a key to its entry. Build entries with `adaptiveNavEntry`. |

### `adaptiveNavEntry`

| Parameter | Description |
|---|---|
| `key` | The `NavKey` this entry renders. |
| `title`, `navigationIcon`, `actions` | Material `TopAppBar` slots. Ignored on iOS. |
| `expandedHeight`, `windowInsets`, `colors`, `scrollBehavior` | Material `TopAppBar` overrides. `null` (default) keeps Material's defaults. Ignored on iOS. |
| `largeTitle` | `LargeTopAppBar` on Material, a large `UINavigationBar` title on iOS. Default `false`. |
| `iosTitle` | Title of the native navigation bar. |
| `iosBackTitle` | Title of the back button shown by the *next* screen. `null` (default): UIKit's default. |
| `iosLeadingItems`, `iosTrailingItems` | Native bar button items. |
| `iosConfiguration` | Native bar appearance (`UIKitNavigationBarConfiguration`). |
| `barShown` | Shows the top bar. Default `true`. |
| `tabBarShown` | Inside `AdaptiveTabDisplay`: keeps the tab bar while this entry is on top. Default `true`. |
| `iosGestureEnabled` | Allows the interactive swipe back, both the edge swipe and iOS 26's content swipe. Default `true`. |
| `paneRole` | The entry's place in a [list-detail](#list-detail) layout: `AdaptivePaneRole.None` (default), `List(detailPlaceholder)` or `Detail`. Only read by a display that was given `listDetail`. |
| `content` | The screen. Apply the `PaddingValues` it receives. |

### `rememberAdaptiveTabState` and `AdaptiveTabState`

| Parameter | Description |
|---|---|
| `vararg tabs` | The tab keys in bar order. Distinct, at least one. Each is the root of its own stack. |
| `startTab` | The tab selected first, and the one Android's back returns to. Default: the first tab. |
| `configuration` | Persists every tab's stack, as for `rememberAdaptiveNavBackStack`. The selected tab is always saved. |

| Member | Description |
|---|---|
| `tabs`, `startTab` | What the state was created with. |
| `selectedTab` | The selected tab. |
| `backStack` | The selected tab's stack. |
| `backStackOf(tab)` | The stack of any tab. |
| `select(tab)` | Selects a tab; selecting the selected tab pops it to its root. |
| `popToRoot(tab)` | Trims a tab's stack to its root. Default: the selected tab. |

If the tab list changes while the state is remembered, the selection restarts from `startTab`.

### `adaptiveTab`

| Parameter | Description |
|---|---|
| `key` | The tab's key; must be one of the state's tabs. |
| `icon`, `label`, `alwaysShowLabel` | Material navigation item slots. `label` defaults to `null`, `alwaysShowLabel` to `true`. |
| `badge` | Badge text on both platforms. `null` (default): no badge. |
| `iosTitle`, `iosImage`, `iosSelectedImage` | The native `UITabBarItem`. |

### `AdaptiveTabDisplay`

| Parameter | Description |
|---|---|
| `state` | The `AdaptiveTabState`. |
| `tabs` | One `adaptiveTab` per state tab, in the same order. |
| `modifier` | Modifier for the display. |
| `entryDecorators` | Nav3 entry decorators for every tab's stack. `null` (default): saveable state + per-entry ViewModel store, scoped per tab. Not applied on iOS. |
| `materialLayoutType` | Forces bottom bar, rail or drawer. `null` (default): chosen from the window size. |
| `materialColors` | `NavigationSuiteColors`. `null` (default): Material's defaults. |
| `iosConfiguration` | Native tab bar appearance (`UIKitTabBarConfiguration`). |
| `iosSidebarAdaptable` | iOS 18+: lets the tab bar become a sidebar on wide displays. Default `false`. |
| `listDetail` | Lays out every tab whose root entry is an `AdaptivePaneRole.List` as [list-detail](#with-tabs). `null` (default): plain stacks. |
| `entryProvider` | Maps a key to its entry, for every tab. |

### List-detail

| API | Description |
|---|---|
| `AdaptivePaneRole` | `None` (default), `List(detailPlaceholder)`, `Detail`. Passed to `adaptiveNavEntry(paneRole = …)`. |
| `AdaptiveListDetailSettings(iosSplitView)`, `rememberAdaptiveListDetailSettings(iosSplitView)` | Turns a display into list-detail when passed as `listDetail`. |
| `IosSplitViewConfiguration(...)` | iOS only, `@CalfIosOnlyApi`. See [the split view](#ios-only-the-split-view). |
| `IosSplitViewConfiguration.withSplitViewController { }` | iOS only, `iosMain`. Direct access to the `UISplitViewController`. |
| `NavBackStack<NavKey>.selectDetail(list, detail)` | Replaces whatever is above `list` with `detail`. Selecting the detail that is already on top changes nothing; without `list` on the stack it pushes. |

## Relationship to `AdaptiveNavHost`

The older string-route `AdaptiveNavHost` / `AdaptiveNavHostController` API is still available and unchanged. It will be deprecated in favor of `AdaptiveNavDisplay` once the new API stabilizes.
