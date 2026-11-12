package com.mohamedrejeb.calf.ui.navigation

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UINavigationController
import platform.UIKit.UIView

/**
 * Shows or hides one of this navigation controller's bars with a fade in place.
 *
 * UIKit's own animated show slides the bar down from above the screen, and the hide slides it back
 * up: the standalone animation it uses outside its own push, which is what a bar change here is,
 * Compose running the screen transition underneath. A bar that appears where it will stay fits a
 * screen sliding in from the side; one coming down from the status bar reads as a jump. The layout
 * changes at once either way ([setHidden] is called without animation), so the content gets its
 * final insets on the first frame and never moves with the bar.
 *
 * Hiding keeps a snapshot of the bar in its place while it fades, as the bar itself has to go for
 * the layout to change.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun UINavigationController.fadeBar(
    bar: UIView,
    hidden: Boolean,
    animated: Boolean,
    setHidden: (hidden: Boolean) -> Unit,
) {
    if (!animated) {
        setHidden(hidden)
        return
    }
    if (hidden) {
        val frame = bar.frame
        val snapshot = bar.snapshotViewAfterScreenUpdates(false)
        setHidden(true)
        if (snapshot == null) return
        snapshot.setFrame(frame)
        snapshot.userInteractionEnabled = false
        view.addSubview(snapshot)
        UIView.animateWithDuration(
            duration = BAR_FADE_SECONDS,
            animations = { snapshot.alpha = 0.0 },
            completion = { snapshot.removeFromSuperview() },
        )
    } else {
        setHidden(false)
        bar.alpha = 0.0
        UIView.animateWithDuration(
            duration = BAR_FADE_SECONDS,
            animations = { bar.alpha = 1.0 },
            completion = { bar.alpha = 1.0 },
        )
    }
}

/** The same length as the crossfade of a bar's title and items when the screen changes. */
internal const val BAR_FADE_SECONDS = 0.25
