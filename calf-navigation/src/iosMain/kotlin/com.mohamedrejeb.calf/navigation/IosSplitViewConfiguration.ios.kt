package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import platform.UIKit.UISplitViewController

/**
 * The same configuration, plus direct access to the `UISplitViewController` behind the display,
 * for anything [IosSplitViewConfiguration] does not cover. [configure] runs after the other values
 * were applied, and again whenever the configuration changes, so it may override them.
 *
 * `remember` the result: a new lambda makes a new configuration, which is applied again.
 *
 * Leave the split view's delegate and its three column controllers alone. They are how Calf keeps
 * the back stack in charge of what each column shows.
 */
@CalfIosOnlyApi
@ExperimentalCalfNavigationApi
fun IosSplitViewConfiguration.withSplitViewController(
    configure: (UISplitViewController) -> Unit,
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
        controllerHook = configure,
    )
