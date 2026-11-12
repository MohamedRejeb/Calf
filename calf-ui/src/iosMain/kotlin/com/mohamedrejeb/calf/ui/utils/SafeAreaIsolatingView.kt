@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.utils

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsZero
import platform.UIKit.UIView

/**
 * Hosts a native [content] view embedded in Compose so that it never sees the window's safe area.
 *
 * UIKit hands every view the part of the safe area it overlaps, wherever it sits in the hierarchy.
 * A view inside a scrolling Compose page therefore receives a growing top inset as it slides
 * under the status bar or a navigation bar, and a bottom inset near the home indicator. A
 * `UICalendarView` or `UIDatePicker` lays its content out inside those insets, so it shrinks and
 * grows while the page scrolls. Compose already keeps its content clear of the safe area, so the
 * native view should see none of it.
 *
 * A view's subviews derive their safe area from what the view reports, so a host that reports
 * none isolates everything inside it (verified against UIKit in `SafeAreaIsolatingViewTest`; a
 * plain scroll view, which turns its own insets into a content inset, still passes them on).
 * Layout margins travel separately: UIKit widens a view's margins by its safe area and a system
 * view may preserve its superview's margins, so the host keeps its own margins fixed too. The
 * content always fills the host, and the host measures as its content.
 */
internal class SafeAreaIsolatingView(
    val content: UIView,
) : UIView(frame = CGRectZero.readValue()) {

    init {
        backgroundColor = null
        insetsLayoutMarginsFromSafeArea = false
        preservesSuperviewLayoutMargins = false
        addSubview(content)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        content.setFrame(bounds)
    }

    override fun safeAreaInsets(): CValue<UIEdgeInsets> = UIEdgeInsetsZero.readValue()

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> = content.sizeThatFits(size)
}
