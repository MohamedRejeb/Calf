package com.mohamedrejeb.calf.navigation

import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCalfNavigationApi::class)
class AdaptiveTabTest {
    private data object Home : NavKey

    @Test
    fun defaults() {
        val tab = adaptiveTab(Home, icon = {})

        assertEquals(Home, tab.key)
        assertNull(tab.label)
        assertTrue(tab.alwaysShowLabel)
        assertNull(tab.badge)
        assertEquals("", tab.iosTitle)
        assertNull(tab.iosImage)
        assertNull(tab.iosSelectedImage)
    }

    @Test
    fun capturesOptions() {
        val image = UIKitImage.SystemName("house")
        val selected = UIKitImage.SystemName("house.fill")

        val tab = adaptiveTab(
            key = Home,
            icon = {},
            label = {},
            alwaysShowLabel = false,
            badge = "3",
            iosTitle = "Home",
            iosImage = image,
            iosSelectedImage = selected,
        )

        assertEquals("3", tab.badge)
        assertEquals("Home", tab.iosTitle)
        assertEquals(image, tab.iosImage)
        assertEquals(selected, tab.iosSelectedImage)
    }
}
