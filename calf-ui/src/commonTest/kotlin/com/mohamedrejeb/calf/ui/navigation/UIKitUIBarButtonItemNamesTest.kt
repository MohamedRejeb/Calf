package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UIKitUIBarButtonItemNamesTest {

    private val star = UIKitImage.SystemName("star")
    private val vector = UIKitImage.Vector(ImageVector.Builder("plus", 24.dp, 24.dp, 24f, 24f).build())

    @Test
    fun `image item carries its label as the native title and accessibility label`() {
        val names = UIKitUIBarButtonItem.image(star, label = "Favorite").resolveNames()
        assertEquals(BarButtonItemNames(nativeTitle = "Favorite", accessibilityLabel = "Favorite"), names)
    }

    @Test
    fun `image item falls back to its title when it has no label`() {
        val names = UIKitUIBarButtonItem(title = "Favorite", image = star).resolveNames()
        assertEquals(BarButtonItemNames(nativeTitle = "Favorite", accessibilityLabel = "Favorite"), names)
    }

    @Test
    fun `image item without a name leaves both to UIKit`() {
        assertEquals(BarButtonItemNames(null, null), UIKitUIBarButtonItem.image(star).resolveNames())
    }

    @Test
    fun `text item keeps its title and uses the label only for accessibility`() {
        val names = UIKitUIBarButtonItem.title("Sort", label = "Sort items").resolveNames()
        assertEquals(BarButtonItemNames(nativeTitle = "Sort", accessibilityLabel = "Sort items"), names)
    }

    @Test
    fun `text item without a label leaves accessibility to UIKit`() {
        assertEquals(BarButtonItemNames("Sort", null), UIKitUIBarButtonItem.title("Sort").resolveNames())
    }

    @Test
    fun `system item is named by its label and keeps UIKit's name without one`() {
        val item = UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Add, label = "Add item")
        assertEquals(BarButtonItemNames(nativeTitle = "Add item", accessibilityLabel = "Add item"), item.resolveNames())
        assertEquals(BarButtonItemNames(null, null), UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Add).resolveNames())
    }

    @Test
    fun `spaces never get a name`() {
        assertEquals(BarButtonItemNames(null, null), UIKitUIBarButtonItem.flexibleSpace().copy(label = "x").resolveNames())
        assertEquals(BarButtonItemNames(null, null), UIKitUIBarButtonItem.fixedSpace().copy(label = "x").resolveNames())
    }

    @Test
    fun `menu item with an image is named like an image item`() {
        val item = UIKitUIBarButtonItem.withMenu(image = star, label = "More options")
        assertEquals(BarButtonItemNames("More options", "More options"), item.resolveNames())
    }

    @Test
    fun `only a custom image without any name is reported as unnamed`() {
        assertTrue(UIKitUIBarButtonItem.image(vector).isUnnamedCustomImage)
        assertFalse(UIKitUIBarButtonItem.image(vector, label = "Add").isUnnamedCustomImage)
        assertFalse(UIKitUIBarButtonItem(title = "Add", image = vector).isUnnamedCustomImage)
        assertFalse(UIKitUIBarButtonItem.image(star).isUnnamedCustomImage, "SF Symbols carry their own description")
        assertFalse(UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Add).isUnnamedCustomImage)
    }

    @Test
    fun `visibility priorities mirror UIKit's constants`() {
        assertEquals(-250, UIKitUIBarButtonItemVisibilityPriority.Low.uikitFallbackValue)
        assertEquals(0, UIKitUIBarButtonItemVisibilityPriority.Standard.uikitFallbackValue)
        assertEquals(250, UIKitUIBarButtonItemVisibilityPriority.High.uikitFallbackValue)
        assertEquals(UIKitUIBarButtonItemVisibilityPriority.Standard, UIKitUIBarButtonItem.title("x").visibilityPriority)
    }

    @Test
    fun `a back item is a named chevron that stays in the bar longest`() {
        val back = UIKitUIBarButtonItem.back {}

        assertEquals(UIKitImage.SystemName("chevron.backward"), back.image)
        assertEquals(BarButtonItemNames("Back", "Back"), back.resolveNames())
        assertEquals(UIKitUIBarButtonItemVisibilityPriority.High, back.visibilityPriority)
        assertEquals("Retour", UIKitUIBarButtonItem.back(label = "Retour") {}.label)
    }
}
