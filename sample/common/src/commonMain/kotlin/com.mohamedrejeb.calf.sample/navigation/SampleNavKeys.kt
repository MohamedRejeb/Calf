package com.mohamedrejeb.calf.sample.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Root of the outer stack: it hosts the home list-detail display. */
@Serializable
data object SampleRoot : NavKey

/** One key per [Screen]. The enum stays the single list of demos; this is its back-stack form. */
@Serializable
data class SampleScreen(val screen: Screen) : NavKey

/**
 * Persists both sample stacks across Android configuration changes and process death. Folding or
 * unfolding a device recreates the Activity, so without this the selected demo would be lost.
 */
val SampleSavedState: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SampleRoot::class)
            subclass(SampleScreen::class)
        }
    }
}

/** Where a [Screen] is shown in the sample's two-layer navigation. */
enum class SampleHost {
    /** The home list: root of the inner list-detail display. */
    List,

    /** A demo shown in the detail pane, with the navigator's bar and back button. */
    Detail,

    /** A demo shown in the detail pane that draws its own top bar, so the navigator's is hidden. */
    DetailOwnBar,

    /** A demo that owns the whole screen: pushed full screen from the outer display. */
    FullScreen;

    /** Whether the screen lives in the detail pane of the home list-detail display. */
    val isDetail: Boolean
        get() = this == Detail || this == DetailOwnBar
}

/**
 * Which layer of the sample's navigation shows this screen.
 *
 * A `UISplitViewController` must never be pushed, so anything that nests its own tabs or its own
 * navigation display cannot live in the detail column and is pushed from the outer display
 * instead. Demos whose subject is the top bar keep drawing it themselves.
 */
fun Screen.host(): SampleHost =
    when (this) {
        Screen.Home -> SampleHost.List

        // Nests AdaptiveTabDisplay with a split view inside a tab.
        Screen.AdaptiveNavigation,
        // Owns a plain NavDisplay whose two panes need the whole window.
        Screen.Nav3ListDetail,
        // Draws the window's real geometry (fold, camera, unsafe areas), so it needs all of it.
        Screen.Foldable,
        // Draw their own AdaptiveNavigationBar tab bar (and top bar).
        Screen.NavigationBar,
        Screen.ScaffoldDemo,
        Screen.Showcase,
        -> SampleHost.FullScreen

        // The demo is the bar: rotation markers, a bar menu, a page-title bar with history buttons.
        Screen.TopBarDemo,
        Screen.DropDown,
        Screen.WebView,
        -> SampleHost.DetailOwnBar

        else -> SampleHost.Detail
    }
