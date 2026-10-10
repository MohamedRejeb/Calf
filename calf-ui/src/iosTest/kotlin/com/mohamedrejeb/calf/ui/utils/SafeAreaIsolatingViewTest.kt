@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.additionalSafeAreaInsets
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Against real UIKit on the simulator: a view controller whose safe area is extended at the top,
 * a plain view and a hosted view both placed at the top of its view. UIKit only propagates the
 * safe area inside a window, so the controller is the root of one; the window is never made key
 * (the test host has no UIApplication).
 */
class SafeAreaIsolatingViewTest {

    private val controller = UIViewController().apply {
        additionalSafeAreaInsets = UIEdgeInsetsMake(50.0, 0.0, 0.0, 0.0)
    }
    private val window = UIWindow(frame = CGRectMake(0.0, 0.0, 390.0, 844.0)).apply {
        rootViewController = controller
    }

    private fun attach(view: UIView) {
        view.setFrame(CGRectMake(0.0, 0.0, 390.0, 300.0))
        controller.view.addSubview(view)
    }

    private fun layout() {
        window.hidden = false
        window.layoutIfNeeded()
        controller.view.layoutIfNeeded()
    }

    @Test
    fun `a plain view at the top of the screen receives the safe area`() {
        val plain = UIView()
        attach(plain)
        layout()

        assertEquals(50.0, plain.safeAreaInsets.useContents { top })
        assertEquals(DEFAULT_MARGIN + 50.0, plain.layoutMargins.useContents { top })
    }

    @Test
    fun `a view inside the host keeps zero insets and fills the host`() {
        val content = UIView()
        val host = SafeAreaIsolatingView(content)
        attach(host)
        layout()
        host.layoutIfNeeded()

        assertEquals(0.0, content.safeAreaInsets.useContents { top })
        assertEquals(0.0, host.safeAreaInsets.useContents { top })
        assertEquals(DEFAULT_MARGIN, content.layoutMargins.useContents { top })
        assertEquals(DEFAULT_MARGIN, host.layoutMargins.useContents { top })
        assertEquals(390.0 to 300.0, content.frame.useContents { size.width to size.height })
    }
}

/** UIKit's default layout margin of a plain view. */
private const val DEFAULT_MARGIN = 8.0
