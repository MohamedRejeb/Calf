@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.setAdditionalSafeAreaInsets
import kotlin.math.roundToInt

/**
 * Keeps the host's bars over the column of the screen that owns them. When the owner is a pane
 * (the detail beside a list), the navigation bar and toolbar leave the other panes' width free,
 * the way a split view's secondary column gets the primary column's width as its leading safe area:
 * the title starts after the list, and the bar items still go to an iPhone Duo's vertical bar.
 *
 * Only the bars move: the slot cancels the extra safe area, so the Compose content and every
 * scaffold's insets stay as they were.
 */
internal class BarHostBarColumn {

    private var applied: Applied? = null

    /** Leaves [insets] (whole points, left and right only) beside the bars of [navigationController]. */
    fun apply(navigationController: UINavigationController, slot: UIViewController, insets: IosBarEdgeInsets) {
        val left = insets.left.value.roundToInt()
        val right = insets.right.value.roundToInt()
        val previous = applied
        if (previous != null && previous.navigationController === navigationController &&
            previous.left == left && previous.right == right
        ) return
        // The content moved to another tab's containers: the ones it left go back to no column.
        if (previous != null && previous.navigationController !== navigationController) {
            setColumn(previous.navigationController, previous.slot, left = 0, right = 0)
        }
        setColumn(navigationController, slot, left, right)
        applied = Applied(navigationController, slot, left, right)
    }

    private fun setColumn(navigationController: UINavigationController, slot: UIViewController, left: Int, right: Int) {
        navigationController.setAdditionalSafeAreaInsets(UIEdgeInsetsMake(0.0, left.toDouble(), 0.0, right.toDouble()))
        slot.setAdditionalSafeAreaInsets(UIEdgeInsetsMake(0.0, -left.toDouble(), 0.0, -right.toDouble()))
    }

    private class Applied(
        val navigationController: UINavigationController,
        val slot: UIViewController,
        val left: Int,
        val right: Int,
    )
}
