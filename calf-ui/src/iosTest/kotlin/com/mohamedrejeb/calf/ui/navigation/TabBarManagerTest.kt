@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.LayoutDirection
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarItem
import kotlin.test.Test
import kotlin.test.assertEquals

private const val DENSITY_2X = 2f
private const val DENSITY_3X = 3f

/** A painter that notes the layout direction of every draw, to see when an image is drawn again. */
private class RecordingPainter : Painter() {
    val drawnIn = mutableListOf<LayoutDirection>()

    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawnIn += layoutDirection
    }
}

/**
 * The manager against a real `UITabBar`: which native items the bar holds after the items, the
 * density or the layout direction changed. Runs on the iOS simulator.
 */
class TabBarManagerTest {
    private val tabBar = UITabBar()
    private val manager = TabBarManager(tabBar, onItemSelected = {})

    private fun nativeItems(): List<UITabBarItem> = tabBar.items.orEmpty().filterIsInstance<UITabBarItem>()

    private fun titled(vararg titles: String) = titles.map { UIKitUITabBarItem(title = it) }

    private fun painted(painter: Painter) =
        listOf(UIKitUITabBarItem(title = "Home", image = UIKitImage.Painter(painter)))

    @Test
    fun `items whose content changed are updated in place`() {
        manager.setItems(titled("Home", "Saved"), selectedIndex = 0, density = DENSITY_2X)
        val before = nativeItems()

        manager.setItems(titled("Home", "Profile"), selectedIndex = 0, density = DENSITY_2X)

        val after = nativeItems()
        assertEquals(before, after, "the bar keeps its native items")
        assertEquals(listOf("Home", "Profile"), after.map { it.title })
    }

    @Test
    fun `a different number of items rebuilds the bar`() {
        manager.setItems(titled("Home", "Saved"), selectedIndex = 0, density = DENSITY_2X)

        manager.setItems(titled("Home", "Saved", "Profile"), selectedIndex = 2, density = DENSITY_2X)

        assertEquals(listOf("Home", "Saved", "Profile"), nativeItems().map { it.title })
        assertEquals("Profile", tabBar.selectedItem?.title)
    }

    @Test
    fun `the selection follows the selected index and stays in range`() {
        manager.setItems(titled("Home", "Saved"), selectedIndex = 1, density = DENSITY_2X)
        assertEquals("Saved", tabBar.selectedItem?.title)

        manager.setItems(titled("Home", "Saved"), selectedIndex = 7, density = DENSITY_2X)
        assertEquals("Saved", tabBar.selectedItem?.title)
    }

    @Test
    fun `no items leave the bar as it is`() {
        manager.setItems(titled("Home", "Saved"), selectedIndex = 0, density = DENSITY_2X)

        manager.setItems(emptyList(), selectedIndex = 0, density = DENSITY_2X)

        assertEquals(listOf("Home", "Saved"), nativeItems().map { it.title })
    }

    @Test
    fun `unchanged items are not drawn again`() {
        val painter = RecordingPainter()

        manager.setItems(painted(painter), selectedIndex = 0, density = DENSITY_2X)
        manager.setItems(painted(painter), selectedIndex = 0, density = DENSITY_2X)

        assertEquals(1, painter.drawnIn.size)
    }

    @Test
    fun `a density change draws the images again at the new scale`() {
        val painter = RecordingPainter()
        manager.setItems(painted(painter), selectedIndex = 0, density = DENSITY_2X)
        assertEquals(DENSITY_2X.toDouble(), nativeItems().single().image?.scale)

        manager.setItems(painted(painter), selectedIndex = 0, density = DENSITY_3X)

        assertEquals(DENSITY_3X.toDouble(), nativeItems().single().image?.scale)
    }

    @Test
    fun `a layout direction change draws the images again in the new direction`() {
        val painter = RecordingPainter()
        manager.setItems(painted(painter), selectedIndex = 0, density = DENSITY_2X, layoutDirection = LayoutDirection.Ltr)

        manager.setItems(painted(painter), selectedIndex = 0, density = DENSITY_2X, layoutDirection = LayoutDirection.Rtl)

        assertEquals(listOf(LayoutDirection.Ltr, LayoutDirection.Rtl), painter.drawnIn)
    }
}
