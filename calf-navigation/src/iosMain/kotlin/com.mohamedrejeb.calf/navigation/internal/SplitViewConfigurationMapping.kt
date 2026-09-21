@file:OptIn(ExperimentalForeignApi::class, ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.IosSplitViewBehavior
import com.mohamedrejeb.calf.navigation.IosSplitViewConfiguration
import com.mohamedrejeb.calf.navigation.IosSplitViewConfigurationImpl
import com.mohamedrejeb.calf.navigation.IosSplitViewDisplayMode
import com.mohamedrejeb.calf.navigation.IosSplitViewPrimaryEdge
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGFloat
import platform.UIKit.UISplitViewController
import platform.UIKit.UISplitViewControllerAutomaticDimension
import platform.UIKit.UISplitViewControllerDisplayMode
import platform.UIKit.UISplitViewControllerDisplayModeAutomatic
import platform.UIKit.UISplitViewControllerDisplayModeOneBesideSecondary
import platform.UIKit.UISplitViewControllerDisplayModeOneOverSecondary
import platform.UIKit.UISplitViewControllerDisplayModeSecondaryOnly
import platform.UIKit.UISplitViewControllerPrimaryEdge
import platform.UIKit.UISplitViewControllerSplitBehavior

/**
 * Applies [configuration] to this split view. `null` widths go back to
 * `UISplitViewControllerAutomaticDimension`, so a value that is cleared later is really cleared.
 * A hook set from `iosMain` runs last and may override anything.
 */
internal fun UISplitViewController.applySplitViewConfiguration(configuration: IosSplitViewConfiguration) {
    preferredDisplayMode = configuration.preferredDisplayMode.toUIKit()
    preferredSplitBehavior = configuration.splitBehavior.toUIKit()
    preferredPrimaryColumnWidth = configuration.primaryColumnWidth.toPointsOrAutomatic()
    minimumPrimaryColumnWidth = configuration.minPrimaryColumnWidth.toPointsOrAutomatic()
    maximumPrimaryColumnWidth = configuration.maxPrimaryColumnWidth.toPointsOrAutomatic()
    primaryEdge = configuration.primaryEdge.toUIKit()
    presentsWithGesture = configuration.presentsWithGesture
    showsSecondaryOnlyButton = configuration.showsSecondaryOnlyButton

    val hook = (configuration as? IosSplitViewConfigurationImpl)?.controllerHook
    @Suppress("UNCHECKED_CAST")
    (hook as? (UISplitViewController) -> Unit)?.invoke(this)
}

/** One dp is one UIKit point. */
private fun Dp?.toPointsOrAutomatic(): CGFloat =
    this?.value?.toDouble() ?: UISplitViewControllerAutomaticDimension

private fun IosSplitViewDisplayMode.toUIKit(): UISplitViewControllerDisplayMode =
    when (this) {
        IosSplitViewDisplayMode.Automatic -> UISplitViewControllerDisplayModeAutomatic
        IosSplitViewDisplayMode.SecondaryOnly -> UISplitViewControllerDisplayModeSecondaryOnly
        IosSplitViewDisplayMode.OneBesideSecondary -> UISplitViewControllerDisplayModeOneBesideSecondary
        IosSplitViewDisplayMode.OneOverSecondary -> UISplitViewControllerDisplayModeOneOverSecondary
    }

private fun IosSplitViewBehavior.toUIKit(): UISplitViewControllerSplitBehavior =
    when (this) {
        IosSplitViewBehavior.Automatic -> UISplitViewControllerSplitBehavior.UISplitViewControllerSplitBehaviorAutomatic
        IosSplitViewBehavior.Tile -> UISplitViewControllerSplitBehavior.UISplitViewControllerSplitBehaviorTile
        IosSplitViewBehavior.Overlay -> UISplitViewControllerSplitBehavior.UISplitViewControllerSplitBehaviorOverlay
        IosSplitViewBehavior.Displace -> UISplitViewControllerSplitBehavior.UISplitViewControllerSplitBehaviorDisplace
    }

private fun IosSplitViewPrimaryEdge.toUIKit(): UISplitViewControllerPrimaryEdge =
    when (this) {
        IosSplitViewPrimaryEdge.Leading -> UISplitViewControllerPrimaryEdge.UISplitViewControllerPrimaryEdgeLeading
        IosSplitViewPrimaryEdge.Trailing -> UISplitViewControllerPrimaryEdge.UISplitViewControllerPrimaryEdgeTrailing
    }
