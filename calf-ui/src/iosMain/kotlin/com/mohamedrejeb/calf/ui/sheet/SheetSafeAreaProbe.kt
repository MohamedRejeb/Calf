@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.navigation.IosBarEdgeInsets
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth

/**
 * An empty view filling a sheet's view that reports the sheet's safe area, which is where UIKit
 * placed the sheet relative to the unsafe parts of the window (an iPhone Duo's vertical bar and
 * camera, the Dynamic Island). `ComposeUIViewController` cannot be subclassed to hear it.
 */
internal class SheetSafeAreaProbe(
    private val onSafeAreaChange: (IosBarEdgeInsets) -> Unit,
) : UIView(frame = CGRectZero.readValue()) {

    init {
        userInteractionEnabled = false
        opaque = false
        autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
    }

    /** Puts the probe behind everything in [view], at its full size. */
    fun install(view: UIView) {
        setFrame(view.bounds)
        view.insertSubview(this, atIndex = 0)
    }

    override fun safeAreaInsetsDidChange() {
        super.safeAreaInsetsDidChange()
        onSafeAreaChange(
            safeAreaInsets.useContents {
                IosBarEdgeInsets(left = left.dp, top = top.dp, right = right.dp, bottom = bottom.dp)
            },
        )
    }
}
