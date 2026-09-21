package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.core.CalfIosOnlyApi

/**
 * Turns a display into a list-detail layout. Pass it as `listDetail` to [AdaptiveNavDisplay] or
 * [AdaptiveTabDisplay], and mark the entries with [AdaptivePaneRole].
 *
 *  - **iOS** hosts the back stack in a real `UISplitViewController`, so iOS decides when there is
 *    room for two columns (iPad, the open iPhone Duo) and adapts them to the device.
 *  - **Android / Desktop / Web** use Material 3's list-detail scene for Navigation 3: two panes
 *    from the expanded window width up, and on an open book-style foldable from the medium width
 *    up, placed on either side of a hinge that separates the display.
 *
 * Start the back stack of such a display at the list entry. Entries below the list are shown in
 * the narrow list column on iOS and full screen elsewhere; keep them in an outer display instead.
 */
@ExperimentalCalfNavigationApi
@Immutable
interface AdaptiveListDetailSettings {
    /** iOS only. See [IosSplitViewConfiguration]. Ignored on other platforms. */
    @CalfIosOnlyApi
    val iosSplitView: IosSplitViewConfiguration
}

/** Creates [AdaptiveListDetailSettings]. Without arguments every platform uses its own defaults. */
@ExperimentalCalfNavigationApi
fun AdaptiveListDetailSettings(
    iosSplitView: IosSplitViewConfiguration = IosSplitViewConfigurationImpl(),
): AdaptiveListDetailSettings = AdaptiveListDetailSettingsImpl(iosSplitView)

/** Remembers [AdaptiveListDetailSettings] for as long as [iosSplitView] stays equal. */
@ExperimentalCalfNavigationApi
@Composable
fun rememberAdaptiveListDetailSettings(
    iosSplitView: IosSplitViewConfiguration = IosSplitViewConfigurationImpl(),
): AdaptiveListDetailSettings = remember(iosSplitView) { AdaptiveListDetailSettings(iosSplitView) }

@OptIn(ExperimentalCalfNavigationApi::class, CalfIosOnlyApi::class)
@Immutable
private data class AdaptiveListDetailSettingsImpl(
    override val iosSplitView: IosSplitViewConfiguration,
) : AdaptiveListDetailSettings
