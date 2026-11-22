package com.mohamedrejeb.calf.navigation.internal

import platform.UIKit.UINavigationController
import platform.UIKit.UISplitViewController
import platform.UIKit.UISplitViewControllerStyle
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsSameControllerTest {
    /**
     * A plain UIKit controller (not subclassed in Kotlin) comes back from UIKit as a new Kotlin
     * wrapper, so a reference check missed the split view tab and the tab state kept the old tab.
     */
    @Test
    fun recognises_a_plain_uikit_controller_handed_back_by_uikit() {
        val split = UISplitViewController(style = UISplitViewControllerStyle.UISplitViewControllerStyleDoubleColumn)
        val tabBar = UITabBarController()
        tabBar.setViewControllers(listOf(split), animated = false)

        val fromUiKit = tabBar.viewControllers!!.first() as UIViewController

        assertTrue(isSameController(ours = split, fromUiKit = fromUiKit))
    }

    @Test
    fun tells_two_controllers_apart() {
        assertFalse(isSameController(ours = UINavigationController(), fromUiKit = UINavigationController()))
    }
}
