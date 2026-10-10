# Adaptive UI

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%22calf-ui%22)

| Kotlin version | Compose version | Calf version |
|----------------|-----------------|--------------|
| 2.4.20         | 1.12.0          | 0.14.0       |
| 2.4.10         | 1.11.1          | 0.13.0       |
| 2.3.20         | 1.10.3          | 0.12.0       |
| 2.3.20         | 1.10.3          | 0.11.0       |
| 2.3.20         | 1.10.3          | 0.10.0       |
| 2.2.21         | 1.9.2           | 0.9.0        |
| 2.1.21         | 1.8.0           | 0.8.0        |
| 2.1.10         | 1.7.3           | 0.7.1        |
| 2.1.0          | 1.7.3           | 0.7.0        |
| 2.0.21         | 1.7.0           | 0.6.1        |
| 2.0.10         | 1.6.11          | 0.5.5        |
| 1.9.22         | 1.6.0           | 0.4.1        |
| 1.9.21         | 1.5.11          | 0.3.1        |
| 1.9.20         | 1.5.10          | 0.2.0        |
| 1.9.0          | 1.5.0           | 0.1.1        |

> **Android:** Calf 0.14.0+ is built on Compose Multiplatform 1.12, whose Android artifacts require `compileSdk = 37` (Android Gradle Plugin 9.4 is the first release that supports it). Calf's Android `minSdk` stays 23.

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
api("com.mohamedrejeb.calf:calf-ui:{{ calf_version }}")
```

If you are using `calf-ui` artifact, make sure to export it to binaries:

#### Regular Framework
```kotlin
kotlin {
    targets
        .filterIsInstance<KotlinNativeTarget>()
        .filter { it.konanTarget.family == Family.IOS }
        .forEach {
            it.binaries.framework {
                export("com.mohamedrejeb.calf:calf-ui:{{ calf_version }}")
            }
        }
}
```

#### CocoaPods
```kotlin
kotlin {
    cocoapods {
        framework {
            export("com.mohamedrejeb.calf:calf-ui:{{ calf_version }}")
        }
    }
}
```

> **Important:** Exporting `calf-ui` to binaries is required to make it work on iOS.

## Installing the bar host (iOS)

On iOS, `AdaptiveTopBar`, `AdaptiveNavigationBar` and `AdaptiveToolbar` are native UIKit bars. On their own they are standalone views drawn over your Compose content, so they cannot take part in your navigation's transitions: a pushed screen's bar slides in with the page, and a tab bar stays on screen until the old screen is gone ([#530](https://github.com/MohamedRejeb/Calf/issues/530)). iOS also only moves bars owned by its containers into the vertical bar of an iPhone Duo.

If your app navigates with plain Compose navigation (Navigation 3, Navigation Compose or your own), make the bar host its iOS entry point instead of `ComposeUIViewController`:

```kotlin
// iosMain
fun MainViewController(): UIViewController =
    AdaptiveBarHostUIViewController {
        App()
    }
```

Nothing else changes in your code. Every screen that uses `AdaptiveScaffold` with these bars now gets them as the bars of real UIKit containers: one `UINavigationController` navigation bar and toolbar, and a `UITabBarController` tab bar once a screen shows tabs. Your navigation, transitions, shared elements and predictive back stay in Compose, in one Compose hierarchy; the containers only show the bars.

What you get:

- **The bars stay in place** while Compose moves between screens. When another screen takes over, its title and items crossfade in, and a tab bar leaves as soon as a screen without one appears.
- **Large titles** (`UIKitNavigationBarConfiguration(prefersLargeTitles = true)`) expand and collapse with your content's scrolling. Any nested-scrolling child (`LazyColumn`, `verticalScroll`) takes part.
- **iPhone Duo:** the bar items, the toolbar items and the tab bar move into the vertical bar next to the content, on the side iOS picks, and the content is inset on that side. When the vertical bar is full, items go to its overflow menu, lowest `visibilityPriority` first. With the Duo open in portrait the bar shares the status bar row, as native apps do.
- **Padding:** `AdaptiveScaffold` passes the room the containers' bars leave to its `content`, on all four edges, as far as it reaches that scaffold. A scaffold nested in a padded one, or a pane beside another, is not padded twice. Compose's own `WindowInsets` include the containers' bars too.
- **Stable during transitions:** a screen coming in is laid out with the insets of its own bars from its first frame, and a screen going out keeps the insets it had until it is gone, however long the transition or the back gesture takes. Neither resizes while Compose moves them.
- **Two panes:** when the screen that owns the bars is a pane (the detail of a list-detail layout on an iPad or an open iPhone Duo), the bars cover only that pane, the way a split view's detail column has its own bar: the title starts after the list, the items still go to the Duo's vertical bar, and the list keeps its own insets.

<!-- MEDIA: #530 before / after: the same Navigation 3 push on an iPhone, standalone bars sliding with the page vs. the hosted bar staying put -->
<!-- MEDIA: an open iPhone Duo, a Navigation 3 screen with its bar items, toolbar and tab bar in the vertical bar -->

### Who owns the bars

The screen that composed last owns them: during a push or a pop, the screen that is coming in. While a back gesture runs the bars stay with the current screen, and they change only once the gesture commits. Screens that appear in the same frame (two panes, a restored back stack) share the bars with whichever of them has bars, and a pane without bars that appears beside the owner (the list coming back when an iPhone Duo is opened) leaves the bars to it.

When no screen with an `AdaptiveScaffold` is left (say the app switches to screens built with calf-navigation), the host goes back to how it starts: bars hidden and empty, and no `UITabBarController` around the app, which would otherwise take the iPhone Duo's vertical bar from the tab bars of calf-navigation's containers. An app can move between both kinds of screens at any time.

- A screen with an `AdaptiveScaffold` but no top bar hides the navigation bar. A screen without any `AdaptiveScaffold` does not take the bars, so give every screen one.
- A bar only publishes to the host from inside an `AdaptiveScaffold` composed in the host's own Compose view. Content of an `AdaptiveBottomSheet`, an `AdaptiveAlertDialog`, or an entry of calf-navigation's `AdaptiveNavDisplay` keeps drawing standalone bars, exactly as before.
- The navigation bar has no back button of its own: Compose owns the back stack. Use `UIKitUIBarButtonItem.back(onClick = …)` as the first leading item; unlike a titled "Back" item it moves into the iPhone Duo's vertical bar.
- More than five tab items stay a standalone tab bar, because a `UITabBarController` would move the rest behind its own "More" tab.

### What to rely on

The host is `@ExperimentalCalfUiApi`. Its parts are not equally proven, so pick what a release can afford:

- **Stable:** the hosted navigation bar and toolbar of one stack (title, items, crossfade on a screen change, bar hidden for a screen without one), who owns the bars, the content padding, and the fallback to standalone bars (no host installed, a sheet's content, more than five tabs).
- **Experimental:** the hosted tab bar (a `UITabBarController` is put in place and the Compose view moves between its tabs the first time a screen shows tabs), the large-title collapse driven by Compose scrolling, and the bars of a pane in a two-pane layout. They work in the sample on iPhone, iPad and the iPhone Duo simulator, but they depend on UIKit timing that has no automated coverage yet. An app that needs only the first group can keep tabs in Compose (`AdaptiveNavigationBar` outside the host draws the standalone bar) and leave `prefersLargeTitles` off.

### Limits compared with calf-navigation

The bar host is one set of bars for the whole window, whose items follow the screen. [calf-navigation](navigation.md) gives each screen its own UIKit controller instead:

| | Bar host + plain Compose navigation | calf-navigation |
|---|---|---|
| Bars stay put, items change per screen | Yes, items crossfade | Yes, UIKit's push animation |
| Title and items follow an interactive back gesture | No: they change when it commits | Yes |
| iOS edge swipe-back | Compose's (Navigation 3 predictive back) | UIKit's |
| iPhone Duo vertical bar | Yes | Yes |
| Two panes | The owning pane's bar, over that pane | Each column of the split view has its own bar |

A Compose `Dialog` or `Popup` drawn in the host's view is not detected: the hosted bars stay above it and are not dimmed. The native sheets and alerts of `AdaptiveBottomSheet` and `AdaptiveAlertDialog` dim and block them as usual.

Without the bar host nothing changes: the bars are the standalone views described on each component's page.

## Components

Calf UI provides a set of adaptive UI components that adapt to the platform they are running on. Here's a list of available components:

- [AdaptiveAlertDialog](ui/adaptive-alert-dialog.md) - A dialog that adapts to the platform it is running on
- [AdaptiveBottomSheet](ui/adaptive-bottom-sheet.md) - A bottom sheet that adapts to the platform it is running on
- [AdaptiveCircularProgressIndicator](ui/adaptive-circular-progress-indicator.md) - A circular progress indicator that adapts to the platform it is running on
- [AdaptiveClickable](ui/adaptive-clickable.md) - A clickable modifier that replaces indication on iOS with scaling effect
- [AdaptiveDatePicker](ui/adaptive-date-picker.md) - A date picker that adapts to the platform it is running on
- [AdaptiveTimePicker](ui/adaptive-time-picker.md) - A time picker that adapts to the platform it is running on
