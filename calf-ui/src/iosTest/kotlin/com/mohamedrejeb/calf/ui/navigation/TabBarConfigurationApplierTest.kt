@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.calf.ui.uikit.UIKitFont
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIFont
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarItemAppearance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val TITLE_FONT_SIZE = 13f

/**
 * The applier against a real `UITabBar`: what the bar's tint colors and appearances hold after a
 * configuration is applied. The standalone bar, the bar host and `calf-navigation`'s tab bar
 * controller all go through it. Runs on the iOS simulator.
 */
class TabBarConfigurationApplierTest {
    private val tabBar = UITabBar()
    private val applier = TabBarConfigurationApplier(tabBar)

    private val styled = UIKitTabBarConfiguration(
        selectedIconColor = Color.Red,
        unselectedTitleColor = Color.Green,
        titleFont = UIKitFont.System(size = TITLE_FONT_SIZE, weight = FontWeight.Bold),
    )

    private fun UITabBarAppearance.itemAppearances(): List<UITabBarItemAppearance> =
        listOf(stackedLayoutAppearance, inlineLayoutAppearance, compactInlineLayoutAppearance)

    @Test
    fun `a title font and per-state colors reach every item layout of the appearance`() {
        applier.apply(styled)

        tabBar.standardAppearance.itemAppearances().forEach { item ->
            assertEquals(Color.Red.toUIColor(), item.selected.iconColor)
            assertEquals(
                Color.Green.toUIColor(),
                item.normal.titleTextAttributes[NSForegroundColorAttributeName],
            )
            val selectedFont = item.selected.titleTextAttributes[NSFontAttributeName] as? UIFont
            val normalFont = item.normal.titleTextAttributes[NSFontAttributeName] as? UIFont
            assertEquals(TITLE_FONT_SIZE.toDouble(), selectedFont?.pointSize)
            assertEquals(TITLE_FONT_SIZE.toDouble(), normalFont?.pointSize)
        }
    }

    @Test
    fun `a custom appearance is used at the scroll edge too`() {
        assertNull(tabBar.scrollEdgeAppearance, "a new tab bar has no scroll edge appearance")

        applier.apply(styled)

        val scrollEdge = assertNotNull(tabBar.scrollEdgeAppearance)
        assertEquals(Color.Red.toUIColor(), scrollEdge.stackedLayoutAppearance.selected.iconColor)
    }

    @Test
    fun `a background color is set on the appearance`() {
        applier.apply(UIKitTabBarConfiguration(backgroundColor = Color.Blue))

        assertEquals(Color.Blue.toUIColor(), tabBar.standardAppearance.backgroundColor)
    }

    @Test
    fun `going back to the defaults restores the appearance the bar had`() {
        // UIKit's own appearance already names a title font and colors.
        val original = tabBar.standardAppearance.stackedLayoutAppearance
        val originalFont = original.selected.titleTextAttributes[NSFontAttributeName]
        val originalTitleColor = original.normal.titleTextAttributes[NSForegroundColorAttributeName]
        val originalIconColor = original.selected.iconColor
        applier.apply(styled)

        applier.apply(UIKitTabBarConfiguration())

        assertNull(tabBar.scrollEdgeAppearance)
        val restored = tabBar.standardAppearance.stackedLayoutAppearance
        assertEquals(originalFont, restored.selected.titleTextAttributes[NSFontAttributeName])
        assertEquals(originalTitleColor, restored.normal.titleTextAttributes[NSForegroundColorAttributeName])
        assertEquals(originalIconColor, restored.selected.iconColor)
    }

    @Test
    fun `a configuration without appearance fields leaves the appearances alone`() {
        // An appearance the app set itself, after Calf captured the defaults.
        val appAppearance = UITabBarAppearance().apply {
            configureWithOpaqueBackground()
            backgroundColor = Color.Yellow.toUIColor()
        }
        tabBar.scrollEdgeAppearance = appAppearance

        applier.apply(UIKitTabBarConfiguration(selectedItemColor = Color.Red))

        assertEquals(Color.Yellow.toUIColor(), tabBar.scrollEdgeAppearance?.backgroundColor)
    }

    @Test
    fun `tint colors are applied and go back to the bar's own`() {
        val defaultTint = tabBar.tintColor
        val defaultUnselectedTint = tabBar.unselectedItemTintColor

        applier.apply(UIKitTabBarConfiguration(selectedItemColor = Color.Red, unselectedItemColor = Color.Gray))
        assertEquals(Color.Red.toUIColor(), tabBar.tintColor)
        assertEquals(Color.Gray.toUIColor(), tabBar.unselectedItemTintColor)

        applier.apply(UIKitTabBarConfiguration())
        assertEquals(defaultTint, tabBar.tintColor)
        assertEquals(defaultUnselectedTint, tabBar.unselectedItemTintColor)
    }

    @Test
    fun `translucency follows the configuration`() {
        applier.apply(UIKitTabBarConfiguration(isTranslucent = false))
        assertEquals(false, tabBar.translucent)

        applier.apply(UIKitTabBarConfiguration(isTranslucent = true))
        assertEquals(true, tabBar.translucent)
    }
}
