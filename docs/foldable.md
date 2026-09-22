# Foldable

`calf-foldable` tells your app what a foldable device is doing: how far it is opened, where the fold is,
and how many panes fit around it. The same API works on Android foldables and on iPhone Duo. On top of
that it exposes the iPhone Duo values that a Compose developer has no other way to reach.

<!-- MEDIA: the sample's Foldable screen on iPhone Duo, closed / book / open, side by side with a Pixel Fold -->

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-foldable)](https://search.maven.org/search?q=g:com.mohamedrejeb.calf)

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.mohamedrejeb.calf:calf-foldable:<version>")
}
```

The API is experimental: opt in with `@OptIn(ExperimentalCalfFoldableApi::class)`.

## Quick Start

```kotlin
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
fun Inbox() {
    val foldState = rememberFoldState()
    val windowSize = LocalWindowInfo.current.containerSize.toSize()
    val panes = foldState.calculatePanes(windowSize)

    if (panes.count == 2) {
        // Open foldable: list on one side of the fold, detail on the other.
        TwoPaneInbox(panes)
    } else {
        // Phone, closed foldable, desktop, web.
        SinglePaneInbox()
    }
}
```

`FoldState` is Compose state. Reading `posture`, `fold` or the panes recomposes only when the posture or
a region changes, which happens a few times per fold, not on every frame of the hinge moving.

## What you get

### Posture

```kotlin
when (foldState.posture) {
    FoldPosture.NoFold -> // no fold to take into account
    FoldPosture.Closed -> // folded shut, the app is on the cover display (iOS only)
    FoldPosture.HalfOpen -> // like a book or a laptop
    FoldPosture.Flat -> // opened all the way
}
```

`foldState.posture.isOpen` is `true` for `HalfOpen` and `Flat`.

### The fold

```kotlin
val fold: FoldInfo? = foldState.fold
fold?.bounds        // Rect in pixels, in the window's coordinate space
fold?.orientation   // FoldOrientation.Vertical (left | right) or Horizontal (top / bottom)
fold?.isSeparating  // the fold splits the content into two logical areas
```

A seamless fold has no thickness, so `bounds` is zero wide (or zero high). `foldState.isSeparating` is a
shortcut for `fold?.isSeparating == true`.

### Panes

```kotlin
val panes: FoldPanes = foldState.calculatePanes(windowSize, minPaneSize = 320.dp.toPx())

panes.count        // 1 or 2
panes.first        // the whole window, or the pane left of / above the fold
panes.second       // the pane right of / below the fold, null with one pane
panes.gap          // the strip between them to keep content out of (can be zero wide)
panes.orientation  // direction of the split, null with one pane
```

There are two panes when the device is open, the fold crosses the whole window and both sides are larger
than `minPaneSize`. While the fold separates the content on iOS, the gap also includes the margins UIKit
asks to keep clear around it. `calculateFoldPanes(...)` is the same logic as a pure function, if you want
to feed it your own values.

<!-- MEDIA: two panes laid out from FoldPanes on an open iPhone Duo, with the gap highlighted -->

### A tabletop layout from the raw values

Everything Calf builds on top of the fold uses this same public API, so you are never locked in. This is the layout of the sample's Foldable screen: half open with the fold running across the window, the content stays above the fold and the controls sit below it; in any other pose it is one scrolling column.

```kotlin
/** The two panes of a tabletop posture, `null` in every other pose (a half-open book, a flat device). */
fun tabletopPanes(posture: FoldPosture, panes: FoldPanes): FoldPanes? =
    panes.takeIf {
        posture == FoldPosture.HalfOpen && it.count == 2 && it.orientation == FoldOrientation.Horizontal
    }

@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
fun Player() {
    val foldState = rememberFoldState()
    val windowSize = LocalWindowInfo.current.containerSize.toSize()
    val tabletop = tabletopPanes(foldState.posture, foldState.calculatePanes(windowSize))
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize().onPlaced { positionInRoot = it.positionInRoot() }) {
        if (tabletop == null) {
            FullScreenPlayer()
        } else {
            // The panes are in the window's space; this layout starts at positionInRoot.
            val gap = requireNotNull(tabletop.gap).translate(-positionInRoot)
            val density = LocalDensity.current
            Column(Modifier.fillMaxSize()) {
                Video(Modifier.fillMaxWidth().height(with(density) { gap.top.toDp() }))   // above the fold
                Spacer(Modifier.height(with(density) { gap.height.toDp() }))              // the fold itself
                Controls(Modifier.fillMaxWidth().weight(1f))                               // below it
            }
        }
    }
}
```

The same helper keeps a Material `ModalBottomSheet` in the lower pane: give it `Modifier.padding(top = lowerPane.top.toDp())` (the sample's Bottom Sheet screen does this). Use a top padding rather than a height cap: the sheet computes its anchors from the space it is given, and a capped sheet would open half way up the pane.

<!-- MEDIA: the sample's Foldable screen half open with the fold across (iPhone Duo in Book pose rotated, Pixel Fold in landscape): values above the fold, controls below -->

### Material3 Adaptive

Compose Multiplatform's `currentWindowAdaptiveInfo()` works in common code, but its `windowPosture` is
always empty on iOS. `rememberCalfWindowAdaptiveInfo()` returns the same size class with the posture
from `rememberFoldState()` on the platforms where upstream leaves it empty, so the Material3 adaptive
scaffolds become fold-aware there too:

```kotlin
val adaptiveInfo = rememberCalfWindowAdaptiveInfo()
NavigationSuiteScaffold(layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)) { ... }
```

On Android it returns upstream's own value untouched: Material3 Adaptive already reads the
`FoldingFeature` there, and feeding the same fold twice would be wrong. On desktop and web there is no
fold to add. The pieces are public too: `foldState.toWindowPosture()` gives the `Posture` and
`WindowAdaptiveInfo(windowSizeClass, windowPosture)` is upstream's constructor.

Calf does not re-implement window size classes. Keep using `WindowSizeClass` for breakpoints.

## Navigation 3: two panes around the fold

An app on plain [Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) lays
a list and its detail out side by side with Material 3 adaptive's `rememberListDetailSceneStrategy`.
At the current versions that strategy never sees the fold on iOS, because the directive it builds comes
from a `windowPosture` that is empty there. `calf-foldable-navigation3` is the drop-in that fixes this,
without touching the app's navigation: `NavDisplay`, its transitions, shared elements and predictive
back stay exactly what Navigation 3 provides.

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-foldable-navigation3)](https://search.maven.org/search?q=g:com.mohamedrejeb.calf)

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.mohamedrejeb.calf:calf-foldable-navigation3:<version>")
}
```

One line changes:

```kotlin
@OptIn(ExperimentalCalfFoldableApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Inbox(backStack: NavBackStack<NavKey>) {
    NavDisplay(
        backStack = backStack,
        sceneStrategies = listOf(rememberCalfListDetailSceneStrategy()),
        entryProvider = entryProvider {
            entry<Inbox>(metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { Text("Select a message") })) {
                InboxList(onOpen = { id -> backStack.add(Message(id)) })
            }
            entry<Message>(metadata = ListDetailSceneStrategy.detailPane()) { key -> MessageScreen(key.id) }
        },
    )
}
```

`rememberCalfListDetailSceneStrategy` has the same parameters and defaults as upstream's function and
delegates to it. Only the default `directive` differs: it is `calfPaneScaffoldDirective(rememberCalfWindowAdaptiveInfo())`.

What that gives, platform by platform:

| Platform | Without Calf | With `rememberCalfListDetailSceneStrategy()` |
|---|---|---|
| iPhone Duo, open | Two panes (the inner display is expanded width), but the scaffold does not know where the fold is | Two panes; half open, the detail pane starts after the fold and the fold is kept clear |
| iPhone Duo, closed; iPhone | One pane | One pane |
| Android foldable | Upstream reads the fold itself | Unchanged: upstream's own value is passed through |
| Android foldable narrower than 840dp, open | One pane | Two panes from 600dp when a vertical hinge runs through the window |
| Android tablet, desktop, web | Two panes from 840dp | Unchanged |

The pane count is the one rule calf-navigation's Material list-detail uses, so an app that mixes both
paths gets the same answer on the same window. There is never a third pane.

`calfPaneScaffoldDirective(windowAdaptiveInfo, verticalHingePolicy)` is public for apps that build the
`ListDetailPaneScaffold` or `SupportingPaneScaffold` themselves. It returns upstream's own directive
whenever the rule changes nothing.

Two things to know:

- The excluded bounds are in window pixels, in the hosting view's space on iOS. They are right when the
  `NavDisplay` fills its hosting view, which is the same assumption upstream makes on Android.
- A shared element between the list and the detail should be applied only while one pane is shown. In
  two panes the row and the detail header are on screen at the same time, and two visible matches of
  one key are not a transition. `LocalListDetailSceneScope.current` is non-null only inside the
  two-pane scene, so it is the switch.

<!-- MEDIA: the sample's Navigation 3 List-Detail demo on iPhone Duo: closed (one pane, a tap animates the swatch into the detail header), open (two panes), book (the detail starts after the fold) -->

## Shared vs iOS only

Calf's rule: what both platforms can do is one common API. What only Android can do is left to Jetpack.
What only iOS can do is provided by Calf, because there is no other way to reach it from Compose, and it
is clearly marked: the name starts with `ios`, and using it needs `@OptIn(CalfIosOnlyApi::class)`.

| Member | Android | iOS | Desktop, web |
|---|---|---|---|
| `posture` | `Flat`, `HalfOpen`, otherwise `NoFold` | `Closed`, `HalfOpen`, `Flat`, otherwise `NoFold` | `NoFold` |
| `fold` | `FoldingFeature` bounds and orientation | the division region, without its margins | `null` |
| `isSeparating` | `FoldingFeature.isSeparating` | the posture is `HalfOpen` | `false` |
| `calculatePanes()` | same logic everywhere | same logic, plus UIKit's margins | one pane |
| `toWindowPosture()` | same values as Material3 Adaptive's own | fills Material3 Adaptive's empty posture | empty `Posture()` |
| `iosFoldRegion` | `null` | bounds including margins, margins, `isActive` | `null` |
| `iosOcclusionRegions` | empty | camera and sensor areas | empty |
| `iosHorizontalSizeClass`, `iosVerticalSizeClass` | `Unspecified` | UIKit size classes | `Unspecified` |
| `iosHingeAngle` | never emits | degrees, 0 (shut) to 180 (flat) | never emits |
| `iosCapabilities` | `Unavailable` | which APIs the running iOS has | `Unavailable` |
| `iosDebugDump()` | a note that this is not iOS, plus the snapshot | what iOS exposes and reports | same as Android |

### The hinge angle

The angle changes on every frame while the device moves, so it is a `Flow` and never recomposes anything
by itself. Collect it where the effect lives:

```kotlin
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
fun HingeTilt(foldState: FoldState, content: @Composable () -> Unit) {
    val angle by foldState.iosHingeAngle.collectAsState(initial = 180f)
    Box(Modifier.graphicsLayer { rotationX = (180f - angle) / 10f }) { content() }
}
```

Lay out from `posture`, not from the angle. iOS decides when a posture changes: it reports `Closed` at
roughly 55°, long before the angle reaches 0.

### Size classes

`iosHorizontalSizeClass` and `iosVerticalSizeClass` are the signal iOS uses for its own containers. iPhone
Duo's cover display is compact wide and its inner display is regular wide. Unlike a width breakpoint they
also follow iPad Split View and Slide Over.

### Diagnosing a device you do not have

```kotlin
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
fun report(foldState: FoldState): String = foldState.iosDebugDump()
```

The dump lists the SDK the app was built with, which iOS 27.1 classes and selectors exist, and the current
values. `iosCapabilities.hasHinge` is `true` once iOS has delivered a hinge and `null` until then. It never
becomes `false`, because iOS does not announce that a device has no hinge.

## Platform notes

### iOS

!!! warning "Build your app with the iOS 27.1 SDK (Xcode 27.1 or later)"
    iOS only reports reserved regions to apps built with the iOS 27.1 SDK. With an older Xcode,
    `posture` and `iosHingeAngle` still work on an iPhone Duo, but `fold`, `iosFoldRegion` and
    `iosOcclusionRegions` stay empty, so `calculatePanes()` returns one pane. The system also runs such
    an app in a smaller compatibility size. `iosDebugDump()` shows the SDK your app was built with.

- The fold APIs exist from iOS 27.1. On older versions everything reports no fold; nothing crashes.
- Calf itself builds with Xcode 26 and later. You do not need Xcode 27.1 to depend on `calf-foldable`,
  only to get regions at runtime.
- Regions follow the hinge with a delay: a few milliseconds when flattening, about a second when folding.
  That is why `isSeparating` follows the posture and not the region's `isActive`.
- `fold.bounds` is the crease itself. UIKit's region is wider because it includes margins for interactive
  content: on iPhone Duo 20pt on each side of a zero-wide crease. Read them from `iosFoldRegion`.
- Bounds are in the coordinate space of the view controller that hosts the composition calling
  `rememberFoldState()`.
- On the simulator, if regions are unexpectedly empty, reboot the simulator.

### Android

Calf reads Jetpack WindowManager's `FoldingFeature` of the window the composition is in.

- Android has no "closed" state: a closed foldable shows your app on its outer display, which has no
  folding feature, so `posture` is `NoFold`.
- If a device reports several folding features, the shared model describes the first one.

Calf deliberately does not wrap what only Android has. Use Jetpack directly:

| You need | Use |
|---|---|
| The hinge angle | `SensorManager` with `Sensor.TYPE_HINGE_ANGLE` |
| Whether the hinge hides content (dual-screen devices) | `FoldingFeature.occlusionType` |
| Every display feature | `WindowInfoTracker.getOrCreate(context).windowLayoutInfo(context)` |
| Which postures the device supports | `WindowInfoTracker.supportedPostures` |
| Rear display and dual display modes | `WindowAreaController` |
| Camera cutouts | `WindowInsets.displayCutout` |
| Side-by-side activities | Activity embedding (`androidx.window.embedding`) |

### Desktop and web

There is no fold API. `rememberFoldState()` always reports `FoldSnapshot.NoFold`, so fold-aware code needs
no platform check.

## Previews and tests

`FoldState(snapshot)` builds a state with fixed values, and `LocalFoldStateOverride` makes
`rememberFoldState()` return it for everything below:

```kotlin
@OptIn(ExperimentalCalfFoldableApi::class)
@Preview
@Composable
fun InboxHalfOpenPreview() {
    val fold = FoldInfo(
        bounds = Rect(1000f, 0f, 1000f, 1400f),
        orientation = FoldOrientation.Vertical,
        isSeparating = true,
    )
    val halfOpen = FoldState(FoldSnapshot(posture = FoldPosture.HalfOpen, fold = fold))

    CompositionLocalProvider(LocalFoldStateOverride provides halfOpen) {
        Inbox()
    }
}
```

The `ios` values can be set on `FoldSnapshot` too, and `FoldState(snapshot, iosHingeAngle = flowOf(90f))`
fakes the angle.

## API reference

| API | Description |
|---|---|
| `rememberFoldState()` | Observes the fold for as long as the caller is in the composition |
| `FoldState` | `snapshot`, `posture`, `fold`, `isSeparating`, `calculatePanes()`, `toWindowPosture()` and the `ios` members |
| `rememberCalfWindowAdaptiveInfo()` | Material3 Adaptive's `WindowAdaptiveInfo` with the fold filled in where upstream leaves it empty |
| `FoldSnapshot` | All values at one moment, as an immutable data class. `FoldSnapshot.NoFold` is the empty one |
| `FoldPosture` | `NoFold`, `Closed`, `HalfOpen`, `Flat`, and `isOpen` |
| `FoldInfo`, `FoldOrientation` | The fold's bounds, orientation and `isSeparating` |
| `FoldPanes`, `calculateFoldPanes()` | The pane helper and its pure function |
| `FoldRegion`, `FoldMargins` | An iOS reserved region: `bounds`, `margins`, `isActive`, `reservedBounds` |
| `IosSizeClass`, `IosFoldCapabilities` | iOS-only value types |
| `FoldState(snapshot, iosHingeAngle)` | A state with fixed values for previews and tests |
| `LocalFoldStateOverride` | Replaces what `rememberFoldState()` returns below it |
| `@ExperimentalCalfFoldableApi` | Opt-in for the whole module |
| `@CalfIosOnlyApi` | Opt-in for members that only act on iOS (in `calf-core`) |

`calf-foldable-navigation3`:

| API | Description |
|---|---|
| `rememberCalfListDetailSceneStrategy(...)` | Upstream's `rememberListDetailSceneStrategy` with a fold-aware default directive; same parameters |
| `calfPaneScaffoldDirective(windowAdaptiveInfo, verticalHingePolicy)` | Upstream's `calculatePaneScaffoldDirective` with two panes on an open book-style foldable from the medium width |
