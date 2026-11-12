package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlin.test.Test
import kotlin.test.assertEquals

class BarItemsUpdateTest {

    private val share = UIKitImage.SystemName("square.and.arrow.up")

    private var clicks = 0

    // Every recomposition builds new capturing lambdas, so these are equal in everything but the
    // callbacks (a lambda that captures nothing would be one shared instance).
    private fun shareItem(): UIKitUIBarButtonItem {
        val call = Any()
        return UIKitUIBarButtonItem.image(share, label = "Share") { clicks += call.hashCode() }
    }

    private fun menuItem(): UIKitUIBarButtonItem {
        val call = Any()
        return UIKitUIBarButtonItem(
            image = share,
            menuItems = listOf(AdaptiveDropDownItem(title = "Copy") { clicks += call.hashCode() }),
            menuSections = listOf(
                AdaptiveDropDownSection(items = listOf(AdaptiveDropDownItem(title = "Move") { clicks += call.hashCode() })),
            ),
        )
    }

    @Test
    fun `the same items need nothing`() {
        val items = listOf(shareItem())

        assertEquals(BarItemsUpdate.None, barItemsUpdate(items, items))
    }

    @Test
    fun `new callbacks on the same items only rebind`() {
        assertEquals(BarItemsUpdate.Rebind, barItemsUpdate(listOf(shareItem()), listOf(shareItem())))
    }

    @Test
    fun `new menu callbacks on the same menu only rebind`() {
        assertEquals(BarItemsUpdate.Rebind, barItemsUpdate(listOf(menuItem()), listOf(menuItem())))
    }

    @Test
    fun `a changed look replaces the items`() {
        val disabled = shareItem().copy(enabled = false)

        assertEquals(BarItemsUpdate.Replace, barItemsUpdate(listOf(shareItem()), listOf(disabled)))
    }

    @Test
    fun `a changed menu replaces the items`() {
        val renamed = menuItem().copy(menuItems = listOf(AdaptiveDropDownItem(title = "Paste") {}))

        assertEquals(BarItemsUpdate.Replace, barItemsUpdate(listOf(menuItem()), listOf(renamed)))
    }

    @Test
    fun `an added item replaces the items`() {
        assertEquals(BarItemsUpdate.Replace, barItemsUpdate(listOf(shareItem()), listOf(shareItem(), shareItem())))
    }

    @Test
    fun `reordered items replace the items`() {
        val search = UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Search)

        assertEquals(
            BarItemsUpdate.Replace,
            barItemsUpdate(listOf(shareItem(), search), listOf(search, shareItem())),
        )
    }

    @Test
    fun `items appearing or leaving replace them`() {
        assertEquals(BarItemsUpdate.Replace, barItemsUpdate(null, listOf(shareItem())))
        assertEquals(BarItemsUpdate.Replace, barItemsUpdate(listOf(shareItem()), null))
    }

    @Test
    fun `no items before and after need nothing`() {
        assertEquals(BarItemsUpdate.None, barItemsUpdate(null, null))
        assertEquals(BarItemsUpdate.None, barItemsUpdate(emptyList(), emptyList()))
    }
}
