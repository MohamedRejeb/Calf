package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.core.CalfIosOnlyApi

/**
 * iOS only. How the `UISplitViewController` behind a list-detail display is set up. Every default
 * leaves the decision to iOS, which is what lets it adapt the columns to the device: on
 * iPhone Duo it lines the primary column up with the fold while the device is half open.
 *
 * Create one with the [IosSplitViewConfiguration] function. Other platforms ignore it. For their
 * pane layout use Navigation 3's `NavDisplay` with `rememberListDetailSceneStrategy` from
 * `androidx.compose.material3.adaptive:adaptive-navigation3` directly.
 */
@ExperimentalCalfNavigationApi
@Immutable
interface IosSplitViewConfiguration {
    /** `preferredDisplayMode`: whether the list is beside the detail, over it, or hidden. */
    val preferredDisplayMode: IosSplitViewDisplayMode

    /** `preferredSplitBehavior`: how the detail makes room when the list is shown. */
    val splitBehavior: IosSplitViewBehavior

    /** `preferredPrimaryColumnWidth`. `null` keeps the system width, which follows the fold. */
    val primaryColumnWidth: Dp?

    /** `minimumPrimaryColumnWidth`. `null` keeps the system value. */
    val minPrimaryColumnWidth: Dp?

    /** `maximumPrimaryColumnWidth`. `null` keeps the system value. */
    val maxPrimaryColumnWidth: Dp?

    /** `primaryEdge`: the side of the screen the list is on. */
    val primaryEdge: IosSplitViewPrimaryEdge

    /** `presentsWithGesture`: whether a swipe can show and hide the list. */
    val presentsWithGesture: Boolean

    /** `showsSecondaryOnlyButton`: a bar button that lets the detail take the whole screen. */
    val showsSecondaryOnlyButton: Boolean
}

/** iOS only. `UISplitViewController.DisplayMode` values that apply to two columns. */
@ExperimentalCalfNavigationApi
enum class IosSplitViewDisplayMode {
    /** iOS decides from the size of the window. */
    Automatic,

    /** Only the detail is shown; the list is reached through the bar button or a swipe. */
    SecondaryOnly,

    /** List and detail side by side. */
    OneBesideSecondary,

    /** The list floats over the detail. */
    OneOverSecondary,
}

/** iOS only. `UISplitViewController.SplitBehavior`. */
@ExperimentalCalfNavigationApi
enum class IosSplitViewBehavior {
    /** iOS decides from the size of the window. */
    Automatic,

    /** The detail shrinks to make room for the list. */
    Tile,

    /** The list covers part of the detail. */
    Overlay,

    /** The detail is pushed partly off screen. */
    Displace,
}

/** iOS only. `UISplitViewController.PrimaryEdge`. */
@ExperimentalCalfNavigationApi
enum class IosSplitViewPrimaryEdge { Leading, Trailing }

/**
 * iOS only. Creates an [IosSplitViewConfiguration]; see its properties for what each value sets.
 *
 * The opt-in is on this function and not on the type, so only code that really configures the
 * iOS split view is asked to confirm it; taking the defaults is not.
 */
@CalfIosOnlyApi
@ExperimentalCalfNavigationApi
fun IosSplitViewConfiguration(
    preferredDisplayMode: IosSplitViewDisplayMode = IosSplitViewDisplayMode.Automatic,
    splitBehavior: IosSplitViewBehavior = IosSplitViewBehavior.Automatic,
    primaryColumnWidth: Dp? = null,
    minPrimaryColumnWidth: Dp? = null,
    maxPrimaryColumnWidth: Dp? = null,
    primaryEdge: IosSplitViewPrimaryEdge = IosSplitViewPrimaryEdge.Leading,
    presentsWithGesture: Boolean = true,
    showsSecondaryOnlyButton: Boolean = false,
): IosSplitViewConfiguration =
    IosSplitViewConfigurationImpl(
        preferredDisplayMode = preferredDisplayMode,
        splitBehavior = splitBehavior,
        primaryColumnWidth = primaryColumnWidth,
        minPrimaryColumnWidth = minPrimaryColumnWidth,
        maxPrimaryColumnWidth = maxPrimaryColumnWidth,
        primaryEdge = primaryEdge,
        presentsWithGesture = presentsWithGesture,
        showsSecondaryOnlyButton = showsSecondaryOnlyButton,
    )

/**
 * @property controllerHook set from `iosMain` only, where the type can be named: a
 *   `(UISplitViewController) -> Unit` that runs after the values above were applied.
 */
@OptIn(ExperimentalCalfNavigationApi::class)
@Immutable
internal data class IosSplitViewConfigurationImpl(
    override val preferredDisplayMode: IosSplitViewDisplayMode = IosSplitViewDisplayMode.Automatic,
    override val splitBehavior: IosSplitViewBehavior = IosSplitViewBehavior.Automatic,
    override val primaryColumnWidth: Dp? = null,
    override val minPrimaryColumnWidth: Dp? = null,
    override val maxPrimaryColumnWidth: Dp? = null,
    override val primaryEdge: IosSplitViewPrimaryEdge = IosSplitViewPrimaryEdge.Leading,
    override val presentsWithGesture: Boolean = true,
    override val showsSecondaryOnlyButton: Boolean = false,
    val controllerHook: Any? = null,
) : IosSplitViewConfiguration
